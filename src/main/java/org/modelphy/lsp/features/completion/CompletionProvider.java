package org.modelphy.lsp.features.completion;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;

import java.util.*;

public class CompletionProvider {
    // Predefined collections of completions
    private final List<CompletionItem> typeCompletions = new ArrayList<>();
    private final List<CompletionItem> distributionCompletions = new ArrayList<>();
    private final List<CompletionItem> functionCompletions = new ArrayList<>();
    private final List<CompletionItem> keywordCompletions = new ArrayList<>();
    
    public CompletionProvider() {
        initializeCompletions();
    }
    
    private void initializeCompletions() {
        // Initialize type completions
        addTypeCompletion("Real", "Real-valued number");
        addTypeCompletion("Integer", "Integer-valued number");
        addTypeCompletion("Boolean", "Logical value (true/false)");
        addTypeCompletion("String", "Text value");
        addTypeCompletion("Simplex", "Probability vector with elements that sum to 1.0");
        addTypeCompletion("Vector", "Ordered collection of values");
        addTypeCompletion("Matrix", "2D grid of values");
        addTypeCompletion("Tree", "Phylogenetic tree structure");
        addTypeCompletion("TimeTree", "Time-calibrated tree");
        addTypeCompletion("Alignment", "Multiple sequence alignment");
        addTypeCompletion("Sequence", "Biological sequence");
        addTypeCompletion("QMatrix", "Rate matrix for substitution models");
        addTypeCompletion("PositiveReal", "Positive real number");
        addTypeCompletion("Probability", "Probability value");
        
        // Initialize distribution completions
        addDistributionCompletion("Normal", "Normal(mean=0.0, sd=1.0)", "Normal (Gaussian) distribution");
        addDistributionCompletion("LogNormal", "LogNormal(meanlog=0.0, sdlog=1.0)", "Log-normal distribution for positive values");
        addDistributionCompletion("Gamma", "Gamma(shape=1.0, rate=1.0)", "Gamma distribution for positive values");
        addDistributionCompletion("Beta", "Beta(alpha=1.0, beta=1.0)", "Beta distribution for values in (0,1)");
        addDistributionCompletion("Exponential", "Exponential(rate=1.0)", "Exponential distribution for rate parameters");
        addDistributionCompletion("Dirichlet", "Dirichlet(alpha=[1.0, 1.0, 1.0, 1.0])", "Dirichlet distribution for probability vectors");
        addDistributionCompletion("Uniform", "Uniform(lower=0.0, upper=1.0)", "Uniform distribution for bounded values");
        addDistributionCompletion("Yule", "Yule(birthRate=1.0)", "Yule pure-birth process for trees");
        addDistributionCompletion("BirthDeath", "BirthDeath(birthRate=1.0, deathRate=0.5)", "Birth-death process for trees");
        
        // Initialize function completions
        addFunctionCompletion("JC69", "JC69()", "Jukes-Cantor model with equal rates");
        addFunctionCompletion("K80", "K80(kappa=2.0)", "Kimura 2-parameter model");
        addFunctionCompletion("HKY", "HKY(kappa=2.0, baseFrequencies=${1:[0.25, 0.25, 0.25, 0.25]})", "Hasegawa-Kishino-Yano model");
        addFunctionCompletion("GTR", "GTR(rateMatrix=${1:[1.0, 1.0, 1.0, 1.0, 1.0, 1.0]}, baseFrequencies=${2:[0.25, 0.25, 0.25, 0.25]})", "General Time-Reversible model");
        addFunctionCompletion("LessThan", "LessThan(left=${1:value1}, right=${2:value2})", "Ensures the left value is less than the right value");
        
        // Initialize keyword completions
        addKeywordCompletion("constraint", "constraint ${1:name} = ${2:function};\n", "Define a constraint");
        addKeywordCompletion("observe", "observe", "Observe a variable");
        addKeywordCompletion("from", "from", "Specify data source");
    }
    
    private void addTypeCompletion(String name, String documentation) {
        CompletionItem item = new CompletionItem(name);
        item.setKind(CompletionItemKind.Class);
        item.setDetail("Type");
        item.setDocumentation(documentation);
        typeCompletions.add(item);
    }
    
    private void addDistributionCompletion(String name, String insertText, String documentation) {
        CompletionItem item = new CompletionItem(name);
        item.setKind(CompletionItemKind.Function);
        item.setDetail("Distribution");
        item.setDocumentation(documentation);
        item.setInsertText(insertText);
        item.setInsertTextFormat(InsertTextFormat.Snippet);
        distributionCompletions.add(item);
    }
    
