package org.modelphy.lsp;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.*;
import org.eclipse.lsp4j.SetTraceParams;
import org.eclipse.lsp4j.WorkDoneProgressCancelParams;
import java.util.concurrent.CompletableFuture;
import java.util.Arrays;

public class ModelPhyLanguageServer implements LanguageServer {
    private final TextDocumentService textDocumentService;
    private final WorkspaceService workspaceService;
    private LanguageClient client;
    private int shutdown = 0;

    public ModelPhyLanguageServer() {
        this.textDocumentService = new ModelPhyTextDocumentService(this);
        this.workspaceService = new ModelPhyWorkspaceService(this);
    }
    
    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        System.out.println("Language server initializing...");
        
        // Configure server capabilities
        ServerCapabilities capabilities = new ServerCapabilities();
        
        // Text document sync
        capabilities.setTextDocumentSync(TextDocumentSyncKind.Incremental);
        
        // Completion support
        CompletionOptions completionOptions = new CompletionOptions();
        completionOptions.setTriggerCharacters(Arrays.asList(".", "(", "=", "~", " "));
        capabilities.setCompletionProvider(completionOptions);
        
        // Hover support
        capabilities.setHoverProvider(true);
        
        // Document formatting
        capabilities.setDocumentFormattingProvider(true);
        
        // Diagnostics implicitly supported
        
        InitializeResult result = new InitializeResult(capabilities);
        return CompletableFuture.completedFuture(result);
    }

    public void connect(LanguageClient client) {
        this.client = client;
    }

    // Getter for client
    public LanguageClient getClient() {
        return this.client;
    }
    
    /**
     * Sets the trace notification level.
     * 
     * @param params The trace notification parameters
     */
    public void setTrace(SetTraceParams params) {
        // Just log that we received the notification
        System.out.println("Trace level set to: " + params.getValue());
        
        // In a more complete implementation, you might use this to adjust 
        // your debug logging level
    }

    @Override
    public TextDocumentService getTextDocumentService() {
        return this.textDocumentService;
    }

    @Override
    public WorkspaceService getWorkspaceService() {
        return this.workspaceService;
    }

    @Override
    public void cancelProgress(WorkDoneProgressCancelParams params) {
        // Log cancellation
        System.out.println("Progress cancelled: " + params.getToken());
    } 

    @Override
    public CompletableFuture<Object> shutdown() {
        shutdown = 1;
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void exit() {
        System.exit(shutdown);
    }
}