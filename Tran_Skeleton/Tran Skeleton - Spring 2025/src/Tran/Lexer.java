package Tran;
import java.util.LinkedList;
import java.util.List;

public class Lexer {
    //Check which punctuation you will need
    private final char[] punctuationFirstChars = {  '=', '(', ')', ':', '.',
                                                    '+', '-', '*', '/', '%', ',',
                                                    '!', '<', '>', '\"', '\''};
    private final TextManager txtManager;
    private int currentLine;
    private int currentCol;
    private int indentLevel;
    private int openEndedParenthesis;
    private int[] lastInitialOpenParenthesis;
    LinkedList<Token> tokenList;

    /**
     * Creates a Lexer object with a freshly initialized TextManager field containing the input
     * This Lexer can only lex the current textManager once. If it needs to lex again, it will need
     * to reinitialize or adjust the textManager
     * @param input - the String to be passed into the TextManager
     */
    public Lexer(String input) {
        txtManager = new TextManager(input);
    }

    /**
     * parses through the text value stored in txtManager and turns it into a LinkedList of Tokens.
     * Tokens are given three or four fields depending on the first field, type. The second and third
     * field are the line numbers where the tokens where found (for the first char of the token), as
     * well as the column number for the token. Finally, some tokens, such as WORD and NUMBER contain
     * a value field formatted as a String.
     * @return - A LinkedList of Tokens, formatted as described above
     * @throws SyntaxErrorException - in the event that an unanticipated character is parsed from txtManager
     */
    public List<Token> Lex() throws SyntaxErrorException {
	    tokenList = new LinkedList<Token>();
        currentLine = 1;
        currentCol = 1;
        indentLevel = 0;
        openEndedParenthesis = 0;
        lastInitialOpenParenthesis = new int[]{0, 0};

        while(!txtManager.isAtEnd()) {
            char currentChar = peekCharacter();
            if (Character.isLetter(currentChar)) {          //letters
                tokenList.add(readWord());
            }else if('\n' == currentChar) {                 //newLine
                tokenList.add(newLine());
                adjustIndent();
            } else if('{' == currentChar) {                 //comments
                comment();
            } else if (Character.isDigit(currentChar)) {    //numbers
                tokenList.add(readNumber());
            } else if (isPunctuation(currentChar)) {        //punctuation
                tokenList.add(readPunctuation());
            } else if(Character.isWhitespace(currentChar)){ //whiteSpaces
                    midlineWhitespace();
            } else {                                        //unknown character
                throw new SyntaxErrorException("Unrecognized character: " + currentChar,currentLine,currentCol);
            }
        }

        //in the event that the algorithm ends with an indented statement
        while(indentLevel > 0) {
            tokenList.add(new Token(Token.TokenTypes.DEDENT,currentLine,currentCol));
            indentLevel--;
        }

        //algorithm ended with an open parenthesis
        if(openEndedParenthesis != 0){
            throw new SyntaxErrorException("Not as many Left Parenthesis as Right Parenthesis",currentLine,currentCol);
        }
        return tokenList;
    }

    /**
     * Checks that the initial char passed at position in txtManager is a letter.
     * Parses through txtManager until the next char is not a letter or a digit and passes it into the
     * parsed String.
     * The characters parsed are organized in the order they were given.
     * Increments txtManager and currentCol by however many chars were parsed.
     * Queries the type of token to initialize based on the value of parsed
     * @return - a token of a type dependent on the value of parsed.
     *      The rowNumber is equivalent to the field currentRow at the time of initialization
     *      The columnNumber is equivalent to the initialColIndex
     *      In the case that the type is WORD, the token contains a String value equal to the value parsed.
     */
    public Token readWord() throws SyntaxErrorException {
        //initial check that the character indexed at position in txtManager is a letter char
        if(!Character.isLetter(peekCharacter()))
            throw new SyntaxErrorException("Expected a letter char. Instead received: " + peekCharacter(), currentLine, currentCol);

        //mini-fields
        int initialColIndex = currentCol;
        String parsed = "";
        char currentChar = peekCharacter();


        //parses word (includes letters and digits(after first char))
        while(Character.isLetter(currentChar) || Character.isDigit(currentChar)) {
            parsed = parsed.concat((""+incrementOneChar()));
            currentChar = peekCharacter();  //next char to check
        }

        //checks if parsed is a keyword
        Token.TokenTypes type = typeFromLetters(parsed);
        if((""+type).equals("WORD"))//parsed is not a keyword
            return new Token(Token.TokenTypes.WORD, currentLine, initialColIndex, parsed);
        else    //parsed is a keyword
            return new Token(type, currentLine, initialColIndex);
    }

