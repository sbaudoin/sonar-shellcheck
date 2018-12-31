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
