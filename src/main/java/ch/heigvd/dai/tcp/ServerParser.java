package ch.heigvd.dai.tcp;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
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
    Path fullPath = workDir.resolve(path).normalize();

    if (!Files.exists(fullPath)) {
      sendError(Errno.ENOENT);
      return;
    }

    if (!Files.isReadable(fullPath)) {
      sendError(Errno.EACCES);
      return;
    }

    out.write(String.valueOf(0));
    out.write(Server.NEW_LINE);
    out.flush();

    try (Stream<Path> paths = Files.walk(workDir)) {
      paths
          .forEach(p -> {
            sb.append(p.toString());
            if (Files.isDirectory(p)) {
              sb.append("/");
            }
            sb.append(Server.DELIMITER);
          });
    }
    // remove extra : at the end
    sb.delete(sb.length() - 1, sb.length());

    out.write(sb.toString());
    out.write(Server.EOT);
    out.flush();
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

    System.out.println("Received " + Arrays.toString(tokens));
    switch (tokens[0]) {
      case "LIST" -> {

        if (tokens.length != 2) {
          System.err.println("Invalid tokens" + Arrays.toString(tokens));
          return;
        }

        list(Path.of(tokens[1]));
      }
      case "PUT" -> {
        if (tokens.length == 2) {
          mkdir(Path.of(tokens[1]));
          return;
        }

        if (tokens.length == 3) {
          put(Path.of(tokens[1]), Integer.valueOf(tokens[2]));
          return;
        }

        System.err.println("Invalid tokens" + Arrays.toString(tokens));

      }
      default -> sendError(Errno.ENOTSUP);
    }

  }

}
