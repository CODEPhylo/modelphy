package org.modelphy.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a deterministic variable that is a function of other variables.
 */
public class DeterministicVariable extends Variable {
    private final Object expression;
    
    public DeterministicVariable(String name, String type, Object expression) {
        super(name, type);
        this.expression = expression;
    }
    
    /**
     * Get the expression that defines this variable.
     */
    public Object getExpression() {
        return expression;
    }
    
    /**
     * Get all variables that this deterministic variable depends on.
     */
    @Override
    public List<String> getDependencies() {
        List<String> dependencies = new ArrayList<>();
        
        if (expression instanceof FunctionCall) {
            FunctionCall func = (FunctionCall) expression;
            
            // Add dependencies from function arguments
            for (Argument arg : func.getArguments()) {
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
        } else if (expression instanceof VariableReference) {
            dependencies.add(((VariableReference) expression).getName());
        }
        
        return dependencies;
    }
    
    /**
     * Returns a string representation of the deterministic variable.
     */
    @Override
    public String toString() {
        return getType() + " " + getName() + " = " + expression;
    }
}
