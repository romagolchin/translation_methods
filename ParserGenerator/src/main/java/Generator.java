import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Roman Golchin (romagolchin@gmail.com)
 */
public class Generator {

    private Path grammarPath;

    private Grammar grammar;

    private String lexerName;

    private String parserName;

    private @NotNull BufferedWriter bw;

    private String grammarName;

    private final FirstFollowBuilder builder;

    private int indent = 0;

    private static final String JAVA = ".java";

    private static final String LEXER = "Lexer";

    private static final String PARSER = "Parser";

    private static final String INDENT = "    ";

    private int tabSize;


    public Generator(Path grammarPath, int tabSize) throws IOException {
        this.tabSize = tabSize;
        this.grammarPath = grammarPath;

        grammarName = grammarPath.getFileName().toString();

//        lexerName = capitalize(grammarName + LEXER);
        lexerName = "Lexer";

        parserName = capitalize(grammarName + PARSER);

        GrammarLexer lexer = new GrammarLexer(CharStreams.fromPath(grammarPath));

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        GrammarParser parser = new GrammarParser(tokens);

        GrammarParser.FileContext grammarContext = parser.file();

        ParseTreeWalker walker = new ParseTreeWalker();
        GrammarListenerImpl listener = new GrammarListenerImpl();
        walker.walk(listener, grammarContext);

        grammar = listener.getGrammar();
        String startNonTerminal = grammar.parserRules.isEmpty() ? null : grammar.parserRules.get(0).getLhs();
        builder = new FirstFollowBuilder(grammar, startNonTerminal);

        for (int i = 0; i < grammar.lexerRules.size(); i++) {
            System.out.println(i + " " + grammar.lexerRules.get(i).regex);
        }
    }

    private static String capitalize(@NotNull String s) {
        if (s.isEmpty()) return s;
        return String.valueOf(Character.toUpperCase(s.charAt(0))) + (s.length() > 1 ? s.substring(1) : "");
    }

    private void print(String s, boolean noIndent) {
        try {
            if (!noIndent)
                for (int i = 0; i < indent; i++) {
                    bw.write(INDENT);
                }
            bw.write(s);
        } catch (IOException e) {
            // ignore
        }
    }

    private void print(String s) {
        print(s, false);
    }

    private void println() {
        println("");
    }

    private void println(String s) {
        print(s + "\n");
    }

    private void printBody(Runnable r) {
        try {
            bw.write(" {\n");
        } catch (IOException ignored) {
        }
        indent++;
        r.run();
        println();
        indent--;
        println("}\n");
    }

    private void printBody(String header, Runnable r) {
        print(header);
        printBody(r);
    }

    private void printHeader() {
        println("package " + grammarName + ";");
        println("import java.util.*;");
        println("import java.nio.file.*;");
        println("import java.io.*;");
    }

    void generate() throws IOException {
        String grammarName = grammarPath.getFileName().toString();
        Path genPath = Paths.get("src", "gen");
        Path lexerPath = genPath.resolve("common_lexer");
        Files.createDirectories(lexerPath);
        bw = Files.newBufferedWriter(genPath.resolve(
                Paths.get("common_lexer", lexerName + JAVA)
        ));
        genLexer();
        bw.close();
        Path parserPath = genPath.resolve(grammarName);
        Files.createDirectories(parserPath);
        bw = Files.newBufferedWriter(genPath.resolve(
                Paths.get(grammarName, parserName + JAVA)
        ));
        genParser();
        bw.close();
    }

    private String escape(String source) {
        return source.replaceAll("\\\\", "\\\\\\\\");
    }

    private void genLexer() throws IOException {
        println("package common_lexer;");
        try (BufferedReader br = Files.newBufferedReader(
                Paths.get("src", "main", "java", "Lexer.java")
        )) {
            br.lines().forEach(l -> print(l + "\n", true));
        }

    }

    private String rewriteCode(String code, int alternativeIndex, String lhs) {
        Matcher matcher = Pattern.compile("\\$([a-z][a-z\\d]*)\\.").matcher(code);
        String firstModification = matcher.replaceAll("$1.label" + alternativeIndex + ".attrs.");
        matcher = Pattern.compile("\\$([a-z][a-z\\d]*)([^.])").matcher(firstModification);
        String secondModification = matcher.replaceAll("this.$1");
        return Pattern.compile("\\$([A-Z][A-Z\\d]*)\\.").matcher(secondModification)
                .replaceAll("tokens.$1.");
    }

