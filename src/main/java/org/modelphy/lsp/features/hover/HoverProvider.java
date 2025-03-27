package org.modelphy.lsp.features.hover;

import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.Position;

import java.util.HashMap;
import java.util.Map;

public class HoverProvider {
    private final Map<String, String> distributionDocs = new HashMap<>();
    private final Map<String, String> functionDocs = new HashMap<>();
    private final Map<String, String> typeDocs = new HashMap<>();
    
    public HoverProvider() {
        initializeDocumentation();
    }
    
    private void initializeDocumentation() {
        // Initialize documentation for distributions
        distributionDocs.put("Normal", "**Normal Distribution**\n\nParameters:\n- mean: Mean of the distribution\n- sd: Standard deviation");
        distributionDocs.put("LogNormal", "**LogNormal Distribution**\n\nParameters:\n- meanlog: Mean in log space\n- sdlog: Standard deviation in log space");
        distributionDocs.put("Exponential", "**Exponential Distribution**\n\nParameters:\n- rate: Rate parameter");
        distributionDocs.put("Yule", "**Yule Process**\n\nPure-birth model for trees\n\nParameters:\n- birthRate: Birth rate parameter");
        distributionDocs.put("HKY", "**Hasegawa-Kishino-Yano Model**\n\nNucleotide substitution model with transition/transversion bias\n\nParameters:\n- kappa: Transition/transversion ratio\n- baseFrequencies: Nucleotide frequencies");
        
        // Initialize documentation for functions
        functionDocs.put("LessThan", "**LessThan Constraint**\n\nEnsures the left value is less than the right value\n\nParameters:\n- left: Value that should be less\n- right: Value that should be greater");
        functionDocs.put("sequence", "**Sequence Function**\n\nCreates a sequence for a taxon\n\nParameters:\n- taxon: Name of the taxon\n- data: Sequence data");
        
        // Initialize documentation for types
        typeDocs.put("Real", "**Real Type**\n\nRepresents a real-valued number");
        typeDocs.put("PositiveReal", "**PositiveReal Type**\n\nRepresents a positive real number (value > 0)");
        typeDocs.put("Tree", "**Tree Type**\n\nRepresents a phylogenetic tree structure");
    }
    
    public Hover provideHover(String content, int line, int character) {
        // Determine what the user is hovering over
        TokenInfo tokenInfo = getTokenAtPosition(content, line, character);
        
        if (tokenInfo != null) {
            String tokenText = tokenInfo.getText();
            String documentation = null;
            
            switch (tokenInfo.getType()) {
                case DISTRIBUTION:
                    documentation = distributionDocs.get(tokenText);
                    break;
                case FUNCTION:
                    documentation = functionDocs.get(tokenText);
                    break;
                case TYPE:
                    documentation = typeDocs.get(tokenText);
                    break;
                default:
                    // Look in all maps if we're not sure
                    documentation = distributionDocs.get(tokenText);
                    if (documentation == null) {
                        documentation = functionDocs.get(tokenText);
                    }
                    if (documentation == null) {
                        documentation = typeDocs.get(tokenText);
                    }
                    break;
            }
            
            if (documentation != null) {
                MarkupContent markupContent = new MarkupContent();
                markupContent.setKind(MarkupKind.MARKDOWN);
                markupContent.setValue(documentation);
                
                Range range = new Range(
                    new Position(tokenInfo.getStartLine(), tokenInfo.getStartCharacter()),
                    new Position(tokenInfo.getEndLine(), tokenInfo.getEndCharacter())
                );
                
                return new Hover(markupContent, range);
            }
        }
        
        return new Hover();
    }
    
    private TokenInfo getTokenAtPosition(String content, int line, int character) {
        // In a real implementation, this would use the lexer to get the token at position
        // This is a placeholder
        return null;
    }
    
    // Helper class for token information
    private static class TokenInfo {
        private final String text;
        private final TokenType type;
        private final int startLine;
        private final int startCharacter;
        private final int endLine;
        private final int endCharacter;
        
        public TokenInfo(String text, TokenType type, int startLine, int startCharacter, 
                          int endLine, int endCharacter) {
            this.text = text;
            this.type = type;
            this.startLine = startLine;
            this.startCharacter = startCharacter;
            this.endLine = endLine;
            this.endCharacter = endCharacter;
        }
        
        public String getText() {
            return text;
        }
        
        public TokenType getType() {
            return type;
        }
        
        public int getStartLine() {
            return startLine;
        }
        
        public int getStartCharacter() {
            return startCharacter;
        }
        
        public int getEndLine() {
            return endLine;
        }
        
        public int getEndCharacter() {
            return endCharacter;
        }
    }
    
    // Helper enum for token types
    private enum TokenType {
        DISTRIBUTION, FUNCTION, TYPE, VARIABLE, KEYWORD, UNKNOWN
    }
}