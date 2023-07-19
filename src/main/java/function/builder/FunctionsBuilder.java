package function.builder;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class used to extract the functions from the MEOS library.
 * Run with ./script folder or as follows:
 * <ul>
 *     <li>cd src\main\java\function\builder</li>
 *     <li>javac .\FunctionsBuilder.java</li>
 *     <li>java .\FunctionsBuilder.java</li>
 * </ul>
 *
 * @author Killian Monnier
 * @since 27/06/2023
 */
public class FunctionsBuilder {
	private static final String FILE_PATH = "";
	private static final String C_FUNCTIONS_PATH = FILE_PATH + "tmp/functions.h"; // File generated by the FunctionsExtractor class
	private static final String C_TYPES_PATH = FILE_PATH + "tmp/types.h"; // File generated by the FunctionsExtractor class
	private static final HashMap<String, String> TYPES = typesBuild(); // Creation of the C type dictionary and its equivalent in Java
	private static final String FUNCTIONS_CLASS_PATH = FILE_PATH + "../functions.java"; // Generated functions class
	private static final ArrayList<String> unSupportedTypes = new ArrayList<>(); // List of unsupported types
	
	/**
	 * Construit le tableau de modification des types.
	 * <p>
	 * Clé: ancien type
	 * <p>
	 * Valeur: nouveau type
	 *
	 * @return dictionnaire des types
	 */
	private static HashMap<String, String> typesBuild() {
		HashMap<String, String> typeChange = new HashMap<>();
		typeChange.put("\\*", "Pointer");
		typeChange.put("\\*\\[\\]", "Pointer");
		typeChange.put("byte\\[\\]", "byte[]");
		typeChange.put("\\*char", "String");
		typeChange.put("void", "void");
		typeChange.put("bool", "boolean");
		typeChange.put("float", "float");
		typeChange.put("double", "double");
		typeChange.put("int", "int");
		typeChange.put("int32_t", "int");
		typeChange.put("int32", "int");
		typeChange.put("int64", "long");
		typeChange.put("uint8_t", "short");
		typeChange.put("uint16_t", "short");
		typeChange.put("uint32", "int");
		typeChange.put("uint64", "long");
		typeChange.put("uintptr_t", "long");
		typeChange.put("size_t", "long");
		typeChange.put("interpType", "int"); // enum in C
		
		readFileLines(C_TYPES_PATH, line -> { // Added typedefs extracted from C file
			Pattern pattern = Pattern.compile("^typedef\\s(\\w+)\\s(\\w+);");
			Matcher matcher = pattern.matcher(line);
			if (matcher.find()) {
				String rawType = matcher.group(1);
				String typeDef = matcher.group(2);
				
				if (typeChange.containsKey(rawType))
					rawType = typeChange.get(rawType); // Changing Types with Java Compatible Types
				
				typeChange.put(typeDef, rawType);
			} else {
				System.out.println("Cannot extract type for row: " + line);
			}
		});
		
		return typeChange;
	}
	
	/**
	 * Main script execution function.
	 *
	 * @param args arguments
	 */
	public static void main(String[] args) {
		StringBuilder functionsBuilder = generateFunctions();
		StringBuilder interfaceBuilder = generateInterface(functionsBuilder);
		StringBuilder classBuilder = generateClass(functionsBuilder, interfaceBuilder);
		writeClassFile(classBuilder);
	}
	
	/**
	 * Used to generate the class of functions.
	 *
	 * @param functionsBuilder builder of functions
	 * @param interfaceBuilder interface builder
	 * @return the class builder
	 */
	private static StringBuilder generateClass(StringBuilder functionsBuilder, StringBuilder interfaceBuilder) {
		StringBuilder builder = new StringBuilder();
		builder.append("""
				package function;
				
				import jnr.ffi.LibraryLoader;
				import jnr.ffi.Pointer;
						
				public class functions {
				""");
		appendBuilderWith(interfaceBuilder, builder, "\t", "\n\n"); // Added interface
		
		StringBuilder functionBodyBuilder = new StringBuilder(); // Addition of functions
		readBuilderLines(functionsBuilder, line -> {
			if (!line.isBlank()) {
				String functionSignature = "public static " + removeSemicolon(line) + " {\n";
				functionBodyBuilder.append(functionSignature);
				String functionBody = "\t" + "return MeosLibrary.meos." + extractFunctionName(line) + "(" + getListWithoutBrackets(extractParamNames(line)) + ");\n}\n\n";
				
				if (getFunctionTypes(line).get(0).equals("void"))
					functionBody = "\t" + "MeosLibrary.meos." + extractFunctionName(line) + "(" + getListWithoutBrackets(extractParamNames(line)) + ");\n}\n\n"; // When the function returns nothing
				
				functionBodyBuilder.append(functionBody);
			}
		});
		appendBuilderWith(functionBodyBuilder, builder, "\t", "\n");
		builder.append("}");
		return builder;
	}
	
