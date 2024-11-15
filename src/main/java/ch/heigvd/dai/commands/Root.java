package ch.heigvd.dai.commands;

import java.net.UnknownHostException;
import java.util.concurrent.Callable;

import ch.heigvd.dai.tcp.Server;
import ch.heigvd.dai.tcp.Client;
import picocli.CommandLine;

@CommandLine.Command(description = "A small CLI that compresses and deflates files.", version = "1.0.0", scope = CommandLine.ScopeType.INHERIT, mixinStandardHelpOptions = true)
public class Root implements Callable<Integer> {
  private static enum Mode {
    Client, Server
  };

  @CommandLine.Option(names = { "-a",
      "--address" }, description = "The IP address to listen on when using server mode", defaultValue = "localhost")
  private String address;

  @CommandLine.Option(names = { "-p",
      "--port" }, description = "The port to listen on when using server mode", defaultValue = "1234")
  private int port;

  @CommandLine.Option(names = { "-c",
      "--connections" }, description = "The number of max connections handled simultaneously. Only available on server mode!(will be ignored on client mode", defaultValue = "2")
  private int number_of_max_connections;

  @CommandLine.Option(names = { "-m",
      "--mode" }, description = "The mode you want to run this program.", required = true)
  protected Mode mode;

  public Integer call() {
    if (mode == Mode.Server) {
      try {
        Server server = new Server(address, port, number_of_max_connections);
        server.launch();
        return 0;
      } catch (UnknownHostException e) {
        System.err.println("Invalid host or DNS problem regarding address:" + address);
        return -1;
      }
    }

    Client client = new Client(address, port);
    client.launch();

    return 0;
  }
}
