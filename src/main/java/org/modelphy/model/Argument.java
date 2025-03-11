package org.modelphy.model;

/**
 * Represents a named or positional argument in a function call or distribution.
 */
public class Argument {
    private final String name;    // Can be null for positional arguments
    private final Object value;   // The argument value
    
    /**
     * Create a new argument with a name and value.
     * 
     * @param name The argument name, or null for positional arguments
     * @param value The argument value
     */
    public Argument(String name, Object value) {
        this.name = name;
        this.value = value;
    }
    
    /**
     * Get the argument name.
     * 
     * @return The argument name, or null if this is a positional argument
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get the argument value.
     * 
     * @return The argument value
     */
    public Object getValue() {
        return value;
    }
    
    /**
     * Check if this is a named argument.
     * 
     * @return true if this argument has a name, false otherwise
     */
    public boolean isNamed() {
        return name != null;
    }
    
    /**
     * Returns a string representation of the argument.
     */
    @Override
    public String toString() {
        if (name != null) {
            return name + "=" + value;
        }
        return value.toString();
    }
}
