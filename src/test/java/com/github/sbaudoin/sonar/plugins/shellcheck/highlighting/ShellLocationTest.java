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
package com.github.sbaudoin.sonar.plugins.shellcheck.highlighting;

import com.github.sbaudoin.sonar.plugins.shellcheck.lexer.Token;
import com.github.sbaudoin.sonar.plugins.shellcheck.lexer.TokenType;
import junit.framework.TestCase;

public class ShellLocationTest extends TestCase {
    private static final String SCRIPT = "#!/bin/bash\necho 'foo bar'\n";


    public void testConstructors() {
        ShellLocation location1 = new ShellLocation(SCRIPT, new Token(TokenType.SHEBANG, 0, 12, 0, 0));
        assertEquals(1, location1.line());
        assertEquals(1, location1.column());
        assertTrue(location1.isSameAs(new ShellLocation(SCRIPT, 1, 1, 0)));
        assertFalse(location1.isSameAs(new ShellLocation(SCRIPT, 1, 1, 1)));
    }

    public void testToString() {
        String script = "#!/bin/bash\n";
        assertEquals("{ content: \"" + script + "\"; line: 1; column: 1; characterOffset: 0 }", new ShellLocation(script, 1, 1, 0).toString());
    }

    public void testShift() {
        ShellLocation location1 = new ShellLocation(SCRIPT, 1, 1, 0);
        ShellLocation location2 = location1.shift(13);
        assertEquals(2, location2.line());
        assertEquals(2, location2.column());
        assertTrue(location2.isSameAs(new ShellLocation(SCRIPT, 1, 1, 13)));
        try {
            location2.shift(40);
            fail("Invalid shift value accepted");
        } catch (IllegalStateException e) {
            assertEquals("Cannot shift by 40 characters", e.getMessage());
        }
    }
}
