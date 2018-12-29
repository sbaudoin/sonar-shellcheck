/*
 * JFlex specification for Bash
 * Heavily inspired from https://github.com/BashSupport/BashSupport/blob/master/src/com/ansorgit/plugins/bash/lang/lexer/bash.flex
 */
package com.github.sbaudoin.sonar.plugins.shellcheck.lexer;

%%

%public
%abstract
%class BashLexerBase
%extends AbstractBashLexer
%unicode
%char
%line
%column

%type Token

%{setEmptyConditionalCommand
    @Override
    protected int yychar() {
        return yychar;
    }

    @Override
    protected int yyline() {
        return yyline;
    }

    @Override
    protected int yycolumn() {
        return yycolumn;
    }

    /**
     * Closes a {@code here_string} content token if the lexer is currently reading a here string
     */
    protected void closeHereStringIfAvailable() {
        if (yystate() == X_HERE_STRING) {
            if (isInHereStringContent()) {
                leaveHereStringContent();
            }
            backToPreviousState();
        }
    }
%}

/***** Custom user code *****/

InputCharacter = [^\r\n]
LineTerminator = \r\n | \r | \n
LineContinuation = "\\" {LineTerminator}
WhiteSpace=[ \t\f]
WhiteSpaceLineCont=[ \t\f] {LineContinuation}*

Shebang = "#!" {InputCharacter}* {LineTerminator}?
Comment = "#"  {InputCharacter}*

EscapedChar = "\\" [^\n]
StringStart = "$\"" | "\""

