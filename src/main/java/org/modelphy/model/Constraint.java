package org.modelphy.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a constraint in a ModelPhy model, such as time calibrations or topological constraints.
 */
public class Constraint {
    private final String name;  // Can be null for anonymous constraints
    private final FunctionCall function;
    private Distribution distribution;  // Stochastic constraint (can be null)
    
    /**
     * Create a named constraint with a function.
     * 
     * @param name The constraint name
     * @param function The function defining the constraint
     */
    public Constraint(String name, FunctionCall function) {
        this.name = name;
        this.function = function;
        this.distribution = null;
    }
    
    /**
     * Create an anonymous constraint with a function.
     * 
     * @param function The function defining the constraint
     */
    public Constraint(FunctionCall function) {
        this.name = null;
        this.function = function;
        this.distribution = null;
    }
    
    /**
     * Create a constraint with a function and a distribution.
     * 
     * @param function The function defining the constraint
     * @param distribution The distribution for the constraint
     */
    public Constraint(FunctionCall function, Distribution distribution) {
        this.name = null;
        this.function = function;
        this.distribution = distribution;
    }
    
    /**
     * Get the constraint name.
     * 
     * @return The constraint name, or null if this is an anonymous constraint
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get the function defining this constraint.
     * 
     * @return The function
     */
    public FunctionCall getFunction() {
        return function;
    }
    
    /**
     * Check if this constraint has a stochastic distribution.
     * 
     * @return true if this constraint has a distribution, false otherwise
     */
    public boolean hasDistribution() {
        return distribution != null;
    }
    
    /**
     * Get the distribution for this constraint.
     * 
     * @return The distribution, or null if this is not a stochastic constraint
     */
    public Distribution getDistribution() {
        return distribution;
    }
    
    /**
     * Set the distribution for this constraint.
     * 
     * @param distribution The distribution
     */
    public void setDistribution(Distribution distribution) {
        this.distribution = distribution;
    }
    
    /**
     * Get all variables that this constraint depends on.
     * 
     * @return List of variable names referenced by this constraint
     */
    public List<String> getDependencies() {
        List<String> dependencies = new ArrayList<>();
        
        // Add dependencies from the function
        for (Argument arg : function.getArguments()) {
            if (arg.getValue() instanceof VariableReference) {
                dependencies.add(((VariableReference) arg.getValue()).getName());
            } else if (arg.getValue() instanceof ArrayValue) {
                // Check for variable references in array elements
                ArrayValue array = (ArrayValue) arg.getValue();
                dependencies.addAll(array.getVariableReferences());
            }
        }
        
        // Add dependencies from the distribution, if present
        if (distribution != null) {
            for (Argument arg : distribution.getArguments()) {
                if (arg.getValue() instanceof VariableReference) {
                    dependencies.add(((VariableReference) arg.getValue()).getName());
                } else if (arg.getValue() instanceof ArrayValue) {
                    ArrayValue array = (ArrayValue) arg.getValue();
                    dependencies.addAll(array.getVariableReferences());
                }
            }
        }
        
        return dependencies;
    }
    
    /**
     * Returns a string representation of the constraint.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        if (name != null) {
            sb.append(name).append(" = ");
        }
        
        sb.append(function);
        
        if (distribution != null) {
            sb.append(" ~ ").append(distribution);
        }
        
        return sb.toString();
    }
}
