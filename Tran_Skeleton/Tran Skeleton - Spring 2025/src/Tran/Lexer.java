package Tran;
import java.util.LinkedList;
import java.util.List;

public class Lexer {
    /*private final char[] punctuation = {  '.', ',', '!', '?', '"',
                                            '\'', '[', ']', '{',
                                            '}', '<', '>', ':', '=',
                                            '\\', '/', '(', ')', '+',
                                            '-', '*', '%'};*/
    private final TextManager txtManager;
    private int currentLine;
    private int currentCol;

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
     * @throws Exception
     */
    public List<Token> lex() throws Exception {
	    LinkedList<Token> tokenList = new LinkedList<Token>();
        currentLine = 0;
        currentCol = 0;

        while(!txtManager.isAtEnd()) {
            char currentChar = peekCharacter();
            Token currentToken;
            if (Character.isLetter(currentChar)) {          //letters
                currentToken = readWord();
                tokenList.add(currentToken);
            /*} else if (Character.isDigit(currentChar)) {    //numbers
                currentToken = readNumber();
                tokenList.add(currentToken);
            } else if (isPunctuation(currentChar)) {        //punctuation
                currentToken = readPunctuation();
                tokenList.add(currentToken);*/
            }else if('\n' == currentChar) {                 //newLine
                //adds a newline token, and increment txtManager's position
                currentToken = new Token(Token.TokenTypes.NEWLINE, currentLine, currentCol);
                tokenList.add(currentToken);
                txtManager.getCharacter();

                //TODO currentCol = 0;
                //TODO currentLine++;
                //adjust indent/dedent
                //TODO setIndentDedent();
            /*} else if(Character.isWhitespace(currentChar)){ //whiteSpaces
                    midlineWhiteSpaces();
            */} else { //unknown character
                //TODO currentCol++;
                //TODO: adjust after all the checks have been properly set up. Don't throw char, use an error message and throw an exception

                //throws away char and increments position for txtManager
                currentChar = txtManager.getCharacter();
            }
        }
        return tokenList;
    }

    /**
     * Checks that the initial char passed at position in txtManager is a letter.
     * Parses through txtManager until the next char is not a letter or a digit and passes it into the
     * parsed String.
     * The characters parsed are organized in the order they were given.
     * Increments currentCol by however many chars were parsed.
     * Queries the type of token to initialize based on the value of parsed
     * @return - a token of a type dependent on the value of parsed.
     *      The rowNumber is equivalent to the field currentRow at the time of initialization
     *      The columnNumber is equivalent to the initialColIndex
     *      In the case that the type is WORD, the token contains a String value equal to parsed.
     */
    public Token readWord() {
        //initial check that the character indexed at position in txtManager is a letter char


        //mini-fields
        int initialColIndex = currentCol;
        String parsed = "";
        char currentChar = peekCharacter();

        //parses word (includes letters and digits(after first char))
            //increases currentCol accordingly

        //checks if parsed is a keyword
        //parsed is a keyword
            //returns a token of a type indicated by parsed,
        //parsed is not a keyword
            //returns a token of type WORD with a value containing parsed
    }

    /**
     * checks that the initial char at position in txtManager is either a digit or '.'.
     * parses through txtManager until the next char is not a digit or a '.' and passes it into the
     * value String. Only 1 '.' is read into the value String.
     * The characters parsed are organized in the order they were given.
     * Increments currentCol by however many chars were parsed.
     * @return - a token of type NUMBER
     *          The rowNumber is equivalent to the field currentRow at the time of initialization
     *          The columnNumber is  equivalent to the initialColIndex
     *          The value is equal to value, a parsed String from txtManager
     */
    /*public Token readNumber() {
        //initial check that the character indexed at position in txtManager is a digit char or '.'
        //mini-fields
        int decimalCounter = 0;
        int initialColIndex = currentCol;
        char currentChar = peekCharacter();
        String value = "";

        /*parses txtManager until position is not a digit or '.' OR position is a '.' and there is already
         a decimal (decimalCounter > 0). *
        while(
            //TODO adjust currentCol accordingly
        //returns a Token of type NUMBER with a value equal to the parsed String value.
    }*/

