package ch.heigvd.dai.tcp;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import ch.heigvd.dai.Errno;
import ch.heigvd.dai.exceptions.ServerHasGoneException;

public class ClientParser extends ConnectionParser {
  public final Path workDir;

  /**
   * ClientParser constructor
   *
   * @param in      the input stream
   * @param out     the output stream
   * @param workDir the working directory used to resolve the path of files
   */
  public ClientParser(DataInputStream in, DataOutputStream out, Path workDir) {
    super(in, out);
    this.workDir = workDir;
  }

  public ClientParser(InputStream in, OutputStream out, Path work_dir) {
    super(in, out);
    this.workDir = work_dir;
  }

  /**
   * Send a request to the server
   * 
   * @param command the command to send
   * @throws IOException
   */
  private void sendRequest(String command)
      throws IOException {
    System.out.println("Sending: " + Arrays.toString(command.trim().split(" ")));
    byte[] message = command.getBytes(StandardCharsets.UTF_8);

    try {
      out.write(message);
      out.flush();
    } catch (IOException e) {
      System.err.println("Failed to communicate with the server. It may have gone offline.");
      throw new ServerHasGoneException();
    }
  }

  /**
   * Parse the status of the response
   * 
   * @return true if the status is 0, false otherwise
   * @throws IOException
   */
  private boolean parseStatus() throws IOException {
    int byteRead;
    StringBuilder buffer = new StringBuilder();

    // status like ENOTDIR (20) are sent as two different chars/bytes: 2 (0x32) and
    // then 0 (0x30)
    while ((byteRead = in.read()) != -1) {
      buffer.append((char) byteRead);

      if (byteRead == Server.EOT) {
        break;
      }
    }

    int status = Integer.parseInt(buffer.toString().trim());
    if (status != 0) {
      System.err.println("Got error: " + Errno.getErrorMessage(status));

      return false;
    }

    return true;
  }

  /**
   * List the files in the server
   * 
   * @param path the path to list
   * @throws IOException
   */
  private void list(String path) throws IOException {
    String command = "LIST " + path + Server.NEW_LINE;
    sendRequest(command);

    if (!parseStatus()) {
      return;
    }

    StringBuilder result = new StringBuilder();

    int byteRead;
    // read the response from the server (delimited by EOT)
    while ((byteRead = in.read()) != -1) {
      char c = (char) byteRead;
      if (c == Server.EOT) {
        break;
      }
      result.append(c);
    }

    // Print the result
    System.out.println("\nGot result: \n");
    Arrays.stream(result.toString().split(Server.DELIMITER)).forEach(s -> {
      System.out.println(s);
    });
  }

  /**
   * Download a file from the server
   * 
   * @param remote the remote path
   * @param local  the local path
   * @throws IOException
   */
  private void get(Path remote, Path local) throws IOException {
    Path localFullPath = workDir.resolve(local).normalize();

    if (Files.exists(localFullPath)) {
      System.err.println(localFullPath + " already exists!");
      return;
    }

    String command = "GET " + remote + Server.NEW_LINE;
    sendRequest(command);

    if (!parseStatus()) {
      return;
    }

    // create parent directories if needed
    Path parentDir = localFullPath.getParent();
    // if user only specifier filename like: myfile.java instread of
    // mydir/myfile.java
    if (!parentDir.equals(workDir.toAbsolutePath())) {
      try {
        Files.createDirectories(parentDir);
      } catch (IOException e) {
        System.err.println("Failed to create directories for file: " + local);
        return;
      }
    }

    int byteRead = 0;
    StringBuilder b = new StringBuilder();

    // status like ENOTDIR (20) are sent as two different chars/bytes: 2 (0x32) and
    // then 0 (0x30)
    while ((byteRead = in.read()) != -1) {
      b.append((char) byteRead);

      if (byteRead == Server.EOT) {
        break;
      }
    }

    int length = Integer.parseInt(b.toString().trim());
    System.out.println("Downloading file of length: " + length);

    if (Files.notExists(localFullPath)) {
      Files.createFile(localFullPath);
      System.out.println("File created: " + localFullPath);
    }

    try (FileOutputStream fout = new FileOutputStream(localFullPath.toFile());) {
      System.out.println("Writing local file:");

      // read the file using 4K chunks
      byte[] buffer = new byte[4096];
      int bytesRead;

      while (length > 0 && (bytesRead = in.read(buffer, 0, Math.min(length, buffer.length))) != -1) {
        fout.write(buffer, 0, bytesRead);
        length -= bytesRead;
      }

      fout.flush();
    } catch (FileNotFoundException e) {
      System.err.println("Unable to open file: " + e.getMessage() + ", path:" + localFullPath.toAbsolutePath());
    }
  }

