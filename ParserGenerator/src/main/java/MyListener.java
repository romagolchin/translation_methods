import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;

/**
 * @author Roman Golchin (romagolchin@gmail.com)
 */
public class MyListener extends GrammarBaseListener {
    Grammar grammar;

    final List<ParserRule> parserRules = new ArrayList<>();

    final List<LexerRule> lexerRules = new ArrayList<>();

    final Map<String, Integer> tokenNameToId = new HashMap<>();


    public Grammar getGrammar() {
        return grammar;
    }

    @Override
    public void exitFile(GrammarParser.FileContext ctx) {
        super.exitFile(ctx);
        grammar = new Grammar(parserRules, lexerRules);
        System.out.println("lexer rules " + lexerRules);
        System.out.println("parser rules " + parserRules);
    }

// elem: STRING | (LC_ID '=')? LC_ID {System.out.println($LC_ID.text);} ATTRS? {}   | UC_ID;

    @Override
    public void exitParser_rule(GrammarParser.Parser_ruleContext ctx) {
        super.exitParser_rule(ctx);
        String left = ctx.LC_ID.getText();
        List<ParserRule.Alternative> alternatives = new ArrayList<>();
        for (GrammarParser.AltContext altContext : ctx.alt()) {
            List<ParserRule.Application> applications = new ArrayList<>();
            Map<String, Integer> labels = new HashMap<>();
            for (GrammarParser.ElemContext elemContext : altContext.elem()) {
                TerminalNode string = elemContext.STRING();
                Token nonTerminal = elemContext.LC_ID;
                TerminalNode terminal = elemContext.UC_ID();
                if (string != null) {
                    // implicit token
                    String text = string.getSymbol().getText();
                    applications.add(new ParserRule.Application(lexerRules.size(), null, null));
                    lexerRules.add(new LexerRule(text.substring(1, text.length() - 1), false));
                } else if (nonTerminal != null) {
                    List<TerminalNode> nodes = elemContext.LC_ID();
                    String label = nodes != null ? nodes.get(0).getText() : nonTerminal.getText();
                    labels.putIfAbsent(label, applications.size());
                    TerminalNode attrs = elemContext.ARGS();
                    applications.add(new ParserRule.Application(-1, nonTerminal.getText(),
                            attrs == null ? "" : attrs.getText()));
                } else if (terminal != null) {
                    // explicit token
                    Integer tokenId = tokenNameToId.get(terminal.getSymbol().getText());
                    if (tokenId != null)
                        applications.add(new ParserRule.Application(tokenId, null, null));
                    else {
                        Token symbol = terminal.getSymbol();
                        throw new RuntimeException("undeclared token " + symbol.getText() + " at " + symbol.getLine() + ":"
                                + symbol.getCharPositionInLine());
                    }
                }
            }
            alternatives.add(new ParserRule.Alternative(applications, labels, altContext.CODE().getSymbol().getText()));
        }

        ParserRule rule = new ParserRule(left, alternatives);
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
