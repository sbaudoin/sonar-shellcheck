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

import java.util.ArrayDeque;

/**
 * Class to store information about the current parsing of strings.
 * In bash strings can be nested. An expression like "$("$("abcd")")" is one string which contains subshell commands.
 * Each subshell command contains a separate string.  To scan this we need a stack of parsing states.
 * This is what this class does.
 *
 * @author jansorg
 * @see <a href="https://github.com/BashSupport/BashSupport/blob/master/src/com/ansorgit/plugins/bash/lang/lexer/StringLexingstate.java">the IntelliJ IDEA Bash Support plugin</a>
 */
public class StringLexingstate {
    private final ArrayDeque<SubshellState> subshells = new ArrayDeque<>(5);

    /**
     * Notifies that we enter the first/topmost/last-added subshell
     */
    void enterString() {
        if (!subshells.isEmpty()) {
            subshells.peek().enterString();
        }
    }

    /**
     * Notifies that we leave the first/topmost/last-added subshell
     */
    void leaveString() {
        if (!subshells.isEmpty()) {
            subshells.peek().leaveString();
        }
    }

    /**
     * Tells if we still have entered the first/topmost/last-added subshell
     *
     * @return {@code true} if the first/topmost/last-added subshell has an entered into counter greater than 0
     */
    boolean isInSubstring() {
        return !subshells.isEmpty() && subshells.peek().isInString();
    }

    /**
     * Tells if we can enter a new subshell or not
     *
     * @return {@code true} if the last-added subshell is not longer in the entered state, {@code false}
     * if the last-added subshell has still an enter counter greater than 0
     */
    boolean isSubstringAllowed() {
        return !subshells.isEmpty() && !subshells.peek().isInString();
    }

    /**
     * Tells if the stack of subshells is empty or not
     *
     * @return {@code true} if the stack is empty, {@code false} if not
     */
    boolean isInSubshell() {
        return !subshells.isEmpty();
    }

    /**
     * Notifies that we enter a new subshell, i.e. pushes a new subshell to the stack
     */
    void enterSubshell() {
        subshells.push(new SubshellState());
    }

    /**
     * Leaves the current (i.e. the first/topmost/last-added subshell) subshell, i.e. removes it from
     * the stack
     */
    void leaveSubshell() {
        assert !subshells.isEmpty();

        subshells.pop();
    }


    /**
     * Internal counter of the number of times a sushell is entered
     */
    private static final class SubshellState {
        private int inString = 0;

        boolean isInString() {
            return inString > 0;
        }

        void enterString() {
            inString++;
        }

        void leaveString() {
            assert inString > 0 : "The inString stack should not be empty";
            inString--;
        }
    }
}
