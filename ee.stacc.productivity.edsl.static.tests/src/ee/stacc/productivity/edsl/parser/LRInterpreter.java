package ee.stacc.productivity.edsl.parser;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Map;

import ee.stacc.productivity.edsl.lexer.alphabet.IAbstractInputItem;
import ee.stacc.productivity.edsl.sqlparser.ErrorState;
import ee.stacc.productivity.edsl.sqlparser.IParserStack;
import ee.stacc.productivity.edsl.sqlparser.IParserState;
import ee.stacc.productivity.edsl.sqlparser.IStackFactory;
import ee.stacc.productivity.edsl.sqlparser.LRParser;

public class LRInterpreter {

	public static IParserState interpret(LRParser parser, IStackFactory<IParserStack> factory, String input,
			boolean expectingAccept) {
		Map<String, Integer> namesToTokenNumbers = parser.getNamesToTokenNumbers();
		ByteArrayOutputStream traceData = new ByteArrayOutputStream();
		PrintStream trace = new PrintStream(traceData);
		parser.setTrace(trace);
		trace.print("Parsing: " + input + "\n");
		String[] split = input.split(" ");
		trace.append("Input tokens: ");
		for (String token : split) {
			Integer tokenNumber = namesToTokenNumbers.get(token.trim());
			trace.print(tokenNumber + ",");
		}
		trace.append("\n");
		IParserStack stack = factory.newStack(parser.getInitialState());
		for (String token : split) {
			final String trimmedToken = token.trim();
			Integer tokenNumber = namesToTokenNumbers.get(trimmedToken);
			trace.append("Token: " + token + "\n");
			trace.append("Stack: " + stack + "\n");
			if (tokenNumber == null) {
				throw new IllegalArgumentException("Unknown token: " + token);
			}
			
			IParserStack newStack = parser.processToken(new IAbstractInputItem() {
				
				@Override
				public String toString() {
					return trimmedToken;
				}
				
				@Override
				public int getCode() {
					return 0;
				}
			}, tokenNumber, stack);
//			if (stacks.size() > 1) {
//				throw new IllegalStateException("Only simple stacks are supported");
//			}
			stack = newStack;
			trace.println();
		}
		IParserState top = stack.top();
		if (expectingAccept && top.isError()) {
			IAbstractInputItem unexpectedSymbol = ((ErrorState) top).getUnexpectedItem();
			System.out.println(traceData.toString());
			System.out.println(unexpectedSymbol);
//			System.out.println(parser.getSymbolNumbersToNames().get(unexpectedSymbol));
		}
		return top;
	}

}
