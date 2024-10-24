import java.io.*;
import java.util.regex.*;

public class SerialToParallelConverter {
    public static String convertToParallel(String cCode) {
        StringBuilder parallelCode = new StringBuilder();

        if (!cCode.contains("#include <omp.h>")) {
            parallelCode.append("#include <omp.h>\n");
        }

        Pattern forLoopPattern = Pattern.compile("(\\s*for\\s*\\(.*\\))\\s*\\{");
        Matcher matcher = forLoopPattern.matcher(cCode);

        int lastEnd = 0;
        while (matcher.find()) {
            parallelCode.append(cCode.substring(lastEnd, matcher.start()));
            String leadingSpaces = matcher.group(1).substring(0, matcher.group(1).indexOf("for"));
            parallelCode.append(leadingSpaces).append("#pragma omp parallel for");
            parallelCode.append(matcher.group());
            lastEnd = matcher.end();
        }

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
            BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
            StringBuilder cCode = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                cCode.append(line).append("\n");
            }
            reader.close();

            String parallelCode = convertToParallel(cCode.toString());

            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath));
            writer.write(parallelCode);
            writer.close();

            System.out.println("Parallelized code has been written to " + outputFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
