package org.modelphy.model;

/**
 * Represents a reference to a variable in a ModelPhy model.
 */
public class VariableReference {
    private final String name;
    
    /**
     * Create a new variable reference.
     * 
     * @param name The name of the referenced variable
     */
    public VariableReference(String name) {
        this.name = name;
    }
    
    /**
     * Get the name of the referenced variable.
     * 
     * @return The variable name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Returns a string representation of the variable reference.
     */
    @Override
    public String toString() {
        return name;
    }
    
    /**
     * Check if this variable reference equals another object.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        VariableReference that = (VariableReference) obj;
        return name.equals(that.name);
    }
    
    /**
     * Generate a hash code for this variable reference.
     */
    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
