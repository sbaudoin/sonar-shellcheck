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
import java.util.List;

/**
 * Shell lexers should implement this interface.
 * Implementations should pay attention to the expected role of the constructors: some must offer all necessary parameters
 * so that the {@link #scan()} and {@link #next()} methods can work as expected, without parameters.
 */
public interface ShellLexer {
    /**
     * Returns all non-overlapping found tokens for each recognized token in the stream set by the constructor.
     *
     * @return the list of Tokens found in the script
     * @throws IOException if an error occurred reading the input script
     */
    List<Token> scan() throws IOException;

    /**
     * Contrary to the {@link #scan()} method, the tokens are returned one by one by this method: it resumes scanning
     * until the next regular expression is matched, the end of input is encountered or an I/O error occurs
     *
     * @return the next token
     * @exception IOException if any I/O error occurs
     */
    Token next() throws java.io.IOException;

    /**
     * Returns the token just read with {@link #next()} or {@code null} if no token read yet, {@link #scan()} was called or
     * the last token was reached (i.e. no more token left)
     *
     * @return the token just read with {@link #next()} or {@code null} if no token read yet, {@link #scan()} was called or
     * the last token was reached (i.e. no more token left)
     */
    Token getToken();
}