	/**
	 * Generation of the interface.
	 *
	 * @param functionsBuilder builder of functions
	 * @return the interface builder
	 */
	private static StringBuilder generateInterface(StringBuilder functionsBuilder) {
		StringBuilder builder = new StringBuilder();
		
		builder.append("""
				public interface MeosLibrary {
					functions.MeosLibrary INSTANCE = LibraryLoader.create(functions.MeosLibrary.class).load("meos");
					functions.MeosLibrary meos = functions.MeosLibrary.INSTANCE;
				""");
		appendBuilderWith(functionsBuilder, builder, "\t", "\n");
		builder.append("}");
		return builder;
	}
	
	/**
	 * Generation of functions with their modified types.
	 *
	 * @return the function builder
	 */
	private static StringBuilder generateFunctions() {
		StringBuilder builder = new StringBuilder();
		
		readFileLines(FunctionsBuilder.C_FUNCTIONS_PATH, line -> {
			String processedLine = changeFunctionType(line) + "\n";
			builder.append(processedLine);
		});
		System.out.println("Unsupported types: " + unSupportedTypes);
		return builder;
	}
	
	/**
	 * Adding one builder to another.
	 *
	 * @param sourceBuilder the builder to add
	 * @param targetBuilder the builder who receives
	 * @param startOfLine   each line starts with this string
	 * @param endOfLine     each line ends with this string
	 */
	public static void appendBuilderWith(StringBuilder sourceBuilder, StringBuilder targetBuilder, String startOfLine, String endOfLine) {
		String[] lines = sourceBuilder.toString().split("\n");
		for (String line : lines) {
			targetBuilder.append(startOfLine).append(line).append(endOfLine);
		}
	}
	
	/**
	 * Allows to read lines iteratively from a builder.
	 *
	 * @param builder the builder in question
	 * @param process the lambda expression to run
	 */
	private static void readBuilderLines(StringBuilder builder, Consumer<String> process) {
		String[] lines = builder.toString().split("\n");
		for (String line : lines) {
			process.accept(line);
		}
	}
	
	/**
	 * Delete it ; at the end of a line.
	 *
	 * @param input the line string
	 * @return the line without the ;
	 */
	public static String removeSemicolon(String input) {
		if (input.endsWith(";")) {
			return input.substring(0, input.length() - 1);
		}
		return input;
	}
	
	/**
	 * Allows you to retrieve the values of a list without the [ ].
	 *
	 * @param list the list containing character strings
	 * @return a string of list values
	 */
	public static String getListWithoutBrackets(ArrayList<String> list) {
		String stringRepresentation = list.toString(); // Convert ArrayList to String
		return stringRepresentation.replace("[", "").replace("]", ""); // Remove '[' and ']'
	}
	
	/**
	 * Allows you to extract the name of a function.
	 *
	 * @param signature function signature
	 * @return the name of the function
	 */
	public static String extractFunctionName(String signature) {
		// Set regex pattern to extract function name
		String regex = "\\b([A-Za-z_][A-Za-z0-9_]*)\\s*\\(";
		
		// Create the pattern and the matcher for the signature
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(signature);
		
		// Check if the function name was found
		if (matcher.find()) {
			return matcher.group(1); // Return the first captured group
		}
		
		// If no function name is found, return an empty string or handle the error as needed
		return "";
	}
	
