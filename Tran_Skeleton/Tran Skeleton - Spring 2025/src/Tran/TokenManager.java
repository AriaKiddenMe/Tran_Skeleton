package Tran;
import java.io.Serial;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * A class built for use with the tran parser class.
 * It contains a tokenList passed in within the constructor that manages the list.
 *      private final List<Token> tokenList
 *      public TokenManager(List<Token> tokens){}
 * This class contains several classic list functions as described below:
 *      public boolean done() //to indicate whether the current list is empty
 *      public boolean done(int i) //to indicate whether the current list has a token in the index given
 *      public Optional<Token> matchAndRemove(Token.TokenTypes type) //returns an optional containing the type listed if
 *          //and only if the first element of the list matches the type passed in, otherwise returns an empty optional
 *      public Optional<Token> peek() //returns an optional containing the first element or returns an empty optional (empty list)
 *      public Optional<Token> peek() //returns an optional containing the element at the given index or returns an
 *                                    //empty optional if the list is not that long
 *      public boolean nextTwoTokensMatch(Token.TokenTypes first, Token.TokenTypes second)
 *          //returns true if and only if the first two token types of the list match the parameters passed in. accepts null as input
 *      public boolean nextIsEither(Token.TokenTypes first, Token.TokenTypes second)
 *          //returns true if the first token's type matches either of the token types passed in
 *      public int getCurrentLine() //returns the line number of the first element
 *      public int getCurrentColumn()   //returns the column number of the first element
 *  Authors: Aria Nova Chaudoir and Professor Michael Phipps (Outlined Algorithm)
 */
public class TokenManager {
    private final List<Token> tokenList;    //holds a list of tokens that represent keywords and sometimes hold values.

    /**
     * Constructs a TokenManager object that contains a field of a list of tokens accordingly called tokenList
     * @param tokens - the list of tokens that will be assigned to tokenList
     */
    public TokenManager(List<Token> tokens) {
        this.tokenList = (List<Token>) tokens;
    }

    /**
     * @return whether the list is empty
     */
    public boolean done() {
	    return tokenList.isEmpty();
    }

    /**
     * @param i - the index to be tested
     * @return - whether the list ends before the index given
     */
    public boolean done(int i){
        if(i < 0){ //very foolish but possible
            throw new IndexOutOfBoundsException("requested to check if the file finishes at a negative index");
        }
        return i >= tokenList.size();
    }

    /**
     * This method first peeks at the first element in the list getting an optional as a return.
     * It then checks if that optional is empty(null) indicating the end of the list has been reached.
     * After that, it checks if the current first element (node) of the list is of the type passed in.
     *      If it is, it removes that element from the list and returns an optional of it.
     *      Otherwise, it returns an empty optional
     * @param type - the type of the optional we are looking for / expecting
     * @return an optional containing either:
     *      An empty optional indicating that the list is either
     *          empty or
     *          the first element is not of the type passed in.
     *      An optional of the type passed in
     */
    public Optional<Token> matchAndRemove(Token.TokenTypes type) {
        var optionalOfToken = peek();
        if(optionalOfToken.isEmpty()) return Optional.empty();

        if(type.equals(optionalOfToken.get().getType())){
            tokenList.removeFirst();
            return optionalOfToken;
        }
        else return Optional.empty();
    }

    /**
     * This function employs the Optional<Token> peek(int index) function at an index of 0
     * @return either   - An optional containing The token requested
     *                  - An optional that is empty (list is empty)
     */
    public Optional<Token> peek(){
        return peek(0);
    }

    /**
     * This function starts by ensuring that the index passed in is a non-negative value.
     * @param index     - the index of the Token in the list being returned
     * @return either   - An optional containing The token requested
     *                  - An optional that is empty (request out of bounds of the list)
     */
    public Optional<Token> peek(int index) {
	    if(index < 0){
            throw new IndexOutOfBoundsException("Requested to peek at a token with a negative index");
        }
        if(done(index)){ //at the end of the array for the currentIndex + i
            return Optional.empty();
        }
        return Optional.ofNullable(tokenList.get(index));
    }

    /**
     * Note one Parameters: at least for now, null is being considered a fair value as input due to the presence of so
     * many Optional<Token> objects being present.
     * @param first - the type being compared to the first token in the token list
     * @param second - the type being compared to the second token in the token list
     * @return - a boolean representing whether the next two tokens are of the types passed in
     */
    public boolean nextTwoTokensMatch(Token.TokenTypes first, Token.TokenTypes second) {
        Optional<Token> firstOptionalToken = peek();
        Optional<Token> secondOptionalToken = peek(1);

        //handles null input (expected and otherwise) and end of list
        if(firstOptionalToken.isEmpty() && secondOptionalToken.isEmpty()) return (first == null && second == null);
        if(firstOptionalToken.isEmpty()) throw new IllegalArgumentException("tried" +
                "to compare a non null token with preceding token type parameter being null");
        if(secondOptionalToken.isEmpty()) return (first.equals(firstOptionalToken.get().getType()) && second == null);

        //non-null input and now safe to use get()
        return (firstOptionalToken.get().getType().equals(first) && secondOptionalToken.get().getType().equals(second));
    }

    /**
     * @param first - the first potential type to be compared against
     * @param second - the second potential type to be compared against
     * @return true if the first or second type passed in is equivalent to the type of the current token
     */
    public boolean nextIsEither(Token.TokenTypes first, Token.TokenTypes second) {
	    Optional<Token> optionalToken = peek();

        //for null input or end of file
        if(optionalToken.isEmpty()) return (first == null || second == null);

        //current token is not null, IOW token list is not empty
        var type = optionalToken.get().getType();
        return type.equals(first) || type.equals(second);
    }

    /**
     * Useful when users want to know if the size of the tokenList has changed since their algorithm started
     * @return
     */
    public int tokensLeft(){
        return tokenList.size();
    }

    /**
     * Returns true when the next Token is of the type passed in. Null inclusive
     * @param type - the type being compared to the first Token's type
     * @return - True if their types allign
     *           False if their types are different
     */
    public boolean nextIs(Token.TokenTypes type) {
        Optional<Token> optionalToken = peek();
        if(optionalToken.isEmpty()) return type == null;
        else return type == optionalToken.get().getType();
    }

    /**
     * @return - the current line number of the current token
     */
    public int getCurrentLine() {
            if(done(0)) return -1;
            return tokenList.getFirst().getLineNumber();
    }

    /**
     * @return = the current column number of the currentToken
     */
    public int getCurrentColumnNumber() {
            if(done(0)) return -1;
            return tokenList.getFirst().getColumnNumber();
    }
}
