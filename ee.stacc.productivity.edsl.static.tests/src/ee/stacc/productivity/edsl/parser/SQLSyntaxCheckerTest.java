package ee.stacc.productivity.edsl.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ee.stacc.productivity.edsl.checkers.sqlstatic.AbstractStringSizeCounter;
import ee.stacc.productivity.edsl.checkers.sqlstatic.SyntacticalSQLChecker;
import ee.stacc.productivity.edsl.sqlparser.ParserSimulator;
import ee.stacc.productivity.edsl.string.IAbstractString;
import ee.stacc.productivity.edsl.string.parser.AbstractStringParser;
import ee.stacc.productivity.edsl.string.util.AbstractStringOptimizer;


@RunWith(Parameterized.class)
public class SQLSyntaxCheckerTest {

	@Parameters
	public static Collection<Object[]> parameters() throws FileNotFoundException {
		List<Object[]> result = new ArrayList<Object[]>();
/*
*/		
		addFromFile("data/sql_sub_all_ok.txt", true, result);
		addFromFile("data/sql_sub_fail.txt", false, result);
		addFromFile("data/compiere_ok.txt", true, result);
		addFromFile("data/compiere_fail.txt", false, result);
		addFromFile("data/earved_sqls.txt", true, result);
		addFromFile("data/earved_escape.txt", true, result);
		addFromFile("data/earved_basic.txt", true, result);
		addFromFile("data/earved_bugs.txt", false, result);
		addFromFile("data/expect_fail.txt", false, result);
//		addFromFile("data/big.txt", false, result);
//		addFromFile("data/new_big.txt", false, result);
		
//		System.out.println(result.size());
		
		return result;
	}

	private static void addFromFile(String fileName, boolean expected,
			List<Object[]> result) throws FileNotFoundException {
		List<IAbstractString> abstractStrings = AbstractStringParser.parseFile(fileName);
		
		for (IAbstractString str : abstractStrings) {
			result.add(new Object[] {str, expected});
		}
	}
	
	private final IAbstractString abstractString;
	private final boolean expected;
	
	public SQLSyntaxCheckerTest(IAbstractString abstractString, boolean expected) {
		this.abstractString = abstractString;
		this.expected = expected;
	}

	@Test
	public void testSQL() throws Exception {
		IAbstractString optimized = abstractString;
		int size = AbstractStringSizeCounter.size(optimized);
		optimized = AbstractStringOptimizer.optimize(abstractString);
		size = size - AbstractStringSizeCounter.size(optimized);
		if (size > 0) {
//			System.out.println(size);
		}
		assertTrue("String is too big: " + AbstractStringSizeCounter.size(optimized), SyntacticalSQLChecker.hasAcceptableSize(optimized));
//		List<String> errors = SQLSyntaxChecker.INSTANCE.check(optimized);
		List<String> errors = ParserSimulator.LALR_INSTANCE.check(optimized);
		assertEquals(errors + "   " + optimized.toString(), expected, errors.isEmpty());
	}
	
//	@Test
//	public void test() throws Exception {
//		assertFalse(check("\" 1asd\""));
//		assertFalse(check("\" 'sadfasf\""));
//		assertFalse(check("\" №\""));
//		assertTrue(check("\"SELECT a FROM b\""));
//	}
//
//	@Test(expected = IllegalArgumentException.class)
//	public void testLoopChecker() throws Exception {
//		check("(\"a\")+");
//		fail();
//	}
//	
//	private boolean check(String astr) {
//		return SQLSyntaxChecker.INSTANCE.check(AbstractStringParser.parseOneFromString(astr)).isEmpty();
//	}
}