	/**
	 * Allows to extract the names of the parameters of a function.
	 *
	 * @param signature function signature
	 * @return the list of parameter names
	 */
	public static ArrayList<String> extractParamNames(String signature) {
		ArrayList<String> paramNames = new ArrayList<>();
		
		// Using a regular expression to extract parameter names
		Pattern pattern = Pattern.compile("\\b\\w+\\b(?=\\s*,|\\s*\\))");
		Matcher matcher = pattern.matcher(signature);
		
		while (matcher.find()) {
			String paramName = matcher.group();
			paramNames.add(paramName);
		}
		
		return paramNames;
	}
	
	/**
	 * Processes the rows to generate the functions.
	 *
	 * @param line the line corresponding to a function
	 * @return the processed line
	 */
	private static String changeFunctionType(String line) {
		if (!line.isBlank()) {
			// Remove keywords that are not of interest to us
			line = line.replaceAll("extern ", "");
			line = line.replaceAll("const ", "");
			line = line.replaceAll("static inline ", "");
			
			// Changing types with *
			line = line.replaceAll("char\\s\\*", "*char ");
			line = line.replaceAll("\\w+\\s\\*\\*", "*[] ");
			line = line.replaceAll("\\w+\\s\\*(?!\\*)", "* ");
			
			// Changing special types or names
			line = line.replaceAll("\\*char\\stz_str", "byte[] tz_str"); // For the function meos_initialize(const char *tz_str);
			line = line.replaceAll("\\(void\\)", "()"); // For the function meos_finish(void);
			line = line.replaceAll("synchronized", "synchronize"); // For the function temporal_simplify(const Temporal *temp, double eps_dist, bool synchronized);
			
			// Replaces types from type dictionary
			for (Map.Entry<String, String> entry : TYPES.entrySet()) {
				String oldType = entry.getKey();
				String newType = entry.getValue();
				line = line.replaceAll("((^|\\(|\\s)+)" + oldType + "\\s", "$1" + newType + " ");
			}
			
			List<String> typesNotSupported = getFunctionTypes(line).stream().filter(type -> !TYPES.containsValue(type)).toList(); // Retrieving unsupported types for the line
			unSupportedTypes.addAll(typesNotSupported.stream().filter(type -> !unSupportedTypes.contains(type)).toList()); // Fetch unsupported types that are not yet in the global list
		}
		
		return line;
	}
	
	/**
	 * Gives the types of the function's parameters and return from a line corresponding to the format of a function.
	 *
	 * @param line text line of a function
	 * @return type list
	 */
	private static ArrayList<String> getFunctionTypes(String line) {
		Pattern pattern = Pattern.compile("(\\w+(?:\\[])?)\\s+\\w+\\s*\\(([^)]*)\\)");
		Matcher matcher = pattern.matcher(line);
		
		ArrayList<String> typesList = new ArrayList<>();
		while (matcher.find()) {
			String returnType = matcher.group(1);
			String paramTypes = matcher.group(2);
			
			ArrayList<String> paramTypeArray = new ArrayList<>(List.of(paramTypes.split("\\s\\w+,\\s|\\s\\w+")));
			
			if (!returnType.isBlank()) typesList.add(returnType); // added function return type
			if (!paramTypeArray.isEmpty())
				if (!paramTypeArray.get(0).isEmpty())
					typesList.addAll(paramTypeArray);
		}
		
		return typesList;
	}
	
	/**
	 * Allows you to read the lines of a file and make changes to them.
	 *
	 * @param filepath file path
	 * @param process  lambda expression
	 */
	private static void readFileLines(String filepath, Consumer<String> process) {
		try (BufferedReader reader = new BufferedReader(new FileReader(filepath))) {
			String line;
			while ((line = reader.readLine()) != null) {
				process.accept(line);
			}
		} catch (IOException e) {
			System.out.println("Error reading file: " + e.getMessage());
		}
	}
	
	/**
	 * Allows writing a file with a StringBuilder.
	 *
	 * @param builder the builder
	 */
	private static void writeClassFile(StringBuilder builder) {
		try (PrintWriter writer = new PrintWriter(new FileWriter(FunctionsBuilder.FUNCTIONS_CLASS_PATH))) {
			writer.write(builder.toString());
			System.out.println("The file " + FunctionsBuilder.FUNCTIONS_CLASS_PATH + " was created successfully!");
		} catch (IOException e) {
			System.out.println("Error creating file " + FunctionsBuilder.FUNCTIONS_CLASS_PATH + ": " + e.getMessage());
		}
	}
}