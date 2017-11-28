package ru.golchin;

/**
 * @author Roman Golchin (romagolchin@gmail.com)
 */
public class ParseException extends Exception {
    public ParseException(String message) {
        super(message);
    }

    public ParseException(char c, Tokenizer.Coord coord) {
        super("unexpected character " + c + " at "  + coord);
    }

    public ParseException(Token token, Tokenizer.Coord coord) {
        super("unexpected token " + token.str + " at " + coord);
    }
}
