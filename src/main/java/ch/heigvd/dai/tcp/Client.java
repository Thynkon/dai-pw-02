package ch.heigvd.dai.tcp;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client extends Service {
  private static final int CLIENT_ID = (int) (Math.random() * 1000000);

  public Client() {
    this("localhost", 1234);
  }

  public Client(String host, int port) {
    this.address = host;
    this.port = port;
  }

  @Override
  public void launch() {

    try (Socket socket = new Socket(address, port);
        ClientParser parser = new ClientParser(socket.getInputStream(), socket.getOutputStream());) {
      System.out.println("[Client " + CLIENT_ID + "] connected to " + address + ":" + port);

      Scanner sc = new Scanner(System.in);
      while (sc.hasNextLine()) {
        String buffer = sc.nextLine();
        if (buffer.toLowerCase().contains("exit")) {
          System.out.println("Exiting");
          break;
        }

        String[] tokens = buffer.split(" ");

        if (tokens.length == 0) {
          System.err.println("no action!");
        }

        try {
          parser.parse(tokens);
          System.out.println("");
        } catch (IOException e) {
          System.err.println("Got exception: " + e.getMessage());
        }

      }

      sc.close();
    } catch (IOException e) {
      System.out.println("[Client] exception: " + e);
    }
  }

}
