import java.nio.file.Paths;

/**
 * @author Roman Golchin (romagolchin@gmail.com)
 */
public class Main {

    public static void main(String[] args) throws Exception {
        Generator generator = new Generator(Paths.get("grammarArithm"));
        generator.generate();
    }
}
