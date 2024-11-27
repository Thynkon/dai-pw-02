package ch.heigvd.dai.tcp;

import java.io.*;
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
   * @brief Send an error and flush the buffer
   * @param errno the error number from {@link Errno}
   */
  private void sendError(int errno) throws IOException {
    out.writeBytes(String.valueOf(errno) + String.valueOf((char) Server.EOT));
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

    out.writeBytes(String.valueOf(0) + String.valueOf((char) Server.EOT));
    out.flush();

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

    out.writeBytes(sb.toString() + String.valueOf((char) Server.EOT));
    out.flush();
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

    out.writeBytes(String.valueOf(0) + String.valueOf((char) Server.EOT));
    out.flush();
  }

  private void put(Path path, int size) throws IOException {

    Path full_path = workDir.resolve(path).normalize();
    File file = full_path.toFile();
    System.out.println("expected size: " + size);

    System.out.println("creating file");
    if (!file.createNewFile()) {
      sendError(Errno.EACCES); // TODO: use the correct error
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

      out.writeBytes(String.valueOf(0) + String.valueOf((char) Server.EOT));
      out.flush();
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
    out.writeBytes(String.valueOf(0) + String.valueOf((char) Server.EOT));
    out.flush();
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
