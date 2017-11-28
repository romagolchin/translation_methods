package ru.golchin;

import java.io.BufferedReader;

/**
 * @author Roman Golchin (romagolchin@gmail.com)
 */
public class Parser {
    private final Tokenizer tokenizer;

    public Parser(BufferedReader bufferedReader) {
        tokenizer = new Tokenizer(bufferedReader);
    }

    private void skip(Token token) throws ParseException {
        if (token != tokenizer.getToken())
            throw new ParseException("expected " + token + " at " + tokenizer.getCoord()
                    + ", got " + tokenizer.getToken());
        tokenizer.next();
    }

    public Node parse() throws ParseException {
        Node expr = expression();
        if (tokenizer.getToken() != Token.END)
            throw new ParseException("extra tokens from position " + tokenizer.getCoord());
        return expr;
    }

    private Node expression() throws ParseException {
        Token token = tokenizer.getToken();
        if (token != Token.LETTER && token != Token.LPAREN)
            throw new ParseException(token, tokenizer.getCoord());
        return new Node("E", alternative(), expression1());
    }

    public Node expression1() throws ParseException {
        Token token = tokenizer.getToken();
        switch (token) {
            case ALT: {
                tokenizer.next();
                return new Node("E1", new Node("|"), alternative(), expression1());
            }
            case RPAREN:
            case END: {
                return new Node("E1", new Node("Eps"));
            }
            default:
                throw new ParseException(token, tokenizer.getCoord());
        }

    }

    private Node alternative() throws ParseException {
        return new Node("A", concatenation(), alternative1());
    }

    private Node alternative1() throws ParseException {
        Token token = tokenizer.getToken();
        switch (token) {
            case LETTER:
            case LPAREN:
                return new Node("A1", concatenation(), alternative1());
            case ALT:
            case RPAREN:
            case END:
                return new Node("A1", new Node("Eps"));
            default:
                throw new ParseException(token, tokenizer.getCoord());
        }
    }

    private Node concatenation() throws ParseException {

        if (tokenizer.getToken() == Token.LPAREN) {
            tokenizer.next();
            Node exprInParens = expression();
            skip(Token.RPAREN);
            return new Node("C", new Node("("), exprInParens, new Node(")"), concatenation1());
        } else {
            skip(Token.LETTER);
            return new Node("C", new Node("n"), concatenation1());
        }

    }

    private Node concatenation1() throws ParseException {
        if (tokenizer.getToken() == Token.ASTERISK) {
            tokenizer.next();
            return new Node("C1", new Node("*"), concatenation1());
        }
        else return new Node("C1", new Node("Eps"));
    }
}
