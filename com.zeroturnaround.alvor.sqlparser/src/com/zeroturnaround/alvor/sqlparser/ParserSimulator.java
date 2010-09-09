package com.zeroturnaround.alvor.sqlparser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.zeroturnaround.alvor.lexer.alphabet.IAbstractInputItem;
import com.zeroturnaround.alvor.lexer.alphabet.Token;
import com.zeroturnaround.alvor.lexer.automata.AutomataTransduction;
import com.zeroturnaround.alvor.lexer.automata.EmptyTransitionEliminator;
import com.zeroturnaround.alvor.lexer.automata.IAlphabetConverter;
import com.zeroturnaround.alvor.lexer.automata.State;
import com.zeroturnaround.alvor.lexer.automata.StringToAutomatonConverter;
import com.zeroturnaround.alvor.lexer.automata.Transition;
import com.zeroturnaround.alvor.lexer.automata.EmptyTransitionEliminator.IEmptinessExpert;
import com.zeroturnaround.alvor.lexer.sql.SQLLexer;
import com.zeroturnaround.alvor.string.IAbstractString;

/**
 * Interpreter that performs the abstract parsing.
 * 
 * @author abreslav
 *
 * @param <S> type of stacks to work with.
 */
public class ParserSimulator<S extends IParserStackLike> {

	private static ParserSimulator<IParserStack> LALR_INSTANCE = null;
	
	private static ParserSimulator<GLRStack> GLR_INSTANCE = null;
	
	/**
	 * Normal LR-parsing with bounded stacks
	 */
	public static ParserSimulator<IParserStack> getLALRInstance() {
		if (LALR_INSTANCE == null) {
			LALR_INSTANCE = new ParserSimulator<IParserStack>(Parsers.getLALRParserForSQL(), BoundedStack.getFactory(100, null));
		}
		return LALR_INSTANCE;
	}
	
	/**
	 * GLR-parsing with bounded stacks (see GLRStack.FACTORY)
	 */
	public static ParserSimulator<GLRStack> getGLRInstance() {
		if (GLR_INSTANCE == null) {
			GLR_INSTANCE = new ParserSimulator<GLRStack>(Parsers.getGLRParserForSQL(), GLRStack.FACTORY);
		}
		return GLR_INSTANCE;
	}

	private final IStackFactory<S> stackFactory;
	private final ILRParser<S> parser;
	// For debugging purposes only
	public long allTime;

	public ParserSimulator(ILRParser<S> parser, IStackFactory<S> factory) {
		this.stackFactory = factory;
		this.parser = parser;
	}

	/**
	 * Performs abstract parsing, returns a list of error messages. Used for testing
	 * @param str the abstract string to parser
	 * @return a list of error messages
	 */
	public List<String> check(IAbstractString str) {
		final List<String> errors = new ArrayList<String>();
		State asAut = StringToAutomatonConverter.INSTANCE.convert(str);
		
		checkAutomaton(parser, asAut, stackFactory, new IParseErrorHandler() {
			
			@Override
			public void unexpectedItem(IAbstractInputItem item) {
				if (item instanceof Token) {
					Token token = (Token) item;
					errors.add("Unexpected token: " + SQLLexer.tokenToString(token));
				} else {
					errors.add("Unexpected token: " + parser.getSymbolNumbersToNames().get(item.getCode()));
				}
			}
			
			@Override
			public void other() {
				if (errors.isEmpty()) {
					errors.add("SQL syntax error. Most likely unfinished query");
				}
			}
			
			@Override
			public void overabstraction() {
				errors.add("Syntax analysis failed: recursion is too deep");
			}
		});
		return errors;
	}

	/**
	 * Parses the given automaton and reports the errors to the given error handler
	 */
	public void checkAutomaton(State asAut, IParseErrorHandler errorHandler) {
		checkAutomaton(parser, asAut, stackFactory, errorHandler);
	}

