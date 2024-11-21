package ch.heigvd.dai.tcp;

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

}
