package ee.stacc.productivity.edsl.lexer.automata;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ee.stacc.productivity.edsl.lexer.CharacterSetFactory;
import ee.stacc.productivity.edsl.lexer.ICharacterSet;

public class AutomataParser {

	public static class Automaton {
		private final Set<State> states;
		private final State initialState;
		
		public Automaton(
				Set<ee.stacc.productivity.edsl.lexer.automata.State> states,
				ee.stacc.productivity.edsl.lexer.automata.State initialState) {
			this.states = states;
			this.initialState = initialState;
		}

		public Set<State> getStates() {
			return states;
		}

		public State getInitialState() {
			return initialState;
		}
		
		public String toString() {
			StringBuilder stringBuilder = new StringBuilder();
			for (State state : states) {
				if (initialState == state) {
					stringBuilder.append("->");
				}
				stringBuilder
					.append(state);
				if (!state.getOutgoingTransitions().isEmpty()) {
					stringBuilder.append(" -> ");
				}
				for (Transition transition : state.getOutgoingTransitions()) {
					stringBuilder
						.append(transition.getTo())
						.append(transition.getInSet().toString()
								.replace('[', '{')
								.replace(']', '}'))
						.append("/")
						.append(transition.getOutStr())
						.append("/")
						.append(" ");
				}
				stringBuilder.append(";\n");
			}
			return stringBuilder.toString();
		}
	}
	
	public static Automaton parse(String text) {
		text = text.replace('-', ' ');
		String[] lines = text.split(";");
		
		Map<String, State> map = new HashMap<String, State>();
		State initialState = null;
		for (String line : lines) {
			Pattern pattern = Pattern.compile("(!?[A-Za-z0-9]+)(:([A-Za-z0-9_\"]))?(/([A-Za-z0-9_]?))?");
			Matcher matcher = pattern.matcher(line);
			if (!matcher.find()) {
				continue;
			}
			
			State from = getState(map, matcher.group(1));
			if (initialState == null) {
				initialState = from;
			}
			while (matcher.find()) {
				State to = getState(map, matcher.group(1));
				char c = matcher.group(3).charAt(0);
				ICharacterSet range = CharacterSetFactory.range(c, c);
				String outStr = matcher.group(5);
				Transition transition = new Transition(from, to, range, outStr == null ? "" : outStr);
				from.getOutgoingTransitions().add(transition);
				to.getIncomingTransitions().add(transition);
			}
		}
		return new Automaton(new HashSet<State>(map.values()), initialState);
	}
	
	private static State getState(Map<String, State> map, String name) {
		State state = map.get(name);
		if (state == null) {
			state = new State(name, name.startsWith("!"));
			map.put(name, state);
		}
		return state;
	}	
	
}
