package ru.golchin;


import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * @author Roman Golchin (romagolchin@gmail.com)
 */
public class TestGenerator {

    private final int maxDepth;

    private final Path path;

    public TestGenerator(int maxDepth, Path path) {
        this.maxDepth = maxDepth;
        this.path = path;
    }

    private final Random random = new Random(System.nanoTime());

    private static final Node EPS = new Node("Eps");

    private final List<Node> rules = List.of(new Node("E", new Node("A"), new Node("E1")),
            new Node("E1", new Node("|"), new Node("A"), new Node("E1")),
            new Node("E1", new Node("Eps")),
            new Node("A", new Node("C"), new Node("A1")),
            new Node("A1", new Node("C"), new Node("A1")),
            new Node("A1", new Node("Eps")),
            new Node("C", new Node("n"), new Node("C1")),
            new Node("C", new Node("("), new Node("E"), new Node(")"), new Node("C1")),
            new Node("C1", new Node("*"), new Node("C1")),
            new Node("C1", new Node("Eps"))
    );

    private void genHelper(Node cur, int depth, StringBuilder[] toReturn) {
        List<Node> possibleRules = new ArrayList<>();
        String name = cur.getName();
        for (Node rule : rules) {
            String nonTerminal = rule.getName();
            if (nonTerminal.equals(name) &&
                    (depth + 1 < maxDepth || rule.getChildren().get(0).getName().equals("Eps") || !nonTerminal.endsWith("1")))
                possibleRules.add(rule);
        }
        int rulesNumber = possibleRules.size();
        if (rulesNumber == 0) {
            if (!"Eps".equals(name))
                toReturn[0].append(name);
        } else {
            int index = random.nextInt(rulesNumber);
            cur.setChildren(possibleRules.get(index).getChildren().stream().map(Node::new).collect(Collectors.toList()));
            for (Node child : cur.getChildren()) {
                genHelper(child, depth + 1, toReturn);
            }
        }
    }

    public Node gen() {
        try (BufferedWriter bw = Files.newBufferedWriter(path)) {
            StringBuilder[] ret = {new StringBuilder()};
            Node test = new Node("E");
            genHelper(test, 0, ret);
            bw.write(ret[0].toString());
            return test;
        } catch (IOException ignored) {
            return null;
        }
    }

}
