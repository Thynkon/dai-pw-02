package ch.heigvd.dai.tcp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import ch.heigvd.dai.Errno;

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

  public void list(BufferedReader in, BufferedWriter out, String path) throws IOException {
    out.write("LIST " + path + Server.NEW_LINE);
    out.flush();

    int status = Character.getNumericValue(in.read());
    if (status != 0) {
      System.err.println("Got error: " + Errno.getErrorMessage(status));
      return;
    }

    // remove \n or EOT chars
    in.read();
    StringBuilder result = new StringBuilder();

    int byteRead;
    while ((byteRead = in.read()) != -1) {
      char c = (char) byteRead;
      if (c == Server.EOT) {
        break;
      }
      result.append(c);
    }

    System.out.println("Got result: ");
    Arrays.stream(result.toString().split(Server.DELIMITER)).forEach(s -> {
      System.out.println(s);
    });

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

      list(in, out, "/asdf");

    } catch (IOException e) {
      System.out.println("[Client " + CLIENT_ID + "] exception: " + e);
    }
  }
}
