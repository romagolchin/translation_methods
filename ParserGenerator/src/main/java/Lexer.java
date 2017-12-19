import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Roman Golchin (romagolchin@gmail.com)
 */
public class Lexer {
    class Rule {
        String regex;

        boolean rule;
    }

    String delim = "([ \\t\\n]+)";

    Matcher matcher;


    public Lexer(Path file) throws IOException {
//        regexes.put(0, "\\Q[0-9]*\\E");
//        regexes.put(3, "\\Q+\\E");
//        regexes.put(4, "\\Q*\\E");
//        regexes.put(5, "\\Q(\\E");
//        regexes.put(6, "\\Q)\\E");
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = Files.newBufferedReader(file)) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        matcher = Pattern.compile("([ \\t\\n]+)|([0-9]+)|(\\+)|(\\*)|(\\()|(\\))").matcher(sb);
    }


    public int next() {
        if (matcher.find()) {
            for (int i = 0; i < 6; i++) {
                String group = matcher.group(i + 1);
                if (group != null) {
                    System.out.println("g " + group + " id " + i);
                    return i;
                }
            }
        }
        return -1;
    }

}
