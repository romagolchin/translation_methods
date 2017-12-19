import java.util.List;

/**
 * @author Roman Golchin (romagolchin@gmail.com)
 */
public class Grammar {
    List<ParserRule> parserRules;

    List<LexerRule> lexerRules;

    public Grammar(List<ParserRule> parserRules, List<LexerRule> lexerRules) {
        this.parserRules = parserRules;
        this.lexerRules = lexerRules;
    }
}
