package org.modelphy.lsp;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.modelphy.lsp.features.completion.CompletionProvider;
import org.modelphy.lsp.features.diagnostics.DiagnosticProvider;
import org.modelphy.lsp.features.hover.HoverProvider;
import org.modelphy.model.*;
import org.modelphy.parser.ModelPhyParserWrapper;
import java.util.ArrayList;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ModelPhyTextDocumentService implements TextDocumentService {
    private final ModelPhyLanguageServer server;
    private final Map<String, String> documents = new HashMap<>();
    private final DiagnosticProvider diagnosticProvider;
    private final CompletionProvider completionProvider;
    private final HoverProvider hoverProvider;
    // to track active requests
    private final ConcurrentHashMap<String, CompletableFuture<?>> activeRequests = new ConcurrentHashMap<>();


    public ModelPhyTextDocumentService(ModelPhyLanguageServer server) {
        this.server = server;
        this.diagnosticProvider = new DiagnosticProvider();
        this.completionProvider = new CompletionProvider();
        this.hoverProvider = new HoverProvider();
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        System.out.println("Document opened: " + params.getTextDocument().getUri());
        String uri = params.getTextDocument().getUri();
        String text = params.getTextDocument().getText();
        documents.put(uri, text);
        
        // Run diagnostics when a document is opened
        reportDiagnostics(uri, text);
    }
    
    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        // Remove document from our collection when closed
        String uri = params.getTextDocument().getUri();
        documents.remove(uri);
        
        // Clear diagnostics when document is closed
        server.getClient().publishDiagnostics(
            new PublishDiagnosticsParams(uri, new ArrayList<>())
        );
    }
    
    @Override
    public void didSave(DidSaveTextDocumentParams params) {
        // Handle document save event
        String uri = params.getTextDocument().getUri();
        String content = documents.get(uri);
        
        // Re-validate on save
        if (content != null) {
            reportDiagnostics(uri, content);
        }
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        String uri = params.getTextDocument().getUri();
        
        // Apply changes to the document
        String content = documents.get(uri);
        for (TextDocumentContentChangeEvent change : params.getContentChanges()) {
            if (change.getRange() == null) {
                content = change.getText();
            } else {
                content = applyChange(content, change);
            }
        }
        documents.put(uri, content);
        
        // Re-run diagnostics on change
        reportDiagnostics(uri, content);
    }
    
    /**
     * Validates all open documents.
     * Called when configuration changes to apply new settings.
     */
    public void validateAllDocuments() {
        for (Map.Entry<String, String> entry : documents.entrySet()) {
            reportDiagnostics(entry.getKey(), entry.getValue());
        }
    }

    private void reportDiagnostics(String uri, String content) {
        System.out.println("Reporting diagnostics for: " + uri);
        List<Diagnostic> diagnostics = diagnosticProvider.provideDiagnostics(content);
        server.getClient().publishDiagnostics(new PublishDiagnosticsParams(uri, diagnostics));
    }

    @Override
    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams params) {
        CompletableFuture<Either<List<CompletionItem>, CompletionList>> result = new CompletableFuture<>();
        
        // Create a simple item
        List<CompletionItem> items = new ArrayList<>();
        items.add(new CompletionItem("Test"));
        
        // Complete the future immediately
        result.complete(Either.forLeft(items));
        
        return result.exceptionally(e -> {
            System.err.println("Completion error: " + e.getMessage());
            e.printStackTrace();
            return Either.forLeft(new ArrayList<>());
        });
    }    
    
    @Override
    public CompletableFuture<Hover> hover(HoverParams params) {
        String uri = params.getTextDocument().getUri();
        String content = documents.get(uri);
        Position position = params.getPosition();
        
        // Create a new future that can be canceled
        final CompletableFuture<Hover> result = new CompletableFuture<>();
        
        // Register the request so it can be cancelled
        String requestId = "hover-" + uri + "-" + System.currentTimeMillis();
        activeRequests.put(requestId, result);
        
        // Execute the hover operation asynchronously
        CompletableFuture.supplyAsync(() -> {
            try {
                // Check for cancellation
                if (result.isCancelled()) {
                    throw new CancellationException("Hover request cancelled");
                }
                
                // Process the hover request
                return hoverProvider.provideHover(content, position.getLine(), position.getCharacter());
            } finally {
                // Remove the request when done
                activeRequests.remove(requestId);
            }
        }).thenAccept(hover -> {
            // Complete the future with the result
            result.complete(hover);
        }).exceptionally(e -> {
            // Handle exceptions
            if (!(e.getCause() instanceof CancellationException)) {
                result.completeExceptionally(e);
            }
            return null;
        });
        
        return result;
    }
    
    private String applyChange(String content, TextDocumentContentChangeEvent change) {
        // Convert position to offset and apply the change
        // Implementation omitted for brevity
        return content;
    }
}