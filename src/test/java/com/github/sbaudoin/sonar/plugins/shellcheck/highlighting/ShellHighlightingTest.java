package com.github.sbaudoin.sonar.plugins.shellcheck.highlighting;

import com.github.sbaudoin.sonar.plugins.shellcheck.Utils;
import com.github.sbaudoin.sonar.plugins.shellcheck.lexer.BashLexer;
import org.junit.Rule;
import org.junit.Test;
import org.sonar.api.batch.sensor.highlighting.TypeOfText;
import org.sonar.api.utils.log.LogTester;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ShellHighlightingTest {
    @Rule
    public LogTester logTester = new LogTester();

    @Test
    public void testConstructors() throws IOException {
        try {
            new ShellHighlighting(null);
            fail("Null values should not be accepted");
        } catch (IllegalArgumentException e) {
            assertEquals("Input source code cannot be null", e.getMessage());
        }

        ShellHighlighting sh = new ShellHighlighting(Utils.getInputFile("test1.sh").contents());
        assertEquals(5, sh.getHighlightingData().size());
    }

    @Test
    public void testHighlightAllTokenTypes() throws IOException {
        ShellHighlighting sh = new ShellHighlighting(Utils.getInputFile("test4.sh").contents());

        assertEquals(22, sh.getHighlightingData().size());
        sh.getHighlightingData().forEach(data -> System.out.println(data.getStartLine() + ":" + data.getStartColumnIndex() + " = " + data.getTypeOfText()));

        // SHEBANG
        assertHighlightingData(sh.getHighlightingData().get(0), 1, 1, 2, 1, TypeOfText.COMMENT);

        // COMMENT
        assertHighlightingData(sh.getHighlightingData().get(1), 3, 1, 3, 10, TypeOfText.COMMENT);

        // Variable assignment: ASSIGNMENT_WORD + EQ + something that is not highlighted
        assertHighlightingData(sh.getHighlightingData().get(2), 4, 1, 4, 4, TypeOfText.KEYWORD);
        assertHighlightingData(sh.getHighlightingData().get(3), 4, 4, 4, 5, TypeOfText.KEYWORD_LIGHT);

        // WORD + STRING_BEGIN + STRING_CONTENT + STRING_END
        assertHighlightingData(sh.getHighlightingData().get(4), 6, 6, 6, 7, TypeOfText.STRING);
        assertHighlightingData(sh.getHighlightingData().get(5), 6, 7, 6, 14, TypeOfText.STRING);
        assertHighlightingData(sh.getHighlightingData().get(6), 6, 14, 6, 15, TypeOfText.STRING);
        // WORD + STRING2
        assertHighlightingData(sh.getHighlightingData().get(7), 7, 6, 7, 27, TypeOfText.STRING);
        // WORD + BACKQUOTE + VARIABLE + BACKQUOTE
        assertHighlightingData(sh.getHighlightingData().get(8), 8, 6, 8, 7, TypeOfText.STRING);
        assertHighlightingData(sh.getHighlightingData().get(9), 8, 7, 8, 11, TypeOfText.CONSTANT);
        assertHighlightingData(sh.getHighlightingData().get(10), 8, 11, 8, 12, TypeOfText.STRING);

        // WORD + HEREDOC_MARKER_TAG + HEREDOC_MARKER_START + HEREDOC_CONTENT + HEREDOC_MARKER_END
        assertHighlightingData(sh.getHighlightingData().get(11), 10, 6, 10, 8, TypeOfText.STRUCTURED_COMMENT);
        assertHighlightingData(sh.getHighlightingData().get(12), 10, 9, 10, 12, TypeOfText.STRUCTURED_COMMENT);
        assertHighlightingData(sh.getHighlightingData().get(13), 11, 1, 13, 1, TypeOfText.STRUCTURED_COMMENT);
        assertHighlightingData(sh.getHighlightingData().get(14), 13, 1, 13, 4, TypeOfText.STRUCTURED_COMMENT);

        // IF_KEYWORD + EXPR_CONDITIONAL + VARIABLE + COND_OP + WORD + EXPR_CONDITIONAL_END
        assertHighlightingData(sh.getHighlightingData().get(15), 15, 1, 15, 3, TypeOfText.KEYWORD);
        assertHighlightingData(sh.getHighlightingData().get(16), 15, 6, 15, 8, TypeOfText.CONSTANT);
        assertHighlightingData(sh.getHighlightingData().get(17), 15, 9, 15, 12, TypeOfText.KEYWORD_LIGHT);
        // THEN_KEYWORD
        assertHighlightingData(sh.getHighlightingData().get(18), 16, 1, 16, 5, TypeOfText.KEYWORD);
        // WORD + STRING2 + GREATER_THAN + FILEDESCRIPTOR
        assertHighlightingData(sh.getHighlightingData().get(19), 17, 10, 17, 17, TypeOfText.STRING);
        // WORD + INTEGER_LITERAL
        assertHighlightingData(sh.getHighlightingData().get(20), 18, 10, 18, 11, TypeOfText.CONSTANT);
        // FI_KEYWORD
        assertHighlightingData(sh.getHighlightingData().get(21), 19, 1, 19, 3, TypeOfText.KEYWORD);
    }

    private void assertHighlightingData(HighlightingData hd, int startLine, int startColumnIndex, int endLine, int endColumnIndex, TypeOfText typeOfText) {
        assertEquals(startLine, hd.getStartLine());
        assertEquals(startColumnIndex, hd.getStartColumnIndex());
        assertEquals(endLine, hd.getEndLine());
        assertEquals(endColumnIndex, hd.getEndColumnIndex());
        assertEquals(typeOfText, hd.getTypeOfText());
    }
}
