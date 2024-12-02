package ch.heigvd.dai.tcp;

import java.nio.file.Path;
import java.util.Arrays;

public abstract class Service {
  protected int port;
  protected String address;
  protected Path work_dir;

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
        // System.out.println("action: " + action.name() + "(" + action.name().length()
        // + ")");
        if (action.name().equalsIgnoreCase(input)) {
          return action;
        }
      }
      System.out.println("actual: " + input + "(" + input.length() + ")");
      System.err.println(input + " not in list " + Arrays.toString(Action.values()));
      throw new IllegalArgumentException();
    }
  }

  abstract public void launch();

}
