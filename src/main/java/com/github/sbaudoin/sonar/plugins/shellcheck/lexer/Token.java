/**
 * Copyright (c) 2018-2020, Sylvain Baudoin
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

import java.io.Serializable;

/**
 * This class represents the tokens identified by Shell lexers
 */
public class Token implements Serializable, Comparable {
    /**
     * The token type
     */
    public final TokenType type;

    /**
     * The offset (in characters) at which the token was identified in the source Shell code
     */
    public final int start;

    /**
     * The length of the token (in characters)
     */
    public final int length;

    /**
     * The line number at which the token starts (starting from 0)
     */
    public final int line;

    /**
     * The column at which the token starts in the line (starting from 0)
     */
    public final int column;


    /**
     * Constructs a new token
     *
     * @param start the offset (in characters) at which the token starts
     * @param type the token type
     * @param column the column (in characters, starting from 0) where the token starts on the line
     * @param length the token's length
     * @param line the line number (starting from 0) where the token was found
     * @see TokenType
     */
    public Token(TokenType type, int start, int length, int line, int column) {
        this.type = type;
        this.start = start;
        this.length = length;
        this.line = line;
        this.column = column;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Token) {
            Token token = (Token) obj;
            return ((this.start == token.start) &&
                    (this.length == token.length) &&
                    (this.type.equals(token.type)));
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(start);
    }

    @Override
    public String toString() {
        return String.format("%s (%d, %d, %d:%d)", type, start, length, line, column);
    }

    @Override
    public int compareTo(Object o) {
        Token t = (Token) o;
        if (this.start != t.start) {
            return (this.start - t.start);
        } else if (this.length != t.length) {
            return (this.length - t.length);
        } else {
            return this.type.compareTo(t.type);
        }
    }

    /**
     * return the end position of the token
     *
     * @return start + length
     */
    public int end() {
        return start + length;
    }
}
