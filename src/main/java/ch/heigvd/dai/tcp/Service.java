package ch.heigvd.dai.tcp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class Service {
  protected int port;
  protected String address;

  abstract public void launch();

  abstract public void list(BufferedReader in, BufferedWriter out, Path path) throws IOException;

  protected void parseList(BufferedReader in, BufferedWriter out, String[] tokens) throws IOException {
    if (tokens.length != 2) {
      System.err.println("Wrong amount of arguments.\n Take a look at the spec!");
      return;
    }

    list(in, out, Paths.get(tokens[1]));
  }

  abstract public void delete(BufferedReader in, BufferedWriter out, Path path) throws IOException;

  protected void parseDelete(BufferedReader in, BufferedWriter out, String[] tokens) throws IOException {
    if (tokens.length != 2) {
      System.err.println("Wrong amount of arguments.\n Take a look at the spec!");
      return;
    }

    delete(in, out, Path.of(tokens[1]));
  }

  protected void parseTokens(BufferedReader in, BufferedWriter out, String[] tokens) throws IOException {
    String action = tokens[0];
    switch (action) {
      case "LIST":
        parseList(in, out, tokens);
        break;
      case "DELETE":
        parseDelete(in, out, tokens);
        break;

      default:
        System.err.println("Unknown action: " + action);
        break;
    }
  }

}
