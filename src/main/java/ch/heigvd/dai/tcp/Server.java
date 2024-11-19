package ch.heigvd.dai.tcp;

import java.io.*;
import java.net.*;
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

  public Server() throws UnknownHostException {
    this("localhost", 1234, 2);
  }

  public Server(String address, int port, int number_of_connections) throws UnknownHostException {
    this.port = port;
    this.number_of_threads = number_of_connections;
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

  public void list(BufferedReader in, BufferedWriter out, String path) throws IOException {
    String work_dir_path = "/tmp/dai" + path;
    // String work_dir_path = "/tmp/dai";
    System.out.println("checkong on server at:" + work_dir_path);
    StringBuilder sb = new StringBuilder();
    File work_dir = new File(work_dir_path);

    if (!work_dir.exists()) {
      sendError(out, Errno.ENOENT);
      return;
    } else if (!work_dir.canRead()) {
      sendError(out, Errno.EACCES);
    } else {
      out.write(String.valueOf(0));
      out.write(Server.NEW_LINE);
      out.flush();
    }

    try (Stream<Path> paths = Files.walk(work_dir.toPath())) {
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
  }
}
