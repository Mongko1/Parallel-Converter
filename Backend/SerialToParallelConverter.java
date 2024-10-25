import java.io.*;
import java.util.regex.*;

public class SerialToParallelConverter {

    public static String checkForLoop(String cCode , int numThread) {
        StringBuilder parallelCode = new StringBuilder();

        if (!cCode.contains("#include <omp.h>")) {
            parallelCode.append("#include <omp.h>\n");
        }

        Pattern forLoopPattern = Pattern.compile("(\\s*for\\s*\\([^)]*\\))\\s*\\{\\s*([^}]*)\\s*\\}");
        Matcher matcher = forLoopPattern.matcher(cCode);
        int lastEnd = 0;
        String num_Threads = "";
        Boolean isReduction = false;
        if (numThread > 0){
            num_Threads = String.format("num_threads(%d)", numThread);
        } 
        while (matcher.find()) {
            Pattern forLoop = Pattern.compile("(\\s*for\\s*\\([^)]*\\))\\s*\\{");
            Matcher matcherr = forLoop.matcher(matcher.group());
            int count = 0;
            while (matcherr.find()){
                count++;
            }

            Pattern reductionPattern = Pattern.compile("\\+=|-=|\\*=|/=");
            Matcher matcherrr = reductionPattern.matcher(matcher.group());
            while (matcherrr.find()){
                isReduction = true;
            }
            
            parallelCode.append(cCode.substring(lastEnd, matcher.start()));
            String leadingSpaces = matcher.group(1).substring(0, matcher.group(1).indexOf("for"));
            if(count > 1){
                //if(isReduction){
                    parallelCode.append(leadingSpaces).append(String.format("#pragma omp parallel for collapse(%d) %s\n",count , num_Threads));
                //}
            } else {
                parallelCode.append(leadingSpaces).append(String.format("#pragma omp parallel for %s\n" , num_Threads));
            }
            
            parallelCode.append(matcher.group().replaceAll("(^\\n+)|(\\n+$)", ""));
            lastEnd = matcher.end();
        }
        parallelCode.append(cCode.substring(lastEnd));

        return parallelCode.toString();
    }

    /*
    public static String checkTest(String cCode , int numThread) {
        StringBuilder parallelCode = new StringBuilder();

        if (!cCode.contains("#include <omp.h>")) {
            parallelCode.append("#include <omp.h>\n");
        }

        Pattern forLoopPattern = Pattern.compile("(\\s*for\\s*\\([^)]*\\))\\s*\\{\\s*([^}]*)\\s*\\}");
        Matcher matcher = forLoopPattern.matcher(cCode);
        int lastEnd = 0;
        int forCount = 0;
        while (matcher.find()) {
            forCount++;
        }
        System.out.println(forCount);

        if(forCount > 1){
            matcher = forLoopPattern.matcher(cCode);
            boolean inSection = false;
            while (matcher.find()) {
                if(!inSection){
                    parallelCode.append(cCode.substring(lastEnd, matcher.start()));
                    String leadingSpaces = matcher.group(1).substring(0, matcher.group(1).indexOf("for"));
                    parallelCode.append(leadingSpaces).append("#pragma omp parallel sections").append(leadingSpaces).append("{\n").append(leadingSpaces.replaceAll("(^\\n+)|(\\n+$)","")).append(leadingSpaces.replaceAll("(^\\n+)|(\\n+$)","")).append("#pragma omp section\n");
                    parallelCode.append(matcher.group().replaceAll("(^\\n+)|(\\n+$)", ""));
                    lastEnd = matcher.end();
                    inSection = true;
                    System.out.println("test");
                }
                else {
                    Pattern forLoop = Pattern.compile("(\\s*for\\s*\\([^)]*\\))\\s*\\{");
                    Matcher matcherr = forLoop.matcher(matcher.group());
                    int count = 0;
                    while (matcherr.find()){
                        count++;
                    }
                    
                    parallelCode.append(cCode.substring(lastEnd, matcher.start()));
                    String leadingSpaces = matcher.group(1).substring(0, matcher.group(1).indexOf("for"));
                    if(count > 1){
                        parallelCode.append(leadingSpaces).append(String.format("#pragma omp parallel for collapse(%d)\n",count ));
                    } else {
                        parallelCode.append(leadingSpaces).append("#pragma omp parallel for\n");
                    }
                    
                    parallelCode.append(matcher.group().replaceAll("(^\\n+)|(\\n+$)", ""));
                    lastEnd = matcher.end();
                }
            }
            parallelCode.append(cCode.substring(lastEnd));
        }

        return parallelCode.toString();
    }*/

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

            String parallelCode = "";
            if(args.length > 2){
                parallelCode = checkForLoop(cCode.toString() , Integer.valueOf(args[2]));
            } else {
                parallelCode = checkForLoop(cCode.toString() , 0);
            }
            //parallelCode = checkForLoop(parallelCode);

            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath));
            writer.write(parallelCode);
            writer.close();

            System.out.println("Parallelized code has been written to " + outputFilePath);
        } catch (IOException e) {
            System.out.println("Error processing files: " + e.getMessage());
        }
    }
}
