package ch.heigvd.dai.tcp;

import java.io.*;

/**
 * Abstract class used to handle the streams of the sockets in both text and
 * binary.
 * We are using DataInputStream and DataOutputStream specifically because the
 * use of buffers led to issues when switching between binary and text since the
 * BufferedReader would sometimes eat the binary content
 *
 * If we weren't short on time we could try using a BufferedInputStream and
 * BufferedOutputStream which would have a buffer and share it.
 */
public abstract class ConnectionParser implements AutoCloseable {
  protected final DataInputStream in;
  protected final DataOutputStream out;
  final boolean ownsData;

  /**
   * ConnectionParser constructor with borrowed streams.
   * 
   * @implNote The streams won't be closed when this instance is destroyed since
   *           they owned by the caller.
   * @param in  the input stream
   * @param out the ouptut stream
   */
  public ConnectionParser(DataInputStream in, DataOutputStream out) {
    this.in = in;
    this.out = out;
    this.ownsData = false;
  }

  /**
   * ConnectionParser constructor for owned streams.
   * 
   * @implNote The streams will be closed once this instance is destroyed.
   * @param in  the input stream
   * @param out the ouptut stream
   */
  public ConnectionParser(InputStream in, OutputStream out) {
    this.in = new DataInputStream(in);
    this.out = new DataOutputStream(out);
    this.ownsData = true;
  }

  /**
   * Parse the arguments and handle accordingly
   * 
   * @throws IOException when there is no token to parse
   * @param tokens to parse
   */
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
