package ch.heigvd.dai.tcp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Scanner;

import ch.heigvd.dai.Errno;

public class Client extends Service {
  private static final int CLIENT_ID = (int) (Math.random() * 1000000);

  public Client() {
    this("localhost", 1234);
  }

  public Client(String host, int port) {
    this.address = host;
    this.port = port;
  }

  public void list(BufferedReader in, BufferedWriter out, Path path) throws IOException {
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

  public void delete(BufferedReader in, BufferedWriter out, Path path) throws IOException {
    out.write("DELETE " + path + Server.NEW_LINE);
    out.flush();

    int status = Character.getNumericValue(in.read());
    // remove \n or EOT chars
    in.read();
    if (status != 0) {
      System.err.println("Got error: " + Errno.getErrorMessage(status));
      return;
    }

    // TODO: process result ?
  }

  @Override
  public void launch() {

    try (Socket socket = new Socket(address, port);
        BufferedReader in = new BufferedReader(
            new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        BufferedWriter out = new BufferedWriter(
            new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));) {
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
          parseTokens(in, out, tokens);
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
