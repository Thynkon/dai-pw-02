package ch.heigvd.dai.tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import ch.heigvd.dai.exceptions.ServerHasGoneException;

public class Client extends Service {
  private static final int CLIENT_ID = (int) (Math.random() * 1000000);

  /**
   * Client constructor
   *
   * @throws NullPointerException when the work_dir is null
   * @param host     the address of the remote host
   * @param port     the service port
   * @param work_dir the working directory used to resolve file paths throughout
   *                 the application
   */
  public Client(String host, int port, Path work_dir) throws NullPointerException {
    super(port, host, work_dir);
  }

  /**
   * Display the help message for the REPL
   */
  public static void usage() {
    System.out.println("Available commands: \n");
    System.out.println("\tLIST <path_to_dir>");
    System.out.println("\tGET <remote_path> <local_path>");
    System.out.println("\tPUT <local_path> <remote_path>");
    System.out.println("\tMKDIR <remote_path>");
    System.out.println("\tDELETE <path_to_file>\n");
  }

  @Override
  public void launch() {
    System.out.println("[Client " + CLIENT_ID + "] connecting to " + address + ":" + port);

    try (Socket socket = new Socket(address, port);
        ClientParser parser = new ClientParser(socket.getInputStream(), socket.getOutputStream(), work_dir);) {

      System.out.println("[Client " + CLIENT_ID + "] connected to " + address + ":" + port);

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
          String[] tokens = buffer.trim().split(" ");
          if (buffer.toLowerCase().contains("exit")) {
            socket.close();
            break;
          }

          if (tokens.length == 0) {
            System.err.println("no action!");
            continue;
          }

          try {
            parser.parse(tokens);
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
