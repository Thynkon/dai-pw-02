package ch.heigvd.dai.tcp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;

import ch.heigvd.dai.Errno;
import ch.heigvd.dai.exceptions.ServerHasGoneException;

public class Client extends Service {
  private static final int CLIENT_ID = (int) (Math.random() * 1000000);

  public Client() {
    this("localhost", 1234);
  }

  public Client(String host, int port) {
    this.address = host;
    this.port = port;
  }

  private void usage() {
    System.out.println("Available commands: \n");
    System.out.println("\tLIST <path_to_dir>");
    System.out.println("\tGET <path_to_file>");
    System.out.println("\tPUT <path_to_file>");
    System.out.println("\tDELETE <path_to_file>\n");
  }

  public void sendRequest(BufferedReader in, BufferedWriter out, String command)
      throws IOException {
    out.write(command);
    out.flush();

    in.mark(1);
    if (in.read() == -1) {
      throw new ServerHasGoneException();
    }
    in.reset();
  }

  public boolean parseStatus(BufferedReader in) throws IOException {
    int byteRead;
    StringBuilder buffer = new StringBuilder();

    // status like ENOTDIR (20) are sent as two different chars/bytes: 2 (0x32) and
    // then 0 (0x30)
    while ((byteRead = in.read()) != -1) {
      buffer.append((char) byteRead);

      if (byteRead == Server.EOT) {
        break;
      }
    }

    int status = Integer.parseInt(buffer.toString().trim());
    if (status != 0) {
      System.err.println("Got error: " + Errno.getErrorMessage(status));

      return false;
    }

    return true;
  }

  public void list(BufferedReader in, BufferedWriter out, Path path) throws IOException {
    String command = "LIST " + path + Server.NEW_LINE;
    sendRequest(in, out, command);

    if (!parseStatus(in)) {
      return;
    }

    // remove \n or EOT chars
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

    System.out.println("Target deleted successfully");
  }

  @Override
  public void launch() {
    System.out.println("[Client " + CLIENT_ID + "] connecting to " + address + ":" + port);

    try (Socket socket = new Socket(address, port);
        BufferedReader in = new BufferedReader(
            new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        BufferedWriter out = new BufferedWriter(
            new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));) {

      // Run REPL until user quits
      while (!socket.isClosed()) {
        // Display prompt
        System.out.print("> ");

        // Read user input
        Reader inputReader = new InputStreamReader(System.in, StandardCharsets.UTF_8);
        BufferedReader bir = new BufferedReader(inputReader);
        String buffer = bir.readLine();

        // ctl-d
        if (buffer == null) {
          socket.close();
          continue;
        }

        try {
          String[] tokens = buffer.split(" ", 2);
          if (buffer.toLowerCase().contains("exit")) {
            socket.close();
            break;
          }

          if (tokens.length == 0) {
            System.err.println("no action!");
            continue;
          }

          try {
            parseTokens(in, out, tokens);
            System.out.println("");
          } catch (ServerHasGoneException e) {
            System.err.println(e.getMessage());
          } catch (IOException e) {
            System.err.println("Got exception: " + e.getMessage());
          } catch (IllegalArgumentException e) {
            System.err.println("Invalid command!");
            usage();
          }
        } catch (Exception e) {
          System.out.println("Invalid command. Please try again.");
          System.out.println(e.getMessage());
          continue;
        }
      }
      System.out.println("[Client] Closing connection and quitting...");
    } catch (IOException e) {
      System.out.println("[Client] exception: " + e);
    }
  }

}
