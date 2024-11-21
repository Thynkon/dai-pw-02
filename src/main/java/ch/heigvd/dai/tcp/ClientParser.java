package ch.heigvd.dai.tcp;

import java.io.*;
import java.nio.file.Path;
import java.util.Arrays;

import ch.heigvd.dai.Errno;

public class ClientParser extends ConnectionParser {

  public ClientParser(BufferedReader in, BufferedWriter out, InputStream bin, OutputStream bout) {
    super(in, out, bin, bout);
  }

  public ClientParser(InputStream bin, OutputStream bout) {
    super(bin, bout);
  }

  private void list(String path) throws IOException {

    out.write("LIST " + path + Server.NEW_LINE);
    out.flush();

    int status = Character.getNumericValue(in.read());
    if (status != 0) {
      System.err.println("Got error: " + Errno.getErrorMessage(status));
      return;
    }

    // remove \n or EOT chars
    in.read();
    StringBuilder result = new StringBuilder();

    int byteRead;
    while ((byteRead = in.read()) != -1) {
      char c = (char) byteRead;
      if (c == Server.EOT) {
        break;
      }
      result.append(c);
    }

    System.out.println("Got result: ");
    Arrays.stream(result.toString().split(Server.DELIMITER)).forEach(s -> {
      System.out.println(s);
    });
  }

  /**
   * Upload a file to the server
   */
  private void put(Path localPath, String remotePath) throws IOException {
    File file = localPath.toFile();

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
      out.write("PUT " + remotePath + " " + file.length() + "\n");
      out.flush();

      // read the file using 4K blocks
      byte[] buffer = new byte[4096];
      int bytesRead;

      while ((bytesRead = fin.read(buffer)) != -1) {
        bout.write(buffer, 0, bytesRead);
      }
      bout.flush();
      System.out.println("file sent");
    } catch (FileNotFoundException e) {
      System.err.println("Unable to open file");
    }

    // Check if the server reveived correctly

    System.out.println("waiting for answer");
    // TODO: Check more than the first character. Might want to switch to \n instead
    // of EOT since readLine would work as expected
    int status = Character.getNumericValue(in.read());
    // remove \n or EOT chars
    in.read();
    if (status != 0) {
      System.err.println("Got error: " + Errno.getErrorMessage(status));
      return;
    }

    System.out.println("Uploaded successfully");
  }

  /**
   * Create a directory on the server
   */
  private void mkdir(String remotePath) {
    throw new RuntimeException("Not implemented");
  }

  @Override
  public void parse(String[] tokens) throws IOException {
    super.parse(tokens);

    switch (tokens[0]) {
      case "LIST", "ls", "list" -> {

        // use current directory by default
        if (tokens.length == 1) {
          list(".");
          return;
        }

        if (tokens.length != 2) {
          System.err.println("Usage: LIST " + tokens[0] + " <remote path (defaults to '.')>");

          // TODO: replace with logging
          System.err.println("Invalid tokens: " + Arrays.toString(tokens));
          return;
        }

        list(tokens[1]);
      }
      case "PUT", "put" -> {
        if (tokens.length != 3) {
          System.err.println("Usage: " + tokens[0] + " <local path> <remote path>");

          // TODO: replace with logging
          System.err.println("Invalid tokens: " + Arrays.toString(tokens));
          return;
        }

        put(Path.of(tokens[1]), tokens[2]);

      }
      case "MKDIR", "mkdir" -> {
        if (tokens.length != 2) {
          System.err.println("Usage: " + tokens[0] + " <remote path>");

          // TODO: replace with logging
          System.err.println("Invalid tokens: " + Arrays.toString(tokens));
          return;
        }

        mkdir(tokens[1]);
      }
      default -> {
        System.err.println("Invalid command");
        System.err.println("Commands:");
        System.err.println(" LIST   List files and directories");
        System.err.println(" PUT    Upload a file");
        System.err.println(" MKDIR  Create a new directory on the server");

        // TODO: replace with logging
        System.err.println("Received invalid tokens to parse: " + Arrays.toString(tokens));
      }
    }
  }

}
