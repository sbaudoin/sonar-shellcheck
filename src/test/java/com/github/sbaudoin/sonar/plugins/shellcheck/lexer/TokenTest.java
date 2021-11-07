/**
 * Copyright (c) 2018-2021, Sylvain Baudoin
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

import junit.framework.TestCase;

import static org.junit.Assert.assertNotEquals;

public class TokenTest extends TestCase {
    public void test() {
        Token token1 = new Token(TokenType.SHEBANG, 4, 12, 2, 2);
        Token token2 = new Token(TokenType.SHEBANG, 4, 12, 2, 2);
        Token token3 = new Token(TokenType.COMMENT, 4, 9, 2, 2);
        Token token4 = new Token(TokenType.SHEBANG, 2, 3, 5, 3);
        Token token5 = new Token(TokenType.LINE_FEED, 2, 3, 5, 3);

        assertFalse("".equals(token1));
        assertEquals(token2, token1);
        assertNotEquals(token4, token1);
        assertNotEquals(token5, token4);

        assertEquals(Integer.hashCode(4), token1.hashCode());

        assertEquals("SHEBANG (4, 12, 2:2)", token1.toString());

        assertTrue(token1.compareTo(token4) > 0);
        assertTrue(token1.compareTo(token3) > 0);
        assertTrue(token4.compareTo(token5) == TokenType.SHEBANG.compareTo(TokenType.LINE_FEED));

        assertEquals(16, token1.end());
    }
}
