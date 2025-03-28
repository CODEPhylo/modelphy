package org.modelphy.lsp;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Main {
    public static void main(String[] args) {
        System.out.println("ModelPhy Language Server starting...");
        
        try {
            // Create the language server instance
            ModelPhyLanguageServer server = new ModelPhyLanguageServer();
            System.out.println("Server instance created");
            
            // Create the JSON RPC launcher for the language server
            Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(
                server, 
                System.in, 
                System.out
            );
            System.out.println("Launcher created");
            
            // Get the client proxy
            LanguageClient client = launcher.getRemoteProxy();
            System.out.println("Client proxy obtained");
            
            // Connect the server to the client
            server.connect(client);
            System.out.println("Server connected to client");
            
            // Start listening for client messages
            Future<?> startListening = launcher.startListening();
            System.out.println("Started listening for client messages");
            
            // Wait until the communication is shut down
            startListening.get();
            System.out.println("Communication shutdown received");
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Fatal error in language server: " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }
}