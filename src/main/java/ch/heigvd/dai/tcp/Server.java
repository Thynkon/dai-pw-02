package ch.heigvd.dai.tcp;

import java.io.*;
import java.net.*;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server extends Service {
  private static final int SERVER_ID = (int) (Math.random() * 1000000);
  private final int number_of_threads;
  private final InetAddress iaddress;
  public static final String DELIMITER = ":";
  public static final String NEW_LINE = "\n";
  public static final int EOT = 0x04;

  /**
   * Server constructor
   *
   * @throws UnknownHostException when the listening address cannot be resolved
   * @throws NullPointerException when the working directory is null
   * @param address               the address to listen on
   * @param port                  the port to listen on
   * @param number_of_connections the number of concurrent connections to handle
   * @param work_dir              the working directory used when interracting
   *                              with files and directories
   */
  public Server(String address, int port, int number_of_connections, Path work_dir)
      throws UnknownHostException, NullPointerException {
    super(port, address, work_dir);
    this.number_of_threads = number_of_connections;
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
          DataInputStream in = new DataInputStream(socket.getInputStream());
          DataOutputStream out = new DataOutputStream(socket.getOutputStream());
          ServerParser parser = new ServerParser(in, out, server.work_dir);) {

        System.out.println(
            "[Server "
                + SERVER_ID
                + "] new client connected from "
                + socket.getInetAddress().getHostAddress()
                + ":"
                + socket.getPort());

        String buffer;
        StringBuilder sb = new StringBuilder();
        int c;
        while ((c = in.read()) != -1) { // Keep reading until the client closes the connection
          if (!(c == (int) '\n' || c == Server.EOT)) {
            sb.append((char) c);
            continue;
          }
          System.out.println("Server.ClientHandler.run(), in.available() = " + in.available());
          buffer = sb.toString();
          sb.setLength(0);
          String[] tokens = buffer.split(" ");

          if (tokens.length == 0) {
            System.err.println("no action!");
            continue; // Skip to the next request
          }

          System.out
              .println("Server.ClientHandler.run(), tokens[0] = '" + tokens[0] + "', length: " + tokens[0].length());

          if (buffer.toLowerCase().contains("exit")) {
            break;
          }

          try {
            parser.parse(tokens);
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
