package brooklyn.entity.mule.main;

import io.airlift.command.Command;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import brooklyn.catalog.BrooklynCatalog;
import brooklyn.cli.Main;
import brooklyn.entity.mule.app.MuleServerApp;

/**
 * This class provides a static main entry point for launching a custom Brooklyn-based app. It inherits the standard Brooklyn
 * CLI options from {@link Main}, plus adds a few more shortcuts for favourite blueprints to the {@link LaunchCommand}.
 */
public class BrooklynMuleMain extends Main {

    private static final Logger log = LoggerFactory.getLogger(BrooklynMuleMain.class);

    public static final String DEFAULT_LOCATION = "localhost";

    public static void main(String... args) {
        log.debug("CLI invoked with args " + Arrays.asList(args));
        new BrooklynMuleMain().execCli(args);
    }

    @Override
    protected String cliScriptName() {
        return "start.sh";
    }

    @Override
    protected Class<? extends BrooklynCommand> cliLaunchCommand() {
        return LaunchCommand.class;
    }

    @Command(name = "launch", description = "Starts a server, and optionally an application. "
            + "Use e.g. --single or --cluster to launch one-node and clustered variants of the sample web application.")
    public static class LaunchCommand extends Main.LaunchCommand {

        @Override
        public Void call() throws Exception {
            return super.call();
        }

        @Override
        protected void populateCatalog(BrooklynCatalog catalog) {
            super.populateCatalog(catalog);
            catalog.addItem(MuleServerApp.class);
        }
    }
}
