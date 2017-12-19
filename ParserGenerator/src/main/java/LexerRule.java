/**
 * @author Roman Golchin (romagolchin@gmail.com)
 */
public class LexerRule {
    public LexerRule(String regex, boolean skip) {
        this.regex = regex;
        this.skip = skip;
    }

    public String getRegex() {
        return regex;
    }

    public boolean isSkip() {
        return skip;
    }

    String regex;


    boolean skip;

    @Override
    public String toString() {
        return "LexerRule{" +
                "regex='" + regex + '\'' +
                ", skip=" + skip +
                '}';
    }
}
