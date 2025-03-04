package Tran;
import AST.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class Parser {

    private TokenManager tokenManager;
    private final TranNode tranNode;

    public Parser(TranNode top, List<Token> tokens) {
        tokenManager = new TokenManager(tokens);
        tranNode = top;
    }

    /**
     * EBNF: Tran = ( Class | Interface )*
     * @throws SyntaxErrorException - in the event that the first Token in TokenManager is not CLASS or INTERFACE
     * (or any number of NEWLINE Tokens)
     */
    public void Tran() throws Exception {
        Optional<Token> optionalToken = tokenManager.peek();
        if (optionalToken.isEmpty()) { //tokenManager is empty
            return;
        }

        //while the next Token is CLASS or INTERFACE
        while(!tokenManager.done()) {
            deletePotNewLines();    //removes extra NEWLINE Tokens
            if (tokenManager.nextIsEither(Token.TokenTypes.CLASS, Token.TokenTypes.INTERFACE)) {    //CLASS or INTERFACE
/*                if(tokenManager.matchAndRemove(Token.TokenTypes.CLASS).isPresent()){            //CLASS Token Found
                    tranNode.Classes.add(classNode());//TODO-UNDO THIS
                } else*/ if(tokenManager.matchAndRemove(Token.TokenTypes.INTERFACE).isPresent()){ //INTERFACE Token Found
                    tranNode.Interfaces.add(interfaceNode());
                }
            } else {            //NON-NEWLINE Token found outside of CLASS or INTERFACE blocks
                throw new SyntaxErrorException("tran file did not start with a CLASS or INTERFACE token",
                        tokenManager.getCurrentLine(), tokenManager.getCurrentColumnNumber());
            }
        }
    }

    /**
     * EBNF: Interface = "interface" IDENTIFIER NEWLINE INDENT MethodHeader* DEDENT
     * Assume that an INTERFACE token was just removed from the list just before this call
     * @return An Interface node containing the values name and methods.
     *      Name holds the value found in the IDENTIFIER Token
     *      methods is an array that holds the method headers found in the interface
     * @throws SyntaxErrorException - when the Tokens passed in do not follow the format of the EBNF
     */
    private InterfaceNode interfaceNode() throws SyntaxErrorException {
        InterfaceNode interfaceNode = new InterfaceNode();
        //for the Interface IDENTIFIER
        interfaceNode.name = requireAndReturnIDENTIFIER();
        //requires >= 1 NEWLINE token
        requireNewLine();
        //indent
        requireToken(Token.TokenTypes.INDENT);
        //methodHeaders
        Optional<MethodHeaderNode> methodHeaderNode = getMethodHeaderNode();
        while(methodHeaderNode.isPresent()) {
            interfaceNode.methods.add(methodHeaderNode.get());
            methodHeaderNode = getMethodHeaderNode();
        }
        //dedent
        requireToken(Token.TokenTypes.DEDENT);      //TODO-UNDO This
        return interfaceNode;
    }

    //Class =  "class" IDENTIFIER ( "implements" IDENTIFIER ( "," IDENTIFIER )* )? NEWLINE INDENT ( Constructor | MethodDeclaration | Member )* DEDENT
    private ClassNode classNode() throws SyntaxErrorException {
        //TODO-UNDO this
        ClassNode classNode = new ClassNode();
        /*
        //name
        classNode.name = requireIDENTIFIER();
        //interfaces
        if(tokenManager.nextTwoTokensMatch(Token.TokenTypes.IMPLEMENTS, Token.TokenTypes.WORD)) {
            //first interface
            tokenManager.matchAndRemove(Token.TokenTypes.IMPLEMENTS);
            classNode.interfaces.add(tokenManager.matchAndRemove(Token.TokenTypes.WORD).get().getValue());
            //any other interfaces
            while(tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.COMMA)) {
                tokenManager.matchAndRemove(Token.TokenTypes.COMMA);
                classNode.interfaces.add(tokenManager.matchAndRemove(Token.TokenTypes.WORD).get().getValue());
            }
        }
        //requires >= 1 NEWLINE Token
        requireNewLine();
        //indent
        requireToken(Token.TokenTypes.INDENT);
        boolean moreConstsMethodsMembers = true;
        while (moreConstsMethodsMembers) {
            moreConstsMethodsMembers = false;
            //if there are lines before the next line of runnable code
            deletePotNewLines();
            //in case tokens run out without exiting the class
            requireNotDone();
            if(tokenManager.matchAndRemove(Token.TokenTypes.CONSTRUCT).isPresent()) {          //Constructor
                classNode.constructors.add(getConstructorNode());
                moreConstsMethodsMembers = true;
            } else if(tokenManager.nextIsEither(Token.TokenTypes.PRIVATE, Token.TokenTypes.SHARED)) {   //MethodDeclaration
                //TODO: Hold up, can't fields have privacy modifiers too?
                classNode.methods.add(getMethodDeclarationNode());
                moreConstsMethodsMembers = true;
            } else if() {        //Member
                classNode.members.add(getMemeberNode());
                moreConstsMethodsMembers = true;
            } else{
                //TODO Throw Exception
            }
        }
        requireToken(Token.TokenTypes.DEDENT);*/
        return classNode;
    }

    /**
     * EBNF: MethodHeader = IDENTIFIER "(" ParameterVariableDeclarations ")" (":" ParameterVariableDeclarations)? NEWLINE
     * Queries for a MethodHeaderNode. If the next few Tokens indicate its presence, it creates one holding the fields:
     *  name: the name of the method
     *  parameters: VariableDeclarationNodes containing the type and name of any potential parameters
     *  returns: VariableDeclarationNodes containing the type and name of any potential returns.
     * WARNING: This method can incorrectly ID and remove tokens if used in the wrong context and the first two Tokens
     * are WORD and LPAREN. It will likely throw an error in these circumstances, but one is not guaranteed.
     *      i.e. if it were used when getMethodCall would be
     * @return  If the next few tokens do not indicate a MethodHeaderNode it creates and returns an empty optional
     *          If it did find the aforementioned indicators, it parses through and then returns the MethodHeaderNode
     * @throws SyntaxErrorException - in the event that the Tokens passed in do not meet the required format
     */
    private Optional<MethodHeaderNode> getMethodHeaderNode() throws SyntaxErrorException {
        if(!tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.LPAREN)){ //the next two are not
            return Optional.empty();
        }

        MethodHeaderNode methodHeaderNode = new MethodHeaderNode();

        methodHeaderNode.name = requireAndReturnIDENTIFIER();

        requireToken(Token.TokenTypes.LPAREN);
        //0 or greater parameters
        methodHeaderNode.parameters = getParameterVariableDeclarations();
        requireToken(Token.TokenTypes.RPAREN);

        if(tokenManager.matchAndRemove(Token.TokenTypes.COLON).isPresent()) {
            //first return value (required when COLON Token found)
            String errorMessageFollowingType = "No variable declarations given despite following a " + Token.TokenTypes.COLON;
            methodHeaderNode.returns = getParameterVariableDeclarationsRequireFirst(errorMessageFollowingType);
        }
        requireNewLineOrPeekDedent();
        return Optional.of(methodHeaderNode);
    }

    //MethodDeclaration = "private"? "shared"? MethodHeader NEWLINE MethodBody


    //ParameterVariableDeclaration = IDENTIFIER IDENTIFIER
    private VariableDeclarationNode getVariableDeclarationNode() throws SyntaxErrorException {
        VariableDeclarationNode variableDeclarationNode = new VariableDeclarationNode();
        if(tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.WORD)) {
            variableDeclarationNode.type = tokenManager.matchAndRemove(Token.TokenTypes.WORD).get().getValue();
            variableDeclarationNode.name = tokenManager.matchAndRemove(Token.TokenTypes.WORD).get().getValue();
        } else throw new SyntaxErrorException("Expected the next two tokens to be of type WORD",
                tokenManager.getCurrentLine(), tokenManager.getCurrentColumnNumber());
        return variableDeclarationNode;
    }

    //ParameterVariableDeclarations =  ParameterVariableDeclaration  ("," ParameterVariableDeclaration)*


    //Constructor = "construct" "(" ParameterVariableDeclarations ")" NEWLINE MethodBody
    private Optional<ConstructorNode> getConstructorNode() throws SyntaxErrorException {
        //checking for construct token
        if(tokenManager.matchAndRemove(Token.TokenTypes.CONSTRUCT).isEmpty()) {
            return Optional.empty();
        }

        ConstructorNode constructorNode = new ConstructorNode();

        requireToken(Token.TokenTypes.LPAREN);

        constructorNode.parameters = getParameterVariableDeclarations();

        requireToken(Token.TokenTypes.RPAREN);

        requireNewLine();

        //TODO check works
        constructorNode.statements = getMethodBody();

        return Optional.of(constructorNode);
    }

    //MethodBody = INDENT ( VariableDeclarations )*  Statement* DEDENT
    private LinkedList<StatementNode> getMethodBody() throws SyntaxErrorException {
        //TODO
        return new LinkedList<StatementNode>();
    }

    //Member = VariableDeclarations

    //VariableDeclarations =  IDENTIFIER VariableNameValue ("," VariableNameValue)* NEWLINE

    //Statement = If | Loop | MethodCall | Assignment

    //VariableNameValue = IDENTIFIER ( "=" Expression)?

    //If = "if" BoolExpTerm NEWLINE Statements ("else" NEWLINE (Statement | Statements))?

    //Loop = "loop" (VariableReference "=" )?  ( BoolExpTerm ) NEWLINE Statements

    //MethodCall = (VariableReference ( "," VariableReference )* "=")? MethodCallExpression NEWLINE

    //Assignment = VariableReference "=" Expression NEWLINE

    //Expression = Term ( ("+"|"-") Term )*

    //BoolExpTerm = MethodCallExpression | (Expression ( "==" | "!=" | "<=" | ">=" | ">" | "<" ) Expression) | VariableReference

    //Statements = INDENT Statement*  DEDENT

    //VariableReference = IDENTIFIER

    //MethodCallExpression =  (IDENTIFIER ".")? IDENTIFIER "(" (Expression ("," Expression )* )? ")"

    //Term = Factor ( ("*"|"/"|"%") Factor )*

    //Factor = NUMBER | VariableReference |  STRINGLITERAL | CHARACTERLITERAL | MethodCallExpression | "(" Expression ")" | "new" IDENTIFIER "(" (Expression ("," Expression )*)? ")"

    private void deletePotNewLines(){
        while(tokenManager.matchAndRemove(Token.TokenTypes.NEWLINE).isPresent());//removes all the sequential newlines
    }

    private LinkedList<VariableDeclarationNode> getParameterVariableDeclarations() throws SyntaxErrorException {
        LinkedList<VariableDeclarationNode> paramVarDeclNodes = new LinkedList<VariableDeclarationNode>();
        while(tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.WORD)) {
            paramVarDeclNodes.add(getVariableDeclarationNode());
            if(tokenManager.matchAndRemove(Token.TokenTypes.COMMA).isEmpty()) return paramVarDeclNodes;
        }
        return paramVarDeclNodes;
    }

    private LinkedList<VariableDeclarationNode> getParameterVariableDeclarationsRequireFirst(String typeFollowsErrorMessage)
            throws SyntaxErrorException {
        LinkedList<VariableDeclarationNode> paramVarDeclNodes = new LinkedList<VariableDeclarationNode>();
        if(!tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.WORD)) {
            throw new SyntaxErrorException(typeFollowsErrorMessage, tokenManager.getCurrentLine(), tokenManager.getCurrentColumnNumber());
        }
        paramVarDeclNodes.add(getVariableDeclarationNode());
        if(tokenManager.matchAndRemove(Token.TokenTypes.COMMA).isPresent()) {
            paramVarDeclNodes.addAll(getParameterVariableDeclarations());
        }
        return paramVarDeclNodes;
    }

    private void requireNotDone() throws SyntaxErrorException {
        if(tokenManager.done()){
            throw new SyntaxErrorException("Received an empty Token prematurely. tokenList emptied improperly",
                    tokenManager.getCurrentLine(), tokenManager.getCurrentColumnNumber());
        }
    }

    private Optional<Token> requireToken(Token.TokenTypes tokenType) throws SyntaxErrorException {
        Optional<Token> optionalOfToken = tokenManager.matchAndRemove(tokenType);
        presentOrThrowExpected(tokenType, optionalOfToken);
        return optionalOfToken;
    }

    /**
     * Ensures that the next line is a newline. If it is, it matches and removes that NEWLINE and any other NEWLINE
     * tokens following directly after it
     * @throws SyntaxErrorException - thrown when a NEWLINE token is not present for the current token of the list
     */
    private void requireNewLine() throws SyntaxErrorException {
        requireToken(Token.TokenTypes.NEWLINE);
        deletePotNewLines();
    }

    private void requireNewLineOrPeekDedent() throws SyntaxErrorException {
        if(tokenManager.nextIsEither(Token.TokenTypes.NEWLINE, Token.TokenTypes.DEDENT)){
            tokenManager.matchAndRemove(Token.TokenTypes.NEWLINE);
            //accepts a dedent but does not remove it. Another function will do that
        } else{
            throw errorNotExpectedTypes(new Token.TokenTypes[]{Token.TokenTypes.NEWLINE, Token.TokenTypes.DEDENT},
                                        tokenManager.peek());
        }
    }

    private String requireAndReturnIDENTIFIER() throws SyntaxErrorException {
        Optional<Token> optionalOfToken = requireToken(Token.TokenTypes.WORD);
        return optionalOfToken.get().getValue();
    }

    /**
     * @param optionalOfToken the Optional object to be checked. If it is empty, this method throws an exception
     * @throws SyntaxErrorException - if the Optional passed in is empty. Describes the expected vs actual Tokens it recieved
     */
    void presentOrThrowExpected(Token.TokenTypes expectedType, Optional<Token> optionalOfToken) throws SyntaxErrorException {
        if (optionalOfToken.isEmpty()) throw errorNotExpectedType(expectedType, tokenManager.peek());
    }

    /**
     * @param expectedType - the Token expected and not received
     * @param foundOptionalOfToken - the Optional of the Token found in place of the expected one
     * @return A SyntaxErrorException with the address of the error and an appropriate method listing the types excepted
     *          and the types found
     */
    private SyntaxErrorException errorNotExpectedType(Token.TokenTypes expectedType, Optional<Token> foundOptionalOfToken) {
        if(foundOptionalOfToken.isPresent()){
            return new SyntaxErrorException("Expected a " + expectedType + " Token, but found " +foundOptionalOfToken.get().getType(),
                    tokenManager.getCurrentLine(), tokenManager.getCurrentColumnNumber());
        } else {
            return new SyntaxErrorException("Expected a " + expectedType + " Token, but found " + null,
                    tokenManager.getCurrentLine(), tokenManager.getCurrentColumnNumber());
        }
    }

    private SyntaxErrorException errorNotExpectedTypes(Token.TokenTypes[] expectedTypes, Optional<Token> foundOptionalOfToken) {
        if(foundOptionalOfToken.isPresent()){
            return new SyntaxErrorException("Expected Any of the Following " + Arrays.toString(expectedTypes) + " Tokens\n" +
                    "Instead found " +foundOptionalOfToken.get().getType(),
                    tokenManager.getCurrentLine(), tokenManager.getCurrentColumnNumber());
        } else {
            return new SyntaxErrorException("Expected Any of the Following " + Arrays.toString(expectedTypes) + " Tokens\n" +
                    "Instead found " + null, tokenManager.getCurrentLine(), tokenManager.getCurrentColumnNumber());
        }
    }
}