package org.modelphy.lsp.features.completion;

import org.antlr.v4.runtime.*;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.modelphy.antlr.ModelPhyLexer;
import org.modelphy.antlr.ModelPhyParser;

import java.util.*;

public class CompletionProvider {
    // Predefined types from your grammar
    private static final List<String> TYPES = Arrays.asList(
        "Real", "Integer", "Boolean", "String", "Simplex", "Vector", "Matrix",
        "TimeTree", "Tree", "Alignment", "Sequence", "QMatrix", "PositiveReal",
        "Probability", "Taxon", "TaxonSet", "TreeNode"
    );
    
    // Distributions from PhyloSpec
    private static final List<String> DISTRIBUTIONS = Arrays.asList(
        "Normal", "LogNormal", "Gamma", "Beta", "Exponential", "Dirichlet", "Uniform",
        "Yule", "BirthDeath", "Coalescent", "PhyloCTMC", "DiscreteGamma"
    );
    
    // Functions from PhyloSpec
    private static final List<String> FUNCTIONS = Arrays.asList(
        "JC69", "K80", "F81", "HKY", "GTR", "WAG", "JTT", "LG",
        "mrca", "treeHeight", "nodeAge", "branchLength", "sequence",
        "LessThan", "GreaterThan", "Equals", "Bounded"
    );
    
    // Keywords from your grammar
    private static final List<String> KEYWORDS = Arrays.asList(
        "constraint", "observe", "from"
    );
    
    public List<CompletionItem> provideCompletions(String content, int line, int character) {
        List<CompletionItem> completions = new ArrayList<>();
        
        // Determine the context where completion is requested
        CompletionContext context = determineContext(content, line, character);
        
        switch (context.getType()) {
            case TYPE_DECLARATION:
                addTypeCompletions(completions);
                break;
            case DISTRIBUTION:
                addDistributionCompletions(completions);
                break;
            case FUNCTION_CALL:
                addFunctionCompletions(completions);
                break;
            case VARIABLE_REFERENCE:
                addVariableCompletions(completions, context.getScope());
                break;
            case KEYWORD:
                addKeywordCompletions(completions);
                break;
            default:
                // Add all possible completions
                addTypeCompletions(completions);
                addDistributionCompletions(completions);
                addFunctionCompletions(completions);
                addKeywordCompletions(completions);
                addVariableCompletions(completions, context.getScope());
                break;
        }
        
        return completions;
    }
    
    private CompletionContext determineContext(String content, int line, int character) {
        // Parse the document up to the position to determine context
        // This is a simplified example - a real implementation would use
        // the parse tree to more accurately determine context
        String[] lines = content.split("\n");
        
        // Get text up to cursor position
        if (line >= lines.length) {
            return new CompletionContext(CompletionContextType.GENERAL, Collections.emptyList());
        }
        
        String lineText = lines[line];
        String textUpToCursor = character <= lineText.length() 
            ? lineText.substring(0, character) 
            : lineText;
        
        // Simplified context detection
        if (textUpToCursor.trim().endsWith("~")) {
            return new CompletionContext(CompletionContextType.DISTRIBUTION, Collections.emptyList());
        } else if (textUpToCursor.contains("=") && !textUpToCursor.contains(";")) {
            return new CompletionContext(CompletionContextType.FUNCTION_CALL, Collections.emptyList());
        } else if (textUpToCursor.trim().length() == 0 || textUpToCursor.trim().endsWith(";")) {
            return new CompletionContext(CompletionContextType.TYPE_DECLARATION, Collections.emptyList());
        }
        
        // Extract all variables from the document
        List<String> variables = extractVariables(content);
        return new CompletionContext(CompletionContextType.VARIABLE_REFERENCE, variables);
    }
    
    private List<String> extractVariables(String content) {
        // In a real implementation, you would parse the document and extract variables
        // This is a placeholder
        List<String> variables = new ArrayList<>();
        // Extract variables logic would go here
        return variables;
    }
    
    private void addTypeCompletions(List<CompletionItem> completions) {
        for (String type : TYPES) {
            CompletionItem item = new CompletionItem(type);
            item.setKind(CompletionItemKind.Class);
            completions.add(item);
        }
    }
    
    private void addDistributionCompletions(List<CompletionItem> completions) {
        for (String dist : DISTRIBUTIONS) {
            CompletionItem item = new CompletionItem(dist);
            item.setKind(CompletionItemKind.Function);
            completions.add(item);
        }
    }
    
    private void addFunctionCompletions(List<CompletionItem> completions) {
        for (String func : FUNCTIONS) {
            CompletionItem item = new CompletionItem(func);
            item.setKind(CompletionItemKind.Function);
            completions.add(item);
        }
    }
    
    private void addKeywordCompletions(List<CompletionItem> completions) {
        for (String keyword : KEYWORDS) {
            CompletionItem item = new CompletionItem(keyword);
            item.setKind(CompletionItemKind.Keyword);
            completions.add(item);
        }
    }
    
    private void addVariableCompletions(List<CompletionItem> completions, List<String> variables) {
        for (String variable : variables) {
            CompletionItem item = new CompletionItem(variable);
            item.setKind(CompletionItemKind.Variable);
            completions.add(item);
        }
    }
    
    // Helper enum and class for tracking completion context
    private enum CompletionContextType {
        TYPE_DECLARATION, DISTRIBUTION, FUNCTION_CALL, VARIABLE_REFERENCE, KEYWORD, GENERAL
    }
    
    private static class CompletionContext {
        private final CompletionContextType type;
        private final List<String> scope;
        
        public CompletionContext(CompletionContextType type, List<String> scope) {
            this.type = type;
            this.scope = scope;
        }
        
        public CompletionContextType getType() {
            return type;
        }
        
        public List<String> getScope() {
            return scope;
        }
    }
}