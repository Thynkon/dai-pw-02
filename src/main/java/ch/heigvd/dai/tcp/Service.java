package ch.heigvd.dai.tcp;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import org.tinylog.Logger;

public abstract class Service {
  protected final int port;
  protected final String address;
  protected final Path work_dir;

  /**
   * Service constructor
   *
   * @throws NullPointerException when the work_dir is null
   * @param port     the port used for the service
   * @param address  the address used for the service
   * @param work_dir the working directory used to resolve the paths.
   */
  public Service(int port, String address, Path work_dir) throws NullPointerException {
    this.port = port;
    this.address = address;
    this.work_dir = Objects.requireNonNull(work_dir);
  }

  /**
   * Enumeration representing the protocol actions
   */
  public static enum Action {
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

  /**
   * Start the service
   */
  abstract public void launch();

}
