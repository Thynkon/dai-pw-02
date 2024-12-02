package ch.heigvd.dai.tcp;

import java.nio.file.Path;
import java.util.Arrays;
import org.tinylog.Logger;

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
        if (action.name().equalsIgnoreCase(input)) {
          return action;
        }
      }
      Logger.debug("actual: " + input + "(" + input.length() + ")");
      Logger.error(input + " not in list " + Arrays.toString(Action.values()));
      throw new IllegalArgumentException();
    }
  }

  abstract public void launch();

}
