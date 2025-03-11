package org.modelphy.model;

import java.util.List;

/**
 * Represents a probability distribution in a ModelPhy model.
 */
public class Distribution {
    private final String name;
    private final List<Argument> arguments;
    
    public Distribution(String name, List<Argument> arguments) {
        this.name = name;
        this.arguments = arguments;
    }
    
    /**
     * Get the distribution name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get the distribution arguments.
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
     * Check if this distribution has an argument with the given name.
     */
    public boolean hasArgument(String name) {
        return arguments.stream()
            .anyMatch(arg -> name.equals(arg.getName()));
    }
    
    /**
     * Map this ModelPhy distribution to the equivalent CodePhy distribution type.
     */
    public String toCodePhyType() {
        return switch (name.toLowerCase()) {
            case "lognormal" -> "LogNormal";
            case "normal" -> "Normal";
            case "gamma" -> "Gamma";
            case "exponential" -> "Exponential";
            case "beta" -> "Beta";
            case "dirichlet" -> "Dirichlet";
            case "uniform" -> "Uniform";
            case "yule" -> "Yule";
            case "birthdeath" -> "BirthDeath";
            case "calibrated_birthdeath" -> "ConstrainedYule";
            case "phyloctmc" -> "PhyloCTMC";
            // Add more mappings as needed
            default -> name; // Use original name if no mapping exists
        };
    }
    
    /**
     * Get what type this distribution generates based on its name.
     */
    public String getGeneratesType() {
        return switch (name.toLowerCase()) {
            case "lognormal", "normal", "gamma", "exponential", "beta", "uniform" -> "REAL";
            case "dirichlet" -> "REAL_VECTOR";
            case "yule", "birthdeath", "calibrated_birthdeath" -> "TREE";
            case "phyloctmc" -> "ALIGNMENT";
            // Add more mappings as needed
            default -> "REAL"; // Default to REAL if unknown
        };
    }
    
    /**
     * Returns a string representation of the distribution.
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
