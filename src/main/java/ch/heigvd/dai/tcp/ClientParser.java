package ch.heigvd.dai.tcp;

import java.io.*;
import java.util.Arrays;

import ch.heigvd.dai.Errno;

public class ClientParser extends ConnectionParser {

  public ClientParser(BufferedReader in, BufferedWriter out, InputStream bin, OutputStream bout) {
    super(in, out, bin, bout);
  }

  public ClientParser(InputStream bin, OutputStream bout) {
    super(bin, bout);
  }

  private void list(String path) throws IOException {

    out.write("LIST " + path + Server.NEW_LINE);
    out.flush();

    int status = Character.getNumericValue(in.read());
    if (status != 0) {
      System.err.println("Got error: " + Errno.getErrorMessage(status));
      return;
    }

    // remove \n or EOT chars
    in.read();
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

  @Override
  public void parse(String[] tokens) throws IOException {
    super.parse(tokens);

    switch (tokens[0]) {
      case "LIST" -> {
        if (tokens.length != 2) {
          System.err.println("Invalid tokens: " + tokens);
          return;
        }

        list(tokens[1]);
      }
      default -> System.err.println("Received invalid tokens to parse: " + Arrays.toString(tokens));
    }
  }

}
