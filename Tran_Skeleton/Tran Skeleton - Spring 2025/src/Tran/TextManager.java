package Tran;
public class TextManager {
    private final String text;
    private int position;

    /**
     * creates a TextManager object containing a String input with a position int at 0
     * @param input - assigned to the field text
     */
    public TextManager(String input) {
        text = input;
        position = 0;
    }

    /**
     * @return true when position is an index or more past the last index
     */
    public boolean isAtEnd() {
	    return position >= text.length();
    }

    /**
     * @param dist - the interval ahead of dist that were checking to see if it is the end
     *             (or greater than it) of parsable chars in text
     * @return true when position + dist is an index or more past the last index
     */
    public boolean isAtEnd(int dist) {
        return position + dist >= text.length();
    }

    /**
     * @return the character at the index specified by the position field
     */
    public char peekCharacter() {
            return text.charAt(position);
    }

    /**
     * @param dist - an int representing the offset from the index of position
     * @return the character at the index specified by the position field + the given dist
     */
    public char peekCharacter(int dist){
        return text.charAt(position+dist);
    }

    /**
     * grabs the char at the current position then indexes position and returns the grabbed char
     * @return the character at the index specified by the position field
     */
    public char getCharacter() {
        char theCharacter = text.charAt(position);
        position++;
        return theCharacter;
    }
}
