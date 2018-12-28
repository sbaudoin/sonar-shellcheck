package com.github.sbaudoin.sonar.plugins.shellcheck.lexer;

import java.io.*;
import java.util.ArrayDeque;

/**
 * Abstract class the JFlex Bash lexer will extend to inherit methods required to properly
 * analyze Bash scripts
 *
 * Heavily inspired by <a href="https://github.com/BashSupport/BashSupport/blob/master/src/com/ansorgit/plugins/bash/lang/lexer/_BashLexer.java">IntelliJ IDEA Bash Support plugin</a>
 * @see <a href="https://github.com/BashSupport/BashSupport/blob/master/src/com/ansorgit/plugins/bash/lang/lexer/_BashLexer.java">the IntelliJ IDEA Bash Support plugin</a>
 */
public abstract class AbstractBashLexer {
    private final ArrayDeque<Integer> lastStates = new ArrayDeque(25);
    private boolean inHereString = false;
    private final StringLexingstate string = new StringLexingstate();
    private final HeredocLexingState heredocState = new HeredocLexingState();
    // parameter expansion parsing states
    private boolean paramExpansionHash = false;
    private boolean paramExpansionWord = false;
    private boolean paramExpansionOther = false;
    private int openParenths = 0;
    boolean isBash4 = true;
    //True if the parser is in the case body. Necessary for proper lexing of the IN keyword
    private boolean inCaseBody = false;
    //conditional expressions
    private boolean emptyConditionalCommand = false;

    /**
     * Create and return a Token of given type from start with length
     * offset is added to start
     *
     * @param type the token type
     * @param start the offset at which the token starts
     * @param length the token length
     * @return a {@code Token} built from the passed parameters
     */
    protected Token token(TokenType type, int start, int length) {
        return new Token(type, start, length, yyline(), yycolumn());
    }

    /**
     * Create and return a {@code Token} of given type. The start offset is obtained from
     * {@link AbstractBashLexer#yychar()}, the length from {@link AbstractBashLexer#yylength()},
     * the line number from {@link AbstractBashLexer#yyline()} and the column number from
     * {@link AbstractBashLexer#yycolumn()}.
     *
     * @param type the token type
     * @return a {@code Token} of the given type
     */
    protected Token token(TokenType type) {
        return new Token(type, yychar(), yylength(), yyline(), yycolumn());
    }

    /**
     * This will be called to reset the the lexer.
     * This is created automatically by JFlex.
     *
     * @param reader a reader to the content to be analyzed
     */
    protected abstract void yyreset(Reader reader);

    /**
     * This is called to return the next Token from the input {@link Reader}
     *
     * @return next token, or null if no more tokens.
     * @throws java.io.IOException if the input reader cannot be read
     */
    protected abstract Token yylex() throws java.io.IOException;

    /**
     * Returns the character at position <tt>pos</tt> from the
     * matched text.
     * It is equivalent to {@code yytext().charAt(pos)}, but faster.
     *
     * @param pos the position of the character to fetch.
     *            A value from 0 to {@code yylength()-1}.
     * @return the character at position pos
     */
    protected abstract char yycharat(int pos);

    /**
     * Returns the length of the matched text region.
     * This method is automatically implemented by JFlex lexers.
     *
     * @return the length of the matched text region
     */
    protected abstract int yylength();

    /**
     * Returns the text matched by the current regular expression.
     * This method is automatically implemented by JFlex lexers.
     *
     * @return the text matched by the current regular expression
     */
    protected abstract String yytext();

    /**
     * Returns the char number from beginning of input stream.
     * This is NOT implemented by JFlex, so the code must be
     * added to create this and return the private yychar field.
     *
     * @return the char number from beginning of input stream
     */
    protected abstract int yychar();

    /**
     * Returns the start line number from beginning of input stream (starting from 0).
     * This is NOT implemented by JFlex, so the code must be
     * added to create this and return the private yyline field.
     *
     * @return the start line number from beginning of input stream (starting from 0)
     */
    protected abstract int yyline();

    /**
     * Returns the column number from beginning of the line (starting from 0).
     * This is NOT implemented by JFlex, so the code must be
     * added to create this and return the private yycolumn field
     *
     * @return the column number from beginning of the line (starting from 0)
     */
    protected abstract int yycolumn();

    /**
     * Enters a new lexical state.
     * This method is automatically implemented by JFlex lexers.
     *
     * @param newState the new nexical state
     */
    protected abstract void yybegin(int newState);

    /**
     * Returns the current lexical state
     *
     * @return the current lexical state
     */
    protected abstract int yystate();

    /**
     * Tells if the lexer is currently analyzing a in-here string
     *
     * @return {@code true} if the lexer is analyzing a in-here string, {@code false} if not
     */
    protected boolean isInHereStringContent() {
        return inHereString;
    }

    /**
     * Sets the in-here string state to {@code false}
     */
    protected void leaveHereStringContent() {
        inHereString = false;
    }

    /**
     * Goes back to the previous state of the lexer. If there
     * is no previous state then {@code YYINITIAL}, the initial state, is chosen.
     */
    protected void backToPreviousState() {
        // pop() will throw an exception if empty
        yybegin(lastStates.pop());
    }

