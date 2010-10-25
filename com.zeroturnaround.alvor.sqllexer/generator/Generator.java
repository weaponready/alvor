import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Transforms JFlex output to a java file which prints what we need as a lexer representation.
 *   
 * @author abreslav
 *
 */
public class Generator {

	public static void main(String[] args) throws IOException {
		String inFolder = "generated/";
		String outFolder = "generated/";
		String inClassName = "SQLLexer";
		String outClassName = "SQLLexerGen";
		String keywordsFileName = "keywords.txt";
		
		if (args.length > 0) {
			inFolder = args[0];
			inClassName = args[1];
			outFolder = args[2];
			outClassName = args[3];
			keywordsFileName = args[4];
		}
		File inFile = new File(new File(inFolder), inClassName + ".java");
		File outFile = new File(new File(outFolder),  outClassName + ".java");

		System.out.println("From: " + inFile);
		System.out.println("To: " + outFile);
		
		// A file generated by JFlex
		StringBuilder jflexFile = readFile(inFile);

		// Looking for actions, e.g.
		// case 15 : {/*ID*/}
		Pattern pattern;
		pattern = Pattern.compile("case ([0-9]+):\\s*\\{\\s*/\\*(.*?)\\*/",
				Pattern.MULTILINE				
		);
		Matcher matcher = pattern.matcher(jflexFile);

		StringBuilder tokens = new StringBuilder();
		tokens.append("        System.out.println(\"/** Tokens (action - name)*/\");\n");
		tokens.append("        System.out.println(\"public static final String[] TOKENS = new String[ACTIONS.length];\");\n");
		tokens.append("        System.out.println(\"static {\");\n");
		int maxToken = 0;  
		while (matcher.find()) {
			String index = matcher.group(1);
			String text = matcher.group(2).replace("\n", "\\n");
			tokens.append("        System.out.format(\"    TOKENS[%4d] = \\\"%s\\\";\\n\", " + index + ", \"" + text + "\");\n");
			maxToken = Math.max(maxToken, Integer.parseInt(index));
		}
		tokens.append("        System.out.println(\"}\");\n");
		tokens.append("        System.out.println(\"}\");\n");
		
		// The beginning of the main function
		InputStream main = Generator.class.getResourceAsStream("main.txt");
		StringBuilder mainTemplate = readFile(main);

		// Rename a class
		String source = jflexFile.toString()
				.replace(inClassName, outClassName);
		
		FileWriter fileWriter = new FileWriter(outFile);
		// Copy everything from a JFlex file, except for the closing '}'
		fileWriter.write(source, 0, source.lastIndexOf('}'));
		// Write the beginning of the main function
		fileWriter.write(mainTemplate.toString());
		
		// Write keywords
		fileWriter.write("System.out.println(\"    public static final String[] KEYWORDS = {\");\n");
		BufferedReader keywordsReader = new BufferedReader(new InputStreamReader(new FileInputStream(keywordsFileName)));
		do {
			String readLine = keywordsReader.readLine();
			if (readLine == null) {
				break;
			}
			fileWriter.write("System.out.println(\"        \\\"" + readLine + "\\\",\");\n");
		} while(true);
		fileWriter.write("System.out.println(\"    };\");\n");
		
		// Write the statements which denote tokens
		fileWriter.write(tokens.toString());
		
		
		// Close the main function and the class
		fileWriter.write("\n    }\n}");
		fileWriter.close();
		
		System.out.println("Generation finished");
	}

	private static StringBuilder readFile(File file)
			throws FileNotFoundException, IOException {
		FileReader reader = new FileReader(file);
		return readFile(reader);
	}

	private static StringBuilder readFile(InputStream in)
	throws FileNotFoundException, IOException {
		Reader reader = new InputStreamReader(in);
		return readFile(reader);
	}
	
	private static StringBuilder readFile(Reader reader) throws IOException {
		int c;
		StringBuilder builder = new StringBuilder();
		while ((c = reader.read()) != -1) {
			builder.append((char) c);
		}
		reader.close();
		return builder;
	}

}
