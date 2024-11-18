package ch.heigvd.dai.tcp;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Server extends Service {

  // private final int port;
  // private final String address;
  private static final int SERVER_ID = (int) (Math.random() * 1000000);
  private final int number_of_threads;
  private final InetAddress iaddress;

  public Server() throws UnknownHostException {
    this("localhost", 1234, 2);
  }

  public Server(String address, int port, int number_of_connections) throws UnknownHostException {
    this.port = port;
    this.number_of_threads = number_of_connections;
    // this.address = InetAddress.getByName(address);
    this.address = address;
    this.iaddress = InetAddress.getByName(address);
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
        executor.submit(new ClientHandler(clientSocket));
      }
    } catch (IOException e) {
      System.out.println("[Server " + Server.SERVER_ID + "] exception: " + e);
    }
  }

  static class ClientHandler implements Runnable {

    private final Socket socket;

    public ClientHandler(Socket socket) {
      this.socket = socket;
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

        boolean result;

        do {

          String[] args = in.readLine().split(" ");

          if (args.length < 1) {
            out.write("EINVAL\4");
            out.flush();
            return;
          }

          result = switch (args[0]) {
            case "DELETE" -> {
              if (args.length != 2) {
                out.write("EINVAL\4");
                yield false;
              }

              yield delete(args[1], out);
            }
            default -> {
              out.write("ENOTSUP\4");
              yield false;
            }
          };

          out.flush();
        } while (result);

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
