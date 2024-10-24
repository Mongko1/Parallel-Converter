import java.io.*;
import java.util.regex.*;

public class SerialToParallelConverter {

    public static String convertToParallel(String cCode) {
        StringBuilder parallelCode = new StringBuilder();

        // Check if OpenMP include is present, if not, add it
        if (!cCode.contains("#include <omp.h>")) {
            parallelCode.append("#include <omp.h>\n");
        }

        // Regex to find 'for' loops
        Pattern forLoopPattern = Pattern.compile("(\\s*for\\s*\\([^)]*\\))\\s*\\{?");
        Matcher matcher = forLoopPattern.matcher(cCode);

        int lastEnd = 0;
        int depth = 0; // Track the current depth in braces
        boolean insideLoop = false; // Track if we are inside a loop

        // Loop through the code and find 'for' loops
        while (matcher.find()) {
            // Append the code between the last match and the current one
            parallelCode.append(cCode.substring(lastEnd, matcher.start()));

            // Get the leading spaces for formatting
            String leadingSpaces = matcher.group(1).substring(0, matcher.group(1).indexOf("for"));

            // Only add the parallel pragma if we are not inside another loop
            if (!insideLoop) {
                parallelCode.append(leadingSpaces).append("#pragma omp parallel for");
                insideLoop = true;  // Now we are inside a loop
            }

            // Append the 'for' loop
            parallelCode.append(matcher.group());

            // Update the last end position
            lastEnd = matcher.end();

            // Keep track of nested block depths by scanning for '{' and '}' characters
            int i = matcher.end();
            while (i < cCode.length() && (cCode.charAt(i) != '{' && cCode.charAt(i) != '}')) {
                i++;
            }
            if (i < cCode.length() && cCode.charAt(i) == '{') {
                depth++;  // Entered a block
            }
        }

        // Append the rest of the code
        parallelCode.append(cCode.substring(lastEnd));

        // Post-process to handle exiting from nested loops
        // Here we track closing braces and reset 'insideLoop' when depth returns to 0
        for (int i = 0; i < parallelCode.length(); i++) {
            if (parallelCode.charAt(i) == '{') {
                depth++;
            } else if (parallelCode.charAt(i) == '}') {
                depth--;
                if (depth == 0) {
                    insideLoop = false;  // We are back at the outermost level
                }
            }
        }

        return parallelCode.toString();
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java SerialToParallelConverter <input_file.c> <output_file.c>");
            return;
        }

        String inputFilePath = args[0];
        String outputFilePath = args[1];

        try {
            BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
            StringBuilder cCode = new StringBuilder();
            String line;

            // Read the input C code
            while ((line = reader.readLine()) != null) {
                cCode.append(line).append("\n");
            }
            reader.close();

            // Convert the code to its parallelized version
            String parallelCode = convertToParallel(cCode.toString());

            // Write the parallelized code to the output file
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath));
            writer.write(parallelCode);
            writer.close();

            System.out.println("Parallelized code has been written to " + outputFilePath);
        } catch (IOException e) {
            System.out.println("Error processing files: " + e.getMessage());
        }
    }
}
