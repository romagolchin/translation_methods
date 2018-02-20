import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Roman Golchin (romagolchin@gmail.com)
 */
public class Lexer {
    public static class Rule {
        String regex;

        boolean skip;

        public Rule(String regex, boolean skip) {
            this.regex = regex;
            this.skip = skip;
        }

        public String getRegex() {
            return regex;
        }

        public boolean isSkip() {
            return skip;
        }
    }

    public static class Token {
        public String text;

        public int id;

        public Integer integer;

        public Coord coord = new Coord(0, 0);

        public Token(String text, int id) {
            this.id = id;
            this.text = id == -1 ? "END" : text;
            try {
                integer = Integer.valueOf(text);
            } catch (NumberFormatException e) {
                // ignore
            }
        }

    }

    static class Coord {
        int lineNumber;
        int pos;

        public Coord(int lineNumber, int pos) {
            this.lineNumber = lineNumber;
            this.pos = pos;
        }

        @Override
        public String toString() {
            return "(" + (lineNumber + 1) + ", " + (pos + 1) + ")";
        }
    }

    private int tabSize;

    private Coord rightCoord = new Coord(0, 0);

    private Coord leftCoord = new Coord(0, 0);

    private Token token;

    private List<Rule> rules;

    private Matcher matcher;


    private int start = 0;

    public Token getToken() {
        return token;
    }

    public Lexer(Path file, List<Rule> rules, int tabSize) throws IOException {
        this.tabSize = tabSize;
        this.rules = rules;
        String regex = rules.stream()
                .map(Rule::getRegex)
                .collect(Collectors.joining("|"));
        try (BufferedReader br = Files.newBufferedReader(file)) {
            matcher = Pattern.compile(regex).matcher(br.lines()
                    .collect(Collectors.joining("\n")));
        }
        next();
    }

    private void updateCoord(String s) {
        leftCoord.pos = rightCoord.pos;
        leftCoord.lineNumber = rightCoord.lineNumber;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\n') {
                rightCoord.lineNumber++;
                rightCoord.pos = 0;
            } else if (c == '\t') {
                rightCoord.pos += tabSize - (rightCoord.pos % tabSize);
            } else {
                rightCoord.pos++;
            }
        }
    }

    public void next() {
        if (start == matcher.regionEnd()) {
            token = new Token("", -1);
            token.coord = leftCoord;
            return;
        }
        matcher.region(start, matcher.regionEnd());
        if (matcher.find()) {
            for (int i = 0; i < rules.size(); i++) {
                // group indices start with one: zero denotes the entire pattern
                String group = matcher.group(i + 1);
                if (group != null) {
                    updateCoord(group);
                    start += group.length();
                    if (rules.get(i).skip) {
                        next();
                        return;
                    }
                    token = new Token(group, i);
                    token.coord = leftCoord;
                    return;
                }
            }
        }
        throw new RuntimeException("unrecognized token at " + rightCoord);
    }

}
