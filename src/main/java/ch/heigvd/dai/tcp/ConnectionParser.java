package ch.heigvd.dai.tcp;

import java.io.*;

public abstract class ConnectionParser implements AutoCloseable {
  protected DataInputStream in;
  protected DataOutputStream out;
  boolean ownsData;

  public ConnectionParser(DataInputStream in, DataOutputStream out) {
    this.in = in;
    this.out = out;
  }

  public ConnectionParser(InputStream in, OutputStream out) {
    this.in = new DataInputStream(in);
    this.out = new DataOutputStream(out);
    this.ownsData = true;
  }

  public void parse(String[] tokens) throws IOException {
    if (tokens.length < 1) {
      throw new IllegalArgumentException("Parsing requires at least one value");
    }
  }

  @Override
  public void close() throws IOException {
    if (ownsData) {
      in.close();
      out.close();
    }
  }

}
