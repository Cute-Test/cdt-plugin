package ch.hsr.ifs.cute.macronator.test.common;

import static org.junit.Assert.*;

import org.eclipse.cdt.core.parser.IToken;
import org.junit.Test;

import ch.hsr.ifs.cute.macronator.common.LexerAdapter;

public class LexerAdapterTest {	
	
	@Test
	public void testShouldReturnNextToken() {
		String input = "token";
		LexerAdapter lexerAdapter = new LexerAdapter(input);
		IToken nextToken = lexerAdapter.nextToken();
		assertEquals("token", nextToken.getImage());
		assertTrue(lexerAdapter.atEndOfInput());
	}	
	
	@Test
	public void testShouldReportEndOfInput() {
		String input = "token";
		LexerAdapter lexerAdapter = new LexerAdapter(input);
		IToken nextToken = lexerAdapter.nextToken();
		assertEquals("token", nextToken.getImage());
		assertTrue(lexerAdapter.atEndOfInput());
	}
	
	@Test
	public void testShouldReportEndOfInputCorrectlyWhenInputEmpty() {
		String emptyInput = "";
		LexerAdapter lexerAdapter = new LexerAdapter(emptyInput);
		assertTrue(lexerAdapter.atEndOfInput());
	}
}
