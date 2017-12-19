import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * @author Roman Golchin (romagolchin@gmail.com)
 */
public class Generator {

    Path grammarPath;

    Grammar grammar;

    String lexerName;

    String parserName;

    @NotNull BufferedWriter bw;

    private int indent = 0;

    static final String JAVA = ".java";

    static final String LEXER = "Lexer";

    static final String PARSER = "Parser";

    static final String INDENT = "    ";

    static String capitalize(@NotNull String s) {
        if (s.isEmpty()) return s;
        return String.valueOf(Character.toUpperCase(s.charAt(0))) + (s.length() > 1 ? s.substring(1) : "");
    }

    void print(String s) {
        try {
            for (int i = 0; i < indent; i++) {
                bw.write(INDENT);
            }
            bw.write(s);
        } catch (IOException e) {
            // ignore
        }
    }

    void println(String s) {
        print(s + "\n");
    }

    void printBody(Runnable r, int... addIndent) {
        int add = addIndent.length > 0 ? addIndent[0] : 1;
        try {
            bw.write(" {\n");
        } catch (IOException ignored) {
        }
        indent += add;
        r.run();
        indent -= add;
        println("}");
    }

    void printBody(String header, Runnable r) {
        print(header);
        printBody(r);
    }

    public Generator(Path grammarPath) throws IOException {
        this.grammarPath = grammarPath;

        String grammarName = grammarPath.getFileName().toString();

        lexerName = capitalize(grammarName + LEXER);

        parserName = capitalize(grammarName + PARSER);

        GrammarLexer lexer = new GrammarLexer(CharStreams.fromPath(grammarPath));

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        GrammarParser parser = new GrammarParser(tokens);

        GrammarParser.FileContext grammarContext = parser.file();

        ParseTreeWalker walker = new ParseTreeWalker();
        MyListener listener = new MyListener();
        walker.walk(listener, grammarContext);

        grammar = listener.getGrammar();

    }

    public void generate() throws IOException {
        Path grammarName = grammarPath.getFileName();
        Path genPath = Paths.get("src", "gen", grammarName.toString());
        Files.createDirectories(genPath);
        bw = Files.newBufferedWriter(genPath.resolve(lexerName + JAVA));
        genLexer();
        bw.close();
        bw = Files.newBufferedWriter(genPath.resolve(parserName + JAVA));
        genParser();
        bw.close();
    }

    private String escape(String source) {
        return "\\\\Q" + source + "\\\\E";
//        return Pattern.quote(source);
//        return source.replaceAll("\\\\t", "\\\\\\\\t").replaceAll("\\\\n", "\\\\\\\\n")
//                .replaceAll("\\\\\\\\", "\\\\\\\\\\\\\\\\")
//                ;
    }

    private void genLexer() throws IOException {
        StringJoiner delim = new StringJoiner("|");
        Map<Integer, String> regexes = new TreeMap<>();
        List<LexerRule> lexerRules = grammar.lexerRules;
        for (int i = 0; i < lexerRules.size(); i++) {
            LexerRule r = lexerRules.get(i);
            if (r.skip)
                delim.add("(" + r.regex + ")");
            else regexes.put(i, r.regex);

        }
        println("import java.util.*;");
        println("import java.nio.file.*;");
        println("import java.io.*;");

        print("public class " + lexerName);
        printBody(() -> {
            println("Scanner scanner;");
            println("String delim = \"" + escape(delim.toString()) + "\";");
            println("Map<Integer, String> regexes = new TreeMap<>();");

            print("public " + lexerName + "(Path file) throws IOException");
            printBody(() -> {
                println("scanner = new Scanner(file);");
                println("scanner.useDelimiter(delim);");
                regexes.forEach((k, v) -> {
                    println("regexes.put(" + k + ", \"" + escape(v) + "\");");
                });
            });
            println("");
            print("public int next()");
            printBody(() -> {
                print("for (Integer i : regexes.keySet())");
                printBody(() -> {
                    printBody("if (scanner.hasNext(regexes.get(i)))",
                            () -> {
                                println("scanner.next();");
                                println("return i;");
                            });

                });
                println("return -1;");
            });
        });
    }

    private void genParser() throws IOException {

    }

}
