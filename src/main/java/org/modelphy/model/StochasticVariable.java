package org.modelphy.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a stochastic variable that is drawn from a probability distribution.
 */
public class StochasticVariable extends Variable {
    private final Distribution distribution;
    
    public StochasticVariable(String name, String type, Distribution distribution) {
        super(name, type);
        this.distribution = distribution;
    }
    
    /**
     * Get the probability distribution for this variable.
     */
    public Distribution getDistribution() {
        return distribution;
    }
    
    /**
     * Get all variables that this stochastic variable depends on through its distribution parameters.
     */
    @Override
    public List<String> getDependencies() {
        List<String> dependencies = new ArrayList<>();
        
        // Add dependencies from distribution arguments
        for (Argument arg : distribution.getArguments()) {
            if (arg.getValue() instanceof VariableReference) {
                dependencies.add(((VariableReference) arg.getValue()).getName());
            } else if (arg.getValue() instanceof ArrayValue) {
                // Check for variable references in array elements
                ArrayValue array = (ArrayValue) arg.getValue();
                for (Object item : array.getElements()) {
                    if (item instanceof VariableReference) {
                        dependencies.add(((VariableReference) item).getName());
                    }
                }
            }
        }
        
        return dependencies;
    }
    
    /**
     * Returns a string representation of the stochastic variable.
     */
    @Override
    public String toString() {
        return getType() + " " + getName() + " ~ " + distribution;
    }
}
