package ch.heigvd.dai.tcp;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ch.heigvd.dai.Errno;

import org.tinylog.Logger;

public class ServerParser extends ConnectionParser {
  public final Path workDir;
  private static final ConcurrentHashMap<Path, ReentrantLock> fileLocks = new ConcurrentHashMap<>();

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

  // Function to fetch or create the lock for the given file
  // private ReentrantLock getLockForFile(String filename) {
  // // Compute and fetch the lock for the file, creating it if necessary
  // return fileLocks.computeIfAbsent(filename, k -> new ReentrantLock());
  // }
  private ReentrantLock getLockForFile(Path filename) {
    return fileLocks.computeIfAbsent(filename, k -> new ReentrantLock());
  }

  // Function to lock the file (if not already locked)
  private void lockFile(Path filename) {
    ReentrantLock lock = getLockForFile(filename);
    lock.lock();
    Logger.debug("Lock acquired for file: " + filename);
  }

  // Function to unlock the file
  private void unlockFile(Path filename) {
    ReentrantLock lock = getLockForFile(filename);
    lock.unlock();
    Logger.debug("Lock released for file: " + filename);
  }

  /**
   * @brief Send a status code and flush the buffer
   * @param code the error number from {@link Errno}
   * @throws IOException when unable to write to the socket output
   */
  private void sendCode(int code) throws IOException {
    byte[] data = (String.valueOf(code) + (char) Server.EOT).getBytes(StandardCharsets.UTF_8);

    out.write(data);
    out.flush();
  }

  /**
   * @brief Send an error code and flushes the socket
   * @param errno the error number from {@link Errno}
   * @throws IOException when unable to write to the socket output
   */
  private void sendError(int errno) throws IOException {
    sendCode(errno);
  }

  /**
   * @brief Send a success code and flushes the socket
   * @thworws IOException when unable to write to the socket output
   */
  private void sendSucess() throws IOException {
    sendCode(0);
  }

  /**
   * @brief Send a message to the client using an internal buffer
   * @param message the message to send
   * @throws IOException when unable to write to the socket output
   */
  private void sendMessage(String message) throws IOException {
    // Read the file in 4k blocks
    byte[] buffer = new byte[4096];
    int bytesRead;
    int size = message.length();

    Logger.debug("in.available(): " + in.available());

    // Convert the message to bytes
    byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
    int offset = 0; // Current position in the message

    Logger.debug("Total bytes to send: " + messageBytes.length);

    // Write the data in chunks
    while (size > 0) {
      bytesRead = Math.min(buffer.length, size);

      System.arraycopy(messageBytes, offset, buffer, 0, bytesRead);

      out.write(buffer, 0, bytesRead);

      offset += bytesRead;
      size -= bytesRead;
    }
    Logger.debug("finished reading");

    out.write(Server.EOT);
    out.flush();
  }

  /**
   * @brief Answer with the list items contained in the target path
   * @param path the path given by the client
   * @throws IOException when unable to write to the socket output
   */
  private void list(Path path) throws IOException {
    StringBuilder sb = new StringBuilder();
    Path full_path = workDir.resolve(path).normalize();
    lockFile(full_path);
    Logger.debug("locked file: " + full_path.toString());

    // first, check if path verifies the conditions specified in the protocol
    if (!Files.exists(full_path)) {
      sendError(Errno.ENOENT);
      unlockFile(full_path);
      return;
    }

    if (!Files.isDirectory(full_path)) {
      sendError(Errno.ENOTDIR);
      unlockFile(full_path);
      return;
    }

    if (!Files.isReadable(full_path)) {
      sendError(Errno.EACCES);
      unlockFile(full_path);
      return;
    }

    // Announce the client everything is fine
    sendSucess();

    // Send the list of files/directories
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
    unlockFile(full_path);
  }

  /**
   * @brief Answer with the content of the file at the given path
   * @param path the path to the file
   * @throws IOException when unable to write to the socket output
   */
  private void get(Path path) throws IOException {
    Path full_path = workDir.resolve(path).normalize();

    lockFile(full_path);
    // first, check if path verifies the conditions specified in the protocol
    if (!Files.exists(full_path)) {
      sendError(Errno.ENOENT);
      unlockFile(full_path);
      return;
    }

    if (!Files.isReadable(full_path)) {
      sendError(Errno.EACCES);
      unlockFile(full_path);
      return;
    }

    if (Files.isDirectory(full_path)) {
      sendError(Errno.EISDIR);
      unlockFile(full_path);
      return;
    }

    if (Files.size(full_path) == 0) {
      sendSucess();
      sendMessage("");
      unlockFile(full_path);
      return;
    }

    Logger.debug("Reading file: " + full_path);
    try (FileInputStream fin = new FileInputStream(full_path.toFile())) {
      // Read the file in 4k blocks
      byte[] buffer = new byte[4096];
      int bytesRead;

      // STATUS EOT
      sendSucess();

      // LENGTH EOT
      Logger.debug("sent length: " + Files.size(full_path));
      sendMessage(String.valueOf(Files.size(full_path)));

      Logger.debug("starting to send file by chunks of " + buffer.length + " bytes");
      while ((bytesRead = fin.read(buffer)) != -1) {
        out.write(buffer, 0, bytesRead);
      }
      out.flush();
      Logger.debug("finished sending file");
    } catch (FileNotFoundException e) {
      // Shouldn't ever happen
      Logger.error("Cannot open file to write");
      sendError(Errno.ENOENT);
      unlockFile(full_path);
      return;
    }

    unlockFile(full_path);
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
    lockFile(full_path);
    File file = full_path.toFile();

    if (!file.exists()) {
      sendError(Errno.ENOENT);
      unlockFile(full_path);
      return;
    }

    if (!file.getParentFile().canWrite()) {
      sendError(Errno.EACCES);
      unlockFile(full_path);
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
        Logger.error("Unable to delete directory");
        return;
      }
    } else {
      file.delete();
    }

