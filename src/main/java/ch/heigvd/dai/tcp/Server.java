package ch.heigvd.dai.tcp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import ch.heigvd.dai.Errno;

public class Server extends Service {
  private static final int SERVER_ID = (int) (Math.random() * 1000000);
  private final int number_of_threads;
  private final InetAddress iaddress;
  public static final String DELIMITER = ":";
  public static final String NEW_LINE = "\n";
  public static final int EOT = 0x04;
  public final Path work_dir;

  public Server(String address, int port, int number_of_connections, Path work_dir) throws UnknownHostException {
    this.port = port;
    this.number_of_threads = number_of_connections;
    this.address = address;
    this.iaddress = InetAddress.getByName(address);
    this.work_dir = work_dir;
  }

  @Override
  public void launch() {
    int backlog = 50;

    try (ServerSocket serverSocket = new ServerSocket(port, backlog, iaddress);
        ExecutorService executor = Executors.newFixedThreadPool(number_of_threads);) {
      System.out.println("[Server " + Server.SERVER_ID + "] starting with id " +
          Server.SERVER_ID);
      System.out.println("[Server " + Server.SERVER_ID + "] listening on port " + port);

      while (!serverSocket.isClosed()) {
        Socket clientSocket = serverSocket.accept();
        executor.submit(new ClientHandler(clientSocket, this));
      }
    } catch (IOException e) {
      System.out.println("[Server " + Server.SERVER_ID + "] exception: " + e);
    }
  }

  private void sendError(BufferedWriter out, int errno) throws IOException {
    out.write(String.valueOf(errno));
    out.write(Server.EOT);
    out.flush();
  }

  public void list(BufferedReader in, BufferedWriter out, Path path) throws IOException {
    StringBuilder sb = new StringBuilder();
    Path full_path = work_dir.resolve(path).normalize();

    if (!Files.exists(full_path)) {
      sendError(out, Errno.ENOENT);
      return;
    } else if (!Files.isReadable(full_path)) {
      sendError(out, Errno.EACCES);
    } else {
      out.write(String.valueOf(0));
      out.write(Server.NEW_LINE);
      out.flush();
    }

    try (Stream<Path> paths = Files.walk(work_dir)) {
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

  static class ClientHandler implements Runnable {

    private final Socket socket;
    private final Server server;

    public ClientHandler(Socket socket, Server server) {
      this.socket = socket;
      this.server = server;
    }

    @Override
    public void run() {
      try (socket; // This allow to use try-with-resources with the socket
          BufferedReader in = new BufferedReader(
              new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
          BufferedWriter out = new BufferedWriter(
              new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {
        System.out.println(
            "[Server "
                + SERVER_ID
                + "] new client connected from "
                + socket.getInetAddress().getHostAddress()
                + ":"
                + socket.getPort());

        String buffer = in.readLine();
        String[] tokens = buffer.split(" ");

        if (tokens.length == 0) {
          System.err.println("no action!");
        }

        try {
          server.parseTokens(in, out, tokens);
          System.out.println("");
        } catch (IOException e) {
          System.err.println("Got exception: " + e.getMessage());
        }

        System.out.println("[Server " + SERVER_ID + "] closing connection");
      } catch (IOException e) {
        System.out.println("[Server " + SERVER_ID + "] exception: " + e);
      }
    }

    /**
     * Handles DELETE request and its given path.
     * 
     * @param path to delete
     * @param out  The output where the result will be sent
     * @throws IOException when unable to write to the socket output
     */
    private boolean delete(String path, BufferedWriter out) throws IOException {
      Path p = null;

      try {
        p = Path.of(path);
      } catch (InvalidPathException e) {
        // TODO: Replace with actual value
        out.write("EINVAL\4");
        return false;
      }

      File file = p.toFile();

      if (!file.exists()) {
        // TODO: Replace with actual value
        out.write("ENOENT\4");
        return false;
      }

      if (!file.getParentFile().canWrite()) {
        // TODO: Replace with actual value
        out.write("EACCES\4");
        return false;
      }

      if (file.isDirectory()) {
        // Delete the directory content recursively
        // @see https://www.baeldung.com/java-delete-directory#conclusion-1
        try (Stream<Path> paths = Files.walk(p)) {
          // Sort in reverse order to treat the deepest levels first
          List<File> files = paths.sorted(Comparator.reverseOrder())
              .map(Path::toFile)
              .collect(Collectors.toList());

          // Check if we can delete it all
          if (!files.stream().allMatch((f) -> f.getParentFile().canWrite())) {
            // TODO: Replace with actual value
            out.write("EACCES\4");
            return false;
          }

          if (!files.stream().allMatch(File::delete)) {
            // Should never happen but doesn't hurt to check
            // TODO: Replace with actual value
            out.write("EIO\4");
            return false;
          }

        } catch (IOException e) {
          // TODO: Replace with actual value
          out.write("EIO\n");
          System.err.println("Unable to delete directory");
          return false;
        }
      } else {
        file.delete();
      }

      out.write("0\n");

      return true;
    }
  }
}