    /**
     * checks that the initial char at position in txtManager is either a digit or '.'.
     * parses through txtManager until the next char is not a digit or a '.' and passes it into the
     * value String. Only 1 '.' is read into the value String.
     * The characters parsed are organized in the order they were given.
     * Increments txtManager and currentCol by however many chars were parsed.
     * @return - a token of type NUMBER
     *          The rowNumber is equivalent to the field currentRow at the time of initialization
     *          The columnNumber is  equivalent to the initialColIndex
     *          The value is equal to value, a parsed String from txtManager
     */
    public Token readNumber() throws SyntaxErrorException {
        //TODO debug

        //mini-fields
        char currentChar = peekCharacter();
        int decimalCounter = 0;
        int initialColIndex = currentCol;
        String value = "";

        //initial check that the character indexed at position in txtManager is a digit char or '.' followed by a digit
        if(!(Character.isDigit(currentChar) || ('.' == currentChar && Character.isDigit(peekCharacter(1)))))
            throw new SyntaxErrorException("A non-digit or decimal value was passed to readNumber()", currentLine, currentCol);

        /*parses txtManager until position is not a digit or '.' OR position is a '.' and there is already
         a decimal (decimalCounter > 0). */
        while(Character.isDigit(currentChar) || ('.' == currentChar && decimalCounter < 1)){
            //increments txtManager and appends the current char onto the end of value
            value = value.concat(("" + incrementOneChar()));
            if(currentChar == '.')
                decimalCounter++;
            currentChar = peekCharacter();
        }

        return new Token(Token.TokenTypes.NUMBER, currentLine, initialColIndex, value);
    }

