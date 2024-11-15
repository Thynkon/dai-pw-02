package ch.heigvd.dai.tcp;

public abstract class Service {
  protected int port;
  protected String address;

  abstract public void launch();
}
