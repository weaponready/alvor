package ee.stacc.productivity.edsl.lexer.automata;

import static ee.stacc.productivity.edsl.sqllexer.SQLLexerData.*;

public class AutomataConverter {

	public static final AutomataConverter INSTANCE = new AutomataConverter(); 
	
	public State convert() {
		// Create states
		State[] states = new State[STATE_COUNT];
		for (int i = 0; i < states.length; i++) {
			states[i] = new State("S" + i, isAccepting(i));
		}
		
		// Create transitions
		for (int fromIndex = 0; fromIndex < STATE_COUNT; fromIndex++) {
			State from = states[fromIndex];
			for (char cc = 0; cc < CHAR_CLASS_COUNT; cc++) {
				int toIndex = TRANSITIONS[fromIndex][cc];
				if (toIndex != -1) {
					String out = "";
					if (isGenerating(toIndex)) {
						int action = ACTIONS[toIndex];
					    out = action >= 0 ? "" + (char) action : "";
					    if (out.length() > 0) {
					    	System.out.println("out = " + action);
					    }
					}
					Transition transition = new Transition(from, states[toIndex], cc, out);
					from.getOutgoingTransitions().add(transition);
				}
			}
			if (isAccepting(fromIndex)) {
				from.getOutgoingTransitions().add(new Transition(from, states[0], null, 
						isGenerating(fromIndex) ? "" : "" + (char) ACTIONS[fromIndex]));
			}
		}
		
		return states[0];
	}
	
	private boolean isAccepting(int state) {
		return (ATTRIBUTES[state] & 0001) != 0;
	}

	private boolean isGenerating(int state) {
		return (ATTRIBUTES[state] & 0010) != 0;
	}
}
