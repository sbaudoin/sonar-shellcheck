package com.github.sbaudoin.sonar.plugins.shellcheck.lexer;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Main Bash lexer class
 */
public class BashLexer extends BashLexerBase implements ShellLexer {
    /**
     * Constructs a new Bash lexer for the passed Shell script supposed to be a Bash 4 script
     *
     * @param in a reader to the Bash script to be scanned
     */
    public BashLexer(Reader in) {
        this(true, in);
    }

    /**
     * Constructs a new Bash lexer for the passed Shell script
     *
     * @param isBash4 {@code true} if {@code in} is a Bash 4 script
     * @param in a reader to the Bash script to be scanned
     */
    public BashLexer(boolean isBash4, Reader in) {
        super(in);
        this.isBash4 = isBash4;
    }


    /**
     * Analyses the Shell script and returns the list of tokens found
     *
     * @return the list of tokens found in the Bash script passed in the constructor
     */
    @Override
    public List<Token> parse() throws IOException {
        List<Token> tokens = new ArrayList<>();
        for (Token t = yylex(); t != null; t = yylex()) {
            tokens.add(t);
        }
        return tokens;
    }
}
