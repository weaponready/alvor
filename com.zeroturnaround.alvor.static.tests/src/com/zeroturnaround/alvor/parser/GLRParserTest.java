package com.zeroturnaround.alvor.parser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import org.jdom.JDOMException;
import org.junit.Test;

import com.zeroturnaround.alvor.sqlparser.GLRParser;
import com.zeroturnaround.alvor.sqlparser.GLRStack;
import com.zeroturnaround.alvor.sqlparser.ILRParser;
import com.zeroturnaround.alvor.sqlparser.ParserSimulator;
import com.zeroturnaround.alvor.string.IAbstractString;
import com.zeroturnaround.alvor.string.StringConstant;
import com.zeroturnaround.alvor.string.parser.AbstractStringParser;


public class GLRParserTest {

	private static final ParserSimulator<GLRStack> GLR_INSTANCE = new ParserSimulator<GLRStack>(loadParser(), GLRStack.FACTORY);

	private static ILRParser<GLRStack> loadParser() {
		try {
			return GLRParser.build(GLRParserTest.class.getClassLoader().getResource("glr.xml"));
		} catch (MalformedURLException e) {
			throw new AssertionError(e);
		} catch (JDOMException e) {
			throw new AssertionError(e);
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}
	
	@Test
	public void test() throws Exception {
		String text;

		text = "\"1\" (\"+ 1\")+";
		assertParses(AbstractStringParser.parseOneFromString(text));

		text = "\"1\" (\"* 1\")+";
		assertParses(AbstractStringParser.parseOneFromString(text));
		
		text = "1 ";
		assertParses(new StringConstant(text));
		
		text = "1 . 2 ";
		assertParsesNot(new StringConstant(text));
		text = "1 . 2 + 1 . 2 ";
		assertParsesNot(new StringConstant(text));
		text = "1 + 1 . 2";
		assertParsesNot(new StringConstant(text));
		text = "1 . 2 +  ";
		assertParsesNot(new StringConstant(text));
		text = "1 . 2 + 1";
		assertParsesNot(new StringConstant(text));
		text = "+ 1 . 2 ";
		assertParsesNot(new StringConstant(text));
	}

	private void assertParses(IAbstractString str) {
		List<String> check = GLR_INSTANCE.check(str);
		assertTrue(check.toString(), check.isEmpty());
	}

	private void assertParsesNot(IAbstractString str) {
		List<String> check = GLR_INSTANCE.check(str);
		assertFalse(check.isEmpty());
	}
}