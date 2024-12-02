package ch.heigvd.dai.tcp;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ch.heigvd.dai.Errno;

public class ServerParser extends ConnectionParser {
  public final Path workDir;

  /**
   * @brief ServerParser constructor where the streams are owned by the caller
   *
   * @note The goal is to share the same buffer, the streams should only be
   *       accessed through the DataInputStream and DataOutputStream in order to
   *       ensure that no other buffer consumes from the streams.
   * 
   * @param in      The input stream
   * @param out     The output stream
   * @param workDir The working directory of the server
   */
  public ServerParser(DataInputStream in, DataOutputStream out, Path workDir) {
    super(in, out);
    this.workDir = workDir;
  }

  /**
   * @brief ServerParser constructor where the streams are owned by the parser
   * 
   * @param in      The input stream
   * @param out     The output stream
   * @param workDir The working directory of the server
   */
  public ServerParser(InputStream in, OutputStream out, Path workDir) {
    super(in, out);
    this.workDir = workDir;
  }

  /**
   * @brief Send a status code and flush the buffer
   * @param code the error number from {@link Errno}
   */
  private void sendCode(int code) throws IOException {
    byte[] data = (String.valueOf(code) + (char) Server.EOT).getBytes(StandardCharsets.UTF_8);

    out.write(data);
    out.flush();
  }

  private void sendError(int errno) throws IOException {
    sendCode(errno);
  }

  private void sendSucess() throws IOException {
    sendCode(0);
  }

  private void sendMessage(String message) throws IOException {
    // Read the file in 4k blocks
    byte[] buffer = new byte[4096];
    int bytesRead;
    int size = message.length();

    System.out.println("in.available(): " + in.available());

    // Convert the message to bytes
    byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
    int offset = 0; // Current position in the message

    System.out.println("Total bytes to send: " + messageBytes.length);

    // Write the data in chunks
    while (size > 0) {
      bytesRead = Math.min(buffer.length, size);

      System.arraycopy(messageBytes, offset, buffer, 0, bytesRead);

      out.write(buffer, 0, bytesRead);

      offset += bytesRead;
      size -= bytesRead;
    }
    System.out.println("finished reading");

    out.write(Server.EOT);
    out.flush();
  }

  /**
   * @brief Answer with the list items contained in the target path
   * @param path the path given by the client
   */
  private void list(Path path) throws IOException {
    StringBuilder sb = new StringBuilder();
    Path full_path = workDir.resolve(path).normalize();

    if (!Files.exists(full_path)) {
      sendError(Errno.ENOENT);
      return;
    }

    if (!Files.isDirectory(full_path)) {
      sendError(Errno.ENOTDIR);
      return;
    }

    if (!Files.isReadable(full_path)) {
      sendError(Errno.EACCES);
    }

    sendSucess();

    try (Stream<Path> paths = Files.list(full_path.toAbsolutePath())) {
      paths
          .forEach(p -> {
            sb.append(p.toString().replaceAll(full_path.toAbsolutePath().toString() + "/", ""));
            if (Files.isDirectory(p)) {
              sb.append("/");
            }
            sb.append(Server.DELIMITER);
          });
    }

    if (!sb.isEmpty()) {
      // remove extra : at the end
      sb.delete(sb.length() - 1, sb.length());
    } else {
      sb.append("Directory is empty!");
    }

    sendMessage(sb.toString());
  }

  private void get(Path path) throws IOException {
    Path full_path = workDir.resolve(path).normalize();
    if (!Files.exists(full_path)) {
      sendError(Errno.ENOENT);
      return;
    }

    if (!Files.isReadable(full_path)) {
      sendError(Errno.EACCES);
      return;
    }

    if (Files.isDirectory(full_path)) {
      sendError(Errno.EINVAL);
      return;
    }

    if (Files.size(full_path) == 0) {
      sendSucess();
      sendMessage("");
      return;
    }

    System.out.println("Reading file: " + full_path);
    try (FileInputStream fin = new FileInputStream(full_path.toFile())) {
      // Read the file in 4k blocks
      byte[] buffer = new byte[4096];
      int bytesRead;

      // STATUS EOT
      sendSucess();

      // LENGTH EOT
      System.out.println("sent length: " + Files.size(full_path));
      sendMessage(String.valueOf(Files.size(full_path)));

      System.out.println("starting to send file by chunks of " + buffer.length + " bytes");
      while ((bytesRead = fin.read(buffer)) != -1) {
        out.write(buffer, 0, bytesRead);
      }
      out.write(Server.EOT);
      out.flush();
      System.out.println("finished sending file");
    } catch (FileNotFoundException e) {
      // Shouldn't ever happen
      System.err.println("Cannot open file to write");
      sendError(Errno.ENOENT);
      return;
    }
  }

