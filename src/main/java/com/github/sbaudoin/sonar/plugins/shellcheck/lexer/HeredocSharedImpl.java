package com.github.sbaudoin.sonar.plugins.shellcheck.lexer;

import org.apache.commons.lang.StringUtils;

/**
 * Shared code for the Heredoc handling.
 * Based on the work of jansorg with a little bit of cleanup and doc.
 *
 * @author jansorg
 * @see <a href="https://github.com/BashSupport/BashSupport/blob/master/src/com/ansorgit/plugins/bash/lang/util/HeredocSharedImpl.java">the IntelliJ IDEA Bash Support plugin</a>
 */
public class HeredocSharedImpl {
    /**
     * Hide constructor
     */
    private HeredocSharedImpl() {
    }

    /**
     * Returned the marker cleaned out of the meaningless characters suchs as quotes or {@code $}
     *
     * @param marker the marker to be cleaned
     * @param ignoredLeadingTabs {@code true} to ignore possible leading tabs. If {@code false}, the returned
     *        marker may contain those leading tabs.
     * @return a cleaned version of the passed marker
     */
    public static String cleanMarker(String marker, boolean ignoredLeadingTabs) {
        String markerText = trimNewline(marker);
        if (markerText.equals("$")) {
            return markerText;
        }

        TextOffsets offsets = getStartEndOffsets(markerText, ignoredLeadingTabs);
        int start = offsets.start;
        int end = offsets.end;

        return end <= markerText.length() && start < end ? markerText.substring(start, end) : marker;
    }

    /**
     * Tells if the passed string is an evaluating marker. An evaluating marker is a string that
     * does not start with a quote ({@code "} or {@code '}), a backslash or a {@code $}.
     *
     * @param marker a string
     * @return {@code true} if this is an evaluating marker, {@code false} if not
     */
    public static boolean isEvaluatingMarker(String marker) {
        String markerText = trimNewline(marker);

        return !markerText.startsWith("\"") && !markerText.startsWith("'") && !markerText.startsWith("\\") && !markerText.startsWith("$");
    }


    /**
     * Removes the new line character ({@code \n}) at the end on the passed string
     *
     * @param marker a string
     * @return {@code marker} without the new line character located at the end
     */
    private static String trimNewline(String marker) {
        return StringUtils.removeEnd(marker, "\n");
    }

    /**
     * Returns the text offsets (a pair of integer marking the beginning and the end of a portion of text)
     * that mark the true boundaries of the marker: all meaningless characters such as {@code $} or quotes
     * are removed.
     *
     * @param markerText
     * @param ignoredLeadingTabs {@code true} to request that leading tabs are ignored. If not, the text fragment
     *        identified by the offsets may contain the tabs.
     * @return offsets (boundaries) identifying the actual marker
     */
    private static TextOffsets getStartEndOffsets(String markerText, boolean ignoredLeadingTabs) {
        if (markerText.isEmpty()) {
            return new TextOffsets(0, 0);
        }

        if (markerText.length() == 1) {
            return new TextOffsets(0, 1);
        }

        if (markerText.charAt(0) == '\\' && markerText.length() > 1) {
            return new TextOffsets(1, markerText.length());
        }

        int length = markerText.length();
        int start = 0;
        int end = length - 1;

        while (ignoredLeadingTabs && start < (length - 1) && markerText.charAt(start) == '\t') {
            start++;
        }

        if (markerText.charAt(start) == '$' && length > (start + 2) && (markerText.charAt(start + 1) == '"' || markerText.charAt(end) == '\'')) {
            start++;
            length--;
        }

        while (end > 0 && markerText.charAt(end) == '\n') {
            end--;
        }

        if (length > 0 && (markerText.charAt(start) == '\'' || markerText.charAt(start) == '"') && markerText.charAt(end) == markerText.charAt(start)) {
            start++;
            end--;
        }

        return new TextOffsets(start, end + 1);
    }


    /**
     * Tiny class to track pairs of integers marking the start and end of a text section.
     * Replaces the {@code com.intellij.openapi.util.Pair} class in the initial implementation.
     */
    private static class TextOffsets {
        public final int start;
        public final int end;

        public TextOffsets(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }
}
