package Tran;
import AST.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class Parser {

    private TokenManager tokenManager;
    private final TranNode tranNode;
    String errorMessageFollowingColon = "No variable declarations given despite following a " + Token.TokenTypes.COLON;


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
                if(tokenManager.nextIs(Token.TokenTypes.CLASS)){            //CLASS Token Found
                    tranNode.Classes.add(classNode());//TODO-UNDO THIS
                } else if(tokenManager.nextIs(Token.TokenTypes.INTERFACE)){ //INTERFACE Token Found
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
        requireToken(Token.TokenTypes.INTERFACE);
        InterfaceNode interfaceNode = new InterfaceNode();
        //for the Interface IDENTIFIER
        interfaceNode.name = requireAndReturnIDENTIFIER();
        //requires >= 1 NEWLINE token
        requireNewLine();
        //indent
        requireToken(Token.TokenTypes.INDENT);
        deletePotNewLines();
        //methodHeaders
        Optional<MethodHeaderNode> methodHeaderNode = getMethodHeaderNode();
        while(methodHeaderNode.isPresent()) {
            interfaceNode.methods.add(methodHeaderNode.get());
            deletePotNewLines();
            methodHeaderNode = getMethodHeaderNode();
        }
        deletePotNewLines();
        //dedent
        requireToken(Token.TokenTypes.DEDENT);
        return interfaceNode;
    }

    //Class =  "class" IDENTIFIER ( "implements" IDENTIFIER ( "," IDENTIFIER )* )? NEWLINE INDENT ( Constructor | MethodDeclaration | Member )* DEDENT
    private ClassNode classNode() throws SyntaxErrorException {
        requireToken(Token.TokenTypes.CLASS);
        ClassNode classNode = new ClassNode();
        //name
        classNode.name = requireAndReturnIDENTIFIER();
        //interfaces
        if(tokenManager.nextTwoTokensMatch(Token.TokenTypes.IMPLEMENTS, Token.TokenTypes.WORD)) {
            //first interface
            tokenManager.matchAndRemove(Token.TokenTypes.IMPLEMENTS);
            classNode.interfaces.add(tokenManager.matchAndRemove(Token.TokenTypes.WORD).get().getValue());
            //any other interfaces
            while(tokenManager.nextTwoTokensMatch(Token.TokenTypes.COMMA, Token.TokenTypes.WORD)) {
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
            if(tokenManager.nextIs(Token.TokenTypes.CONSTRUCT)) {          //Constructor
                classNode.constructors.add(getConstructorNode().get());
                moreConstsMethodsMembers = true;
            } else if(tokenManager.nextIsEither(Token.TokenTypes.PRIVATE, Token.TokenTypes.SHARED) ||
                    tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.LPAREN)) {   //MethodDeclaration
                Optional<MethodDeclarationNode> methodDelclarationOpt = getMethodDeclarationNode();
                if(methodDelclarationOpt.isPresent()) {
                    classNode.methods.add(methodDelclarationOpt.get());
                    moreConstsMethodsMembers = true;
                } else
                    throw new SyntaxErrorException("expected a method.", tokenManager.getCurrentLine(), tokenManager.getCurrentColumnNumber());
            } else if(tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.WORD)) {        //Member
                Optional<MemberNode> memberNodeOpt = getMemberNode();
                if(memberNodeOpt.isPresent()) {
                    classNode.members.add(memberNodeOpt.get());
                    moreConstsMethodsMembers = true;
                } else throw new SyntaxErrorException("expected a member.", tokenManager.getCurrentLine(), tokenManager.getCurrentColumnNumber());
            } else{
                if(!tokenManager.nextIs(Token.TokenTypes.DEDENT))
                    throw new SyntaxErrorException("expected a constructor, method, or member.",
                            tokenManager.getCurrentLine(), tokenManager.getCurrentColumnNumber());
            }
        }
        requireToken(Token.TokenTypes.DEDENT);
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
        methodHeaderNode.parameters = getVariableDeclarations();
        requireToken(Token.TokenTypes.RPAREN);

        if(tokenManager.matchAndRemove(Token.TokenTypes.COLON).isPresent()) {
            //first return value (required when COLON Token found)
            methodHeaderNode.returns = getVariableDeclarationsRequireFirst(errorMessageFollowingColon);
        }
        requireNewLineOrPeekDedent();
        return Optional.of(methodHeaderNode);
    }

    //MethodDeclaration = "private"? "shared"? MethodHeader NEWLINE MethodBody
    private Optional<MethodDeclarationNode> getMethodDeclarationNode() throws SyntaxErrorException {
        MethodDeclarationNode methodDeclaration = new MethodDeclarationNode();
        if(tokenManager.nextIsEither(Token.TokenTypes.PRIVATE, Token.TokenTypes.SHARED)){
            if(tokenManager.matchAndRemove(Token.TokenTypes.SHARED).isPresent()){
                methodDeclaration.isShared = true;
                methodDeclaration.isPrivate = false;
            } else if (tokenManager.matchAndRemove(Token.TokenTypes.PRIVATE).isPresent()){    //assumes we don't get false positives from TokenManager's nextIsEither method
                methodDeclaration.isShared = false;
                methodDeclaration.isPrivate = true;
            } else {
                throw new SyntaxErrorException("False positive from nextIsEither method in TokenManager Class",
                        tokenManager.getCurrentLine(), tokenManager.getCurrentColumnNumber());
            }
        } else if(!tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.LPAREN)){
            /*the first two tokens are not a privacy modifier nor a word followed by a left parenthesis. There must not be
            a method here*/
            return Optional.empty();
        }

        /*assuming there is a method from this point forward, though a method call could theoretically fit into this syntax,
        this method should never be called within another method. Also assuming this method is not accidentally called when
        processing an interface*/
        Optional<MethodHeaderNode> optionalMethodHeaderNode = getMethodHeaderNode();
        if(optionalMethodHeaderNode.isEmpty()) {
            throw new SyntaxErrorException("Expected a Method Header", tokenManager.getCurrentLine(), tokenManager.getCurrentColumnNumber());
        }
        MethodHeaderNode methodHeader = optionalMethodHeaderNode.get();
        methodDeclaration.name = methodHeader.name;
        methodDeclaration.parameters = methodHeader.parameters;
        methodDeclaration.returns = methodHeader.returns;
        //TODO: TEST local variables set inside getStatements... theoretically
        methodDeclaration.statements = getStatements(methodDeclaration);
        return Optional.of(methodDeclaration);
    }

    //ParameterVariableDeclaration = IDENTIFIER IDENTIFIER
    private Optional<VariableDeclarationNode> getVariableDeclarationNode() throws SyntaxErrorException {
        VariableDeclarationNode variableDeclarationNode = new VariableDeclarationNode();
        if(tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.WORD)) {
            variableDeclarationNode.type = tokenManager.matchAndRemove(Token.TokenTypes.WORD).get().getValue();
            variableDeclarationNode.name = tokenManager.matchAndRemove(Token.TokenTypes.WORD).get().getValue();
            return Optional.of(variableDeclarationNode);
        } else return Optional.empty();
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

        constructorNode.parameters = getVariableDeclarations();

        requireToken(Token.TokenTypes.RPAREN);

        requireNewLine();

        //TODO check works
        constructorNode.statements = getStatements(constructorNode);
        return Optional.of(constructorNode);
    }

    //MethodBody = INDENT ( VariableDeclarations )*  Statement* DEDENT
    private LinkedList<StatementNode> getStatements(Node blockNode) throws SyntaxErrorException {
        LinkedList<StatementNode> statements = new LinkedList<StatementNode>();
        LinkedList<VariableDeclarationNode> variables = new LinkedList<VariableDeclarationNode>();
        deletePotNewLines();
        requireToken(Token.TokenTypes.INDENT);
        deletePotNewLines();
        //getStatementNode() returns either a statementNode (if present) or null
        StatementNode nextStatement = getStatementNode();
        Optional<VariableDeclarationNode> nextVariableDeclaration = getVariableDeclarationNode();
        do {
            if(nextStatement != null) statements.add(nextStatement);
            if(nextVariableDeclaration.isPresent()) variables.add(nextVariableDeclaration.get());
            requireNewLineOrPeekDedent();
            nextStatement = getStatementNode();
            nextVariableDeclaration = getVariableDeclarationNode();
        } while (!tokenManager.nextIs(Token.TokenTypes.DEDENT));
        requireToken(Token.TokenTypes.DEDENT);

        //ensures variables are not declared outside methods and constructors
        if(!variables.isEmpty()){
            //I'd use Interfaces and Generics, but there is not a way for me to do that safety whilst only turning in 4 of the projects files
            if(blockNode instanceof MethodDeclarationNode) {
                MethodDeclarationNode methodDeclarationNode = (MethodDeclarationNode) blockNode;
                methodDeclarationNode.locals = variables;
            }
            else if(blockNode instanceof ConstructorNode){
                ConstructorNode constructorNode = (ConstructorNode) blockNode;
                constructorNode.parameters = variables;
            } else {
                throw new SyntaxErrorException("Local variables found outside of method or constructor block",
                        tokenManager.getCurrentLine(), tokenManager.getCurrentColumnNumber());
            }
        }

        return statements;
    }

    //Member = VariableDeclarations
    private Optional<MemberNode> getMemberNode() throws SyntaxErrorException {
        Optional<VariableDeclarationNode> variableDeclaration = getVariableDeclarationNode();
        if(variableDeclaration.isPresent()) {
            MemberNode memberNode = new MemberNode();
            memberNode.declaration = variableDeclaration.get();
            return Optional.of(memberNode);
        } else return Optional.empty();
    }

    //VariableDeclarations =  IDENTIFIER VariableNameValue ("," VariableNameValue)* NEWLINE

    /**
     * EBNF: Statement = If | Loop | MethodCall | Assignment
     * Due to the return type being an interface, Generics gets in the way of using the Optional Class. Therefore, this
     * method will implement the functionality of the Optional class whilst loosing the Optional wrapping signature to
     * the class
     * @return either an object of type Statement
     *          OR null in the instance where no statement is found
     * @throws SyntaxErrorException - in the event that a statement pattern is recognized and started but does not hold to
     * the syntax of tran.
     */
    private StatementNode getStatementNode() throws SyntaxErrorException {
        //AssignmentNode
        Optional<AssignmentNode> assignmentNodeOptional = getAssignmentNode();
        if(assignmentNodeOptional.isPresent())  return assignmentNodeOptional.get();

        //IfNode
        Optional<IfNode> ifNodeOptional = getIfNode();
        if(ifNodeOptional.isPresent())  return ifNodeOptional.get();

        //LoopNode
        Optional<LoopNode> loopNodeOptional = getLoopNode();
        if(loopNodeOptional.isPresent())  return loopNodeOptional.get();

        //MethodCallStatementNode
        Optional<MethodCallStatementNode> optionalMethodCallStatementNode = getMethodCallStatementNode();
        if(optionalMethodCallStatementNode.isPresent())  return optionalMethodCallStatementNode.get();

        return null;    //no statement found
    }

    //VariableNameValue = IDENTIFIER ( "=" Expression)?

    //If = "if" BoolExpTerm NEWLINE Statements ("else" NEWLINE (Statement | Statements))?
    private Optional<IfNode> getIfNode() throws SyntaxErrorException {
        if(tokenManager.matchAndRemove(Token.TokenTypes.IF).isEmpty())  return Optional.empty();
        IfNode ifNode = new IfNode();
        ifNode.condition = getBoolExpressionNode();
        requireNewLine();
        ifNode.statements = getStatements(ifNode);
        ifNode.elseStatement = getElseNode();
        return Optional.of(ifNode);
    }

    //Loop = "loop" (VariableReference "=" )?  ( BoolExpTerm ) NEWLINE Statements
    private Optional<LoopNode> getLoopNode() throws SyntaxErrorException {
        if(tokenManager.matchAndRemove(Token.TokenTypes.LOOP).isEmpty())  return Optional.empty();
        LoopNode loopNode = new LoopNode();
        if(tokenManager.nextTwoTokensMatch(Token.TokenTypes.WORD, Token.TokenTypes.ASSIGN)) {
            loopNode.assignment = getVariableReferenceNode();
            requireToken(Token.TokenTypes.ASSIGN);
        } //optional so don't need to throw an error
        loopNode.expression = getBoolExpressionNode();
        requireNewLine();
        loopNode.statements = getStatements(loopNode);
        return Optional.of(loopNode);
    }

    //MethodCall = (VariableReference ( "," VariableReference )* "=")? MethodCallExpression NEWLINE
    private Optional<MethodCallStatementNode> getMethodCallStatementNode() throws SyntaxErrorException {
        //TODO - a temporary stub out, so it will compile and execute
        return Optional.empty();
    }

    //Assignment = VariableReference "=" Expression NEWLINE
    private Optional<AssignmentNode> getAssignmentNode() throws SyntaxErrorException {
        //TODO - a temporary stub out, so it will compile and execute
        return Optional.empty();
    }

    //Expression = Term ( ("+"|"-") Term )*
    private ExpressionNode getExpressionNode() throws SyntaxErrorException {
        ExpressionNode expressionNode;
        //BooleanLiteralNode
        //BooleanOpNode
        //CharLiteralNode
        //CompareNode
        //MathOpNode
        //MethodCallExpressionNode
        //NewNode
        //NotOpNode
        //NumbericalLiteralNode
        //StringLiteralNode
        //VariableReferenceNode
        //TODO - a temporary stub out, so it will compile and execute
        return new VariableReferenceNode();
    }

    //BoolExpTerm = MethodCallExpression | (Expression ( "==" | "!=" | "<=" | ">=" | ">" | "<" ) Expression) | VariableReference
    private BooleanOpNode getBoolExpressionNode() throws SyntaxErrorException {
        //TODO - a temporary stub out, so it will compile and execute
        BooleanOpNode boolOpNode = new BooleanOpNode();
        return boolOpNode;
    }

    //Statements = INDENT Statement*  DEDENT

    //VariableReference = IDENTIFIER
    private Optional<VariableReferenceNode> getVariableReferenceNode() throws SyntaxErrorException {
        VariableReferenceNode variableReferenceNode = new VariableReferenceNode();
        Optional<Token> nextToken = tokenManager.matchAndRemove(Token.TokenTypes.WORD);
        if(nextToken.isPresent()){
            variableReferenceNode.name = nextToken.get().getValue();
            return Optional.of(variableReferenceNode);
        }
        else                        return Optional.empty();
    }

    //MethodCallExpression =  (IDENTIFIER ".")? IDENTIFIER "(" (Expression ("," Expression )* )? ")"

    //Term = Factor ( ("*"|"/"|"%") Factor )*

    //Factor = NUMBER | VariableReference |  STRINGLITERAL | CHARACTERLITERAL | MethodCallExpression | "(" Expression ")" | "new" IDENTIFIER "(" (Expression ("," Expression )*)? ")"

    /**
     * EBNF: ("else" NEWLINE (Statement | Statements))?
     * Determines if there is an else block and if there is, it returns an optional of the else statements in the
     * ElseNode Object.
     * @return  either  an empty Optional<ElseNode
     *          or      an Optional<ElseNode> holding an ElseNode containing a list of statements
     * @throws SyntaxErrorException - in the event that one of the Statements meant to be returned is in an incorrect Syntax
     */
    private Optional<ElseNode> getElseNode() throws SyntaxErrorException {
        if(tokenManager.matchAndRemove(Token.TokenTypes.ELSE).isEmpty())  return Optional.empty();
        requireNewLine();
        ElseNode elseNode = new ElseNode();
        elseNode.statements = getStatements(new IfNode());
        return Optional.of(elseNode);
    }

    //Helper Methods
    private void deletePotNewLines(){
        while(tokenManager.matchAndRemove(Token.TokenTypes.NEWLINE).isPresent());//removes all the sequential newlines
    }

    /**
     * Finds the next few variables listed in parameter format and returns them in a list of Variable Declaration Nodes
     * @return - a list of Variable Declaration Nodes of the (potential) parameter variables in tokenManager. Can be empty
     * @throws SyntaxErrorException - in the event that an extra comma is used or a keyword is used
     */
    private LinkedList<VariableDeclarationNode> getVariableDeclarations() throws SyntaxErrorException {
        LinkedList<VariableDeclarationNode> paramVarDeclNodes = new LinkedList<VariableDeclarationNode>();
        Optional<VariableDeclarationNode> nextVariableDeclaration = getVariableDeclarationNode();
        int initTokensLeft = tokenManager.tokensLeft();
        while(nextVariableDeclaration.isPresent()) {
            paramVarDeclNodes.add(nextVariableDeclaration.get());
            if(tokenManager.matchAndRemove(Token.TokenTypes.COMMA).isEmpty()) return paramVarDeclNodes;
            nextVariableDeclaration = getVariableDeclarationNode();
        }
        if(initTokensLeft != tokenManager.tokensLeft()) { //list of parameters ended with an extra comma
            throw errorNotExpectedTypes(new Token.TokenTypes[]{Token.TokenTypes.WORD, Token.TokenTypes.COMMA}, tokenManager.peek());
        } else return paramVarDeclNodes;
    }

    /**
     * Finds the next few variables listed in parameter format and returns them in a list of Variable Declaration Nodes
     * @return - a list of Variable Declaration Nodes of at least 1 parameter variable in tokenManager. Cannot be empty
     * @throws SyntaxErrorException - in the event that an extra comma is used or there is not at least 1 variable declared
     */
    private LinkedList<VariableDeclarationNode> getVariableDeclarationsRequireFirst(String typeFollowsErrorMessage)
            throws SyntaxErrorException {
        LinkedList<VariableDeclarationNode> paramVarDeclNodes = new LinkedList<VariableDeclarationNode>();
        Optional<VariableDeclarationNode> nextVariableDeclaration = getVariableDeclarationNode();
        if(nextVariableDeclaration.isEmpty()) {
            throw new SyntaxErrorException(typeFollowsErrorMessage, tokenManager.getCurrentLine(), tokenManager.getCurrentColumnNumber());
        }
        do{
            paramVarDeclNodes.add(nextVariableDeclaration.get());
            if(tokenManager.matchAndRemove(Token.TokenTypes.COMMA).isEmpty()) return paramVarDeclNodes;
            nextVariableDeclaration = getVariableDeclarationNode();
        } while (nextVariableDeclaration.isPresent());
        //there was a comma and it was not followed by a variable declaration
        throw errorNotExpectedType(Token.TokenTypes.WORD, tokenManager.peek());
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