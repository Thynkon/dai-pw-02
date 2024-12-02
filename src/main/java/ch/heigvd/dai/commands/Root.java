package ch.heigvd.dai.commands;

import java.io.File;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.Callable;

import ch.heigvd.dai.tcp.Server;
import ch.heigvd.dai.tcp.Client;
import picocli.CommandLine;
import picocli.CommandLine.ExitCode;

@CommandLine.Command(description = "A small CLI that compresses and deflates files.", version = "1.0.0", scope = CommandLine.ScopeType.INHERIT, mixinStandardHelpOptions = true)
public class Root implements Callable<Integer> {
  private static enum Mode {
    Client, Server
  };

  private static final int SUCCESS = 0;
  private static final int FAILURE = -1;

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

  public Integer call() {
    if (mode == Mode.Server) {
      if (work_dir == null) {
        System.err.println("The working directory must be specified on server mode!");
        return FAILURE;
      }

      if (!Files.exists(work_dir)) {
        System.err.println("Directory " + work_dir + " does not exist!");
        return FAILURE;
      }

      if (!Files.isDirectory(work_dir)) {
        System.err.println(work_dir + " is not a directory!");
        return FAILURE;
      }

      if (!Files.isWritable(work_dir)) {
        System.err.println("Cannot write in " + work_dir + "!");
        return FAILURE;
      }

      try {
        Server server = new Server(address, port, number_of_max_connections, work_dir);
        server.launch();
        return SUCCESS;
      } catch (UnknownHostException e) {
        System.err.println("Invalid host or DNS problem regarding address:" + address);
        return FAILURE;
      }
    }

    // Default to the current working directory when running in client mode
    work_dir = Objects.requireNonNullElseGet(work_dir, () -> Path.of(""));

    Client client = new Client(address, port, work_dir);
    client.launch();

    return SUCCESS;
  }
}