    /**
     * Checks that the char at position in txtManager has been verified as a punctuation mark.
     * finds the relative punctuation mark, and in some cases, checks the next character if it is
     * relevant to the punctuation as some are two chars long.
     * Adjusts currentCol accordingly.
     * @return - A Token of a type dependent on the String parsed from txtManager
     *      The rowNumber is equivalent to the field currentRow at the time of initialization
     *      The columnNumber is  equivalent to the initialColIndex
     *      The type can sometimes be a NUMBER token, which holds a value equivalent to what's parsed
     * @throws SyntaxErrorException -  in the event that the nextChar in txtManager is not a punctuation char as recognized by
     *                      the tran language
     */
    public Token readPunctuation() throws SyntaxErrorException {
        //checks input
        if(!isPunctuation(peekCharacter()))
            throw new SyntaxErrorException("expected punctuation", currentLine,currentCol);

        int initialColNumber = currentCol;
        char firstChar = peekCharacter();

        //in cases where presumed punctuation is actually a NUMBER token
        if(firstChar == '.' && Character.isDigit(peekCharacter(1))){
            return readNumber();
        }

        //first char is punctuation and Not a NUMBER, incrementing txtManager position and currentCol
        incrementOneChar();

        Token token;
        switch (firstChar) {
            case '=':
                if (peekCharacter() == '=') {         //EQUAL, next char is '='
                    incrementOneChar();
                    token = new Token(Token.TokenTypes.EQUAL, currentLine, initialColNumber);
                } else {                            //ASSIGN, next char is not '='
                    token = new Token(Token.TokenTypes.ASSIGN, currentLine, initialColNumber);
                }
                break;
            case '(':                               //LPAREN
                //'(' must be followed by a ')' somewhere in the same line (except with0 quoted strings), also track the level of encapsulation, IOW numberOf'(' - numberOf')'
                if (openEndedParenthesis == 0) {  //tracks where the earliest encapsulating open parenthesis will be in the event of an error
                    lastInitialOpenParenthesis[0] = currentLine;
                    lastInitialOpenParenthesis[1] = currentCol;
                }

                openEndedParenthesis++;
                token = new Token(Token.TokenTypes.LPAREN, currentLine, initialColNumber);
                break;
            case ')':                               //RPAREN
                if (openEndedParenthesis < 1) {
                    throw new SyntaxErrorException("an end parenthesis was used without a starting parenthesis", lastInitialOpenParenthesis[0], lastInitialOpenParenthesis[1]);
                }

                openEndedParenthesis--;
                token = new Token(Token.TokenTypes.RPAREN, currentLine, initialColNumber);
                break;
            case ':':                               //COLON
                token = new Token(Token.TokenTypes.COLON, currentLine, initialColNumber);
                break;
            case '.':                               //DOT
                //number already checked above
                token = new Token(Token.TokenTypes.DOT, currentLine, initialColNumber);
                break;
            case '+':                               //PLUS
                token = new Token(Token.TokenTypes.PLUS, currentLine, initialColNumber);
                break;
            case '-':                               //MINUS
                token = new Token(Token.TokenTypes.MINUS, currentLine, initialColNumber);
                break;
            case '*':                               //TIMES
                token = new Token(Token.TokenTypes.TIMES, currentLine, initialColNumber);
                break;
            case '/':                               //DIVIDE
                token = new Token(Token.TokenTypes.DIVIDE, currentLine, initialColNumber);
                break;
            case '%':                               //MODULO
                token = new Token(Token.TokenTypes.MODULO, currentLine, initialColNumber);
                break;
            case ',':                               //COMMA
                token = new Token(Token.TokenTypes.COMMA, currentLine, initialColNumber);
                break;
            case '!':                               //NOTEQUAL
                if (peekCharacter() != '=') { //ensures that the next character is '='
                    throw new SyntaxErrorException("Expected '='. '!' is only valid when next to a '=' or in a comment / string", currentLine, initialColNumber + 1);
                }

                incrementOneChar();
                token = new Token(Token.TokenTypes.NOTEQUAL, currentLine, initialColNumber);
                break;
            case '<':
                if(peekCharacter() == '='){         //LESSTHANEQUAL, next char is '='
                    incrementOneChar();
                    token = new Token(Token.TokenTypes.LESSTHANEQUAL, currentLine, initialColNumber);
                }else{                              //LESSTHAN, next char is not '='
                    token = new Token(Token.TokenTypes.LESSTHAN, currentLine, initialColNumber);
                }
                break;
            case '>':
                if(peekCharacter() == '=') {         //GREATERTHANEQUAL, next char is '='
                    incrementOneChar();
                    token = new Token(Token.TokenTypes.GREATERTHANEQUAL, currentLine, initialColNumber);
                } else {                            //GREATERTHAN, next char is not '='
                    token = new Token(Token.TokenTypes.GREATERTHAN, currentLine, initialColNumber);
                }
                break;
            case '\"':                               //QUOTEDSTRING
                token = quotedString();
                break;
            case '\'':                              //QUOTEDCHARACTER
                token = quotedCharacter();
                break;
            default:
                throw new SyntaxErrorException("unexpected character counted as punctuation but wasn't identified:"
                        + firstChar, currentLine, initialColNumber);
        }
        return token;
    }

