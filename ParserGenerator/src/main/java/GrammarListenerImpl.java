import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;

/**
 * @author Roman Golchin (romagolchin@gmail.com)
 */
public class GrammarListenerImpl extends GrammarBaseListener {
    Grammar grammar;

    private final List<ParserRule> parserRules = new ArrayList<>();

    private final List<LexerRule> lexerRules = new ArrayList<>();

    private final Map<String, Integer> tokenNameToId = new HashMap<>();

    // maps an implicit token,
    // i.e. one not declared in lexer rules in grammar file
    // to its id.
    // (Implicit tokens don't have names => cannot be stored in tokenNameToId)
    private final Map<String, Integer> implicitTokens = new HashMap<>();

    private final Set<String> ruleNames = new HashSet<>();

    public Grammar getGrammar() {
        return grammar;
    }

    private String assignLabel(Map<String, Integer> labelCount, String label) {
        Integer count = labelCount.get(label);
        if (count != null) {
            labelCount.put(label, count + 1);
            return label + count;
        } else {
            labelCount.put(label, 1);
        }
        return label;
    }

    @Override
    public void exitFile(GrammarParser.FileContext ctx) {
        super.exitFile(ctx);
        // check if all elements are declared
        for (int i = 0; i < parserRules.size(); i++) {
            ParserRule parserRule = parserRules.get(i);
            for (ParserRule.Alternative alternative : parserRule.getAlternatives()) {
                int finalI = i;
                alternative.getRhs().stream().map(application -> application.elem).forEach(element -> {
                    if (element != null && !ruleNames.contains(element))
                        throw new RuntimeException("undeclared non-terminal " + element + " is used in alternative "
                                + (finalI + 1) + " of rule " + parserRule.getLhs());
                });
            }
        }

        grammar = new Grammar(parserRules, lexerRules);
        System.out.println("lexer rules " + lexerRules);
        System.out.println("parser rules " + parserRules);
    }


    @Override
    public void exitParser_rule(GrammarParser.Parser_ruleContext ctx) {
        super.exitParser_rule(ctx);
        String ruleName = ctx.LC_ID().getText();
        if (ruleNames.contains(ruleName))
            throw new RuntimeException("rule " + ruleName + " is declared multiple times");
        ruleNames.add(ruleName);
        List<ParserRule.Alternative> alternatives = new ArrayList<>();
        for (int i = 0; i < ctx.alt().size(); i++) {
            GrammarParser.AltContext altContext = ctx.alt().get(i);
            List<ParserRule.Application> applications = new ArrayList<>();
            Map<String, Integer> labels = new HashMap<>();
            Map<String, Integer> labelCount = new HashMap<>();
            for (GrammarParser.ElemContext elemContext : altContext.elem()) {
                TerminalNode string = elemContext.STRING();
                List<TerminalNode> terminals = elemContext.UC_ID();
                List<TerminalNode> nonTerminals = elemContext.LC_ID();
                String indexedLabel = null;
                ParserRule.Application application = null;
                String label = null;
                if (string != null) {
                    // implicit token
                    String text = string.getSymbol().getText();
                    label = terminals == null || terminals.isEmpty() ? null : terminals.get(0).getText();
                    if (!implicitTokens.containsKey(text)) {
                        implicitTokens.put(text, lexerRules.size());
                        lexerRules.add(new LexerRule(text.substring(1, text.length() - 1), false));
                    }
                    indexedLabel = label == null ? null : assignLabel(labelCount, label);
                    application = new ParserRule.Application(implicitTokens.get(text), null, null, label);
                } else if (nonTerminals != null && !nonTerminals.isEmpty()) {
                    // non-terminal
                    label = nonTerminals.get(0).getText();
                    TerminalNode attrs = elemContext.ARGS();
                    String args = attrs == null ? "[]" : attrs.getText();
                    indexedLabel = assignLabel(labelCount, label);
                    String elem = nonTerminals.get(nonTerminals.size() - 1).getText();
                    application = new ParserRule.Application(Util.Constants.NONE,
                            elem, args.substring(1, args.length() - 1).trim(), indexedLabel);
                } else if (terminals != null && !terminals.isEmpty()) {
                    // explicit token
                    TerminalNode terminal = terminals.get(terminals.size() - 1);
                    String text = terminal.getSymbol().getText();
                    Integer tokenId = tokenNameToId.get(text);
                    label = terminals.get(0).getText();
                    indexedLabel = assignLabel(labelCount, label);
                    if (tokenId != null)
                        application = new ParserRule.Application(tokenId, text, null, indexedLabel);
                    else {
                        Token symbol = terminal.getSymbol();
                        throw new RuntimeException("undeclared token " + symbol.getText()
                                + " at " + symbol.getLine() + ":" + symbol.getCharPositionInLine());
                    }
                }
                if (label != null) {
                    if (labelCount.get(label) > 1)
                        System.err.println("warning: label " + label + " is assigned to multiple elements " +
                                "in alternative " + (i + 1) + " in rule " + ruleName);
                }
                if (application != null) {
                    applications.add(application);
                }
                int lastAdded = application != null ? 1 : 0;
                if (indexedLabel != null)
                    labels.putIfAbsent(indexedLabel, applications.size() - lastAdded);
            }
            TerminalNode codeNode = altContext.CODE();
            String code = "";
            if (codeNode != null) {
                String codeWithBraces = codeNode.getSymbol().getText();
                code = codeWithBraces.substring(1, codeWithBraces.length() - 1).trim();
            }
            alternatives.add(new ParserRule.Alternative(applications, labels, code));
        }

        ParserRule rule = new ParserRule(ruleName, alternatives);
        // add attributes
        Set<String> params = new HashSet<>();
        TerminalNode args = ctx.ARGS();
        if (args != null) {
            String text = args.getText();
            params.addAll(Arrays.asList(
                    text.substring(1, text.length() - 1).split(",")
            ));
        }
        rule.setParams(params);
        Set<String> retValues = new HashSet<>();
        GrammarParser.RetContext retContext = ctx.ret();
        if (retContext != null) {
            String text = retContext.ARGS().getText();
            retValues.addAll(Arrays.asList(
                    text.substring(1, text.length() - 1).split(",")
            ));
        }
        rule.setReturnValues(retValues);
        parserRules.add(rule);
    }


    @Override
    public void enterLexer_rule(GrammarParser.Lexer_ruleContext ctx) {
        super.enterLexer_rule(ctx);
        boolean skip = ctx.SK() != null;
        TerminalNode ruleName = ctx.UC_ID();
        String regex = ctx.STRING().getText();
        tokenNameToId.put(ruleName.getText(), lexerRules.size());
        lexerRules.add(new LexerRule(regex.substring(1, regex.length() - 1), skip));
    }
}
