package ch.heigvd.dai.exceptions;

import java.io.IOException;

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