    /**
     * Assumes that the char at position in txtManager has been verified as a punctuation mark.
     * finds the relative punctuation mark, and in some cases, checks the next character if it is
     * relevant to the punctuation as some are two chars long.
     * For 2 initial chars, '{' and '"', the program parses through txtManager until it finds its pair
     *      Throws an exception when this pair is not found
     *      '{' is a comment and so ignores all the parsed characters, except '\n' until it's pair is
     *          found. It adjusts currentLine accordingly and sets currentCol to 0.
    *       '"' adjusts currentCol and currentLine accordingly.
     * Adjusts currentCol accordingly.
     * @return - A Token of a type dependent on the String parsed from txtManager
     *      The rowNumber is equivalent to the field currentRow at the time of initialization
     *      The columnNumber is  equivalent to the initialColIndex
     *      The type can sometimes be a NUMBER token, which holds a value equivalent to what's parsed
     */
    /*public Token readPunctuation() {
        //checks input
        if(!isPunctuation(peekCharacter())
            throw new IllegalArgumentException();

        int initialColNumber = currentCol;
        char firstChar = peekCharacter();

        //in cases where presumed punctuation is actually a NUMBER token
        if(firstChar == '.' && Character.isDigit(peekCharacter(1))){
            return readNumber();
        }

        //first char is punctuation and Not a NUMBER, incrementing txtManager position and currentCol
        txtManager.getCharacter();
        currentCol++;

        Token token;
        //walks through each type of punctuation, including types that may be two characters long
        //adjusts currentCol accordingly
        switch (firstChar){
            case: '{'   //comment
                //Throws an exception when this pair is not found
                //is a comment and so ignores all the parsed characters, except '\n' until it's pair is found.
                //It adjusts currentLine accordingly and sets currentCol to 0.
                //special case that throws an exception when it doesn't find its pair
                break;
            case: '"'   //quoted string
            //adjusts currentCol and currentLine accordingly.
            //special case that throws an exception when it doesn't find its pair
                break;
            case: ''
            default:
                break;
            //remember currentCol++ for second chars
        }
        currentCol++;   //for the firstChar
        return token;
    }

    /**
     * Assumes the white space(s) past and including the chars indexed at position in txtManager do
     * not follow directly after a new line and therefor do not affect indentation and dedentation.
     * Increments the current column number (currentCol) and the position in txtManager. Does NOT
     * accept newline characters '\n' as a whitespace
     */
    /*private void midLineWhitespace() {
        char character = peekCharacter();
        while(Character.isWhitespace(character) && (character != '\n')) {
            txtManager.getCharacter();
            if(txtManager.isAtEnd())
                character = '~';
            else
                character = peekCharacter();
            currentCol++;
        }
    }*/

    /**
     * A method used to safely peek at a character without going out of bounds.
     * @return -    If the character being peeked is within the bounds, it returns that character
     *              If the character being peeked is out of bounds, a '~' is returned.
     */
    private char peekCharacter(){
        if(txtManager.isAtEnd())
            return '~';
        else
            return txtManager.peekCharacter();
    }

    /**
     * A method used to safely peek at a character ahead of position without going out of bounds.
     * @param dist - the number of indexes ahead of position for the char being peaked at
     * @return -    If the character being peeked is within the bounds, it returns that character
     *              If the character being peeked is out of bounds, a '~' is returned.
     */
    private char peekCharacter(int dist) {
        if(txtManager.isAtEnd(dist))
            return '~';
        else
            return txtManager.peekCharacter();
    }

    /**
     * @param char - the char to be evaluated
     * @return - whether or not the given char is a punctuation mark according to the tran definition
     */
    /*public boolean isPunctuation(char c) {
        for(char punctuator : punctuation){
            if(c == punctuator){
                return true;
            }
        }
        return false;
    }*/
}