    /**
     * Assumes the white space(s) past and including the chars indexed at position in txtManager do
     * not follow directly after a new line and therefor do not affect indentation and dedentation.
     * Increments the current column number (currentCol) and the position in txtManager. Does NOT
     * accept newline characters '\n' as a whitespace
     * @throws SyntaxErrorException - in the event that another character is attempted to be grabbed past the end of the input
     */
    private void midlineWhitespace() throws SyntaxErrorException {
        char character = peekCharacter();
        while(Character.isWhitespace(character) && (character != '\n')) {
            incrementOneChar();
            character = peekCharacter();
        }
    }

    /**
     * Checks that txtManager is actually at a newLine, then:
     *      creates a NEWLINE Token
     *      adjusts currentCol to 0 and increments currentLine
     * @return a NEWLINE Token
     * @throws SyntaxErrorException - in the event that txtManager is not at a newline char when this function initializes
     */
    private Token newLine() throws SyntaxErrorException {
        //checks it is a newLine
        char currentChar = peekCharacter();
        if (currentChar != '\n') {
            throw new SyntaxErrorException("expected a newline", currentLine, currentCol);
        }

        //adds a newline token, increment txtManager's position, increments currentLine, and sets currentCol to 1
        currentCol = 1;
        Token currentToken = new Token(Token.TokenTypes.NEWLINE, ++currentLine,currentCol);
        txtManager.getCharacter();

        return currentToken;
    }

    /**
     * Assumes (and checks) that the position in txtManager is one directly following a single quote character that was just popped
     * Performs the following checks:
     *       the character preceding the current position in txtManager is a single quote char
     *       the character after the current position in txtManager is a single quote char
     * Then, makes a token of Type QUOTEDCHARACTER at position currentLine, currentCol - 1, with a value found for txtManager.getCharacter()
     * Increments twice for the char value characters parsed, including the value char and the second single quote char.
     * @return - A QUOTEDCHARACTER Token
     * @throws SyntaxErrorException - if the character preceding the current position in txtManager is not a single quote char
 *                                  - if the character after the current position in txtManager is not a single quote char
     */
    private Token quotedCharacter() throws SyntaxErrorException {
        int initialCol = currentCol-1;
        Token token;

        //checks that we were actually passed txtManager after a ' char was popped
        if(peekCharacter(-1) != '\'') throw new SyntaxErrorException("quoted character expected", currentLine, initialCol);
        //char after next must be '
        if(peekCharacter(1) !='\'') throw new SyntaxErrorException("quoted character expected", currentLine, currentCol+1);

        token = new Token(Token.TokenTypes.QUOTEDCHARACTER, currentLine, initialCol, ""+incrementOneChar());
        incrementOneChar();
        return token;
    }

    /**
     * checks that the character preceding position in txtManager is a double quote char
     * iterates through txtManager until it finds a double quote char. Keeps track of currentLine and currentCol.
     * Does NOT adjust indent level, only calculates it to iterate newline whitespaces and calculate currentCol.
     * @return Token of type QUOTEDSTRING
     * @throws SyntaxErrorException - thrown when the character preceding position in txtManager is not a double quote char
     */
    private Token quotedString() throws SyntaxErrorException {
        int initialCol = currentCol-1;
        String value = "";
        char currentChar = peekCharacter();
        Token token;

        //checks that we were actually passed txtManager after a " char was popped
        if(peekCharacter(-1) != '\"') throw new SyntaxErrorException("quoted character expected", currentLine, initialCol);

        //increments through txtManager until a " char is found
        while(currentChar != '\"') {
            //adjusts currentCol and currentLine accordingly.
            if(currentChar != '\n'){    //TODO check if explicit newline chars are noted
                value = value.concat("" + incrementOneChar());
            } else{
                currentLine++;
                calculateIndent();
            }

            //special case that throws an exception when it doesn't find its pair
            if(txtManager.isAtEnd()){
                throw new SyntaxErrorException("quoted string expected", currentLine, initialCol);
            }
            currentChar = peekCharacter();
        }

        incrementOneChar();
        token = new Token(Token.TokenTypes.QUOTEDSTRING, currentLine, initialCol, value);
        return token;
    }

