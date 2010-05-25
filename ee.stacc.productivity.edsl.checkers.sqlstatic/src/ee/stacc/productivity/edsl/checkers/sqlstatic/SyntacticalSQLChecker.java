package ee.stacc.productivity.edsl.checkers.sqlstatic;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import ee.stacc.productivity.edsl.checkers.IAbstractStringChecker;
import ee.stacc.productivity.edsl.checkers.ISQLErrorHandler;
import ee.stacc.productivity.edsl.checkers.IStringNodeDescriptor;
import ee.stacc.productivity.edsl.common.logging.ILog;
import ee.stacc.productivity.edsl.common.logging.Logs;
import ee.stacc.productivity.edsl.lexer.alphabet.IAbstractInputItem;
import ee.stacc.productivity.edsl.lexer.alphabet.Token;
import ee.stacc.productivity.edsl.lexer.automata.State;
import ee.stacc.productivity.edsl.sqlparser.IParseErrorHandler;
import ee.stacc.productivity.edsl.sqlparser.SQLSyntaxChecker;
import ee.stacc.productivity.edsl.string.IAbstractString;
import ee.stacc.productivity.edsl.string.IPosition;
import ee.stacc.productivity.edsl.string.StringChoice;

public class SyntacticalSQLChecker implements IAbstractStringChecker {

	private static int SIZE_THRESHOLD = 25000;
	private static final ILog LOG = Logs.getLog(SyntacticalSQLChecker.class);
	
	@Override
	public void checkAbstractStrings(List<IStringNodeDescriptor> descriptors,
			final ISQLErrorHandler errorHandler, Map<String, Object> options) {
		for (final IStringNodeDescriptor descriptor : descriptors) {
			IAbstractString abstractString = descriptor.getAbstractValue();
			if (!hasAcceptableSize(abstractString)) {
				if (abstractString instanceof StringChoice) { // This may make things slower, but more precise 
					StringChoice choice = (StringChoice) abstractString;
					boolean hasBigSubstrings = false;
					boolean hasSmallSubstrings = false;
					for (IAbstractString option : choice.getItems()) {
						if (!hasAcceptableSize(option)) {
							hasBigSubstrings = true;
						} else {
							try {
								checkStringOfAppropriateSize(errorHandler, descriptor, option);
								hasSmallSubstrings = true;
							} catch (StackOverflowError e) { // TODO: This hack is no good. May be it can be fixed in the FixpointParser   
								hasBigSubstrings = true;
							}
						}
					}
					if (hasBigSubstrings) {
						errorHandler.handleSQLWarning("Abstract string is too big" + (hasSmallSubstrings ? ". Only some parts checked" : ""), descriptor.getPosition());
					}
				} else {
					errorHandler.handleSQLWarning("Abstract string is too big", descriptor.getPosition());
				}
			} else {
				checkStringOfAppropriateSize(errorHandler, descriptor, abstractString);
			}
		}
	}

	private void checkStringOfAppropriateSize(
			final ISQLErrorHandler errorHandler,
			final IStringNodeDescriptor descriptor,
			IAbstractString abstractString) {
		try {
			State automaton = PositionedCharacterUtil.createPositionedAutomaton(abstractString);
			
			SQLSyntaxChecker.INSTANCE.checkAutomaton(automaton, new IParseErrorHandler() {
				
				@Override
				public void unexpectedItem(IAbstractInputItem item) {
					Collection<IPosition> markerPositions = PositionedCharacterUtil.getMarkerPositions(((Token) item).getText());
					for (IPosition pos : markerPositions) {
						errorHandler.handleSQLError("Unexpected token: " + PositionedCharacterUtil.render(item), pos);
					}
				}

				@Override
				public void other() {
					errorHandler.handleSQLError("SQL syntax error. Most likely, unfinished query", descriptor.getPosition());
				}
			});
		} catch (MalformedStringLiteralException e) {
			IPosition errorPosition = e.getLiteralPosition();
			if (errorPosition == null) {
				errorPosition = descriptor.getPosition(); 
			}
			errorHandler.handleSQLError("Malformed literal: " + e.getMessage(), errorPosition);
		} catch (StackOverflowError e) {  // TODO: This hack is no good (see method above)
			throw e;
		} catch (Throwable e) {
			LOG.exception(e);
			errorHandler.handleSQLError("Static checker internal error: " + e.toString(), descriptor.getPosition());
		}
	}

	public static boolean hasAcceptableSize(IAbstractString abstractString) {
		return AbstractStringSizeCounter.size(abstractString) <= SIZE_THRESHOLD;
	}
}
