package ch.heigvd.dai.tcp;

import java.io.*;
import java.nio.file.Path;
import java.util.Arrays;

import ch.heigvd.dai.Errno;
import ch.heigvd.dai.exceptions.ServerHasGoneException;

public class ClientParser extends ConnectionParser {

  public ClientParser(DataInputStream in, DataOutputStream out) {
    super(in, out);
  }

  public ClientParser(InputStream in, OutputStream out) {
    super(in, out);
  }

  private void sendRequest(String command)
      throws IOException {
    out.writeChars(command);
    out.flush();

    // FIXME: following code doesn't work with DataInputStream
    // in.mark(1);
    // if (in.read() == -1) {
    // throw new ServerHasGoneException();
    // }
    // in.reset();
  }

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

  private void list(String path) throws IOException {
    String command = "LIST " + path + Server.NEW_LINE;
    sendRequest(command);

    if (!parseStatus()) {
      return;
    }

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

  public void delete(String path) throws IOException {
    String command = "DELETE " + path + Server.NEW_LINE;
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
  private void mkdir(String remotePath) {
    throw new RuntimeException("Not implemented");
  }

  @Override
  public void parse(String[] tokens) throws IOException, ServerHasGoneException {
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
      case "DELETE", "rm", "delete" -> {

        if (tokens.length != 2) {
          System.err.println("Usage: " + tokens[0] + " <path>");

          // TODO: replace with logging
          System.err.println("Invalid tokens: " + Arrays.toString(tokens));
          return;
        }

        delete(tokens[1]);
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
        // TODO: replace with logging
        System.err.println("Received invalid tokens to parse: " + Arrays.toString(tokens));
      }
    }
  }

}
