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
package com.github.sbaudoin.sonar.plugins.shellcheck.highlighting;

import com.github.sbaudoin.sonar.plugins.shellcheck.lexer.BashLexer;
import com.github.sbaudoin.sonar.plugins.shellcheck.lexer.Token;
import org.sonar.api.batch.sensor.highlighting.TypeOfText;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Class in charge of Shell code highlighting in SonarQube
 */
public class ShellHighlighting {
    private static final Logger LOGGER = Loggers.get(ShellHighlighting.class);

    private List<HighlightingData> highlighting = new ArrayList<>();
    private String code;


    /**
     * Constructor
     *
     * @param script the Shell code to be highlighted
     * @throws IllegalArgumentException if {@code script} is {@code null}
     */
    public ShellHighlighting(String script) {
        if (script == null) {
            throw new IllegalArgumentException("Input source code cannot be null");
        }
        this.code = script;
        try {
            new BashLexer(new StringReader(script)).scan().forEach(this::highlightToken);
        } catch (IOException e) {
            LOGGER.warn("Could not scan Shell script and highlight code", e);
        }
    }


    /**
     * Returns the list of highlighting data found for the Shell script
     *
     * @return the list of highlighting data found for the Shell script (possibly empty but never {@code null})
     */
    public List<HighlightingData> getHighlightingData() {
        return highlighting;
    }


    /**
     * Creates an {@code HighlightingData} for a code token
     *
     * @param token a token to be highlighted
     */
    private void highlightToken(Token token) {
        switch (token.type) {
            case COMMENT: case SHEBANG:
                LOGGER.trace(String.format("Highlighting comment at %d:%d", token.line, token.column));
                addHighlighting(token, TypeOfText.COMMENT);
                break;

            case ARITH_ASS_BIT_AND: case ARITH_ASS_BIT_OR: case ARITH_ASS_BIT_XOR: case ARITH_ASS_DIV:
            case ARITH_ASS_MINUS: case ARITH_ASS_MOD: case ARITH_ASS_MUL: case ARITH_ASS_PLUS:
            case ARITH_ASS_SHIFT_LEFT: case ARITH_ASS_SHIFT_RIGHT: case ARITH_BASE_CHAR: case ARITH_BITWISE_AND:
            case ARITH_BITWISE_NEGATE: case ARITH_BITWISE_XOR: case ARITH_COLON: case ARITH_DIV:
            case ARITH_EQ: case ARITH_EXPONENT: case ARITH_GE: case ARITH_GT: case ARITH_LE:
            case ARITH_LT: case ARITH_MINUS: case ARITH_MINUS_MINUS: case ARITH_MOD: case ARITH_MULT:
            case ARITH_NE: case ARITH_NEGATE: case ARITH_PLUS: case ARITH_PLUS_PLUS: case ARITH_QMARK:
            case ARITH_SHIFT_LEFT: case ARITH_SHIFT_RIGHT: case AND_AND: case OR_OR: case EQ:
            case COND_OP: case COND_OP_EQ_EQ: case COND_OP_NOT: case COND_OP_REGEX:
                LOGGER.trace(String.format("Highlighting arithmetic expression at %d:%d", token.line, token.column));
                addHighlighting(token, TypeOfText.KEYWORD_LIGHT);
                break;

            case CASE_KEYWORD: case BRACKET_KEYWORD: case DO_KEYWORD: case DONE_KEYWORD:
            case ELIF_KEYWORD: case ELSE_KEYWORD: case ESAC_KEYWORD: case FI_KEYWORD: case FOR_KEYWORD:
            case FUNCTION_KEYWORD: case IF_KEYWORD: case LET_KEYWORD: case SELECT_KEYWORD:
            case THEN_KEYWORD: case TIME_KEYWORD: case TRAP_KEYWORD: case UNTIL_KEYWORD: case WHILE_KEYWORD:
            case ASSIGNMENT_WORD:
                LOGGER.trace(String.format("Highlighting keyword at %d:%d", token.line, token.column));
                addHighlighting(token, TypeOfText.KEYWORD);
                break;

            case HEREDOC_LINE: case HEREDOC_CONTENT: case HEREDOC_MARKER_END: case HEREDOC_MARKER_IGNORING_TABS_END:
            case HEREDOC_MARKER_START: case HEREDOC_MARKER_TAG:
                LOGGER.trace(String.format("Highlighting label at %d:%d", token.line, token.column));
                addHighlighting(token, TypeOfText.STRUCTURED_COMMENT);
                break;

            case VARIABLE:
                LOGGER.trace(String.format("Highlighting identifier at %d:%d", token.line, token.column));
                addHighlighting(token, TypeOfText.CONSTANT);
                break;

            case ARITH_HEX_NUMBER: case ARITH_NUMBER: case ARITH_OCTAL_NUMBER: case INTEGER_LITERAL:
                LOGGER.trace(String.format("Highlighting number at %d:%d", token.line, token.column));
                addHighlighting(token, TypeOfText.CONSTANT);
                break;

            case STRING_BEGIN: case STRING_END: case STRING_DATA: case STRING_CONTENT: case STRING2: case BACKQUOTE:
                LOGGER.trace(String.format("Highlighting string or regex at %d:%d", token.line, token.column));
                addHighlighting(token, TypeOfText.STRING);
                break;

            default:
                break;
        }
    }

    /**
     * Creates an {@code HighlightingData} with the passed characteristics
     *
     * @param token the token to highlighting
     * @param typeOfText the type of highlighted text
     */
    private void addHighlighting(Token token, TypeOfText typeOfText) {
        if (token.length == 0) {
            throw new IllegalArgumentException("Cannot highlight an empty token");
        }

        ShellLocation start = new ShellLocation(code, token);
        ShellLocation end = start.shift(token.length);
        highlighting.add(new HighlightingData(start.line(), start.column(), end.line(), end.column(), typeOfText));
    }
}