  /**
   * Handles DELETE request and its given path.
   * 
   * @param path to delete
   * @param out  The output where the result will be sent
   * @throws IOException when unable to write to the socket output
   */
  private void delete(Path path) throws IOException {

    Path full_path = workDir.resolve(path).normalize();
    File file = full_path.toFile();

    if (!file.exists()) {
      sendError(Errno.ENOENT);
      return;
    }

    if (!file.getParentFile().canWrite()) {
      sendError(Errno.EACCES);
      return;
    }

    if (file.isDirectory()) {
      // Delete the directory content recursively
      // @see https://www.baeldung.com/java-delete-directory#conclusion-1
      try (Stream<Path> paths = Files.walk(full_path)) {
        // Sort in reverse order to treat the deepest levels first
        List<File> files = paths.sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .collect(Collectors.toList());

        // Check if we can delete it all
        if (!files.stream().allMatch((f) -> f.getParentFile().canWrite())) {
          sendError(Errno.EACCES);
          return;
        }

        if (!files.stream().allMatch(File::delete)) {
          // Should never happen but doesn't hurt to check
          sendError(Errno.EIO);
          return;
        }

      } catch (IOException e) {
        sendError(Errno.EIO);
        System.err.println("Unable to delete directory");
        return;
      }
    } else {
      file.delete();
    }

    sendSucess();
  }

  private void put(Path path, int size) throws IOException {

    Path full_path = workDir.resolve(path).normalize();
    File file = full_path.toFile();
    System.out.println("expected size: " + size);

    System.out.println("creating file");
    if (!file.createNewFile()) {
      sendError(Errno.EACCES); // TODO: use the correct error

      // TODO: check if the implementation matches the protocol and fix whichever is
      // simpler to fix

      // skip the file content
      in.skipBytes(size);
      return;
    }
    System.out.println("created file");

    try (FileOutputStream fout = new FileOutputStream(file)) {
      // Read the file in 4k blocks
      byte[] buffer = new byte[4096];
      int bytesRead;

      System.out.println("in.available(): " + in.available());

      System.out.println("starting to read");
      while (size > 0 && (bytesRead = in.read(buffer)) != -1) {
        fout.write(buffer, 0, bytesRead);
        size -= bytesRead;
        System.out.println("remaining size: " + size);
      }
      System.out.println("finished reading");

      sendSucess();
      System.out.println("Sent answer");
      fout.flush();

    } catch (FileNotFoundException e) {
      // Shouldn't ever happen
      System.err.println("Cannot open file to write");
      sendError(Errno.ENOENT);
      return;
    }
  }

  private void mkdir(Path path) throws IOException {
    Path full_path = workDir.resolve(path).normalize();
    File file = full_path.toFile();
    if (file.exists()) {
      System.err.println("File exist");
      sendError(Errno.EEXIST);
      return;
    }

    File parent = file.getParentFile();

    if (!parent.exists()) {
      System.err.println("Parent doesn't exist");
      sendError(Errno.ENOENT);
      return;
    }

    if (!parent.isDirectory()) {
      System.err.println("Parent isn't a directory");
      sendError(Errno.ENOTDIR);
      return;
    }

    if (!parent.canWrite()) {
      System.err.println("Cannot write to parent");
      sendError(Errno.EACCES);
      return;
    }

    if (!file.mkdir()) {
      System.err.println("Failed to create dir");
      sendError(Errno.EACCES);
      return;
    }

    System.err.println("All good, replying");
    sendSucess();
  }

  @Override
  public void parse(String[] tokens) throws IOException {
    super.parse(tokens);
    System.out.println("Received " + Arrays.toString(tokens) + " length: " + tokens.length);
    Server.Action action = Server.Action.fromString(tokens[0]);

    switch (action) {
      case Server.Action.LIST -> {
        if (tokens.length == 2) {
          list(Path.of(tokens[1]));
          return;
        }

        System.err.println("Invalid tokens" + Arrays.toString(tokens));
        sendError(Errno.EINVAL);

      }

      case Server.Action.GET -> {
        if (tokens.length == 2) {
          get(Path.of(tokens[1]));
          return;
        }

        System.err.println("Invalid tokens" + Arrays.toString(tokens));
        sendError(Errno.EINVAL);

      }

      case Server.Action.DELETE -> {
        if (tokens.length == 2) {
          delete(Path.of(tokens[1]));
          return;
        }

        // TODO: replace with logging
        System.err.println("Invalid tokens: " + Arrays.toString(tokens));
        sendError(Errno.EINVAL);
      }
      case Server.Action.PUT -> {
        if (tokens.length == 2 && tokens[1].endsWith("/")) {
          System.out.println("Calling mkdir()");
          mkdir(Path.of(tokens[1]));
          return;
        }

        if (tokens.length == 3) {
          System.out.println("Calling put()");
          put(Path.of(tokens[1]), Integer.valueOf(tokens[2]));
          return;
        }

        System.err.println("Invalid tokens" + Arrays.toString(tokens));
        sendError(Errno.EINVAL);
      }
      default -> sendError(Errno.ENOTSUP);
    }
  }
}