	private void checkAutomaton(final ILRParser<S> parser, State asAut, IStackFactory<S> stackFactory, IParseErrorHandler errorHandler) {
		State sqlTransducer = SQLLexer.SQL_TRANSDUCER;
		State transduction = AutomataTransduction.INSTANCE.getTransduction(sqlTransducer, asAut, SQLLexer.SQL_ALPHABET_CONVERTER);
		long time = System.nanoTime();
		transduction = EmptyTransitionEliminator.INSTANCE.eliminateEmptySetTransitions(transduction, new IEmptinessExpert() {
			
			@Override
			public boolean isEmpty(Transition transition) {
				return transition.isEmpty() || SQLLexer.isWhitespace(transition.getInChar().getCode());
			}
		});
		
		final Integer eofTokenIndex = parser.getNamesToTokenNumbers().get("$end");
		IAlphabetConverter converter = new IAlphabetConverter() {
			
			@Override
			public int convert(int c) {
				if (c == -1) {
					return eofTokenIndex;
				}
				String tokenName = SQLLexer.getTokenName(c);
				if (Character.isLetter(tokenName.charAt(0))) {
					return parser.getNamesToTokenNumbers().get(tokenName);
				}
				Integer tokenNumber = parser.getNamesToTokenNumbers().get("'" + tokenName + "'");
				if (tokenNumber == null) {
					tokenNumber = parser.getNamesToTokenNumbers().get("error");
					if (tokenNumber == null) {
						throw new IllegalArgumentException("Unknown token: " + tokenName);
					}
				}
				return tokenNumber;
			}
		};
		FixpointParser<S> fixpointParser = new FixpointParser<S>(parser, converter, SimpleStackSet.<S>getFactory(), stackFactory, eofTokenIndex, errorHandler);
		fixpointParser.parse(transduction);
		time = System.nanoTime() - time;
		allTime += time;
	}
	
	/**
	 * Used for testing.
	 */
	public boolean parseAutomaton(State initial, IAlphabetConverter alphabetConverter) {
		return new FixpointParser<S>(
				parser, 
				alphabetConverter, 
				SimpleStackSet.<S>getFactory(), 
				stackFactory, 
				parser.getEofTokenIndex(),
				IParseErrorHandler.NONE)
			.parse(initial);
	}
	
	/**
	 * A set of stacks of type <S>. 
	 */
	private interface IStackSet<S> {

		/**
		 * Is there a stack with an error in this set
		 */
		IParserState hasError();
		
		/**
		 * @return true if there was an actual change 
		 */
		boolean add(S stack);
		
		/**
		 * @return a java.util.Set of all IAbstractStack objects, which is 
		 *         tolerant for concurrent modification (e.g., a copy)
		 */
		Set<S> asJavaSet();
	}

	/**
	 * A naive implementation of a stack set. May be worth it to optimize it to boost the performance. 
	 */
	private static class SimpleStackSet<S extends IParserStackLike> implements IStackSet<S> {
		
		public static <S extends IParserStackLike> IStackSetFactory<S> getFactory() {
			return new IStackSetFactory<S>() {
				
				@Override
				public IStackSet<S> newAbstractStackSet() {
					return new SimpleStackSet<S>();
				}
			};
		}

		private final Set<S> stacks = new HashSet<S>();
		
		@Override
		public IParserState hasError() {
			for (S stack : stacks) {
				IParserState errorOnTop = stack.getErrorOnTop();
				if (errorOnTop != null) {
					return errorOnTop;
				}
			}
			return null;
		}

		@Override
		public Set<S> asJavaSet() {
			return new HashSet<S>(stacks);
		}

		@Override
		public boolean add(S stack) {
			return stacks.add(stack);
		}
		
		@Override
		public String toString() {
			return stacks.toString();
		}
		
	}
	
	/**
	 * A factory for stack sets
	 */
	public interface IStackSetFactory<S> {
		IStackSet<S> newAbstractStackSet();
	}
	
	/**
	 * The parsing table interpreter itself (uses {@link ILRParser} to do the job). 
	 * Computes until a fix-point or an error. 
	 *
	 * @param <S> the type of stacks to work with.
	 */
	private static final class FixpointParser<S extends IParserStackLike> {
	
