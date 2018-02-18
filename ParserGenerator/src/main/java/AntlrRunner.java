import org.antlr.v4.gui.TestRig;

/**
 * @author Roman Golchin (romagolchin@gmail.com)
 */
public class AntlrRunner {
    public static void main(String[] args) throws Exception {
        TestRig.main(new String[]{"Grammar", "file", "-tokens", "grammarArithm"});
    }
}