    /**
     * This method runs the lexar comment through comments with three functions in the following order
     *      checks that it is actually a comment
     *      increments until it finds the char '}' keeping track of line and column
     *      ensures there is no code on a line following the end of a comment
     * @throws SyntaxErrorException - in the event that txtManager is not at a comment char when this function initializes
     *                              - in the event that an open-ended comment is found
     */
    private void comment() throws SyntaxErrorException {
        //checks that it is actually a comment
        char currentChar = peekCharacter();
        if(currentChar != '{')
            throw new SyntaxErrorException("expected '{'", currentLine, currentCol);

        //increments until it finds the char '}' keeping track of line and column
        while(currentChar != '}') {
            currentChar = txtManager.getCharacter();
            //we don't have to create tokens for newlines WITHIN comments since they do not affect the program's logic
            if(currentChar == '\n'){
                currentLine++;
                currentCol = 1;
            } else {
                currentCol++;
            }
            currentChar = peekCharacter();

            //comment never ended
            if(txtManager.isAtEnd()){
                throw new SyntaxErrorException("File ended in the middle of a comment", currentLine, currentCol);
            }
        }

        //toss the end char of the comment and query the next char
        txtManager.getCharacter();
        currentChar = peekCharacter();

        //ensures there is no code on a line following the end of a comment
        while(currentChar != '\n') {
            if (Character.isWhitespace(currentChar)) {
                currentCol++;
                txtManager.getCharacter();
                currentChar = peekCharacter();
            } else if (currentChar == '\u001a') {     //excludes parsing after or throwing exceptions for comments at the end of files
                break;
            } else {
                throw new SyntaxErrorException("Code found on a line after the end of a comment", currentLine, currentCol);
            }
        }
    }

    /**
     * This function does several things in the following order:
     *      Ensures that this function was called directly following the addition of a NEWLINE token to tokenList
     *      Calculates the indentation level for the currentLine
     *      Checks if the line is empty. If it is, it returns and does nothing else (means an empty line won't exit a block of code)
     *      Adds a INDENT Token to tokenList for every indent deeper the indentation level is compared to the last line
     *      Adds a DEDENT Token to tokenList for every indent shallower the indentation level is compared to the last line
     *      Adjusts the currentCol to be equal to the indentation level of the line
     * @throws SyntaxErrorException -   in the event that the tokenList is not empty and the last element in said list
     *                                  is NOT a NEWLINE Token
     */
    private void adjustIndent() throws SyntaxErrorException {
        int newIndentLevel = calculateIndent();

        //checks we are not on an emptyLine before adjusting indentation level
        if(peekCharacter() == '\n') return;

        //compares the current indentation level with the previous one
        while(indentLevel != newIndentLevel){
            //adds indent tokens and increases indentLevel 1 at a time until it is equal to newIndentLevel
            if(indentLevel < newIndentLevel){
                tokenList.add(new Token(Token.TokenTypes.INDENT, currentLine, 1));
                indentLevel++;
            //adds dedent tokens and decreases indentLevel 1 at a time until it is equal to newIndentLevel
            } else {//indentLevel > newIndentLevel
                tokenList.add(new Token(Token.TokenTypes.DEDENT, currentLine, 1));
                indentLevel--;
            }
        }
    }

