/**
 * 
 */
package ee.stacc.productivity.edsl.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import ee.stacc.productivity.edsl.sqlparser.IAbstractStack;
import ee.stacc.productivity.edsl.sqlparser.IParserState;

public final class SimpleStack implements IAbstractStack {

	private final List<IParserState> stack;
	
	private SimpleStack(List<IParserState> stack) {
		this.stack = stack;
	}

	SimpleStack(IParserState state) {
		this.stack = new ArrayList<IParserState>();
		stack.add(state);
	}
	
	@Override
	public Set<IAbstractStack> pop(int count) {
		return Collections.<IAbstractStack>singleton(
				new SimpleStack(
						new ArrayList<IParserState>(stack.subList(0, stack.size() - count))));
	}

	@Override
	public Set<IAbstractStack> push(IParserState state) {
		ArrayList<IParserState> newStack = new ArrayList<IParserState>(stack);
		newStack.add(state);
		return Collections.<IAbstractStack>singleton(
				new SimpleStack(
						newStack));
	}

	@Override
	public IParserState top() {
		return stack.get(stack.size() - 1);
	}

	@Override
	public String toString() {
		return stack.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((stack == null) ? 0 : stack.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleStack other = (SimpleStack) obj;
		if (stack == null) {
			if (other.stack != null)
				return false;
		} else if (!stack.equals(other.stack))
			return false;
		return true;
	}
}