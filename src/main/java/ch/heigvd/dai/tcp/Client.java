package ch.heigvd.dai.tcp;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class Client extends Service {
  private static final int CLIENT_ID = (int) (Math.random() * 1000000);
  private static final String TEXTUAL_DATA = "👋 from Client " + CLIENT_ID;

  public Client() {
    this("localhost", 1234);
  }

  public Client(String host, int port) {
    this.address = host;
    this.port = port;
  }

  @Override
  public void launch() {
    System.out.println("[Client " + CLIENT_ID + "] starting with id " + CLIENT_ID);
    System.out.println("[Client " + CLIENT_ID + "] connecting to " + address + ":" + port);

    try (Socket socket = new Socket(address, port);
        BufferedReader in = new BufferedReader(
            new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        BufferedWriter out = new BufferedWriter(
            new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));) {
      System.out.println("[Client " + CLIENT_ID + "] connected to " + address + ":" + port);
      System.out.println(
          "[Client "
              + CLIENT_ID
              + "] sending textual data to server "
              + address
              + ":"
              + port
              + ": "
              + TEXTUAL_DATA);

      out.write(TEXTUAL_DATA + "\n");
      out.flush();

      System.out.println("[Client " + CLIENT_ID + "] response from server: " + in.readLine());

      System.out.println("[Client " + CLIENT_ID + "] closing connection");
    } catch (IOException e) {
      System.out.println("[Client " + CLIENT_ID + "] exception: " + e);
    }
  }
}
