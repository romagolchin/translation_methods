import arithm.ArithmParser;
import empty.EmptyParser;
import regex.RegexParser;
import rps.RpsParser;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Roman Golchin (romagolchin@gmail.com)
 */
public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length < 1)
            throw new IllegalArgumentException("expected path to test as command-line argument");

        Path file = Paths.get(args[0]);
//        new ArithmParser(Paths.get(args[0])).new E();
        new RegexParser(file).new E();

//        new RpsParser(Paths.get(args[0])).new S();
//        new EmptyParser(Paths.get(args[0]));
    }
}