    private void genParser() throws IOException {
        List<LexerRule> lexerRules = grammar.lexerRules;
        printHeader();
        println("import common_lexer." + lexerName + ".*;");
        Map<String, Map<Integer, Integer>> lhsToTokenToAlt = builder.getLhsToTokenToAlt();
        Map<String, Map<Integer, Set<Integer>>> lhsToAltToTokens = new HashMap<>();
        System.out.println("first");
        System.out.println(builder.getFirst());
        System.out.println("follow");
        System.out.println(builder.getFollow());
        lhsToTokenToAlt.forEach((lhs, tokenToAlt) -> {
            lhsToAltToTokens.putIfAbsent(lhs, new TreeMap<>());
            lhsToTokenToAlt.get(lhs).forEach((token, alt) -> {
                lhsToAltToTokens.get(lhs).putIfAbsent(alt, new TreeSet<>());
                Map<Integer, Set<Integer>> altToTokens = lhsToAltToTokens.get(lhs);
                altToTokens.get(alt).add(token);
            });
        });

        System.out.println("lhsToAltToTokens\n" + lhsToAltToTokens);

        printBody("public class " + parserName, () -> {
            println("private common_lexer.Lexer lexer;\n");

            print("    public static class ParseException extends Exception {\n" +
                    "        public ParseException(String message) {\n" +
                    "            super(message);\n" +
                    "        }\n" +
                    "    }\n" +
                    "\n", true);

            grammar.parserRules.forEach(parserRule -> {
                String lhs = parserRule.getLhs();
                String capLhs = capitalize(lhs);
                StringJoiner paramList = new StringJoiner(", ", "(", ")");

                printBody("public class " + capLhs, () -> {
                    printBody("class Attrs", () -> {
                        parserRule.params.forEach(p -> {
                            println(p + ";");
                            paramList.add(p);
                        });
                        parserRule.returnValues.forEach(r -> println(r + ";"));
                    });
                    List<ParserRule.Alternative> alternatives = parserRule.getAlternatives();
                    final List<List<String>> labelFields = new ArrayList<>();
                    final List<List<String>> tokenFields = new ArrayList<>();
                    for (int i = 0; i < alternatives.size(); i++) {
                        ParserRule.Alternative alternative = alternatives.get(i);
                        labelFields.add(new ArrayList<>());
                        tokenFields.add(new ArrayList<>());
                        List<String> curLabelFields = labelFields.get(i);
                        List<String> curTokenFields = tokenFields.get(i);
                        alternative.labels.forEach((label, index) -> {
                            ParserRule.Application application = alternative.getRhs().get(index);
                            if (application.isToken())
                                curTokenFields.add("Token " + label + ";");
                            else
                                curLabelFields.add(capitalize(application.elem) + " " + label + ';');
                        });
                    }
                    for (int i = 0; i < alternatives.size(); i++) {
                        int finalI = i;
                        printBody("class Labels" + i, () -> {
                            labelFields.get(finalI).forEach(this::println);
                        });
                        printBody("class Tokens" + i, () -> {
                            tokenFields.get(finalI).forEach(this::println);
                        });
                    }

                    printBody("public " + capLhs + paramList + " throws ParseException", () -> {
                        parserRule.params.forEach(p -> {
                                    String[] splitParam = p.split(" ");
                                    String name = splitParam[splitParam.length - 1];
                                    println(lhs + ".attrs." + name + " = " + name + ";");
                                }
                        );
                        println("Token token = lexer.getToken();");
                        printBody("switch(token.id)", () -> {
                            lhsToAltToTokens.get(lhs).forEach((altIndex, tokens) -> {
                                tokens.forEach(
                                        id -> println("case " + id + ":  // " + (id == Util.Constants.END ? "END" :
                                                grammar.lexerRules.get(id).regex))
                                );
                                ParserRule.Alternative alternative = alternatives.get(altIndex);
                                printBody("", () -> {
                                    alternative.getRhs().forEach(application -> {
                                        if (!application.isToken()) {
                                            String capitalizeElem = capitalize(application.elem);
                                            println(capitalizeElem + " " + application.label + " = new " + capitalizeElem
                                                    + "(" + rewriteCode(application.args, altIndex, lhs) + ");");
                                        } else {
                                            if (application.label != null)
                                                println("Token " + lhs + ".tokens" + altIndex + "." +
                                                        application.label + " = lexer.getToken();");
                                            println("lexer.next();");
                                        }
                                    });
                                    String code = alternative.getCode();
                                    println(rewriteCode(code, altIndex, lhs));
                                    println("break;");
                                });
                            });
                            printBody("default: ", () ->
                                    println("throw new ParseException(\"unexpected token '\" + token.text + \"' at \"" +
                                            " + token.coord);"));
                        });
                    });

                    println();
                    println("Attrs attrs = new Attrs();");
                    for (int i = 0; i < alternatives.size(); i++) {
                        String labelsClassName = "Labels" + i;
                        String tokensClassName = "Tokens" + i;
                        println(labelsClassName + " " + labelsClassName.toLowerCase() + " = new " + labelsClassName +
                                "();");
                        println(tokensClassName + " " + tokensClassName.toLowerCase() + " = new " + tokensClassName +
                                "();");
                    }
                    println(capLhs + " " + lhs + " = this;");
                });
            });
            printBody("public " + parserName + "(Path file) throws IOException", () -> {
                println("lexer = new common_lexer.Lexer(file, " +
                        lexerRules.stream()
                                .map(lexerRule ->
                                        "new Rule(\"(^" + escape(lexerRule.regex) + ")\", " + lexerRule.skip + ")")
                                .collect(Collectors.joining(", ", "List.of(", ")"))
                        + ", " + tabSize + ");");
            });
        });

    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1)
            throw new IllegalArgumentException("expected path to grammar as command-line argument");
        Generator generator = new Generator(Paths.get(args[0]), 4);
        generator.generate();
    }

}
