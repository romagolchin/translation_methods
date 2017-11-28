package ru.golchin;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * @author Roman Golchin (romagolchin@gmail.com)
 */
public class Tokenizer {
    static class Coord {
        int lineNumber;
        int pos;

        public Coord(int lineNumber, int pos) {
            this.lineNumber = lineNumber;
            this.pos = pos;
        }

        @Override
        public String toString() {
            return "(" + lineNumber + ", " + pos + ")";
        }
    }

    private Coord coord = new Coord(0, 0);

    public Coord getCoord() {
        return coord;
    }

    private BufferedReader reader;
    private String curLine = "";
    private Token token;

    public Token getToken() throws ParseException {
        if (token == null)
            next();
        return token;
    }

    public Tokenizer(BufferedReader reader) {
        this.reader = reader;
    }

    public void next() throws ParseException {
        try {
            for (; ; ) {
                if (curLine != null) {
                    for (; coord.pos < curLine.length(); coord.pos++) {
                        char curChar = curLine.charAt(coord.pos);
                        if (!Character.isWhitespace(curChar)) {
                            coord.pos++;
                            if (Character.isLetter(curChar)) token = Token.LETTER;
                            else switch (curChar) {
                                case '(':
                                    token = Token.LPAREN; break;
                                case ')':
                                    token = Token.RPAREN; break;
                                case '|':
                                    token = Token.ALT; break;
                                case '*':
                                    token = Token.ASTERISK; break;
                                default:
                                    throw new ParseException(curChar, coord);
                            }
                            return;
                        }
                    }
                } else  {
                    token = Token.END;
                    return;
                }
                curLine = reader.readLine();
                coord.pos = 0;
                coord.lineNumber++;
            }
        } catch (IOException ignored) {
            token = Token.END;
        }
    }
}
