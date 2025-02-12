package Tests;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.util.LinkedList;
import AST.*;
import Tran.*;
public class Example4ParserTest {

	@Test
	public void Example4ParserTestTest() throws Exception {
		var tokens = new LinkedList<Token>();
		tokens.add(new Token(Token.TokenTypes.CLASS, 1, 5));
		tokens.add(new Token(Token.TokenTypes.WORD, 1, 14, "Example4"));
		tokens.add(new Token(Token.TokenTypes.NEWLINE, 2, 0));
		tokens.add(new Token(Token.TokenTypes.INDENT, 2, 4));
		tokens.add(new Token(Token.TokenTypes.WORD, 2, 10, "number"));
		tokens.add(new Token(Token.TokenTypes.WORD, 2, 12, "a"));
		tokens.add(new Token(Token.TokenTypes.NEWLINE, 3, 0));
		tokens.add(new Token(Token.TokenTypes.DEDENT, 3, 0));
		tokens.add(new Token(Token.TokenTypes.NEWLINE, 4, 0));
		tokens.add(new Token(Token.TokenTypes.INDENT, 4, 4));
		tokens.add(new Token(Token.TokenTypes.SHARED, 4, 10));
		tokens.add(new Token(Token.TokenTypes.WORD, 4, 21, "helloWorld"));
		tokens.add(new Token(Token.TokenTypes.LPAREN, 4, 22));
		tokens.add(new Token(Token.TokenTypes.RPAREN, 4, 23));
		tokens.add(new Token(Token.TokenTypes.NEWLINE, 5, 0));
		tokens.add(new Token(Token.TokenTypes.INDENT, 5, 8));
		tokens.add(new Token(Token.TokenTypes.WORD, 5, 15, "console"));
		tokens.add(new Token(Token.TokenTypes.DOT, 5, 16));
		tokens.add(new Token(Token.TokenTypes.WORD, 5, 21, "print"));
		tokens.add(new Token(Token.TokenTypes.LPAREN, 5, 22));
		tokens.add(new Token(Token.TokenTypes.QUOTEDSTRING, 5, 35, "Hello World"));
		tokens.add(new Token(Token.TokenTypes.RPAREN, 5, 36));
		tokens.add(new Token(Token.TokenTypes.NEWLINE, 6, 0));
		tokens.add(new Token(Token.TokenTypes.NEWLINE, 7, 0));
		tokens.add(new Token(Token.TokenTypes.DEDENT, 7, 4));
		tokens.add(new Token(Token.TokenTypes.WORD, 7, 7, "add"));
		tokens.add(new Token(Token.TokenTypes.LPAREN, 7, 8));
		tokens.add(new Token(Token.TokenTypes.WORD, 7, 14, "number"));
		tokens.add(new Token(Token.TokenTypes.WORD, 7, 16, "a"));
		tokens.add(new Token(Token.TokenTypes.COMMA, 7, 17));
		tokens.add(new Token(Token.TokenTypes.WORD, 7, 24, "number"));
		tokens.add(new Token(Token.TokenTypes.WORD, 7, 26, "b"));
		tokens.add(new Token(Token.TokenTypes.RPAREN, 7, 27));
		tokens.add(new Token(Token.TokenTypes.COLON, 7, 29));
		tokens.add(new Token(Token.TokenTypes.WORD, 7, 36, "number"));
		tokens.add(new Token(Token.TokenTypes.WORD, 7, 40, "sum"));
		tokens.add(new Token(Token.TokenTypes.NEWLINE, 8, 0));
		tokens.add(new Token(Token.TokenTypes.INDENT, 8, 8));
		tokens.add(new Token(Token.TokenTypes.WORD, 8, 11, "sum"));
		tokens.add(new Token(Token.TokenTypes.ASSIGN, 8, 13));
		tokens.add(new Token(Token.TokenTypes.WORD, 8, 15, "a"));
		tokens.add(new Token(Token.TokenTypes.PLUS, 8, 17));
		tokens.add(new Token(Token.TokenTypes.WORD, 8, 19, "b"));
		tokens.add(new Token(Token.TokenTypes.NEWLINE, 9, 0));
		tokens.add(new Token(Token.TokenTypes.NEWLINE, 10, 0));
		tokens.add(new Token(Token.TokenTypes.DEDENT, 10, 4));
		tokens.add(new Token(Token.TokenTypes.PRIVATE, 10, 11));
		tokens.add(new Token(Token.TokenTypes.WORD, 10, 16, "setA"));
		tokens.add(new Token(Token.TokenTypes.LPAREN, 10, 17));
		tokens.add(new Token(Token.TokenTypes.RPAREN, 10, 18));
		tokens.add(new Token(Token.TokenTypes.NEWLINE, 11, 0));
		tokens.add(new Token(Token.TokenTypes.INDENT, 11, 8));
		tokens.add(new Token(Token.TokenTypes.WORD, 11, 9, "a"));
		tokens.add(new Token(Token.TokenTypes.ASSIGN, 11, 11));
		tokens.add(new Token(Token.TokenTypes.NUMBER, 11, 14, "42"));
		tokens.add(new Token(Token.TokenTypes.DEDENT, 11, 15));
		tokens.add(new Token(Token.TokenTypes.DEDENT, 11, 15));
		var tran = new TranNode();
		var p = new Parser(tran,tokens);
		p.Tran();
		Assertions.assertEquals(1,tran.Classes.size());
		Assertions.assertEquals("Example4",tran.Classes.get(0).name);
		Assertions.assertEquals(0,tran.Classes.get(0).interfaces.size());
		Assertions.assertEquals(0,tran.Classes.get(0).constructors.size());
		Assertions.assertEquals(0,tran.Classes.get(0).methods.size());
		Assertions.assertEquals(1,tran.Classes.get(0).members.size());
		Assertions.assertEquals("a",((MemberNode)tran.Classes.get(0).members.get(0)).declaration.name);
		Assertions.assertEquals("number",((MemberNode)tran.Classes.get(0).members.get(0)).declaration.type);
		Assertions.assertEquals(0,tran.Interfaces.size());
		Assertions.assertEquals("Example4",tran.Classes.get(0).name);
		Assertions.assertEquals(0,tran.Classes.get(0).interfaces.size());
		Assertions.assertEquals(0,tran.Classes.get(0).constructors.size());
		Assertions.assertEquals(0,tran.Classes.get(0).methods.size());
		Assertions.assertEquals(1,tran.Classes.get(0).members.size());
		Assertions.assertEquals("a",((MemberNode)tran.Classes.get(0).members.get(0)).declaration.name);
		Assertions.assertEquals("number",((MemberNode)tran.Classes.get(0).members.get(0)).declaration.type);
	}
}
