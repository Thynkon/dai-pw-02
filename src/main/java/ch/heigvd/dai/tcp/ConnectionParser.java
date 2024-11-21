package ch.heigvd.dai.tcp;

import java.io.*;

public abstract class ConnectionParser implements AutoCloseable {
  protected InputStream bin;
  protected BufferedReader in;
  protected OutputStream bout;
  /**
   * Text output
   */
  protected BufferedWriter out;
  private boolean ownsBuf;

  public ConnectionParser(BufferedReader in, BufferedWriter out, InputStream bin, OutputStream bout) {
    this.in = in;
    this.out = out;
    this.bin = bin;
    this.bout = bout;
  }

  public ConnectionParser(InputStream bin, OutputStream bout) {
    this(new BufferedReader(new InputStreamReader(bin)), new BufferedWriter(new OutputStreamWriter(bout)), bin, bout);
    ownsBuf = true;
  }

  public void parse(String[] tokens) throws IOException {
    if (tokens.length < 1) {
      throw new IllegalArgumentException("Parsing requires at least one value");
    }
  }

  @Override
  public void close() throws IOException {
    if (ownsBuf) {
      in.close();
      out.close();
    }

  }

}