SingleCharacter = [^\'] | {EscapedChar}
UnescapedCharacter = [^\']

WordFirst = [\p{Letter}||\p{Digit}||[_/@?.*:%\^+,~-]] | {EscapedChar} | [\u00C0-\u00FF] | {LineContinuation}
WordAfter = {WordFirst} | [#!\[\]]

ArithWordFirst = [a-zA-Z_@?.:] | {EscapedChar} | {LineContinuation}
// No "[" | "]"
ArithWordAfter =  {ArithWordFirst} | [0-9#!]

ParamExpansionWordFirst = [a-zA-Z0-9_] | {EscapedChar} | {LineContinuation}
ParamExpansionWord = {ParamExpansionWordFirst}+

AssignListWordFirst = [[\p{Letter}]||[0-9_/@?.*:&%\^+~,;-]] | {EscapedChar} | {LineContinuation}
AssignListWordAfter =  {AssignListWordFirst} | [$#!]
AssignListWord = {AssignListWordFirst}{AssignListWordAfter}*

Word = {WordFirst}{WordAfter}*
ArithWord = {ArithWordFirst}{ArithWordAfter}*
AssignmentWord = [[\p{Letter}]||[_]] [[\p{Letter}]||[0-9_]]*
Variable = "$" {AssignmentWord} | "$"[@$#0-9?!*_-]

ArithExpr = ({ArithWord} | [0-9a-z+*-] | {Variable} )+

IntegerLiteral = [0] | ([1-9][0-9]*)
HexIntegerLiteral = "0x" [0-9a-fA-F]+
OctalIntegerLiteral = "0" [0-7]+

CaseFirst={EscapedChar} | [^|\"'$)(# \n\r\f\t\f]
CaseAfter={EscapedChar} | [^|\"'$`)( \n\r\f\t\f;]
CasePattern = {CaseFirst} ({LineContinuation}? {CaseAfter})*

Filedescriptor = "&" {IntegerLiteral} | "&-"

/************* STATES ************/
/* If in a conditional expression */
%state S_TEST

/* If in a conditional command  [[  ]]*/
%state S_TEST_COMMAND

/*  If in an arithmetic expression */
%state S_ARITH

/*  If in an arithmetic expression */
%state S_ARITH_SQUARE_MODE

/*  If in an arithmetic expression in an array reference */
%state S_ARITH_ARRAY_MODE

/*  If in a case */
%state S_CASE

/*  If in a case pattern */
%state S_CASE_PATTERN

/*  If in a subshell */
%state S_SUBSHELL

/*  If in the start of a subshell pre expression, i.e. after DOLLAR of $( . The same rules apply as for S_SUBSHELL except that the first ( expression does not open up a new subshell expression
    This is done by switching into the S_SUBSHELL state right after the first LEFT_PAREN token encountered.
*/
%state S_DOLLAR_PREFIXED

/*  If in an array reference, e.g. a[0]=x */
%state S_ARRAY

/*  If in an array list init, e.g. a=(first second) */
%state S_ASSIGNMENT_LIST

/*  If currently a string is parsed */
%xstate X_STRINGMODE

/*  To match tokens in pattern expansion mode ${...} . Needs special parsing of # */
%state S_PARAM_EXPANSION

/*  To match patterns included in parameter expansions */
%state S_PARAM_EXPANSION_PATTERN

/*  To match patterns included in parameter expansions */
%state S_PARAM_EXPANSION_DELIMITER

/*  To match patterns included in parameter expansions */
%state S_PARAM_EXPANSION_REPLACEMENT

/* To match tokens which are in between backquotes. Necessary for nested lexing, e.g. inside of conditional expressions */
%state S_BACKQUOTE

/* To match heredoc documents */
%xstate X_HEREDOC_MARKER
%xstate X_HEREDOC_MARKER_IGNORE_TABS
%xstate X_HEREDOC

/* To match here-strings */
%xstate X_HERE_STRING

%%
/***************************** INITIAL STAATE ************************************/
<YYINITIAL, S_CASE, S_CASE_PATTERN, S_SUBSHELL, S_ASSIGNMENT_LIST> {
  {Shebang}                     { return token(TokenType.SHEBANG); }
  {Comment}                     { return token(TokenType.COMMENT); }
}

<X_HEREDOC_MARKER, X_HEREDOC_MARKER_IGNORE_TABS> {
    {WhiteSpaceLineCont}+        { return token(TokenType.WHITESPACE); }
    {LineContinuation}+          { return token(TokenType.WHITESPACE); }
    {LineTerminator}             { return token(TokenType.LINE_FEED); }

      ("$"? "'" [^\']+ "'")+
    | ("$"? \" [^\"]+ \")+
    | [^ \t\n\r\f;&|]+ {
        heredocState().pushMarker(yytext(), yystate() == X_HEREDOC_MARKER_IGNORE_TABS);
        backToPreviousState();

        return token(TokenType.HEREDOC_MARKER_START);
    }

    .                            { return token(TokenType.BAD_CHARACTER); }
}

<X_HEREDOC> {
    {LineTerminator}+           { if (!heredocState().isEmpty()) {
                                        return token(TokenType.HEREDOC_LINE);
                                  }
                                  return token(TokenType.LINE_FEED);
                                }

    //escaped dollar
    \\ "$" ?                    { return token(TokenType.HEREDOC_LINE); }

    {Variable} {
            if (heredocState().isNextMarker(yytext())) {
                boolean ignoreTabs = heredocState().isIgnoringTabs();

                heredocState().popMarker(yytext());
                popStates(X_HEREDOC);

                return token(ignoreTabs ? TokenType.HEREDOC_MARKER_IGNORING_TABS_END : TokenType.HEREDOC_MARKER_END);
            }

            return token(yystate() == X_HEREDOC && heredocState().isExpectingEvaluatingHeredoc() && !"$".equals(yytext().toString())
                ? TokenType.VARIABLE
                : TokenType.HEREDOC_LINE);
    }

    [^$\n\r\\]+  {
            //support end marker followed by a backtick if nested in a backtick command
            CharSequence markerText = yytext();
            boolean dropLastChar = false;
            if (isInState(S_BACKQUOTE) && yylength() >= 2 && yycharat(yylength()-1) == '`') {
                markerText = markerText.subSequence(0, yylength()-1);
                dropLastChar = true;
            }

            if (heredocState().isNextMarker(markerText)) {
                boolean ignoreTabs = heredocState().isIgnoringTabs();

                heredocState().popMarker(markerText);
                popStates(X_HEREDOC);

                if (dropLastChar) {
                    yypushback(1);
                }

                return token(ignoreTabs ? TokenType.HEREDOC_MARKER_IGNORING_TABS_END : TokenType.HEREDOC_MARKER_END);
            }

            return token(TokenType.HEREDOC_LINE);
    }

    "$"  {
            if (heredocState().isNextMarker(yytext())) {
                boolean ignoreTabs = heredocState().isIgnoringTabs();

                heredocState().popMarker(yytext());
                popStates(X_HEREDOC);

                return token(ignoreTabs ? TokenType.HEREDOC_MARKER_IGNORING_TABS_END : TokenType.HEREDOC_MARKER_END);
         }

         return token(TokenType.HEREDOC_LINE);
     }

    .                            { return token(TokenType.BAD_CHARACTER); }
}

<YYINITIAL, S_CASE, S_SUBSHELL, S_BACKQUOTE> {
  "[ ]"                         { yypushback(1); goToState(S_TEST); setEmptyConditionalCommand(true); return token(TokenType.EXPR_CONDITIONAL); }
  "[ "                          { goToState(S_TEST); setEmptyConditionalCommand(false); return token(TokenType.EXPR_CONDITIONAL); }

  "time"                        { return token(TokenType.TIME_KEYWORD); }

   <S_ARITH, S_ARITH_SQUARE_MODE, S_ARITH_ARRAY_MODE, X_HERE_STRING> {
       "&&"                         { closeHereStringIfAvailable(); return token(TokenType.AND_AND); }

       "||"                         { closeHereStringIfAvailable(); return token(TokenType.OR_OR); }
   }
}

<S_ARRAY> {
    "["     { backToPreviousState(); goToState(S_ARITH_ARRAY_MODE); return token(TokenType.LEFT_SQUARE); }
}

<S_ARITH_ARRAY_MODE> {
    "]" / "=("|"+=("        { backToPreviousState(); goToState(S_ASSIGNMENT_LIST); return token(TokenType.RIGHT_SQUARE); }
    "]"                     { backToPreviousState(); return token(TokenType.RIGHT_SQUARE); }
}

// Parenthesis lexing
<X_STRINGMODE, X_HEREDOC, S_ARITH, S_ARITH_SQUARE_MODE, S_ARITH_ARRAY_MODE, S_CASE, X_HERE_STRING, S_ASSIGNMENT_LIST> {
    "$" / "("               { if (yystate() == X_HEREDOC && !heredocState().isExpectingEvaluatingHeredoc()) return token(TokenType.HEREDOC_LINE); goToState(S_DOLLAR_PREFIXED); return token(TokenType.DOLLAR); }
    "$" / "["               { if (yystate() == X_HEREDOC && !heredocState().isExpectingEvaluatingHeredoc()) return token(TokenType.HEREDOC_LINE); goToState(S_DOLLAR_PREFIXED); return token(TokenType.DOLLAR); }
}

<X_HERE_STRING> {
    {Variable}              { if (!isInHereStringContent()) enterHereStringContent(); return token(TokenType.VARIABLE); }

    "("                     { return token(TokenType.LEFT_PAREN); }
    ")"                     { yypushback(1); backToPreviousState(); }

    "[" | "]" | "{" | "}" |
    {Word}                  { if (!isInHereStringContent()) enterHereStringContent(); return token(TokenType.WORD); }

    {WhiteSpace}            { if (isInHereStringContent()) { leaveHereStringContent(); backToPreviousState(); } return token(TokenType.WHITESPACE); }
}

<YYINITIAL, S_BACKQUOTE, S_DOLLAR_PREFIXED, S_TEST, S_CASE> {
    //this is not lexed in state S_SUBSHELL, because BashSupport treats ((((x)))) as subshell>arithmetic and not as subshell>subshell>arithmetic
    //this is different to the official Bash interpreter
    //currently it's too much effort to rewrite the lexer and parser for this feature
    <S_PARAM_EXPANSION> {
        "((("                   { if (yystate() == S_DOLLAR_PREFIXED) backToPreviousState(); yypushback(2); goToState(S_SUBSHELL); return token(TokenType.LEFT_PAREN); }
    }

    <S_SUBSHELL, S_PARAM_EXPANSION> {
        "(("                { if (yystate() == S_DOLLAR_PREFIXED) backToPreviousState(); goToState(S_ARITH); return token(TokenType.EXPR_ARITH); }
        "("                 { if (yystate() == S_DOLLAR_PREFIXED) backToPreviousState(); stringParsingState().enterSubshell(); goToState(S_SUBSHELL); return token(TokenType.LEFT_PAREN); }
    }

    <S_SUBSHELL> {
        "["                 { if (yystate() == S_DOLLAR_PREFIXED) backToPreviousState(); goToState(S_ARITH_SQUARE_MODE); return token(TokenType.EXPR_ARITH_SQUARE); }
    }
}

<YYINITIAL, S_CASE> {
    ")"                     { return token(TokenType.RIGHT_PAREN); }
}
<S_SUBSHELL> {
    ")"                     { backToPreviousState(); if (stringParsingState().isInSubshell()) stringParsingState().leaveSubshell(); return token(TokenType.RIGHT_PAREN); }
}
<S_CASE_PATTERN> {
    "("                     { return token(TokenType.LEFT_PAREN); }
    ")"                     { backToPreviousState(); return token(TokenType.RIGHT_PAREN); }
}


<S_ARITH, S_ARITH_SQUARE_MODE, S_ARITH_ARRAY_MODE> {
  "))"                          { if (openParenthesisCount() > 0) {
                                    decOpenParenthesisCount();
                                    yypushback(1);

                                    return token(TokenType.RIGHT_PAREN);
                                  } else {
                                    backToPreviousState();

                                    return token(TokenType.EXPR_ARITH_END);
                                  }
                                }

  "("                           { incOpenParenthesisCount(); return token(TokenType.LEFT_PAREN); }
  ")"                           { decOpenParenthesisCount(); return token(TokenType.RIGHT_PAREN); }
}


<YYINITIAL, S_ARITH, S_ARITH_SQUARE_MODE, S_CASE, S_SUBSHELL, S_BACKQUOTE> {
   /* The long followed-by match is necessary to have at least the same length as to global Word rule to make sure this rules matches */
   {AssignmentWord} / "[" {ArithExpr} "]"
                                      { goToState(S_ARRAY); return token(TokenType.ASSIGNMENT_WORD); }

   {AssignmentWord} / "=("|"+=("      { goToState(S_ASSIGNMENT_LIST); return token(TokenType.ASSIGNMENT_WORD); }
   {AssignmentWord} / "="|"+="        { return token(TokenType.ASSIGNMENT_WORD); }
}

<YYINITIAL, S_CASE, S_SUBSHELL, S_BACKQUOTE> {
    <S_ARITH, S_ARITH_SQUARE_MODE> {
       "="                                { return token(TokenType.EQ); }
   }

   "+="                               { return token(TokenType.ADD_EQ); }
}

<S_ASSIGNMENT_LIST> {
  "("                             { return token(TokenType.LEFT_PAREN); }
  ")"                             { backToPreviousState(); return token(TokenType.RIGHT_PAREN); }
  "+="                            { return token(TokenType.ADD_EQ); }
  "="                             { return token(TokenType.EQ); }

 "["                              { goToState(S_ARITH_ARRAY_MODE); return token(TokenType.LEFT_SQUARE); }
  {AssignListWord}                { return token(TokenType.WORD); }
}

<S_ARITH, S_ARITH_SQUARE_MODE, S_ARITH_ARRAY_MODE> {
  ","                             { return token(TokenType.COMMA); }
}

<YYINITIAL, S_CASE, S_SUBSHELL, S_BACKQUOTE> {
/* keywords and expressions */
  "case"                        { setInCaseBody(false); goToState(S_CASE); return token(TokenType.CASE_KEYWORD); }

  "!"                           { return token(TokenType.BANG_TOKEN); }
  "do"                          { return token(TokenType.DO_KEYWORD); }
  "done"                        { return token(TokenType.DONE_KEYWORD); }
  "elif"                        { return token(TokenType.ELIF_KEYWORD); }
  "else"                        { return token(TokenType.ELSE_KEYWORD); }
  "fi"                          { return token(TokenType.FI_KEYWORD); }
  "for"                         { return token(TokenType.FOR_KEYWORD); }
  "function"                    { return token(TokenType.FUNCTION_KEYWORD); }
  "if"                          { return token(TokenType.IF_KEYWORD); }
  "select"                      { return token(TokenType.SELECT_KEYWORD); }
  "then"                        { return token(TokenType.THEN_KEYWORD); }
  "until"                       { return token(TokenType.UNTIL_KEYWORD); }
  "while"                       { return token(TokenType.WHILE_KEYWORD); }
  "[[ "                         { goToState(S_TEST_COMMAND); return token(TokenType.BRACKET_KEYWORD); }
  "trap"                        { return token(TokenType.TRAP_KEYWORD); }
  "let"                         { return token(TokenType.LET_KEYWORD); }
}
/***************** _______ END OF INITIAL STATE _______ **************************/

<S_TEST_COMMAND> {
  " ]]"                         { backToPreviousState(); return token(TokenType.BRACKET_KEYWORD_END); }
  "&&"                          { return token(TokenType.AND_AND); }
  "||"                          { return token(TokenType.OR_OR); }
  "$" / "("                     { goToState(S_DOLLAR_PREFIXED); return token(TokenType.DOLLAR); }
  "("                           { return token(TokenType.LEFT_PAREN); }
  ")"                           { return token(TokenType.RIGHT_PAREN); }
}

<S_TEST> {
  "]"                          { if (isEmptyConditionalCommand()) {
                                    setEmptyConditionalCommand(false);
                                    backToPreviousState();
                                    return token(TokenType.EXPR_CONDITIONAL_END);
                                 } else {
                                    setEmptyConditionalCommand(false);
                                    return token(TokenType.WORD);
                                 }
                               }
  " ]"                         { backToPreviousState(); setEmptyConditionalCommand(false); return token(TokenType.EXPR_CONDITIONAL_END); }
}

<S_TEST, S_TEST_COMMAND> {
  {WhiteSpaceLineCont}         { return token(TokenType.WHITESPACE); }

  /*** Test / conditional expressions ***/

  /* param expansion operators */
  "=="                         { return token(TokenType.COND_OP_EQ_EQ); }

  /* regex operator */
  "=~"                         { return token(TokenType.COND_OP_REGEX); }

  /* misc */
  "!"                          { return token(TokenType.COND_OP_NOT); }
  "-a"                         |
  "-o"                         |
  "-eq"                        |
  "-ne"                        |
  "-lt"                        |
  "-le"                        |
  "-gt"                        |
  "-ge"                        |

  /* string operators */
  "!="                         |
  ">"                          |
  "<"                          |
  "="                          |
  "-n"                         |
  "-z"                         |

  /* conditional operators */
  "-nt"                        |
  "-ot"                        |
  "-ef"                        |
  "-n"                         |
  "-o"                         |
  "-qq"                        |
  "-a"                         |
  "-b"                         |
  "-c"                         |
  "-d"                         |
  "-e"                         |
  "-f"                         |
  "-g"                         |
  "-h"                         |
  "-k"                         |
  "-p"                         |
  "-r"                         |
  "-s"                         |
  "-t"                         |
  "-u"                         |
  "-w"                         |
  "-x"                         |
  "-O"                         |
  "-G"                         |
  "-L"                         |
  "-S"                         |
  "-N"                         { return token(TokenType.COND_OP); }
}

/*** Arithmetic expressions *************/
<S_ARITH> {
    "["                           { return token(TokenType.LEFT_SQUARE); }
    "]"                           { return token(TokenType.RIGHT_SQUARE); }
}

<S_ARITH_SQUARE_MODE> {
  "["                           { return token(TokenType.EXPR_ARITH_SQUARE); }

  "]"                           { backToPreviousState(); return token(TokenType.EXPR_ARITH_SQUARE_END); }
}

<S_ARITH_ARRAY_MODE> {
  "]"                           { backToPreviousState(); return token(TokenType.RIGHT_SQUARE); }
}

<S_ARITH, S_ARITH_SQUARE_MODE, S_ARITH_ARRAY_MODE> {
  {HexIntegerLiteral}           { return token(TokenType.ARITH_HEX_NUMBER); }
  {OctalIntegerLiteral}         { return token(TokenType.ARITH_OCTAL_NUMBER); }
  {IntegerLiteral}              { return token(TokenType.ARITH_NUMBER); }

  ">"                           { return token(TokenType.ARITH_GT); }
  "<"                           { return token(TokenType.ARITH_LT); }
  ">="                          { return token(TokenType.ARITH_GE); }
  "<="                          { return token(TokenType.ARITH_LE); }
  "!="                          { return token(TokenType.ARITH_NE); }

  "<<"                          { return token(TokenType.ARITH_SHIFT_LEFT); }
  ">>"                          { return token(TokenType.ARITH_SHIFT_RIGHT); }

  "*="                          { return token(TokenType.ARITH_ASS_MUL); }
  "/="                          { return token(TokenType.ARITH_ASS_DIV); }
  "%="                          { return token(TokenType.ARITH_ASS_MOD); }
  "+="                          { return token(TokenType.ARITH_ASS_PLUS); }
  "-="                          { return token(TokenType.ARITH_ASS_MINUS); }
  ">>="                         { return token(TokenType.ARITH_ASS_SHIFT_RIGHT); }
  "<<="                         { return token(TokenType.ARITH_ASS_SHIFT_LEFT); }
  "&="                          { return token(TokenType.ARITH_ASS_BIT_AND); }
  "|="                          { return token(TokenType.ARITH_ASS_BIT_OR); }
  "^="                          { return token(TokenType.ARITH_ASS_BIT_XOR); }

  "+"                           { return token(TokenType.ARITH_PLUS); }
  "++"                          { return token(TokenType.ARITH_PLUS_PLUS); }
  "-"                           { return token(TokenType.ARITH_MINUS); }

  "--"/"-"
                                { yypushback(1); return token(TokenType.ARITH_MINUS); }

  "--"/{WhiteSpace}+"-"
                                { yypushback(1); return token(TokenType.ARITH_MINUS); }

  "--"/({HexIntegerLiteral}|{OctalIntegerLiteral}|{IntegerLiteral})
                                { yypushback(1); return token(TokenType.ARITH_MINUS); }

  "--"/{WhiteSpace}+({HexIntegerLiteral}|{OctalIntegerLiteral}|{IntegerLiteral})
                                { yypushback(1); return token(TokenType.ARITH_MINUS); }

  "--"                          { return token(TokenType.ARITH_MINUS_MINUS); }
  "=="                          { return token(TokenType.ARITH_EQ); }

  "**"                          { return token(TokenType.ARITH_EXPONENT); }
  "*"                           { return token(TokenType.ARITH_MULT); }
  "/"                           { return token(TokenType.ARITH_DIV); }
  "%"                           { return token(TokenType.ARITH_MOD); }
  "<<"                          { return token(TokenType.ARITH_SHIFT_LEFT); }

  "!"                           { return token(TokenType.ARITH_NEGATE); }

  "&"                           { return token(TokenType.ARITH_BITWISE_AND); }
  "~"                           { return token(TokenType.ARITH_BITWISE_NEGATE); }
  "^"                           { return token(TokenType.ARITH_BITWISE_XOR); }

  "?"                           { return token(TokenType.ARITH_QMARK); }
  ":"                           { return token(TokenType.ARITH_COLON); }

  "#"                           { return token(TokenType.ARITH_BASE_CHAR); }

  {AssignmentWord} / "["
                                { goToState(S_ARRAY); return token(TokenType.ASSIGNMENT_WORD); }

  {ArithWord}                   { return token(TokenType.WORD); }
}

<S_CASE> {
  "esac"                       { backToPreviousState(); return token(TokenType.ESAC_KEYWORD); }

  ";&"                         { goToState(S_CASE_PATTERN);
                                 if (isBash4()) {
                                    return token(TokenType.CASE_END);
                                 }
                                 else {
                                    yypushback(1);
                                    return token(TokenType.SEMI);
                                 }
                               }

  ";;&"                        { goToState(S_CASE_PATTERN);
                                 if (!isBash4()) {
                                    yypushback(1);
                                 }
                                 return token(TokenType.CASE_END);
                               }

  ";;"                         { goToState(S_CASE_PATTERN); return token(TokenType.CASE_END); }
  "in"                         { if (!isInCaseBody()) { setInCaseBody(true); goToState(S_CASE_PATTERN); }; return token(TokenType.WORD); }
}

<S_CASE_PATTERN> {
  "esac"                        { backToPreviousState(); yypushback(yylength()); }
}

//////////////////// END OF STATE TEST_EXPR /////////////////////

/* string literals */
<X_STRINGMODE> {
  \"                            { if (!stringParsingState().isInSubstring() && stringParsingState().isSubstringAllowed()) {
                                    stringParsingState().enterString();
                                    goToState(X_STRINGMODE);
                                    return token(TokenType.STRING_BEGIN);
                                  }

                                  stringParsingState().leaveString();
                                  backToPreviousState();
                                  return token(TokenType.STRING_END);
                                }

  /* Backquote expression inside of evaluated strings */
  `                           { if (yystate() == S_BACKQUOTE) {
                                    backToPreviousState();
                                }
                                else {
                                    goToState(S_BACKQUOTE);
                                }
                                return token(TokenType.BACKQUOTE); }

  {EscapedChar}               { return token(TokenType.STRING_DATA); }
  [^\"]                       { return token(TokenType.STRING_DATA); }
}

<YYINITIAL, S_BACKQUOTE, S_SUBSHELL, S_CASE> {
  /* Bash 4 */
    "&>>"                         { if (isBash4()) {
                                        return token(TokenType.REDIRECT_AMP_GREATER_GREATER);
                                    } else {
                                        yypushback(2);
                                        return token(TokenType.AMP);
                                    }
                                  }

    "&>"                          { if (isBash4()) {
                                        return token(TokenType.REDIRECT_AMP_GREATER);
                                    } else {
                                        yypushback(1);
                                        return token(TokenType.AMP);
                                    }
                                  }

  /* Bash v3 */
  "<<<"                         { goToState(X_HERE_STRING); return token(TokenType.REDIRECT_HERE_STRING); }
  "<>"                          { return token(TokenType.REDIRECT_LESS_GREATER); }

  "<&" / {ArithWord}            { return token(TokenType.REDIRECT_LESS_AMP); }
  ">&" / {ArithWord}            { return token(TokenType.REDIRECT_GREATER_AMP); }
  "<&" / {WhiteSpaceLineCont}   { return token(TokenType.REDIRECT_LESS_AMP); }
  ">&" / {WhiteSpaceLineCont}   { return token(TokenType.REDIRECT_GREATER_AMP); }

  ">|"                          { return token(TokenType.REDIRECT_GREATER_BAR); }

  {Filedescriptor}              { return token(TokenType.FILEDESCRIPTOR); }
}

<S_PARAM_EXPANSION> {
  "!"                           { return token(TokenType.PARAM_EXPANSION_OP_EXCL); }
  ":="                          { return token(TokenType.PARAM_EXPANSION_OP_COLON_EQ); }
  "="                           { return token(TokenType.PARAM_EXPANSION_OP_EQ); }

  ":-"                          { return token(TokenType.PARAM_EXPANSION_OP_COLON_MINUS); }
  "-"                           { return token(TokenType.PARAM_EXPANSION_OP_MINUS); }

  ":+"                          { return token(TokenType.PARAM_EXPANSION_OP_COLON_PLUS); }
  "+"                           { return token(TokenType.PARAM_EXPANSION_OP_PLUS); }

  ":?"                          { return token(TokenType.PARAM_EXPANSION_OP_COLON_QMARK); }

  ":"                           { return token(TokenType.PARAM_EXPANSION_OP_COLON); }

  "//"                          { goToState(S_PARAM_EXPANSION_PATTERN); return token(TokenType.PARAM_EXPANSION_OP_SLASH_SLASH); }
  "/"                           { goToState(S_PARAM_EXPANSION_PATTERN); return token(TokenType.PARAM_EXPANSION_OP_SLASH);  }

  "##"                          { setParamExpansionHash(isParamExpansionWord()); return token(TokenType.PARAM_EXPANSION_OP_HASH_HASH); }
  "#"                           { setParamExpansionHash(isParamExpansionWord()); return token(TokenType.PARAM_EXPANSION_OP_HASH); }
  "@"                           { return token(TokenType.PARAM_EXPANSION_OP_AT); }
  "*"                           { return token(TokenType.PARAM_EXPANSION_OP_STAR); }
  "%"                           { setParamExpansionOther(true); return token(TokenType.PARAM_EXPANSION_OP_PERCENT); }
  "?"                           { setParamExpansionOther(true); return token(TokenType.PARAM_EXPANSION_OP_QMARK); }
  "."                           { setParamExpansionOther(true); return token(TokenType.PARAM_EXPANSION_OP_DOT); }
  "^"                           { setParamExpansionOther(true); return token(TokenType.PARAM_EXPANSION_OP_UPPERCASE_FIRST); }
  "^^"                          { setParamExpansionOther(true); return token(TokenType.PARAM_EXPANSION_OP_UPPERCASE_ALL); }
  ","                           { setParamExpansionOther(true); return token(TokenType.PARAM_EXPANSION_OP_LOWERCASE_FIRST); }
  ",,"                          { setParamExpansionOther(true); return token(TokenType.PARAM_EXPANSION_OP_LOWERCASE_ALL); }

  "[" / [@*]                    { return token(TokenType.LEFT_SQUARE); }
  "["                           { if (!isParamExpansionOther() && (!isParamExpansionWord() || !isParamExpansionHash())) {
                                    // If we expect an array reference parse the next tokens as arithmetic expression
                                    goToState(S_ARITH_ARRAY_MODE);
                                  }

                                  return token(TokenType.LEFT_SQUARE);
                                }

  "]"                           { return token(TokenType.RIGHT_SQUARE); }

  "{"                           { setParamExpansionWord(false); setParamExpansionHash(false); setParamExpansionOther(false);
                                  return token(TokenType.LEFT_CURLY);
                                }
  "}"                           { setParamExpansionWord(false); setParamExpansionHash(false); setParamExpansionOther(false);
                                  backToPreviousState();
                                  closeHereStringIfAvailable();
                                  return token(TokenType.RIGHT_CURLY);
                                }

  {EscapedChar}                 { setParamExpansionWord(true); return token(TokenType.WORD); }
  {IntegerLiteral}              { setParamExpansionWord(true); return token(TokenType.WORD); }
  {ParamExpansionWord}          { setParamExpansionWord(true); return token(TokenType.WORD); }
}

<S_PARAM_EXPANSION_PATTERN> {
  // pattern followed by the delimiter
  ({EscapedChar} | {LineTerminator} | [^/}])+ / "/" { backToPreviousState(); goToState(S_PARAM_EXPANSION_DELIMITER); return token(TokenType.PARAM_EXPANSION_PATTERN); }

  //no delimiter and no replacement
  ({EscapedChar} | {LineTerminator} | [^/}])+     { backToPreviousState(); return token(TokenType.PARAM_EXPANSION_PATTERN); }

  //empty pattern
  .                           { yypushback(1); backToPreviousState(); }
}

// matches just the delimiter and then changes into the replacement state
<S_PARAM_EXPANSION_DELIMITER> {
    //with replacement
    "/"                         { backToPreviousState(); goToState(S_PARAM_EXPANSION_REPLACEMENT); return token(TokenType.PARAM_EXPANSION_OP_SLASH); }

    //no replacement
    "}"                         { yypushback(1); backToPreviousState(); }
}

<S_PARAM_EXPANSION_REPLACEMENT> {
    [^}]+                       { backToPreviousState(); return token(TokenType.WORD); }

    //probably an empty replacement
    .                           { yypushback(1); backToPreviousState(); }
}

/** Match in all except of string */
<YYINITIAL, S_ARITH, S_ARITH_SQUARE_MODE, S_ARITH_ARRAY_MODE, S_CASE, S_CASE_PATTERN, S_SUBSHELL, S_ASSIGNMENT_LIST, S_PARAM_EXPANSION, S_BACKQUOTE, X_STRINGMODE> {
    /*
     Do NOT match for Whitespace+ , we have some whitespace sensitive tokens like " ]]" which won't match
     if we match repeated whitespace!
    */
    {WhiteSpace}                 { return token(TokenType.WHITESPACE); }
    {LineContinuation}+          { return token(TokenType.LINE_CONTINUATION); }
}

<YYINITIAL, S_TEST, S_TEST_COMMAND, S_ARITH, S_ARITH_SQUARE_MODE, S_ARITH_ARRAY_MODE, S_CASE, S_CASE_PATTERN, S_SUBSHELL, S_ASSIGNMENT_LIST, S_PARAM_EXPANSION, S_BACKQUOTE> {
    <X_HERE_STRING> {
        {StringStart}                 { stringParsingState().enterString(); if (yystate() == X_HERE_STRING && !isInHereStringContent()) enterHereStringContent();
goToState(X_STRINGMODE); return token(TokenType.STRING_BEGIN); }

        "$"\'{SingleCharacter}*\'     |
        \'{UnescapedCharacter}*\'        { if (yystate() == X_HERE_STRING && !isInHereStringContent()) enterHereStringContent(); return token(TokenType.STRING2); }

    /* Single line feeds are required to properly parse heredocs */
        {LineTerminator}             {
                                            if (yystate() == X_HERE_STRING) {
                                                closeHereStringIfAvailable();
                                                return token(TokenType.LINE_FEED);
                                            } else if ((yystate() == S_PARAM_EXPANSION || yystate() == S_SUBSHELL || yystate() == S_ARITH || yystate() == S_ARITH_SQUARE_MODE) && isInState(X_HEREDOC)) {
                                                backToPreviousState();
                                                return token(TokenType.LINE_FEED);
                                            }

                                            if (!heredocState().isEmpty()) {
                                                // first linebreak after the start marker
                                                goToState(X_HEREDOC);
                                                return token(TokenType.LINE_FEED);
                                            }

                                           return token(TokenType.LINE_FEED);
                                     }

        /* Backquote expression */
        `                             { if (yystate() == S_BACKQUOTE) backToPreviousState(); else goToState(S_BACKQUOTE); return token(TokenType.BACKQUOTE); }
    }


  /* Bash reserved keywords */
    "{"                           { return token(TokenType.LEFT_CURLY); }

    "|&"                          { if (isBash4()) {
                                        return token(TokenType.PIPE_AMP);
                                     } else {
                                        yypushback(1);
                                        return token(TokenType.PIPE);
                                     }
                                  }
    "|"                           { return token(TokenType.PIPE); }

  /** Misc expressions */
    "@"                           { return token(TokenType.AT); }
    "$"                           { return token(TokenType.DOLLAR); }
    <X_HERE_STRING> {
        "&"                           { closeHereStringIfAvailable(); return token(TokenType.AMP); }
        ";"                           { closeHereStringIfAvailable(); return token(TokenType.SEMI); }
    }
    "<<-" {
        goToState(X_HEREDOC_MARKER_IGNORE_TABS);
        return token(TokenType.HEREDOC_MARKER_TAG);
    }
    "<<" {
        goToState(X_HEREDOC_MARKER);
        return token(TokenType.HEREDOC_MARKER_TAG);
    }
    ">"                           { return token(TokenType.GREATER_THAN); }
    "<"                           { return token(TokenType.LESS_THAN); }
    ">>"                          { return token(TokenType.SHIFT_RIGHT); }

    <X_STRINGMODE> {
        {Variable}                { return token(TokenType.VARIABLE); }
    }

    "$["                          { yypushback(1); goToState(S_ARITH_SQUARE_MODE); return token(TokenType.DOLLAR); }

    "\\"                          { return token(TokenType.BACKSLASH); }
}

<YYINITIAL, X_HEREDOC, S_PARAM_EXPANSION, S_TEST, S_TEST_COMMAND, S_CASE, S_CASE_PATTERN, S_SUBSHELL, S_ARITH, S_ARITH_SQUARE_MODE, S_ARITH_ARRAY_MODE, S_ARRAY, S_ASSIGNMENT_LIST, S_BACKQUOTE, X_STRINGMODE, X_HERE_STRING> {
    "${"                        { if (yystate() == X_HEREDOC && !heredocState().isExpectingEvaluatingHeredoc()) return token(TokenType.HEREDOC_LINE); goToState(S_PARAM_EXPANSION); yypushback(1); return token(TokenType.DOLLAR); }
    "}"                         { if (yystate() == X_HEREDOC && !heredocState().isExpectingEvaluatingHeredoc()) return token(TokenType.HEREDOC_LINE); return token(TokenType.RIGHT_CURLY); }
}

<S_CASE_PATTERN> {
  {CasePattern}                 { return token(TokenType.WORD); }
}

<YYINITIAL, S_CASE, S_SUBSHELL, S_BACKQUOTE, S_ARRAY> {
    {IntegerLiteral}            { return token(TokenType.INTEGER_LITERAL); }
}

<YYINITIAL, S_CASE, S_TEST, S_TEST_COMMAND, S_SUBSHELL, S_BACKQUOTE> {
  {Word}                       { return token(TokenType.WORD); }
  {WordAfter}+                 { return token(TokenType.WORD); }
}

/** END */

//all x-states
<X_HERE_STRING, X_HEREDOC, X_HEREDOC_MARKER, X_HEREDOC_MARKER_IGNORE_TABS, X_STRINGMODE>{
    [^]                        { return token(TokenType.BAD_CHARACTER); }
}

[^]                            { return token(TokenType.BAD_CHARACTER); }

