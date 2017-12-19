import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * @author Roman Golchin (romagolchin@gmail.com)
 */
public class GrammarArithmLexerTest {
    public GrammarArithmLexerTest() throws IOException {
        lexer = new GrammarArithmLexer(Paths.get("arithm"));
    }

    private GrammarArithmLexer lexer;

    @Test
    public void test() throws Exception {
//        int tokenId = 0;
//        while ((tokenId = lexer.next()) != -1) {
//            System.out.println(tokenId);
//        }
        Matcher matcher = Pattern.compile("(\\d)|(\\+)").matcher("1+2");
        matcher.find();
        System.out.println(matcher.group());
        System.out.println(matcher.group(1));
        System.out.println(matcher.group(2));
        matcher.find();
        System.out.println(matcher.group());
        System.out.println(matcher.group(1));
        System.out.println(matcher.group(2));
        matcher.find();
        System.out.println(matcher.group());
        System.out.println(matcher.group(1));
        System.out.println(matcher.group(2));
        matcher.find();
        System.out.println(matcher.group());
        System.out.println(matcher.group(1));
        System.out.println(matcher.group(2));
        System.out.println(Arrays.asList("1+2".split("\\+")));
    }
}