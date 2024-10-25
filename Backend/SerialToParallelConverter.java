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
        Pattern forLoopPattern = Pattern.compile("(\\s*for\\s*\\([^)]*\\))\\s*\\{\\s*([^}]*)\\s*\\}");
        Matcher matcher = forLoopPattern.matcher(cCode);

        int lastEnd = 0;

        // Loop through the code and find 'for' loops
        while (matcher.find()) {
            
            // Append the code up to the 'for' loop
            parallelCode.append(cCode.substring(lastEnd, matcher.start()));
            String leadingSpaces = matcher.group(1).substring(0, matcher.group(1).indexOf("for"));
            parallelCode.append(leadingSpaces).append("#pragma omp parallel for\n");
            parallelCode.append(matcher.group().replaceAll("(^\\n+)|(\\n+$)", ""));

            lastEnd = matcher.end();


        }

        // Append the rest of the code after the last match
        parallelCode.append(cCode.substring(lastEnd));

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
            // Read the input C code from the file
            BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
            StringBuilder cCode = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                cCode.append(line).append("\n");
            }
            reader.close();

            // Convert the serial code to parallel
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