  /**
   * Delete a file on the server
   * 
   * @param path the path to delete
   * @throws IOException
   */
  public void delete(String path) throws IOException {
    Path localFullPath = workDir.resolve(path).normalize();
    String command = "DELETE " + localFullPath + Server.NEW_LINE;
    sendRequest(command);

    if (!parseStatus()) {
      return;
    }

    System.out.println("Target deleted successfully");
  }

  /**
   * Upload a file to the server
   */
  private void put(Path localPath, String remotePath) throws IOException {
    Path localFullPath = workDir.resolve(localPath).normalize();
    File file = localFullPath.toFile();

    if (!file.exists()) {
      System.err.println("Local file doesn't exist");
      return;
    }

    if (!file.isFile()) {
      System.err.println("Path doesn't point to a file");
      return;
    }

    if (remotePath.endsWith("/")) {
      remotePath += file.getName();
    }

    try (FileInputStream fin = new FileInputStream(file);) {
      System.out.println("Sending file");
      sendRequest("PUT " + remotePath + " " + file.length() + "\n");

      // read the file using 4K blocks
      byte[] buffer = new byte[4096];
      int bytesRead;

      while ((bytesRead = fin.read(buffer)) != -1) {
        out.write(buffer, 0, bytesRead);
      }
      out.flush();
      System.out.println("file sent");
    } catch (FileNotFoundException e) {
      System.err.println("Unable to open file");
    }

    // Check if the server reveived correctly

    System.out.println("waiting for answer");
    if (!parseStatus()) {
      return;
    }

    System.out.println("Uploaded successfully");
  }

  /**
   * Create a directory on the server
   */
  private void mkdir(String remotePath) throws IOException {

    System.out.println("Sending PUT request to create directory");
    if (!remotePath.endsWith("/")) {
      remotePath = remotePath + "/";
    }

    String command = "PUT " + remotePath + Server.NEW_LINE;
    sendRequest(command);

    if (!parseStatus()) {
      return;
    }

    System.out.println("directory created successfully");
  }

  private boolean checkTokensLength(String[] tokens, int expected_length) {
    if (tokens.length != expected_length) {
      Client.usage();

      System.err.println("Invalid tokens: " + Arrays.toString(tokens));
      return false;
    }

    return true;
  }

  /**
   * Parse the tokens and execute the corresponding command
   * 
   * @param tokens the tokens to parse
   * @throws IOException
   * @throws ServerHasGoneException
   */
  @Override
  public void parse(String[] tokens) throws IOException, ServerHasGoneException {
    super.parse(tokens);

    switch (tokens[0].toLowerCase()) {
      case "list", "ls" -> {
        if (!checkTokensLength(tokens, 2)) {
          return;
        }

        list(tokens[1]);
      }

      case "get" -> {
        if (!checkTokensLength(tokens, 3)) {
          return;
        }

        get(Path.of(tokens[1]), Path.of(tokens[2]));
      }

      case "delete", "rm" -> {
        if (!checkTokensLength(tokens, 2)) {
          return;
        }

        delete(tokens[1]);
      }
      case "put" -> {
        if (!checkTokensLength(tokens, 3)) {
          return;
        }

        put(Path.of(tokens[1]), tokens[2]);

      }
      case "mkdir" -> {
        if (!checkTokensLength(tokens, 2)) {
          return;
        }

        mkdir(tokens[1]);
      }
      default -> {
        System.err.println("Received invalid tokens to parse: " + Arrays.toString(tokens));
      }
    }
  }

}
