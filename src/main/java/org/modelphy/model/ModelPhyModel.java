package org.modelphy.model;

import java.util.*;

/**
 * Represents a complete ModelPhy model with variables, constraints, and observations.
 */
public class ModelPhyModel {
    private final List<Variable> variables;
    private final List<StochasticVariable> stochasticVariables;
    private final List<DeterministicVariable> deterministicVariables;
    private final List<Constraint> constraints;
    
    public ModelPhyModel() {
        this.variables = new ArrayList<>();
        this.stochasticVariables = new ArrayList<>();
        this.deterministicVariables = new ArrayList<>();
        this.constraints = new ArrayList<>();
    }
    
    /**
     * Add a variable to the model.
     */
    public void addVariable(Variable variable) {
        variables.add(variable);
    }
    
    /**
     * Add a stochastic variable (with a distribution) to the model.
     */
    public void addStochasticVariable(StochasticVariable variable) {
        stochasticVariables.add(variable);
        variables.add(variable);
    }
    
    /**
     * Add a deterministic variable (function of other variables) to the model.
     */
    public void addDeterministicVariable(DeterministicVariable variable) {
        deterministicVariables.add(variable);
        variables.add(variable);
    }
    
    /**
     * Add a constraint to the model.
     */
    public void addConstraint(Constraint constraint) {
        constraints.add(constraint);
    }
    
    /**
     * Get all variables in the model.
     */
    public List<Variable> getVariables() {
        return Collections.unmodifiableList(variables);
    }
    
    /**
     * Get all stochastic variables in the model.
     */
    public List<StochasticVariable> getStochasticVariables() {
        return Collections.unmodifiableList(stochasticVariables);
    }
    
    /**
     * Get all deterministic variables in the model.
     */
    public List<DeterministicVariable> getDeterministicVariables() {
        return Collections.unmodifiableList(deterministicVariables);
    }
    
    /**
     * Get all constraints in the model.
     */
    public List<Constraint> getConstraints() {
        return Collections.unmodifiableList(constraints);
    }
    
    /**
     * Find a variable by name.
     */
    public Optional<Variable> findVariable(String name) {
        return variables.stream()
            .filter(v -> v.getName().equals(name))
            .findFirst();
    }
    
    /**
     * Get all observed variables.
     */
    public List<Variable> getObservedVariables() {
        return variables.stream()
            .filter(Variable::isObserved)
            .toList();
    }
    
    /**
     * Get all tree variables.
     */
    public List<Variable> getTreeVariables() {
        return variables.stream()
            .filter(v -> v.getType().equals("Tree") || v.getType().equals("TimeTree"))
            .toList();
    }
    
    /**
     * Get all alignment variables.
     */
    public List<Variable> getAlignmentVariables() {
        return variables.stream()
            .filter(v -> v.getType().equals("Alignment"))
            .toList();
    }
    
    /**
     * Get all simplex variables.
     */
    public List<Variable> getSimplexVariables() {
        return variables.stream()
            .filter(v -> v.getType().equals("Simplex"))
            .toList();
    }
    
    /**
     * Get all real variables.
     */
    public List<Variable> getRealVariables() {
        return variables.stream()
            .filter(v -> v.getType().equals("Real") || v.getType().equals("PositiveReal"))
            .toList();
    }
    
    /**
     * Get all substitution model variables.
     */
    public List<Variable> getSubstitutionModelVariables() {
        return variables.stream()
            .filter(v -> v.getType().equals("QMatrix"))
            .toList();
    }
    
    /**
     * Check if the model is valid (has required components).
     */
    public boolean isValid() {
        // A valid model needs at least one tree, one substitution model, and one alignment
        boolean hasTree = !getTreeVariables().isEmpty();
        boolean hasSubModel = !getSubstitutionModelVariables().isEmpty();
        boolean hasAlignment = !getAlignmentVariables().isEmpty();
        
        return hasTree && hasSubModel && hasAlignment;
    }
    
    /**
     * Returns a string representation of the model structure.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ModelPhy Model:\n");
        
        sb.append("  Stochastic Variables (").append(stochasticVariables.size()).append("):\n");
        for (StochasticVariable var : stochasticVariables) {
            sb.append("    ").append(var).append("\n");
        }
        
        sb.append("  Deterministic Variables (").append(deterministicVariables.size()).append("):\n");
        for (DeterministicVariable var : deterministicVariables) {
            sb.append("    ").append(var).append("\n");
        }
        
        sb.append("  Constraints (").append(constraints.size()).append("):\n");
        for (Constraint constraint : constraints) {
            sb.append("    ").append(constraint).append("\n");
        }
        
        sb.append("  Observations:\n");
        for (Variable var : getObservedVariables()) {
            sb.append("    ").append(var.getName()).append(": ").append(var.getObservation()).append("\n");
        }
        
        return sb.toString();
    }
}
