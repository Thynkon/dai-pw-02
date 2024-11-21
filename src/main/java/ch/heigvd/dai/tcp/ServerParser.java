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

  public ServerParser(BufferedReader in, BufferedWriter out, InputStream bin, OutputStream bout, Path workDir) {
    super(in, out, bin, bout);
    this.workDir = workDir;
  }

  public ServerParser(InputStream bin, OutputStream bout, Path workDir) {
    super(bin, bout);
    this.workDir = workDir;
  }

  private void sendError(int errno) throws IOException {
    out.write(String.valueOf(errno));
    out.write(Server.EOT);
    out.flush();
  }

  private void list(Path path) throws IOException {
    StringBuilder sb = new StringBuilder();
    Path full_path = workDir.resolve(path).normalize();

    if (!Files.exists(full_path)) {
      sendError(Errno.ENOENT);
      return;
    } else if (!Files.isDirectory(full_path)) {
      sendError(Errno.ENOTDIR);
      return;
    } else if (!Files.isReadable(full_path)) {
      sendError(Errno.EACCES);
    } else {
      out.write(String.valueOf(0));
      out.write(Server.EOT);
      out.flush();
    }

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

    out.write(sb.toString());
    out.write(Server.EOT);
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
      out.write(String.valueOf(Errno.ENOENT) + Server.EOT);
      out.flush();
      return;
    }

    if (!file.getParentFile().canWrite()) {
      out.write(String.valueOf(Errno.EACCES) + Server.EOT);
      out.flush();
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
          out.write(String.valueOf(Errno.EACCES) + Server.EOT);
          out.flush();
          return;
        }

        if (!files.stream().allMatch(File::delete)) {
          // Should never happen but doesn't hurt to check
          out.write(String.valueOf(Errno.EIO) + Server.EOT);
          out.flush();
          return;
        }

      } catch (IOException e) {
        out.write(String.valueOf(Errno.EIO) + Server.EOT);
        out.flush();
        System.err.println("Unable to delete directory");
        return;
      }
    } else {
      file.delete();
    }

    out.write(String.valueOf(0) + Server.NEW_LINE);
    out.flush();

    return;
  }

  private void put(Path path, int size) throws IOException {
    File file = path.toFile();
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

      System.out.println("starting to read");
      while ((bytesRead = bin.read(buffer)) != -1 || size <= 0) {
        fout.write(buffer, 0, bytesRead);
        size -= bytesRead;
        System.out.println("remaining size: " + size);
      }
      System.out.println("finished reading");

      out.write("0" + Server.EOT);
      out.flush();
      System.out.println("Sent answer");
    } catch (FileNotFoundException e) {
      System.err.println("Cannot open file to write");
      sendError(Errno.EACCES); // TODO: use the correct error
      return;
    }
  }

  private void mkdir(Path path) {
    throw new RuntimeException("Not implemented");
  }

  @Override
  public void parse(String[] tokens) throws IOException {
    super.parse(tokens);
    Server.Action action = Server.Action.fromString(tokens[0]);

    System.out.println("Received " + Arrays.toString(tokens));
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
        if (tokens.length == 2) {
          mkdir(Path.of(tokens[1]));
          return;
        }

        if (tokens.length == 3) {
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
