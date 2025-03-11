package org.modelphy.converter;

import org.modelphy.model.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Converts a ModelPhy model to CodePhy JSON format.
 */
public class CodePhyConverter {
    private final ModelPhyModel model;
    private final ObjectMapper mapper;
    
    /**
     * Create a new CodePhy converter for the given model.
     * 
     * @param model The ModelPhy model to convert
     */
    public CodePhyConverter(ModelPhyModel model) {
        this.model = model;
        this.mapper = new ObjectMapper();
    }
    
    /**
     * Convert the ModelPhy model to CodePhy JSON.
     * 
     * @return A string containing the CodePhy JSON representation
     */
    public String convert() {
        try {
            ObjectNode root = mapper.createObjectNode();
            
            // Add CodePhy version
            root.put("codephyVersion", "0.1");
            
            // Add model name (using first alignment or tree variable as model name)
            String modelName = getModelName();
            root.put("model", modelName);
            
            // Add metadata
            addMetadata(root);
            
            // Add random variables
            ObjectNode randomVariables = root.putObject("randomVariables");
            addRandomVariables(randomVariables);
            
            // Add deterministic functions
            ObjectNode deterministicFunctions = root.putObject("deterministicFunctions");
            addDeterministicFunctions(deterministicFunctions);
            
            // Add constraints
            if (!model.getConstraints().isEmpty()) {
                ArrayNode constraints = root.putArray("constraints");
                addConstraints(constraints);
            }
            
            // Convert to formatted JSON
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (Exception e) {
            throw new RuntimeException("Error converting to CodePhy format", e);
        }
    }
    
    /**
     * Get a name for the model based on its variables.
     */
    private String getModelName() {
        // Try to use the name of the first alignment or tree variable
        for (Variable var : model.getVariables()) {
            if (var.getType().equals("alignment")) {
                return var.getName() + "_model";
            }
        }
        
        for (Variable var : model.getVariables()) {
            if (var.getType().equals("tree") || var.getType().equals("timetree")) {
                return var.getName() + "_model";
            }
        }
        
        return "modelphy_model";
    }
    
    /**
     * Add metadata to the CodePhy JSON.
     */
    private void addMetadata(ObjectNode root) {
        ObjectNode metadata = root.putObject("metadata");
        
        // Basic metadata
        metadata.put("title", "ModelPhy converted model");
        metadata.put("description", "Model converted from ModelPhy format to CodePhy");
        
        // Current date/time
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        metadata.put("created", timestamp);
        metadata.put("modified", timestamp);
        
        // Version
        metadata.put("version", "1.0.0");
        
        // Software
        ObjectNode software = metadata.putObject("software");
        software.put("name", "ModelPhy Converter");
        software.put("version", "1.0.0");
        
        // Tags
        ArrayNode tags = metadata.putArray("tags");
        tags.add("phylogenetics");
        tags.add("modelphy");
        tags.add("converted");
        
        // Add model-specific tags based on variables
        for (Variable var : model.getVariables()) {
            if (var instanceof DeterministicVariable && var.getType().equals("substmodel")) {
                DeterministicVariable detVar = (DeterministicVariable) var;
                if (detVar.getExpression() instanceof FunctionCall) {
                    FunctionCall func = (FunctionCall) detVar.getExpression();
                    tags.add(func.getName().toLowerCase());
                }
            }
        }
    }
    
    /**
     * Add random variables to the CodePhy JSON.
     */
    private void addRandomVariables(ObjectNode randomVariables) {
        for (StochasticVariable var : model.getStochasticVariables()) {
            ObjectNode varNode = randomVariables.putObject(var.getName());
            
            // Add distribution
            ObjectNode distNode = varNode.putObject("distribution");
            addDistribution(distNode, var.getDistribution(), var.getType());
            
            // Add observed value if present
            if (var.isObserved()) {
                Observation obs = var.getObservation();
                
                if (obs.getType() == ObservationType.FILE) {
                    ObjectNode observedValue = varNode.putObject("observedValue");
                    observedValue.put("file", obs.getFilename());
                } else if (obs.getType() == ObservationType.INLINE) {
                    Map<String, Object> keyValues = obs.getKeyValues();
                    
                    if (var.getType().equals("alignment")) {
                        // Handle alignment observations
                        ObjectNode observedValue = varNode.putObject("observedValue");
                        ObjectNode sequences = observedValue.putObject("sequences");
                        
                        for (Map.Entry<String, Object> entry : keyValues.entrySet()) {
                            if (entry.getValue() instanceof FunctionCall) {
                                FunctionCall func = (FunctionCall) entry.getValue();
                                if (func.getName().equals("sequence")) {
                                    Argument strArg = func.getArgument("str");
                                    if (strArg != null && strArg.getValue() instanceof String) {
                                        sequences.put(entry.getKey(), (String) strArg.getValue());
                                    }
                                }
                            }
                        }
                    } else {
                        // Handle other types of observations
                        // This would need custom handling based on the variable type
                        JsonNode valueNode = convertToJsonValue(keyValues);
                        if (valueNode != null) {
                            varNode.set("observedValue", valueNode);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Add deterministic functions to the CodePhy JSON.
     */
    private void addDeterministicFunctions(ObjectNode deterministicFunctions) {
        for (DeterministicVariable var : model.getDeterministicVariables()) {
            ObjectNode varNode = deterministicFunctions.putObject(var.getName());
            
            if (var.getExpression() instanceof FunctionCall) {
                FunctionCall func = (FunctionCall) var.getExpression();
                varNode.put("function", func.getName());
                
                ObjectNode argsNode = varNode.putObject("arguments");
                for (Argument arg : func.getArguments()) {
                    if (arg.getName() != null) {
                        JsonNode valueNode = convertArgumentToJsonValue(arg);
                        argsNode.set(arg.getName(), valueNode);
                    }
                }
            } else if (var.getExpression() instanceof VariableReference) {
                // Simple reference to another variable
                VariableReference ref = (VariableReference) var.getExpression();
                varNode.put("function", "reference");
                
                ObjectNode argsNode = varNode.putObject("arguments");
                ObjectNode varRefNode = argsNode.putObject("source");
                varRefNode.put("variable", ref.getName());
            }
        }
    }
    
    /**
     * Add constraints to the CodePhy JSON.
     */
    private void addConstraints(ArrayNode constraints) {
        for (Constraint constraint : model.getConstraints()) {
            ObjectNode constraintNode = mapper.createObjectNode();
            
            FunctionCall func = constraint.getFunction();
            String funcName = func.getName();
            
            if (funcName.equals("mrca")) {
                // Handle MRCA constraint
                constraintNode.put("type", "equals");
                constraintNode.put("left", constraint.getName() != null ? constraint.getName() : "mrca_node");
                
                // Get taxa from the function call
                Argument taxaArg = func.getArgument("taxa");
                if (taxaArg != null && taxaArg.getValue() instanceof ArrayValue) {
                    ArrayValue taxa = (ArrayValue) taxaArg.getValue();
                    if (constraint.hasDistribution()) {
                        // Age constraint
                        ObjectNode rightNode = constraintNode.putObject("right");
                        rightNode.put("distribution", constraint.getDistribution().getName());
                    }
                }
            } else if (funcName.equals("root")) {
                // Handle root age constraint
                constraintNode.put("type", "equals");
                constraintNode.put("left", constraint.getName() != null ? constraint.getName() : "root_age");
                
                if (constraint.hasDistribution()) {
                    ObjectNode rightNode = constraintNode.putObject("right");
                    rightNode.put("distribution", constraint.getDistribution().getName());
                }
            } else {
                // Handle other constraints
                constraintNode.put("type", "custom");
                constraintNode.put("function", funcName);
                
                // Add arguments
                ObjectNode argsNode = constraintNode.putObject("arguments");
                for (Argument arg : func.getArguments()) {
                    if (arg.getName() != null) {
                        JsonNode valueNode = convertArgumentToJsonValue(arg);
                        argsNode.set(arg.getName(), valueNode);
                    }
                }
            }
            
            constraints.add(constraintNode);
        }
    }
    
    /**
     * Add a distribution to the CodePhy JSON.
     */
    private void addDistribution(ObjectNode distNode, Distribution dist, String varType) {
        // Map ModelPhy distribution to CodePhy type
        String codephyType = dist.toCodePhyType();
        distNode.put("type", codephyType);
        
        // Determine what this distribution generates
        String generates = mapModelPhyTypeToCodePhyGenerates(varType, dist);
        distNode.put("generates", generates);
        
        // Add parameters
        ObjectNode params = distNode.putObject("parameters");
        
        switch (codephyType.toLowerCase()) {
            case "lognormal":
                addLognormalParameters(params, dist);
                break;
            case "normal":
                addNormalParameters(params, dist);
                break;
            case "exponential":
                addExponentialParameters(params, dist);
                break;
            case "uniform":
                addUniformParameters(params, dist);
                break;
            case "gamma":
                addGammaParameters(params, dist);
                break;
            case "dirichlet":
                addDirichletParameters(params, dist);
                break;
            case "yule":
                addYuleParameters(params, dist);
                break;
            case "birthdeath":
                addBirthDeathParameters(params, dist);
                break;
            case "phyloctmc":
                addPhyloCTMCParameters(params, dist);
                break;
            default:
                // For other distributions, add all parameters as-is
                for (Argument arg : dist.getArguments()) {
                    if (arg.getName() != null) {
                        JsonNode valueNode = convertArgumentToJsonValue(arg);
                        params.set(arg.getName(), valueNode);
                    }
                }
                break;
        }
    }
    
    /**
     * Map ModelPhy type to CodePhy "generates" value.
     */
    private String mapModelPhyTypeToCodePhyGenerates(String modelphyType, Distribution dist) {
        switch (modelphyType.toLowerCase()) {
            case "real":
                return "REAL";
            case "integer":
                return "INTEGER";
            case "boolean":
                return "BOOLEAN";
            case "simplex":
                return "REAL_VECTOR";
            case "vector":
                return "REAL_VECTOR";
            case "matrix":
                return "REAL_MATRIX";
            case "tree":
            case "timetree":
                return "TREE";
            case "alignment":
                return "ALIGNMENT";
            default:
                // Use the distribution's default generates type
                return dist.getGeneratesType();
        }
    }
    
    /**
     * Add parameters for a LogNormal distribution.
     */
    private void addLognormalParameters(ObjectNode params, Distribution dist) {
        for (Argument arg : dist.getArguments()) {
            if (arg.getName() != null) {
                String paramName = arg.getName();
                
                // Map ModelPhy parameter names to CodePhy names
                if (paramName.equals("mean")) {
                    params.set("meanlog", convertArgumentToJsonValue(arg));
                } else if (paramName.equals("sigma")) {
                    params.set("sdlog", convertArgumentToJsonValue(arg));
                } else {
                    params.set(paramName, convertArgumentToJsonValue(arg));
                }
            }
        }
    }
    
    /**
     * Add parameters for a Normal distribution.
     */
    private void addNormalParameters(ObjectNode params, Distribution dist) {
        for (Argument arg : dist.getArguments()) {
            if (arg.getName() != null) {
                String paramName = arg.getName();
                
                // Map ModelPhy parameter names to CodePhy names
                if (paramName.equals("sigma")) {
                    params.set("sd", convertArgumentToJsonValue(arg));
                } else {
                    params.set(paramName, convertArgumentToJsonValue(arg));
                }
            }
        }
    }
    
    /**
     * Add parameters for an Exponential distribution.
     */
    private void addExponentialParameters(ObjectNode params, Distribution dist) {
        for (Argument arg : dist.getArguments()) {
            if (arg.getName() != null) {
                String paramName = arg.getName();
                
                // Map ModelPhy parameter names to CodePhy names
                if (paramName.equals("mean")) {
                    // Convert mean to rate (rate = 1/mean)
                    Object value = arg.getValue();
                    if (value instanceof Number) {
                        double mean = ((Number) value).doubleValue();
                        params.put("rate", 1.0 / mean);
                    } else {
                        // If not a simple number, add an expression
                        ObjectNode exprNode = params.putObject("rate");
                        exprNode.put("expression", "1.0 / " + value);
                    }
                } else {
                    params.set(paramName, convertArgumentToJsonValue(arg));
                }
            }
        }
    }
    
    /**
     * Add parameters for a Uniform distribution.
     */
    private void addUniformParameters(ObjectNode params, Distribution dist) {
        for (Argument arg : dist.getArguments()) {
            if (arg.getName() != null) {
                String paramName = arg.getName();
                
                // Map ModelPhy parameter names to CodePhy names
                if (paramName.equals("min")) {
                    params.set("lower", convertArgumentToJsonValue(arg));
                } else if (paramName.equals("max")) {
                    params.set("upper", convertArgumentToJsonValue(arg));
                } else {
                    params.set(paramName, convertArgumentToJsonValue(arg));
                }
            }
        }
    }
    
    /**
     * Add parameters for a Gamma distribution.
     */
    private void addGammaParameters(ObjectNode params, Distribution dist) {
        // CodePhy uses shape and rate
        for (Argument arg : dist.getArguments()) {
            if (arg.getName() != null) {
                params.set(arg.getName(), convertArgumentToJsonValue(arg));
            }
        }
    }
    
    /**
     * Add parameters for a Dirichlet distribution.
     */
    private void addDirichletParameters(ObjectNode params, Distribution dist) {
        for (Argument arg : dist.getArguments()) {
            if (arg.getName() != null && arg.getName().equals("alpha")) {
                JsonNode alphaNode = convertArgumentToJsonValue(arg);
                params.set("alpha", alphaNode);
            }
        }
    }
    
    /**
     * Add parameters for a Yule distribution.
     */
    private void addYuleParameters(ObjectNode params, Distribution dist) {
        for (Argument arg : dist.getArguments()) {
            if (arg.getName() != null) {
                String paramName = arg.getName();
                
                // Map ModelPhy parameter names to CodePhy names
                if (paramName.equals("birthrate")) {
                    params.set("birthRate", convertArgumentToJsonValue(arg));
                } else {
                    // Skip 'n' parameter as it's not needed in CodePhy
                    if (!paramName.equals("n")) {
                        params.set(paramName, convertArgumentToJsonValue(arg));
                    }
                }
            }
        }
    }
    
    /**
     * Add parameters for a BirthDeath distribution.
     */
    private void addBirthDeathParameters(ObjectNode params, Distribution dist) {
        for (Argument arg : dist.getArguments()) {
            if (arg.getName() != null) {
                String paramName = arg.getName();
                
                // Map ModelPhy parameter names to CodePhy names
                if (paramName.equals("birthrate")) {
                    params.set("birthRate", convertArgumentToJsonValue(arg));
                } else if (paramName.equals("deathrate")) {
                    params.set("deathRate", convertArgumentToJsonValue(arg));
                } else {
                    // Skip 'n' parameter as it's not needed in CodePhy
                    if (!paramName.equals("n")) {
                        params.set(paramName, convertArgumentToJsonValue(arg));
                    }
                }
            }
        }
    }
    
    /**
     * Add parameters for a PhyloCTMC distribution.
     */
    private void addPhyloCTMCParameters(ObjectNode params, Distribution dist) {
        for (Argument arg : dist.getArguments()) {
            if (arg.getName() != null) {
                String paramName = arg.getName();
                
                // Map ModelPhy parameter names to CodePhy names
                if (paramName.equals("tree")) {
                    params.set("tree", convertArgumentToJsonValue(arg));
                } else if (paramName.equals("substmodel")) {
                    params.set("Q", convertArgumentToJsonValue(arg));
                } else if (paramName.equals("siterates")) {
                    params.set("siteRates", convertArgumentToJsonValue(arg));
                } else if (paramName.equals("branchrates")) {
                    params.set("branchRates", convertArgumentToJsonValue(arg));
                } else {
                    params.set(paramName, convertArgumentToJsonValue(arg));
                }
            }
        }
    }
    
    /**
     * Convert an argument to a JSON value.
     */
    private JsonNode convertArgumentToJsonValue(Argument arg) {
        return convertToJsonValue(arg.getValue());
    }
    
    /**
     * Convert a value to a JSON value.
     */
    private JsonNode convertToJsonValue(Object value) {
        if (value == null) {
            return NullNode.getInstance();
        } else if (value instanceof Number) {
            Number num = (Number) value;
            if (num instanceof Integer || num instanceof Long || num instanceof Short || num instanceof Byte) {
                return IntNode.valueOf(num.intValue());
            } else {
                return DoubleNode.valueOf(num.doubleValue());
            }
        } else if (value instanceof Boolean) {
            return BooleanNode.valueOf((Boolean) value);
        } else if (value instanceof String) {
            return TextNode.valueOf((String) value);
        } else if (value instanceof VariableReference) {
            ObjectNode node = mapper.createObjectNode();
            node.put("variable", ((VariableReference) value).getName());
            return node;
        } else if (value instanceof ArrayValue) {
            ArrayValue array = (ArrayValue) value;
            ArrayNode node = mapper.createArrayNode();
            
            for (Object element : array.getElements()) {
                node.add(convertToJsonValue(element));
            }
            
            return node;
        } else if (value instanceof FunctionCall) {
            FunctionCall func = (FunctionCall) value;
            ObjectNode node = mapper.createObjectNode();
            node.put("function", func.getName());
            
            ObjectNode argsNode = node.putObject("arguments");
            for (Argument funcArg : func.getArguments()) {
                if (funcArg.getName() != null) {
                    argsNode.set(funcArg.getName(), convertToJsonValue(funcArg.getValue()));
                }
            }
            
            return node;
        } else if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            ObjectNode node = mapper.createObjectNode();
            
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getKey() instanceof String) {
                    node.set((String) entry.getKey(), convertToJsonValue(entry.getValue()));
                }
            }
            
            return node;
        }
        
        // Default to string representation
        return TextNode.valueOf(value.toString());
    }
}
