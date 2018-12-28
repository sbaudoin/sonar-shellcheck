/**
 * Copyright (c) 2018, Sylvain Baudoin
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
package com.github.sbaudoin.sonar.plugins.shellcheck.linecounter;

import com.github.sbaudoin.sonar.plugins.shellcheck.lexer.BashLexer;
import com.github.sbaudoin.sonar.plugins.shellcheck.lexer.TokenType;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Counting lines, comment lines and blank lines in Shell scripts
 */
public final class LineCountParser {
    private int lineNumber;
    private Set<Integer> commentLines;
    private Set<Integer> linesOfCodeLines;
    private LineCountData data;


    /**
     * Constructor. The passed string is parsed and an instance of {@code LineCountData} is created and ready for
     * retrieval with the method {@linkplain #getLineCountData()}
     *
     * @param contents the Shell script to be parsed
     * @throws IOException if there is a problem reading the contents
     * @see #getLineCountData()
     */
    public LineCountParser(String contents) throws IOException {
        commentLines = new HashSet<>();
        linesOfCodeLines = new HashSet<>();
        lineNumber = 0;

        // Get heredoc lines: these are lines to be ignored while looking for comments
        List<Integer> hdLines = new BashLexer(new StringReader(contents)).parse().stream()
                .filter(token -> token.type == TokenType.HEREDOC_LINE)
                .mapToInt(token -> token.line).boxed().collect(Collectors.toList());

        BufferedReader reader = new BufferedReader(new StringReader(contents));
        String line;
        while ((line = reader.readLine()) != null) {
            if (!hdLines.contains(lineNumber) && isCommentLine(line)) {
                commentLines.add(lineNumber);
            } else if (!isBlankLine(line)) {
                linesOfCodeLines.add(lineNumber);
            }
            lineNumber++;
        }

        this.data = new LineCountData(
                lineNumber,
                linesOfCodeLines,
                commentLines);
    }

    /**
     * Returns the {@code LineCountData} describing the passed Shell script
     *
     * @return the {@code LineCountData} describing the passed Shell script
     */
    public LineCountData getLineCountData() {
        return data;
    }


    /**
     * Tells if the passed line of code is a comment line, i.e. a line with only a non-empty comment
     *
     * @param line a line of code
     * @return {@code true} if the passed string represents a line of comment. Inline comments return {@code false}.
     */
    private boolean isCommentLine(String line) {
        return line.trim().matches("^[ \\t]*#[ \\t]*\\S.*");
    }

    /**
     * Tells if the passed line is blank, i.e. it contains nothing but whitespaces
     *
     * @param line a line of code
     * @return {@code true} if the line if blank, {@code false} if not
     */
    private boolean isBlankLine(String line) {
        return StringUtils.isBlank(line);
    }
}
