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

  @Override
  public void parse(String[] tokens) throws IOException {
    super.parse(tokens);

    switch (tokens[0]) {
      case "LIST" -> {

        if (tokens.length != 2) {
          System.err.println("Invalid tokens: " + tokens);
          return;
        }

        System.out.println("list got: " + Arrays.toString(tokens));

        list(Path.of(tokens[1]));
      }
      default -> sendError(Errno.ENOTSUP);
    }

  }

}
