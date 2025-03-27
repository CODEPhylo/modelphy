package org.modelphy.lsp;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        try {
            startServer(System.in, System.out);
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.log(Level.SEVERE, "Exception while starting language server", e);
            System.exit(1);
        }
    }

    private static void startServer(InputStream in, OutputStream out) throws InterruptedException, ExecutionException {
        // Create the language server instance
        ModelPhyLanguageServer server = new ModelPhyLanguageServer();

        // Create the JSON RPC launcher for the language server
        Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(server, in, out);

        // Get the client proxy
        LanguageClient client = launcher.getRemoteProxy();

        // Connect the server to the client
        server.connect(client);

        // Start listening for client messages
        Future<?> startListening = launcher.startListening();

        // Wait until the communication is shut down
        startListening.get();
    }
}