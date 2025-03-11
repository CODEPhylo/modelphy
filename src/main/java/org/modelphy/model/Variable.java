package org.modelphy.model;

/**
 * Base class for all variables in a ModelPhy model.
 */
public class Variable {
    private final String name;
    private final String type;
    private Object value;
    private Observation observation;
    
    public Variable(String name, String type) {
        this.name = name;
        this.type = type;
        this.value = null;
        this.observation = null;
    }
    
    public Variable(String name, String type, Object value) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.observation = null;
    }
    
    /**
     * Get the variable name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get the variable type.
     */
    public String getType() {
        return type;
    }
    
    /**
     * Get the variable value, if set.
     */
    public Object getValue() {
        return value;
    }
    
    /**
     * Set the variable value.
     */
    public void setValue(Object value) {
        this.value = value;
    }
    
    /**
     * Check if this variable has observation data.
     */
    public boolean isObserved() {
        return observation != null;
    }
    
    /**
     * Get the observation data for this variable.
     */
    public Observation getObservation() {
        return observation;
    }
    
    /**
     * Set the observation data for this variable.
     */
    public void setObservation(Observation observation) {
        this.observation = observation;
    }
    
    /**
     * Get variables that this variable depends on.
     */
    public java.util.List<String> getDependencies() {
        return new java.util.ArrayList<>();
    }
    
    /**
     * Returns a string representation of the variable.
     */
    @Override
    public String toString() {
        return type + " " + name + (value != null ? " = " + value : "");
    }
}
