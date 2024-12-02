package ch.heigvd.dai.commands;

import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import ch.heigvd.dai.tcp.Server;
import ch.heigvd.dai.tcp.Client;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import org.tinylog.Logger;

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

  @CommandLine.Option(names = { "-w",
      "--work-dir" }, description = "The directory containing all the files. It is a base directory where every manipulation of files will happen.")
  protected Path work_dir;

  @CommandLine.Option(names = { "-h", "--help" }, usageHelp = true, description = "display this help message")
  boolean usageHelpRequested;

  public Integer call() {
    if (mode == Mode.Server) {
      if (work_dir == null) {
        Logger.error("The working directory must be specified on server mode!");
        return -1;
      }

      if (!Files.exists(work_dir)) {
        Logger.error("Directory " + work_dir + " does not exist!");
        return -1;
      }

      if (!Files.isDirectory(work_dir)) {
        Logger.error(work_dir + " is not a directory!");
        return -1;
      }

      if (!Files.isWritable(work_dir)) {
        Logger.error("Cannot write in " + work_dir + "!");
        return -1;
      }

      try {
        Server server = new Server(address, port, number_of_max_connections, work_dir);
        server.launch();
        return 0;
      } catch (UnknownHostException e) {
        Logger.error("Invalid host or DNS problem regarding address:" + address);
        return -1;
      }
    } else {
      if (usageHelpRequested) {
        Client.usage();
        return 0;
      }
    }

    Client client = new Client(address, port, work_dir);
    client.launch();

    return 0;
  }
}
