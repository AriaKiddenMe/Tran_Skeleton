package Tran;

import AST.NotOpNode;

import java.util.function.Supplier;
import java.util.Optional;

public class SyntaxErrorException extends Exception {
    private final int lineNumber;
    private final int characterPosition;

    public SyntaxErrorException (String message, int lineNumber, int characterPosition) {
        super(message);
        this.lineNumber = lineNumber;
        this.characterPosition = characterPosition;
    }

    @Override
    public String toString() {
        return "Error at line " + lineNumber + " at character " + characterPosition + " at " + super.toString();
    }
}
