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

    public ModelPhyTextDocumentService(ModelPhyLanguageServer server) {
        this.server = server;
        this.diagnosticProvider = new DiagnosticProvider();
        this.completionProvider = new CompletionProvider();
        this.hoverProvider = new HoverProvider();
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
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
        List<Diagnostic> diagnostics = diagnosticProvider.provideDiagnostics(content);
        server.getClient().publishDiagnostics(new PublishDiagnosticsParams(uri, diagnostics));
    }

    @Override
    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams params) {
        String uri = params.getTextDocument().getUri();
        String content = documents.get(uri);
        Position position = params.getPosition();
        
        List<CompletionItem> items = completionProvider.provideCompletions(
            content, 
            position.getLine(), 
            position.getCharacter()
        );
        
        return CompletableFuture.completedFuture(Either.forLeft(items));
    }

    @Override
    public CompletableFuture<Hover> hover(HoverParams params) {
        String uri = params.getTextDocument().getUri();
        String content = documents.get(uri);
        Position position = params.getPosition();
        
        return CompletableFuture.completedFuture(
            hoverProvider.provideHover(content, position.getLine(), position.getCharacter())
        );
    }

    private String applyChange(String content, TextDocumentContentChangeEvent change) {
        // Convert position to offset and apply the change
        // Implementation omitted for brevity
        return content;
    }
}