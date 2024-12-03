package ch.heigvd.dai.exceptions;

import java.io.IOException;

/**
 * This exception is thrown when the server is no longer available.
 */
public class ServerHasGoneException extends IOException {

  public ServerHasGoneException() {
    super("The server is no longer available.");
  }

  public ServerHasGoneException(String message) {
    super(message);
  }

  public ServerHasGoneException(String message, Throwable cause) {
    super(message, cause);
  }

  public ServerHasGoneException(Throwable cause) {
    super(cause);
  }
}