    private void addFunctionCompletion(String name, String insertText, String documentation) {
        CompletionItem item = new CompletionItem(name);
        item.setKind(CompletionItemKind.Function);
        item.setDetail("Function");
        item.setDocumentation(documentation);
        item.setInsertText(insertText);
        item.setInsertTextFormat(InsertTextFormat.Snippet);
        functionCompletions.add(item);
    }
    
    private void addKeywordCompletion(String name, String insertText, String documentation) {
        CompletionItem item = new CompletionItem(name);
        item.setKind(CompletionItemKind.Keyword);
        item.setDetail("Keyword");
        item.setDocumentation(documentation);
        item.setInsertText(insertText);
        item.setInsertTextFormat(InsertTextFormat.Snippet);
        keywordCompletions.add(item);
    }
    
    public List<CompletionItem> provideCompletions(String content, int line, int character) {
        System.out.println("Completion requested at line " + line + ", character " + character);
        
        // Determine context based on the document content
        CompletionContext context = determineContext(content, line, character);
        
        List<CompletionItem> result = new ArrayList<>();
        
        switch (context.getType()) {
            case TYPE_DECLARATION:
                result.addAll(typeCompletions);
                break;
                
            case DISTRIBUTION:
                result.addAll(distributionCompletions);
                break;
                
            case FUNCTION_CALL:
                result.addAll(functionCompletions);
                break;
                
            case KEYWORD:
                result.addAll(keywordCompletions);
                break;
                
            case VARIABLE_REFERENCE:
                result.addAll(context.getVariables());
                break;
                
            default:
                // Add all possible completions for general context
                result.addAll(typeCompletions);
                result.addAll(distributionCompletions);
                result.addAll(functionCompletions);
                result.addAll(keywordCompletions);
                result.addAll(context.getVariables());
                break;
        }
        
        System.out.println("Returning " + result.size() + " completion items");
        return result;
    }
    
    private CompletionContext determineContext(String content, int line, int character) {
        // For now, just a very basic context determination
        String[] lines = content.split("\n");
        
        // Make sure the line is valid
        if (line >= lines.length) {
            return new CompletionContext(ContextType.GENERAL, new ArrayList<>());
        }
        
        String currentLine = lines[line].substring(0, Math.min(character, lines[line].length()));
        List<CompletionItem> variables = extractVariables(content);
        
        // Simple context detection
        if (currentLine.trim().endsWith("~")) {
            return new CompletionContext(ContextType.DISTRIBUTION, variables);
        } else if (currentLine.contains("=") && !currentLine.contains(";")) {
            if (currentLine.indexOf("=") == currentLine.length() - 1) {
                return new CompletionContext(ContextType.FUNCTION_CALL, variables);
            }
        } else if (currentLine.trim().isEmpty() || currentLine.trim().endsWith(";")) {
            return new CompletionContext(ContextType.TYPE_DECLARATION, variables);
        }
        
        return new CompletionContext(ContextType.GENERAL, variables);
    }
    
    private List<CompletionItem> extractVariables(String content) {
        // This is a placeholder - in a real implementation, you'd parse the document
        // to extract variable names and types
        List<CompletionItem> variables = new ArrayList<>();
        
        // Basic regex to find variable declarations
        String[] lines = content.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.contains("=") && line.endsWith(";")) {
                String[] parts = line.split("=")[0].trim().split("\\s+");
                if (parts.length >= 2) {
                    String varType = parts[0];
                    String varName = parts[1];
                    
                    CompletionItem item = new CompletionItem(varName);
                    item.setKind(CompletionItemKind.Variable);
                    item.setDetail(varType);
                    variables.add(item);
                }
            }
        }
        
        return variables;
    }
    
    // Helper enum and class for completion context
    private enum ContextType {
        TYPE_DECLARATION, DISTRIBUTION, FUNCTION_CALL, KEYWORD, VARIABLE_REFERENCE, GENERAL
    }
    
    private static class CompletionContext {
        private final ContextType type;
        private final List<CompletionItem> variables;
        
        public CompletionContext(ContextType type, List<CompletionItem> variables) {
            this.type = type;
            this.variables = variables;
        }
        
        public ContextType getType() {
            return type;
        }
        
        public List<CompletionItem> getVariables() {
            return variables;
        }
    }
}