		/**
		 * Maps an automaton state to a set of stacks with which we come to this state 
		 */
		private final Map<State, IStackSet<S>> abstractStackSets = new HashMap<State, IStackSet<S>>();
		/**
		 * Converts from lexer's output alphabet to parser's input alphabet
		 */
		private final IAlphabetConverter alphabetConverter;
		/**
		 * the parser to interpret
		 */
		private final ILRParser<S> parser;
		/**
		 * A factory to create new stack sets
		 */
		private final IStackSetFactory<S> factory;
		/**
		 * A factory to create new stacks
		 */
		private final IStackFactory<S> stackFactory;
		/**
		 * An index of the EOF (end of file) token in the parser's input alphabet
		 */
		private final int eofTokenIndex;
		/**
		 * The error handler to report errors to
		 */
		private final IParseErrorHandler errorHandler; 
		
		/**
		 * See fields for parameter meanings 
		 */
		public FixpointParser(ILRParser<S> parser,
				IAlphabetConverter alphabetConverter,
				IStackSetFactory<S> factory,
				IStackFactory<S> stackFactory,
				int eofTokenIndex,
				IParseErrorHandler parseErrorHandler) {
			this.parser = parser;
			this.alphabetConverter = alphabetConverter;
			this.factory = factory;
			this.stackFactory = stackFactory;
			this.eofTokenIndex = eofTokenIndex;
			this.errorHandler = parseErrorHandler;
		}
		
		private IStackSet<S> getSet(State state) {
			IStackSet<S> set = abstractStackSets.get(state);
			if (set == null) {
				set = factory.newAbstractStackSet();
				abstractStackSets.put(state, set);
			}
			return set;
		}

		/**
		 * Starts abstract parsing on the automaton represented by its initial state
		 * @param initial initial state of the automaton to parse
		 * @return true iff there was no parsing errors
		 */
		public boolean parse(State initial) {
			S initialStack = stackFactory.newStack(parser.getInitialState());
			getSet(initial).add(initialStack);
			return dfs(initial);
		}
	
		private boolean dfs(State current) {
			IStackSet<S> setForCurrent = getSet(current);
			
			if (current.isAccepting()) {
				if (!closeWithEof(setForCurrent)) {
					errorHandler.other();
					return false;
				}
			}
			
			for (Transition transition : current.getOutgoingTransitions()) {
				if (transition.isEmpty()) {
					throw new IllegalArgumentException("Empty transitions are not supported");
				}
				int tokenIndex = alphabetConverter.convert(transition.getInChar().getCode());
				IStackSet<S> setForTo = getSet(transition.getTo());
				boolean changes = transformSet(setForCurrent, transition.getInChar(), tokenIndex, setForTo);
				if (changes) {
					IParserState errorState = setForTo.hasError();
					if (errorState != null) {
						IAbstractInputItem unexpectedItem = ((ErrorState) errorState).getUnexpectedItem();
						if (unexpectedItem == null) {
							errorHandler.overabstraction();
						} else if (unexpectedItem.getCode() >= 0) {
							errorHandler.unexpectedItem(unexpectedItem);
						} else {
							errorHandler.other();
						}
						return false;
					}
					if (!dfs(transition.getTo())) {
						return false;
					}
				}
			}
			
			return true;
		}

		private boolean transformSet(IStackSet<S> setForCurrent,
				IAbstractInputItem token, int tokenIndex, IStackSet<S> setForTo) {
			Collection<S> hashSet = setForCurrent.asJavaSet();
 			boolean changes = false;
			for (S stack : hashSet) {
				S newStack = parser.processToken(token, tokenIndex, stack);
				if (setForTo.add(newStack)) {
					changes = true;
				}
			}
			return changes;
		}

		private boolean closeWithEof(IStackSet<S> setForCurrent) {
			for (S stack : setForCurrent.asJavaSet()) {
				while (true) {
					S newStack = parser.processToken(IAbstractInputItem.EOF, eofTokenIndex, stack);
					if (newStack.hasErrorOnTop()) {
						return false;
					}
					if (newStack.topAccepts()) {
						break;
					}
					stack = newStack;
				}
			}
			return true;
		}
	}
}