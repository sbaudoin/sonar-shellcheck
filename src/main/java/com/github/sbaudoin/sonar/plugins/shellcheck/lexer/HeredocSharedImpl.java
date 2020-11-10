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
     * Returns the start offset of the passed marker text
     *
     * @param markerText a marker text
     * @param ignoredLeadingTabs {@code true} to request that leading tabs are ignored. If not, the text fragment
     *        identified by the offsets may contain the tabs.
     * @return the start offset of the passed marker text
     */
    public static int startMarkerTextOffset(String markerText, boolean ignoredLeadingTabs) {
        return getStartEndOffsets(markerText, ignoredLeadingTabs).start;
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
     * Replaces the marker name in the original marker by the passed new name. The method will ignore the leading
     * tabs in the determination of the original marker boundaries.
     *
     * @param newName a new name for the marker
     * @param originalMarker the original marker text
     * @return the original marker with a new name
     */
    public static String wrapMarker(String newName, String originalMarker) {
        TextOffsets offsets = getStartEndOffsets(originalMarker, true);
        int start = offsets.start;
        int end = offsets.end;

        return (end <= originalMarker.length() && start < end)
                ? originalMarker.substring(0, start) + newName + originalMarker.substring(end)
                : newName;
    }

    /**
     * Returns the text offsets (a pair of integer marking the beginning and the end of a portion of text)
     * that mark the true boundaries of the marker: all meaningless characters such as {@code $} or quotes
     * are removed.
     *
     * @param markerText a marker text
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
