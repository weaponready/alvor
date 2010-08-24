package ee.stacc.productivity.edsl.string;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ee.stacc.productivity.edsl.string.parser.AbstractStringParser;
import ee.stacc.productivity.edsl.string.util.AbstractStringOptimizer;


public class StringOptimizerTest {

	@Test
	public void test() throws Exception {
		IAbstractString string = AbstractStringParser.parseOneFromString("{{\"SELECT x FROM a\" \"WHERE\"  \"a\" , \"SELECT x FROM a\" \"WHERE\" } \"b\" , {\"SELECT x FROM a\" \"WHERE\"  \"a\" , \"SELECT x FROM a\" \"WHERE\" } \"c\" }");
		IAbstractString expected = AbstractStringParser.parseOneFromString("\"SELECT x FROM a\" \"WHERE\" {\"a\",} {\"b\", \"c\"}  \n");
		IAbstractString optimized = AbstractStringOptimizer.optimize(string);
		assertEquals(expected.toString().replaceAll(" ", ""), optimized.toString().replaceAll(" ", ""));
	}
}

/*
{
	{"SELECT x FROM a" "WHERE" "a" , "SELECT x FROM a" "WHERE" } "b" , 
	{"SELECT x FROM a" "WHERE" "a" , "SELECT x FROM a" "WHERE" } "c" 
} 

*/