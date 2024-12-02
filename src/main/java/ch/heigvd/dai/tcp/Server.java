package ch.heigvd.dai.tcp;

import java.io.*;
import java.net.*;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.tinylog.Logger;

public class Server extends Service {
  private static final int SERVER_ID = (int) (Math.random() * 1000000);
  private final int number_of_threads;
  private final InetAddress iaddress;
  public static final String DELIMITER = ":";
  public static final String NEW_LINE = "\n";
  public static final int EOT = 0x04;

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
      Logger.info("[Server " + Server.SERVER_ID + "] starting with id " +
          Server.SERVER_ID);
      Logger.info("[Server " + Server.SERVER_ID + "] listening on port " + port);

      while (!serverSocket.isClosed()) {
        Socket clientSocket = serverSocket.accept();
        executor.submit(new ClientHandler(clientSocket, this));
      }
    } catch (IOException e) {
      Logger.error("[Server " + Server.SERVER_ID + "] exception: " + e);
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

        Logger.info(
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
          Logger.debug("Server.ClientHandler.run(), in.available() = " + in.available());
          buffer = sb.toString();
          sb.setLength(0);
          String[] tokens = buffer.split(" ");

          if (tokens.length == 0) {
            Logger.error("no action!");
            continue; // Skip to the next request
          }

          if (buffer.toLowerCase().contains("exit")) {
            break;
          }

          try {
            parser.parse(tokens);
            out.flush(); // Ensure the response is sent immediately
          } catch (IOException e) {
            Logger.error("Got exception while parsing tokens: " + e.getMessage());
          }
        }

        Logger.info("[Server " + SERVER_ID + "] closing connection");
      } catch (IOException e) {
        Logger.info("[Server " + SERVER_ID + "] exception: " + e);
      }
    }
  }
}
