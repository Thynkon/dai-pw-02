package ch.heigvd.dai;

import java.io.File;
import picocli.CommandLine;

@CommandLine.Command(description = "A small CLI with subcommands to demonstrate picocli.", version = "1.0.0", subcommands = {
    // ADD commands
}, scope = CommandLine.ScopeType.INHERIT, mixinStandardHelpOptions = true)
public class Main {

  @CommandLine.Parameters(index = "0", description = "The name of the user (default: World).", defaultValue = "World")
  protected String name;

  public String getName() {
    return this.name;
  }

  public static void main(String[] args) {
    String jarFilename =
        // Source: https://stackoverflow.com/a/11159435
        new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath())
            .getName();

    int exitCode = new CommandLine(new Main()).setCommandName(jarFilename).execute(args);

    System.exit(exitCode);
  }
}
