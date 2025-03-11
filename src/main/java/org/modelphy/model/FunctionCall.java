package org.modelphy.model;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a function call with arguments in a ModelPhy model.
 */
public class FunctionCall {
    private final String name;
    private final List<Argument> arguments;
    
    public FunctionCall(String name, List<Argument> arguments) {
        this.name = name;
        this.arguments = arguments;
    }
    
    /**
     * Get the function name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get the function arguments.
     */
    public List<Argument> getArguments() {
        return arguments;
    }
    
    /**
     * Get an argument by name.
     */
    public Argument getArgument(String name) {
        return arguments.stream()
            .filter(arg -> name.equals(arg.getName()))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Get an argument by position.
     */
    public Argument getArgument(int position) {
        if (position >= 0 && position < arguments.size()) {
            return arguments.get(position);
        }
        return null;
    }
    
    /**
     * Check if this function call has an argument with the given name.
     */
    public boolean hasArgument(String name) {
        return arguments.stream()
            .anyMatch(arg -> name.equals(arg.getName()));
    }
    
    /**
     * Get references to variables in this function call.
     */
    public List<String> getVariableReferences() {
        return arguments.stream()
            .filter(arg -> arg.getValue() instanceof VariableReference)
            .map(arg -> ((VariableReference) arg.getValue()).getName())
            .collect(Collectors.toList());
    }
    
    /**
     * Returns a string representation of the function call.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(name);
        sb.append("(");
        
        boolean first = true;
        for (Argument arg : arguments) {
            if (!first) {
                sb.append(", ");
            }
            first = false;
            
            if (arg.getName() != null) {
                sb.append(arg.getName()).append("=");
            }
            sb.append(arg.getValue());
        }
        
        sb.append(")");
        return sb.toString();
    }
}
