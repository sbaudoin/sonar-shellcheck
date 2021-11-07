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

import java.util.LinkedList;
import java.util.Objects;

/**
 * Heredoc lexing state used in the lexer
 *
 * @author jansorg
 * @see <a href="https://github.com/BashSupport/BashSupport/blob/master/src/com/ansorgit/plugins/bash/lang/lexer/HeredocLexingState.java">the IntelliJ IDEA Bash Support plugin</a>
 */
public class HeredocLexingState {
    private final LinkedList<HeredocMarkerInfo> expectedHeredocs = new LinkedList<>();

    /**
     * Tells if the state stack is empty or not
     *
     * @return {@code true} if the here-doc lexing state is empty, {@code false} if not
     */
    public boolean isEmpty() {
        return expectedHeredocs.isEmpty();
    }

    /**
     * Tells if the passed marker is the next marker, i.e. has the same name as the marker
     * currently at the top of the stack (i.e. the next marker to be popped)
     *
     * @param markerText a here-doc marker
     * @return {@code true} if the passed marker has the same name as the marker stored at the
     * top of the stack
     */
    boolean isNextMarker(CharSequence markerText) {
        return !expectedHeredocs.isEmpty() && expectedHeredocs.peekFirst().nameEquals(markerText);
    }

    /**
     * Tells if the marker currently at the top of the stack (i.e. the next here-doc marker to be popped)
     * is an evaluating marker
     *
     * @return {@code true} if the first here-doc marker is an evaluating marker, {@code false} if not
     * @throws IllegalStateException if the stack is empty
     */
    boolean isExpectingEvaluatingHeredoc() {
        if (isEmpty()) {
            throw new IllegalStateException("isExpectingEvaluatingHeredoc called on an empty marker stack");
        }

        return !expectedHeredocs.isEmpty() && expectedHeredocs.peekFirst().evaluating;
    }

    /**
     * Tells if the marker currently at the top of the stack (i.e. the next here-doc marker to be popped)
     * was pushed with {@code ignoreTabs} set to {@code true} or not
     *
     * @return {@code true} if the first here-doc marker was pushed with {@code ignoreTabs} set to {@code true}
     * @throws IllegalStateException if the stack is empty
     */
    boolean isIgnoringTabs() {
        if (isEmpty()) {
            throw new IllegalStateException("isIgnoringTabs called on an empty marker stack");
        }

        return !expectedHeredocs.isEmpty() && expectedHeredocs.peekFirst().ignoreLeadingTabs;
    }

    /**
     * Pushes a here-doc marker to the stack
     *
     * @param marker a here-doc marker
     * @param ignoreTabs {@code true} to ignore the leading tabs when determining the marker's name
     */
    void pushMarker(CharSequence marker, boolean ignoreTabs) {
        expectedHeredocs.add(new HeredocMarkerInfo(marker, ignoreTabs));
    }

    /**
     * Pops the first/top here-doc marker from the stack
     *
     * @param marker the marker to be popped
     * @throws IllegalStateException if the passed marker's name is not the same as the name of
     * the first/top marker in the stack
     */
    void popMarker(CharSequence marker) {
        if (!isNextMarker(HeredocSharedImpl.cleanMarker(marker.toString(), false))) {
            throw new IllegalStateException("Heredoc marker isn't expected to be removed: " + marker);
        }

        expectedHeredocs.removeFirst();
    }


    /**
     * Internal class that stores data about here documents
     */
    private static class HeredocMarkerInfo {
        final boolean ignoreLeadingTabs;
        final boolean evaluating;
        final CharSequence markerName;

        HeredocMarkerInfo(CharSequence markerText, boolean ignoreLeadingTabs) {
            String markerTextString = markerText.toString();

            this.markerName = HeredocSharedImpl.cleanMarker(markerTextString, ignoreLeadingTabs);
            this.evaluating = HeredocSharedImpl.isEvaluatingMarker(markerTextString);
            this.ignoreLeadingTabs = ignoreLeadingTabs;
        }

        boolean nameEquals(CharSequence markerText) {
            return this.markerName.equals(HeredocSharedImpl.cleanMarker(markerText.toString(), ignoreLeadingTabs));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            HeredocMarkerInfo that = (HeredocMarkerInfo) o;

            if (ignoreLeadingTabs != that.ignoreLeadingTabs) return false;
            if (evaluating != that.evaluating) return false;
            return Objects.equals(markerName, that.markerName);
        }

        @Override
        public int hashCode() {
            int result = (ignoreLeadingTabs ? 1 : 0);
            result = 31 * result + (evaluating ? 1 : 0);
            result = 31 * result + (markerName != null ? markerName.hashCode() : 0);
            return result;
        }
    }
}
