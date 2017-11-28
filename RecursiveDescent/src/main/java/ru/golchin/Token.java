package ru.golchin;

/**
 * @author Roman Golchin (romagolchin@gmail.com)
 */
public enum Token {
    LETTER("n"), LPAREN("("), RPAREN(")"), ASTERISK("*"), ALT("|"), END("end");
    String str;
    Token(String str) {
        this.str = str;
    }
}
