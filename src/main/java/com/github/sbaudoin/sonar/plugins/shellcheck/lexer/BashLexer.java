/**
 * Copyright (c) 2018-2019, Sylvain Baudoin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.sbaudoin.sonar.plugins.shellcheck.lexer;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Main Bash lexer class
 */
public class BashLexer extends BashLexerBase implements ShellLexer {
    private Token currentToken;
    // Required to aggregate STRING_DATA and EREDOC_LINE into STRING_CONTENT and HEREDOC_CONTENT
    private Token nextToken;


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
        this.currentToken = null;
    }


    @Override
    public List<Token> scan() throws IOException {
        List<Token> tokens = new ArrayList<>();
        for (Token t = next(); t != null; t = next()) {
            tokens.add(t);
        }
        return tokens;
    }

    @Override
    public Token next() throws IOException {
        if (isStringOrHeredocContent(currentToken)) {
            currentToken = nextToken;
            nextToken = null;
        } else {
            currentToken = yylex();
            if (isStringOrHeredocContent(currentToken)) {
                Token firstToken = currentToken;
                int length = firstToken.length;
                while ((nextToken = yylex()) != null) {
                    if (isStringOrHeredocContent(nextToken)) {
                        length += nextToken.length;
                    } else {
                        break;
                    }
                }
                currentToken = new Token(
                        firstToken.type == TokenType.STRING_DATA ? TokenType.STRING_CONTENT : TokenType.HEREDOC_CONTENT,
                        firstToken.start,
                        length,
                        firstToken.line,
                        firstToken.column
                );
            }
        }
        return currentToken;
    }

    @Override
    public Token getToken() {
        return currentToken;
    }


    private boolean isStringOrHeredocContent(Token token) {
        return token != null && (token.type == TokenType.STRING_DATA || token.type == TokenType.HEREDOC_LINE
                || token.type == TokenType.STRING_CONTENT || token.type == TokenType.HEREDOC_CONTENT);
    }
}
