package org.modelphy.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents observed data in a ModelPhy model.
 */
public class Observation {
    private final String variableName;
    private final ObservationType type;
    private final String filename;  // Only used for FILE type
    private final Map<String, Object> keyValues;  // Only used for INLINE type
    
    /**
     * Create a new file-based observation.
     * 
     * @param variableName The name of the variable being observed
     * @param type The observation type
     * @param filename The filename to load data from (only for FILE type)
     */
    public Observation(String variableName, ObservationType type, String filename) {
        this.variableName = variableName;
        this.type = type;
        this.filename = filename;
        this.keyValues = new HashMap<>();
    }
    
    /**
     * Create a new inline observation.
     * 
     * @param variableName The name of the variable being observed
     * @param type The observation type
     */
    public Observation(String variableName, ObservationType type) {
        this.variableName = variableName;
        this.type = type;
        this.filename = null;
        this.keyValues = new HashMap<>();
    }
    
    /**
     * Get the name of the variable being observed.
     * 
     * @return The variable name
     */
    public String getVariableName() {
        return variableName;
    }
    
    /**
     * Get the observation type.
     * 
     * @return The observation type
     */
    public ObservationType getType() {
        return type;
    }
    
    /**
     * Get the filename for file-based observations.
     * 
     * @return The filename, or null if this is not a file-based observation
     */
    public String getFilename() {
        return filename;
    }
    
    /**
     * Check if this is a file-based observation.
     * 
     * @return true if this is a file-based observation, false otherwise
     */
    public boolean isFile() {
        return type == ObservationType.FILE;
    }
    
    /**
     * Add a key-value pair to an inline observation.
     * 
     * @param key The key
     * @param value The value
     */
    public void addKeyValue(String key, Object value) {
        keyValues.put(key, value);
    }
    
    /**
     * Get all key-value pairs for an inline observation.
     * 
     * @return The map of key-value pairs
     */
    public Map<String, Object> getKeyValues() {
        return new HashMap<>(keyValues);
    }
    
    /**
     * Get the value for a specific key in an inline observation.
     * 
     * @param key The key
     * @return The value, or null if the key is not found
     */
    public Object getValue(String key) {
        return keyValues.get(key);
    }
    
    /**
     * Returns a string representation of the observation.
     */
    @Override
    public String toString() {
        if (type == ObservationType.FILE) {
            return "observe from \"" + filename + "\"";
        } else {
            StringBuilder sb = new StringBuilder("observe [ ");
            boolean first = true;
            
            for (Map.Entry<String, Object> entry : keyValues.entrySet()) {
                if (!first) {
                    sb.append(", ");
                }
                first = false;
                
                sb.append(entry.getKey()).append(" = ").append(entry.getValue());
            }
            
            sb.append(" ]");
            return sb.toString();
        }
    }
}
