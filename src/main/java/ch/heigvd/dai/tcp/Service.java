package ch.heigvd.dai.tcp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import ch.heigvd.dai.exceptions.ServerHasGoneException;

public abstract class Service {
  protected int port;
  protected String address;

  public enum Action {
    LIST,
    DELETE,
    PUT,
    GET;

    /**
     * Converts a string to an Action enum value, case-insensitively.
     * Throws IllegalArgumentException if the input doesn't match any action.
     *
     * @param input the string to convert
     * @return the corresponding Action
     * @throws IllegalArgumentException if no match is found
     */
    public static Action fromString(String input) throws IllegalArgumentException {
      for (Action action : Action.values()) {
        if (action.name().equalsIgnoreCase(input)) {
          return action;
        }
      }
      throw new IllegalArgumentException();
    }
  }

  abstract public void launch();

  abstract public void list(BufferedReader in, BufferedWriter out, Path path)
      throws IOException;

  protected void parseList(BufferedReader in, BufferedWriter out, String[] tokens)
      throws IOException, ServerHasGoneException {
    if (tokens.length != 2) {
      System.err.println("Wrong amount of arguments.\n Take a look at the spec!");
      return;
    }

    list(in, out, Paths.get(tokens[1]));
  }

  protected void parseTokens(BufferedReader in, BufferedWriter out, String[] tokens)
      throws IOException, IllegalArgumentException {
    Action action = Action.fromString(tokens[0]);

    switch (action) {
      case LIST:
        parseList(in, out, tokens);
        break;

      default:
        System.err.println("Unknown action: " + action);
        break;
    }
  }

}
