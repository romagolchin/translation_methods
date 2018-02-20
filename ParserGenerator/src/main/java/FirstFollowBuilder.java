
import java.io.IOException;
import java.util.*;

/**
 * @author Roman Golchin (romagolchin@gmail.com)
 */
public class FirstFollowBuilder {
    private final List<ParserRule> parserRules;

    private final String startNonTerminal;

    private final Grammar grammar;

    private final Map<String, Set<Integer>> first = new HashMap<>();

    private final Map<String, Set<Integer>> follow = new HashMap<>();

    private final Map<String, Map<Integer, Integer>> lhsToTokenToAlt = new HashMap<>();

    public Map<String, Map<Integer, Integer>> getLhsToTokenToAlt() {
        return lhsToTokenToAlt;
    }

    public FirstFollowBuilder(Grammar grammar, String startNonTerminal) throws IOException {
        this.grammar = grammar;
        this.startNonTerminal = startNonTerminal;
        parserRules = grammar.parserRules;
        build();
        computeTokenToAltMap();
    }

    private void initFirstFollow() {
        for (ParserRule rule : parserRules) {
            String left = rule.getLhs();
            first.put(left, new HashSet<>());
            Set<Integer> followInit = new HashSet<>();
            if (left.equals(startNonTerminal))
                followInit.add(Util.Constants.END);
            follow.put(left, followInit);
        }
    }

    Set<Integer> computeFirst(List<ParserRule.Application> rightPart) {
        HashSet<Integer> tokens = new HashSet<>();
        if (rightPart.isEmpty())
            tokens.add(Util.Constants.EPS);
        for (ParserRule.Application a : rightPart) {
            String elem = a.elem;
            if (elem != null && elem.isEmpty())
                tokens.add(Util.Constants.EPS);
            if (a.isToken()) {
                Integer token = a.tokenId;
                tokens.add(token);
                break;
            } else {
                // non-terminal
                if (elem != null && !elem.isEmpty()) {
                    Set<Integer> nonTerminalFirst = first.get(elem);
                    if (nonTerminalFirst == null)
                        throw new AssertionError("no such non-terminal " + elem);
                    else {
                        tokens.addAll(nonTerminalFirst);
                        if (!nonTerminalFirst.contains(Util.Constants.EPS))
                            break;
                    }
                } else {
                    tokens.add(Util.Constants.EPS);
                }
            }
        }
        return tokens;
    }

    void build() {
        initFirstFollow();
        // first
        boolean changed = true;
        while (changed) {
            changed = false;
            for (ParserRule rule : parserRules) {
                String left = rule.getLhs();
                for (ParserRule.Alternative alternative : rule.getAlternatives()) {
                    List<ParserRule.Application> rightPart = alternative.getRhs();
                    changed = changed || first.get(left).addAll(computeFirst(rightPart));
                }
            }
        }
        // follow
        changed = true;
        while (changed) {
            changed = false;
            for (ParserRule rule : parserRules) {
                for (ParserRule.Alternative alternative : rule.getAlternatives()) {
                    List<ParserRule.Application> rhs = alternative.getRhs();
                    for (int i = 0; i < rhs.size(); i++) {
                        String s = rhs.get(i).elem;
                        Set<Integer> curFollow = follow.get(s);
                        if (curFollow != null) {
                            List<ParserRule.Application> rightPart = rhs.subList(i + 1, rhs.size());
                            Set<Integer> tokens = computeFirst(rightPart);
                            curFollow.addAll(tokens);
                            if (curFollow.remove(Util.Constants.EPS)) {
                                changed = changed || curFollow.addAll(follow.get(rule.getLhs()));
                            }
                        }
                    }
                }
            }
        }
    }


    public Map<String, Set<Integer>> getFirst() {
        return first;
    }

    public Map<String, Set<Integer>> getFollow() {
        return follow;
    }

    private void computeTokenToAltMap() {

        for (ParserRule rule : parserRules) {
            String lhs = rule.getLhs();
            Set<Integer> curFollow = follow.get(lhs);
            lhsToTokenToAlt.put(lhs, new HashMap<>());
            List<ParserRule.Alternative> alternatives = rule.getAlternatives();
            for (int altNo = 0; altNo < alternatives.size(); altNo++) {
                ParserRule.Alternative alternative = alternatives.get(altNo);
                Set<Integer> firstTokens = computeFirst(alternative.getRhs());
                if (firstTokens.contains(Util.Constants.EPS)) {
                    firstTokens.addAll(curFollow);
                }
                firstTokens.remove(Util.Constants.EPS);
                final int finalAltNo = altNo;
                firstTokens.forEach(token -> {
                    Map<Integer, Integer> tokenToAlt = lhsToTokenToAlt.get(lhs);
                    Integer oldAlt = tokenToAlt.get(token);
                    if (oldAlt != null) {
                        throw new RuntimeException("Grammar is not LL(1): token '"
                                + grammar.lexerRules.get(token).regex + "' leads to alternatives " +
                                (oldAlt + 1) + " and " + (finalAltNo + 1) + " in rule " + lhs);
                    }
                    tokenToAlt.put(token, finalAltNo);
                });
            }
        }

        System.out.println(lhsToTokenToAlt);
    }
}