    /**
     * Tells if the current lexical state is the passed state (possibly nesting/parent stated)
     *
     * @param state a lexical state to test
     * @return {@code true} if the passed state is in the current lexical state stack, {@code false} if not
     */
    protected boolean isInState(int state) {
        return (state == 0 && lastStates.isEmpty()) || lastStates.contains(state);
    }

    /**
     * Returns the {@code HeredocLexingState} object used by the lexer to deal with here-docs
     *
     * @return the {@code HeredocLexingState} object used by the lexer to deal with here-docs
     */
    protected HeredocLexingState heredocState() {
        return heredocState;
    }

    /**
     * Goes to the given state and stores the previous state on the stack of states.
     * This makes it possible to have several levels of lexing, e.g. for {@code $(( 1+ $(echo 3) ))}.
     *
     * @param newState the new state to go to
     */
    protected void goToState(int newState) {
        lastStates.push(yystate());
        yybegin(newState);
    }

    /**
     * Returns the {@code StringLexingState} object used by the lexer to deal with strings
     *
     * @return the {@code StringLexingState} object used by the lexer to deal with strings
     */
    protected StringLexingstate stringParsingState() {
        return string;
    }

    /**
     * Tells if an empty conditional command ({@code [ ]}) was met or not
     *
     * @return {@code true} if an empty conditional command ({@code [ ]}) was met, {@code false} if not
     */
    protected boolean isEmptyConditionalCommand() {
        return emptyConditionalCommand;
    }

    /**
     * Sets the flag that tracks empty conditional commands
     *
     * @param emptyConditionalCommand the value for the empty conditional command flag
     */
    protected void setEmptyConditionalCommand(boolean emptyConditionalCommand) {
        this.emptyConditionalCommand = emptyConditionalCommand;
    }

    /**
     * Decrements the open parenthesis counter
     */
    protected void decOpenParenthesisCount() {
        openParenths--;
    }

    /**
     * Increments the open parenthesis counter
     */
    protected void incOpenParenthesisCount() {
        openParenths++;
    }

    /**
     * Returns the open parenthesis counter, i.e. the parenthesis nesting level
     *
     * @return the open parenthesis counter
     */
    protected int openParenthesisCount() {
        return openParenths;
    }

    /**
     * Notifies that the lexer is entering a here-string content
     */
    protected void enterHereStringContent() {
        assert !inHereString : "inHereString must be false when entering a here string";

        inHereString = true;
    }

    /**
     * Tells if the lexer is currently considering a parameter expansion based on {@code #} (e.g. <code>${parameter##word}</code>)
     *
     * @return {@code true} if the lexer is currently analyzing parameter expansion with # or not ({@code false})
     */
    protected boolean isParamExpansionHash() {
        return paramExpansionHash;
    }

    /**
     * Sets the flag that tracks parameter expansion based on {@code #}
     *
     * @param paramExpansionHash the new value for the flag
     */
    protected void setParamExpansionHash(boolean paramExpansionHash) {
        this.paramExpansionHash = paramExpansionHash;
    }

    /**
     * Tells if the current lexer token is a parameter expansion word
     *
     * @return {@code true} if the current token is a parameter expansion word, {@code false} if not
     */
    protected boolean isParamExpansionWord() {
        return paramExpansionWord;
    }

    /**
     * Sets the flag that tracks parameter expansion words
     *
     * @param paramExpansionWord the new value for the flag
     */
    protected void setParamExpansionWord(boolean paramExpansionWord) {
        this.paramExpansionWord = paramExpansionWord;
    }

    /**
     * Tells if the current token is a parameter expansion marker other than {@code #} (e.g. {@code %})
     *
     * @return {@code true} if the current token is a parameter expansion other that a word, {@code false} if not
     */
    protected boolean isParamExpansionOther() {
        return paramExpansionOther;
    }

    /**
     * Sets the flag that tracks parameter expansion marker other than {@code #}
     *
     * @param paramExpansionOther the new value for the flag
     */
    protected void setParamExpansionOther(boolean paramExpansionOther) {
        this.paramExpansionOther = paramExpansionOther;
    }

    /**
     * Pops the passed state from the stack of nested states. Child states are also popped out.
     *
     * @param lastStateToPop the state to be popped
     */
    protected void popStates(int lastStateToPop) {
        if (yystate() == lastStateToPop) {
            backToPreviousState();
            return;
        }

        while (isInState(lastStateToPop)) {
            boolean finished = (yystate() == lastStateToPop);
            backToPreviousState();

            if (finished) {
                break;
            }
        }
    }

    /**
     * Tells if the lexer is applying the Bash 4 syntax to analyze the code
     *
     * @return {@code true} if the lexer is usgin the Bash 4 grammar, {@code false} if not
     */
    public boolean isBash4() {
        return isBash4;
    }

    /**
     * Tells if the lexer is currently in a case statement
     *
     * @return {@code true} if the lexer is in a case statement, {@code false} if not
     */
    protected boolean isInCaseBody() {
        return inCaseBody;
    }

    /**
     * Sets the flag that tells if the lexer is in a case statement
     *
     * @param inCaseBody the new value for the flag
     */
    protected void setInCaseBody(boolean inCaseBody) {
        this.inCaseBody = inCaseBody;
    }
}
