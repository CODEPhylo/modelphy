package org.modelphy.model;

/**
 * Enumeration of possible observation types in a ModelPhy model.
 */
public enum ObservationType {
    /**
     * Observations specified directly in the model file.
     */
    INLINE,
    
    /**
     * Observations loaded from an external file.
     */
    FILE
}
