package org.modelphy.model;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Represents an array of values in a ModelPhy model.
 */
public class ArrayValue {
    private final List<Object> elements;
    
    /**
     * Create a new array value.
     * 
     * @param elements The elements in the array
     */
    public ArrayValue(List<Object> elements) {
        this.elements = new ArrayList<>(elements);
    }
    
    /**
     * Get the array elements.
     * 
     * @return The list of elements in the array
     */
    public List<Object> getElements() {
        return new ArrayList<>(elements);
    }
    
    /**
     * Get the element at the specified index.
     * 
     * @param index The index
     * @return The element at the specified index
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public Object getElement(int index) {
        return elements.get(index);
    }
    
    /**
     * Get the number of elements in the array.
     * 
     * @return The number of elements
     */
    public int size() {
        return elements.size();
    }
    
    /**
     * Extract all variable references in this array.
     * 
     * @return A list of variable names referenced in this array
     */
    public List<String> getVariableReferences() {
        List<String> refs = new ArrayList<>();
        
        for (Object element : elements) {
            if (element instanceof VariableReference) {
                refs.add(((VariableReference) element).getName());
            }
        }
        
        return refs;
    }
    
    /**
     * Returns a string representation of the array.
     */
    @Override
    public String toString() {
        return "[" + 
            elements.stream()
                .map(Object::toString)
                .collect(Collectors.joining(", ")) +
            "]";
    }
}
