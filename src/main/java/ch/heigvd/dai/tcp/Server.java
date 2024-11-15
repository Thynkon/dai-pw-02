package ch.heigvd.dai.tcp;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server extends Service {

  // private final int port;
  // private final String address;
  private static final int SERVER_ID = (int) (Math.random() * 1000000);
  private final int number_of_threads;
  private static final String TEXTUAL_DATA = "👋 from Server " + SERVER_ID;
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

        System.out.println(
            "[Server " + SERVER_ID + "] received textual data from client: " + in.readLine());

        try {
          System.out.println(
              "[Server " + SERVER_ID + "] sleeping for 10 seconds to simulate a long operation");
          Thread.sleep(10000);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }

        System.out.println(
            "[Server " + SERVER_ID + "] sending response to client: " + TEXTUAL_DATA);

        out.write(TEXTUAL_DATA + "\n");
        out.flush();

        System.out.println("[Server " + SERVER_ID + "] closing connection");
      } catch (IOException e) {
        System.out.println("[Server " + SERVER_ID + "] exception: " + e);
      }
    }
  }
}