    sendSucess();
    unlockFile(full_path);
  }

  /**
   * Handles PUT request and its given path.
   * 
   * @param path to put
   * @param size size of the file to put
   * @param out  The output where the result will be sent
   * @throws IOException when unable to write to the socket output
   */
  private void put(Path path, int size) throws IOException {
    Path full_path = workDir.resolve(path).normalize();
    lockFile(full_path);
    File file = full_path.toFile();
    Logger.debug("expected size: " + size);

    // check if the file size is within the limits
    if (size > Server.max_upload_size) {
      sendError(Errno.EFBIG);
      return;
    }

    // create parent directories if needed
    Path parentDir = full_path.getParent();
    // if user only specifier filename like: myfile.java instread of
    // mydir/myfile.java
    if (!parentDir.equals(workDir.toAbsolutePath())) {
      try {
        Files.createDirectories(parentDir);
      } catch (IOException e) {
        Logger.debug("Failed to create directories for file: " + path);
        sendError(Errno.EIO);
        unlockFile(full_path);
        return;
      }
    }

    Logger.debug("creating file");
    if (!file.createNewFile()) {
      sendError(Errno.EACCES);

      // skip the file content
      in.skipBytes(size);
      unlockFile(full_path);
      return;
    }
    Logger.debug("created file");

    try (FileOutputStream fout = new FileOutputStream(file)) {
      // Read the file in 4k blocks
      byte[] buffer = new byte[4096];
      int bytesRead;

      Logger.debug("in.available(): " + in.available());

      Logger.debug("starting to read");
      while (size > 0 && (bytesRead = in.read(buffer, 0, Math.min(size, buffer.length))) != -1) {
        fout.write(buffer, 0, bytesRead);
        size -= bytesRead;
        Logger.debug("remaining size: " + size);
      }
      Logger.debug("finished reading");

      sendSucess();
      Logger.debug("Sent answer");
      fout.flush();

    } catch (FileNotFoundException e) {
      // Shouldn't ever happen
      Logger.error("Cannot open file to write");
      sendError(Errno.ENOENT);
      unlockFile(full_path);
      return;
    }

    unlockFile(full_path);
  }

  /**
   * Handles MKDIR request and its given path.
   * 
   * @param path to create
   * @param out  The output where the result will be sent
   * @throws IOException when unable to write to the socket output
   */
  private void mkdir(Path path) throws IOException {
    Path full_path = workDir.resolve(path).normalize();
    lockFile(full_path);

    File file = full_path.toFile();
    if (file.exists()) {
      Logger.error("File exist");
      sendError(Errno.EEXIST);
      unlockFile(full_path);
      return;
    }

    File parent = file.getParentFile();

    if (!parent.exists()) {
      Logger.error("Parent doesn't exist");
      sendError(Errno.ENOENT);
      unlockFile(full_path);
      return;
    }

    if (!parent.isDirectory()) {
      Logger.error("Parent isn't a directory");
      sendError(Errno.ENOTDIR);
      unlockFile(full_path);
      return;
    }

    if (!parent.canWrite()) {
      Logger.error("Cannot write to parent");
      sendError(Errno.EACCES);
      unlockFile(full_path);
      return;
    }

    if (!file.mkdir()) {
      Logger.error("Failed to create dir");
      sendError(Errno.EACCES);
      unlockFile(full_path);
      return;
    }

    Logger.error("All good, replying");
    sendSucess();
    unlockFile(full_path);
  }

  /**
   * @brief Parse the tokens and execute the action
   * @param tokens the tokens received from the client
   * @throws IOException when unable to write to the socket output
   */
  @Override
  public void parse(String[] tokens) throws IOException {
    super.parse(tokens);
    Logger.debug("Received " + Arrays.toString(tokens) + " length: " + tokens.length);
    Server.Action action = Server.Action.fromString(tokens[0]);

    switch (action) {
      case Server.Action.LIST -> {
        if (tokens.length == 2) {
          list(Path.of(tokens[1]));
          return;
        }

        Logger.error("Invalid tokens" + Arrays.toString(tokens));
        sendError(Errno.EINVAL);

      }

      case Server.Action.GET -> {
        if (tokens.length == 2) {
          get(Path.of(tokens[1]));
          return;
        }

        Logger.error("Invalid tokens" + Arrays.toString(tokens));
        sendError(Errno.EINVAL);

      }

      case Server.Action.DELETE -> {
        if (tokens.length == 2) {
          delete(Path.of(tokens[1]));
          return;
        }

        Logger.error("Invalid tokens: " + Arrays.toString(tokens));
        sendError(Errno.EINVAL);
      }
      case Server.Action.PUT -> {
        if (tokens.length == 2 && tokens[1].endsWith("/")) {
          Logger.debug("Calling mkdir()");
          mkdir(Path.of(tokens[1]));
          return;
        }

        if (tokens.length == 3) {
          Logger.debug("Calling put()");
          put(Path.of(tokens[1]), Integer.valueOf(tokens[2]));
          return;
        }

        Logger.error("Invalid tokens" + Arrays.toString(tokens));
        sendError(Errno.EINVAL);
      }
      default -> sendError(Errno.ENOTSUP);
    }
  }
}