    /**
     * Calculates the indent for the current line. Assumes and checks there is a newLine character preceding position in txtManager
     * parses through whitespaces until it reaches a non-white space or newline
     *      a tab counts as 1 indent
     *      4 consecutive space chars count as 1 indent
     * Finally, this function sets currentCol equal to the newIndent level
     * @return  - the indentation level
     * @throws SyntaxErrorException - if there is not a newline character preceding the position in txtManager
     */
    private int calculateIndent() throws SyntaxErrorException {
        int newIndentLevel = 0;
        char currentChar = peekCharacter();
        int spaces = 0;

        //checks that the call is valid
        if(peekCharacter(-1) != '\n')
            throw new SyntaxErrorException("Indentation cannot be adjusted except after a newline.", currentLine, currentCol);

        //counts the indentation level of this line
        while(Character.isWhitespace(currentChar) && currentChar != '\n'){

            if(currentChar == '\t'){    //increase newIndentLevel by 1, resets spaces
                newIndentLevel++;
                currentCol++;
                spaces = 0;
            } else if(currentChar == ' '){  //1 of up to 4 spaces required in a row to count as 1 indent
                spaces++;
                if(spaces == 4){  //increase newIndentLevel by 1, resets spaces
                    newIndentLevel++;
                    currentCol++;
                    spaces=0;
                }
            }
            //increments txtManager
            txtManager.getCharacter();

            //next char to check
            currentChar = peekCharacter();
        }
        currentCol = indentLevel+1;   //adjusts currentCol accordingly

        return newIndentLevel;
    }

    /**
     * A method used to safely peek at a character without going out of bounds.
     * @return -    If the character being peeked is within the bounds, it returns that character
     *              If the character being peeked is out of bounds, a '\u001a' is returned.
     */
    private char peekCharacter(){
        if(txtManager.isAtEnd())
            return '\u001a';
        else
            return txtManager.peekCharacter();
    }

    /**
     * A method used to safely peek at a character ahead of position without going out of bounds.
     * @param dist - the number of indexes ahead of position for the char being peaked at
     * @return -    If the character being peeked is within the bounds, it returns that character
     *              If the character being peeked is out of bounds, a '\u001a' is returned.
     */
    private char peekCharacter(int dist) {
        if(txtManager.isAtEnd(dist))
            return '\u001a';
        else
            return txtManager.peekCharacter(dist);
    }

    /**
     * finds the associated token type for the String parameter passed in. If it does not
     * match any of the tokens exactly (case-insensitive), this program returns a type of WORD
     * @param word - the String that is evaluated to determine which type is passed back
     * @return the type associated with the String passed in
     */
    private Token.TokenTypes typeFromLetters(String word) {
        switch (word) {
            case "implements" -> {
                return Token.TokenTypes.IMPLEMENTS;
            }
            case "class" -> {
                return Token.TokenTypes.CLASS;
            }
            case "interface" -> {
                return Token.TokenTypes.INTERFACE;
            }
            case "loop" -> {
                return Token.TokenTypes.LOOP;
            }
            case "if" -> {
                return Token.TokenTypes.IF;
            }
            case "else" -> {
                return Token.TokenTypes.ELSE;
            }
            case "new" -> {
                return Token.TokenTypes.NEW;
            }
            case "private" -> {
                return Token.TokenTypes.PRIVATE;
            }
            case "shared" -> {
                return Token.TokenTypes.SHARED;
            }
            case "construct" -> {
                return Token.TokenTypes.CONSTRUCT;
            }
            default -> {
                return Token.TokenTypes.WORD;
            }
        }
    }

    /**
     * @param c - the char to be evaluated
     * @return - whether the given char is a punctuation mark according to the tran definition
     */
    private boolean isPunctuation(char c) {
        for(char punctuator : punctuationFirstChars){
            if(c == punctuator){
                return true;
            }
        }
        return false;
    }

    /**
     * This method is used to increment txtManager and currentCol by one char
     * @return char - returns the char that was stored in txtManager
     * @throws SyntaxErrorException - in the event that a char past the ending char is attempted to be incremented past.
     */
    private char incrementOneChar() throws SyntaxErrorException {
        if(txtManager.isAtEnd()){
            throw new SyntaxErrorException("Tried to increment past the end char.", currentLine, currentCol);
        }
        currentCol++;
        return txtManager.getCharacter();
    }
}
