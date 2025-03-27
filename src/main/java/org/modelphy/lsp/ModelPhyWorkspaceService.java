package org.modelphy.lsp;

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.DidChangeWorkspaceFoldersParams;
import org.eclipse.lsp4j.FileChangeType;
import org.eclipse.lsp4j.FileEvent;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.services.WorkspaceService;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of the LSP WorkspaceService for ModelPhy.
 * Handles workspace-related events like configuration changes and file changes.
 */
public class ModelPhyWorkspaceService implements WorkspaceService {
    private final Set<WorkspaceFolder> workspaceFolders = new HashSet<>();
    private ModelPhyLanguageServer server;
    
    // Configuration settings for ModelPhy
    private ModelPhySettings settings = new ModelPhySettings();
    
    public ModelPhyWorkspaceService(ModelPhyLanguageServer server) {
        this.server = server;
    }
    
    /**
     * Handles changes to the workspace configuration.
     */
    @Override
    public void didChangeConfiguration(DidChangeConfigurationParams params) {
        // Extract and update settings from configuration
        Object settingsObject = params.getSettings();
        if (settingsObject instanceof ModelPhySettings) {
            this.settings = (ModelPhySettings) settingsObject;
        }
        
        // Re-validate all open documents with new settings
        if (server != null && server.getTextDocumentService() instanceof ModelPhyTextDocumentService) {
            ModelPhyTextDocumentService textDocumentService = 
                (ModelPhyTextDocumentService) server.getTextDocumentService();
            textDocumentService.validateAllDocuments();
        }
    }
    
    /**
     * Handles changes to files in the workspace.
     */
    @Override
    public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
        // Process file change events
        List<String> changedMphyFiles = new ArrayList<>();
        
        for (FileEvent event : params.getChanges()) {
            String uri = event.getUri();
            if (uri.endsWith(".mphy")) {
                if (event.getType() == FileChangeType.Created || 
                    event.getType() == FileChangeType.Changed) {
                    changedMphyFiles.add(uri);
                }
            }
        }
        
        // Update any internal caches or indexes based on file changes
        if (!changedMphyFiles.isEmpty()) {
            updateModelIndex(changedMphyFiles);
        }
    }
    
    /**
     * Handles changes to workspace folders.
     */
    @Override
    public void didChangeWorkspaceFolders(DidChangeWorkspaceFoldersParams params) {
        // Add new workspace folders
        if (params.getEvent().getAdded() != null) {
            workspaceFolders.addAll(params.getEvent().getAdded());
        }
        
        // Remove deleted workspace folders
        if (params.getEvent().getRemoved() != null) {
            workspaceFolders.removeAll(params.getEvent().getRemoved());
        }
        
        // Update workspace-wide indices or caches
        refreshWorkspaceIndex();
    }
    
    /**
     * Updates the model index when files change.
     */
    private void updateModelIndex(List<String> changedFiles) {
        // This would be implemented to update any caches or indexes
        // of ModelPhy models in the workspace
        
        // For now, just log the changes
        System.out.println("Files changed: " + changedFiles);
    }
    
    /**
     * Refreshes the entire workspace index.
     */
    private void refreshWorkspaceIndex() {
        // This would scan all workspace folders and rebuild indexes
        // of ModelPhy models in the workspace
        
        // For now, just log the event
        System.out.println("Refreshing workspace index for folders: " + workspaceFolders);
    }
    
    /**
     * Gets the workspace settings.
     */
    public ModelPhySettings getSettings() {
        return settings;
    }
    
    /**
     * Configuration settings class for ModelPhy.
     */
    public static class ModelPhySettings {
        private boolean validateOnSave = true;
        private boolean validateOnType = true;
        private int maxNumberOfProblems = 100;
        private String modelPhyPath = "";
        
        public boolean isValidateOnSave() {
            return validateOnSave;
        }
        
        public void setValidateOnSave(boolean validateOnSave) {
            this.validateOnSave = validateOnSave;
        }
        
        public boolean isValidateOnType() {
            return validateOnType;
        }
        
        public void setValidateOnType(boolean validateOnType) {
            this.validateOnType = validateOnType;
        }
        
        public int getMaxNumberOfProblems() {
            return maxNumberOfProblems;
        }
        
        public void setMaxNumberOfProblems(int maxNumberOfProblems) {
            this.maxNumberOfProblems = maxNumberOfProblems;
        }
        
        public String getModelPhyPath() {
            return modelPhyPath;
        }
        
        public void setModelPhyPath(String modelPhyPath) {
            this.modelPhyPath = modelPhyPath;
        }
    }
}