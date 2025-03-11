package org.modelphy;

import org.modelphy.model.ModelPhyModel;
import org.modelphy.parser.ModelPhyParserWrapper;
import org.modelphy.converter.CodePhyConverter;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Main entry point for the ModelPhy application.
 */
public class Main {
    
    /**
     * Main method.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            printUsage();
            System.exit(1);
        }
        
        // Extract flags and commands
        List<String> filteredArgs = new ArrayList<>();
        boolean debug = false;
        
        for (String arg : args) {
            if (arg.equals("--debug") || arg.equals("-d")) {
                debug = true;
            } else {
                filteredArgs.add(arg);
            }
        }
        
        if (filteredArgs.isEmpty()) {
            printUsage();
            System.exit(1);
        }
        
        // Get command (first non-flag argument)
        String command = filteredArgs.get(0);
        
        try {
            switch (command) {
                case "parse":
                    if (filteredArgs.size() < 2) {
                        System.err.println("Error: Missing input file");
                        printUsage();
                        System.exit(1);
                    }
                    parseFile(filteredArgs.get(1), debug);
                    break;
                    
                case "convert":
                    if (filteredArgs.size() < 3) {
                        System.err.println("Error: Missing input or output file");
                        printUsage();
                        System.exit(1);
                    }
                    convertToCodaPhy(filteredArgs.get(1), filteredArgs.get(2), debug);
                    break;
                    
                case "help":
                    printUsage();
                    System.exit(0);
                    break;
                    
                default:
                    System.err.println("Error: Unknown command: " + command);
                    printUsage();
                    System.exit(1);
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Parse a ModelPhy file and print the model.
     * 
     * @param filename The input file
     * @param debug Whether to print debug information
     * @throws Exception If an error occurs
     */
    private static void parseFile(String filename, boolean debug) throws Exception {
        Path path = Paths.get(filename);
        
        System.out.println("Parsing " + path.toAbsolutePath() + "...");
        
        ModelPhyParserWrapper parser = new ModelPhyParserWrapper(debug);
        ModelPhyModel model = parser.parse(path);
        
        System.out.println("\nParsed model:");
        System.out.println(model);
        
        System.out.println("\nModel is " + (model.isValid() ? "valid" : "invalid"));
    }
    
    /**
     * Convert a ModelPhy file to CodePhy JSON format.
     * 
     * @param inputFile The input ModelPhy file
     * @param outputFile The output CodePhy JSON file
     * @param debug Whether to print debug information
     * @throws Exception If an error occurs
     */
    private static void convertToCodaPhy(String inputFile, String outputFile, boolean debug) throws Exception {
        Path inputPath = Paths.get(inputFile);
        File outputFileObj = new File(outputFile);
        
        System.out.println("Converting " + inputPath.toAbsolutePath() + " to " + outputFileObj.getAbsolutePath() + "...");
        
        ModelPhyParserWrapper parser = new ModelPhyParserWrapper(debug);
        ModelPhyModel model = parser.parse(inputPath);
        
        CodePhyConverter converter = new CodePhyConverter(model);
        String json = converter.convert();
        
        try (FileWriter writer = new FileWriter(outputFileObj)) {
            writer.write(json);
        }
        
        System.out.println("Conversion completed successfully.");
    }
    
    /**
     * Print usage information.
     */
    private static void printUsage() {
        System.out.println("ModelPhy - Phylogenetic Model Interchange Language");
        System.out.println();
        System.out.println("Usage:");
        System.out.println("  java -jar modelphy.jar [--debug|-d] parse <input-file>");
        System.out.println("  java -jar modelphy.jar [--debug|-d] convert <input-file> <output-file>");
        System.out.println("  java -jar modelphy.jar help");
        System.out.println();
        System.out.println("Commands:");
        System.out.println("  parse     Parse a ModelPhy file and print the model");
        System.out.println("  convert   Convert a ModelPhy file to CodePhy JSON format");
        System.out.println("  help      Print this help message");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  --debug, -d    Print debug information during parsing");
    }
}