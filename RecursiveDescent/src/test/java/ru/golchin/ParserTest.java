package ru.golchin;

import com.google.common.base.Charsets;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Roman Golchin (romagolchin@gmail.com)
 */
class ParserTest {

    private void runTestsFromPath(Path path, boolean bad) throws Throwable {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path)) {
            directoryStream.forEach(
                    p -> {
                        Path fileName = p.getFileName();

                        try (BufferedReader br = Files.newBufferedReader(p, Charsets.UTF_8)) {
                            Node parsed = new Parser(br).parse();
                            Files.createDirectories(Paths.get("answers"));
                            Files.write(Paths.get("answers", fileName.toString() + ".dot"), Arrays.asList(parsed.toGraphString()));
                            if (bad)
                                fail("execution of the 'bad' test " + fileName + " must throw ParseException");
                        } catch (ParseException pe) {
                            if (!bad)
                                fail("execution of the 'good' test " + fileName + " must not throw");

                        } catch (IOException ignored) {
                        } catch (Exception e) {
                            fail("no exceptions but IOException or ParseException are allowed", e);
                        }
                    });
        }
    }

    @Test
    void expression() throws Throwable {
        runTestsFromPath(Paths.get("tests", "good"), false);
        runTestsFromPath(Paths.get("tests", "bad"), true);
    }

    @Test
    void random() throws Throwable {
        Path testPath = Paths.get("tests", "random");
        Node expectedNode = new TestGenerator(50, testPath).gen();
        try (BufferedReader br = Files.newBufferedReader(testPath, StandardCharsets.UTF_8)) {
            Node receivedNode = new Parser(br).parse();
            assertTrue(receivedNode.equals(expectedNode));
        }
    }
}