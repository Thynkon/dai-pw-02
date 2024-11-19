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

        String buffer;
        while ((buffer = in.readLine()) != null) { // Keep reading until the client closes the connection
          String[] tokens = buffer.split(" ");

          if (tokens.length == 0) {
            System.err.println("no action!");
            continue; // Skip to the next request
          }

          if (buffer.toLowerCase().contains("exit")) {
            break;
          }

          try {
            server.parseTokens(in, out, tokens);
            out.flush(); // Ensure the response is sent immediately
            System.out.println("");
          } catch (IOException e) {
            System.err.println("Got exception while parsing tokens: " + e.getMessage());
          }
        }

        System.out.println("[Server " + SERVER_ID + "] closing connection");
      } catch (IOException e) {
        System.out.println("[Server " + SERVER_ID + "] exception: " + e);
      }
    }
  }
}
