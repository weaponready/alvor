package ee.stacc.productivity.edsl.lexer.automata;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashSet;

import org.junit.Test;

import ee.stacc.productivity.edsl.lexer.automata.AutomataParser.Automaton;


public class AutomataInclusionTest {

	@Test
	public void testInclusion() throws Exception {
		String automaton1;
		String automaton2;

		
		automaton1 = "A - !B:q;";
		automaton2 = automaton1;
		assertTrue(
				AutomataInclusion.INSTANCE.checkInclusion(
						AutomataParser.parse(automaton1).getInitialState(), 
						AutomataParser.parse(automaton2).getInitialState())
		);

		
		automaton1 = "!A - !B:q;";
		automaton2 = automaton1;
		assertTrue(
				AutomataInclusion.INSTANCE.checkInclusion(
						AutomataParser.parse(automaton1).getInitialState(), 
						AutomataParser.parse(automaton2).getInitialState())
		);
		
		automaton1 = "!A - !A:q;";
		automaton2 = automaton1;
		assertTrue(
				AutomataInclusion.INSTANCE.checkInclusion(
						AutomataParser.parse(automaton1).getInitialState(), 
						AutomataParser.parse(automaton2).getInitialState())
		);
		
		
		automaton1 = 
				"!A - B:q C:x;" +
				"B - B:k C:m;" +
				"C - !D:l";
		automaton2 = automaton1;
		assertTrue(
				AutomataInclusion.INSTANCE.checkInclusion(
						AutomataParser.parse(automaton1).getInitialState(), 
						AutomataParser.parse(automaton2).getInitialState())
		);

		
		automaton1 = "!A - !A:x;";
		automaton2 = 
			"!A - !B:x;" +
			"!B - !B:x";
		assertTrue(
				AutomataInclusion.INSTANCE.checkInclusion(
						AutomataParser.parse(automaton1).getInitialState(), 
						AutomataParser.parse(automaton2).getInitialState())
		);
		
		
		automaton1 = "!A - !A:x;";
		automaton2 = "!A1 - !B1:y;";
		assertFalse(
				AutomataInclusion.INSTANCE.checkInclusion(
						AutomataParser.parse(automaton1).getInitialState(), 
						AutomataParser.parse(automaton2).getInitialState())
		);
		
		
		automaton1 = "!A - !A:x;";
		automaton2 = 
				"!A1 - !B1:x;" +
				"!B1 - !B1:y";
		assertFalse(
				AutomataInclusion.INSTANCE.checkInclusion(
						AutomataParser.parse(automaton1).getInitialState(), 
						AutomataParser.parse(automaton2).getInitialState())
		);
		
		
		automaton1 = "!A - !A:x;";
		automaton2 = 
			"!A1 - !B1:y;" +
			"!B1 - !B1:x";
		assertFalse(
				AutomataInclusion.INSTANCE.checkInclusion(
						AutomataParser.parse(automaton1).getInitialState(), 
						AutomataParser.parse(automaton2).getInitialState())
		);
		
		
		automaton1 = 
			"S1 - !S2:a;" +
			"!S2 - S3:b;" +
			"S3 - S2:a";
		automaton2 = 
			"A1 - A2:a !A3:a;" +
			"!A2 - A1:b";
		assertTrue(
				AutomataInclusion.INSTANCE.checkInclusion(
						AutomataParser.parse(automaton1).getInitialState(), 
						AutomataParser.parse(automaton2).getInitialState())
		);
		
		
		automaton1 = 
			"S1 - !S2:a;" +
			"!S2 - S3:b;" +
			"S3 - S2:a";
		automaton2 = 
			"!A1 - !A2:a !A3:a;" +
			"!A2 - !A1:b";
		assertFalse(
				AutomataInclusion.INSTANCE.checkInclusion(
						AutomataParser.parse(automaton1).getInitialState(), 
						AutomataParser.parse(automaton2).getInitialState())
		);
		
		
		automaton1 = 
			"S1 - !S2:a S1:x;" +
			"!S2 - S3:b;" +
			"S3 - S2:a";
		automaton2 = 
			"A1 - A2:a !A3:a;" +
			"!A2 - A1:b";
		assertTrue(
				AutomataInclusion.INSTANCE.checkInclusion(
						AutomataParser.parse(automaton1).getInitialState(), 
						AutomataParser.parse(automaton2).getInitialState())
		);
		
		
		automaton1 = 
			"S1 - !S2:a;" +
			"!S2 - S3:b;" +
			"S3 - S2:a";
		automaton2 = 
			"A1 - A2:a !A3:a A1:x;" +
			"!A2 - A1:b";
		assertFalse(
				AutomataInclusion.INSTANCE.checkInclusion(
						AutomataParser.parse(automaton1).getInitialState(), 
						AutomataParser.parse(automaton2).getInitialState())
		);
		

		// aa*
		automaton1 = 
			"A1 - !A2:a;" +
			"!A2 - !A2:a A3:b";
		// aaa*
		automaton2 = 
			"S1 - S2:a;" +
			"S2 - !S3:a;" +
			"!S3 - !S3:a;";
		assertTrue(
				AutomataInclusion.INSTANCE.checkInclusion(
						AutomataParser.parse(automaton1).getInitialState(), 
						AutomataParser.parse(automaton2).getInitialState())
		);
		
		
		// VV*SS*V | SS*V | V
		automaton1 = 
			"C1 - !C13:V C2:S;" +
			"C2 - C2:S !C3:V;" +
			"!C13 - !C13:V C6:S;" +
			"C6 - C6:S !C7:V;";
		automaton2 = 
			"A1 - !A3:V A4:S;" +
			"!A3 - !A3:V A4:S;" +
			"A4 - A4:S !A5:V;";
		assertTrue(
				AutomataInclusion.INSTANCE.checkInclusion(
						AutomataParser.parse(automaton1).getInitialState(), 
						AutomataParser.parse(automaton2).getInitialState())
		);
		
		
		automaton1 = 
			"C1 - C1:V C2:S !C3:V;" +
			"C2 - C2:S !C3:V;";
		automaton2 = 
			"A1 - A4:S !A5:V A1:V;" +
			"A4 - A4:S !A5:V;" +
			"!A5;";
		assertTrue(
				AutomataInclusion.INSTANCE.checkInclusion(
						AutomataDeterminator.determinate(AutomataParser.parse(automaton1).getInitialState()), 
						AutomataDeterminator.determinate(AutomataParser.parse(automaton2).getInitialState()))
		);
		
		
		automaton1 = 
			"C1 - !C13:V C2:S;" +
			"C2 - C2:S !C3:V;" +
			"!C13 - !C13:V C6:S;" +
			"C6 - C6:S !C7:V;";
		automaton2 = 
			"A1 - A4:S !A5:V A1:V;" +
			"A4 - A4:S !A5:V;" +
			"!A5;";
		assertTrue(
				AutomataInclusion.INSTANCE.checkInclusion(
						AutomataParser.parse(automaton1).getInitialState(), 
						AutomataDeterminator.determinate(AutomataParser.parse(automaton2).getInitialState()))
		);
		
	}
	
	
	@Test
	public void testTransduction() throws Exception {
		String automatonStr;
		String transducerStr;
		String checkStr;
		Automaton automaton;
		Automaton transducer;
		Automaton check;
		State transduction;

		automatonStr = "S1 - !S2:a;" +
			"!S2 - S3:b;" +
			"S3 - S2:a";
		transducerStr = "S1 - !S2:a/x;" +
			"!S2 - S3:b/y;" +
			"S3 - S2:a/z";
		checkStr = "S1 - !S2:x;" +
			"!S2 - S3:y;" +
			"S3 - S2:z";

		automaton = AutomataParser.parse(automatonStr);
		transducer = AutomataParser.parse(transducerStr);
		check = AutomataParser.parse(checkStr);
		transduction = AutomataInclusion.INSTANCE.getTrasduction(transducer.getInitialState(), automaton.getInitialState());
		assertTrue(AutomataInclusion.INSTANCE.checkInclusion(check.getInitialState(), transduction));
		assertTrue(AutomataInclusion.INSTANCE.checkInclusion(transduction, check.getInitialState()));

		// 0*_+("")*0
		automatonStr = 
			"A1 - A1:0 A2:_;" +
			"A2 - A2:_ A3:\" !A5:0;" +
			"A3 - A4:\";" +
			"A4 - A3:\" !A5:0";
		// (0+/V | "a*"/S | _)*
		transducerStr = 
			"!T1 - !T1:_ !T2:0/V T3:\" ;" +
			"!T2 - !T2:0 !T1:_ T3:\" ;" +
			"T3 - T3:a !T1:\"/S ;";
		// V*S*V
		checkStr = 
			"C1 - C1:V C2:S !C3:V;" +
			"C2 - C2:S !C3:V;";
		
		automaton = AutomataParser.parse(automatonStr);
		transducer = AutomataParser.parse(transducerStr);
		check = AutomataParser.parse(checkStr);
		State checkInit = AutomataDeterminator.determinate(check.getInitialState());
		transduction = AutomataInclusion.INSTANCE.getTrasduction(transducer.getInitialState(), automaton.getInitialState());
		transduction = EmptyTransitionEliminator.INSTANCE.eliminateEmptySetTransitions(transduction);
		transduction = AutomataDeterminator.determinate(transduction);
		assertTrue(AutomataInclusion.INSTANCE.checkInclusion(transduction, checkInit));
		assertTrue(AutomataInclusion.INSTANCE.checkInclusion(checkInit, transduction));
		
	}


	private void printAutomaton(State transduction) {
		LinkedHashSet<State> states = new LinkedHashSet<State>();
		EmptyTransitionEliminator.INSTANCE.dfs(transduction, states);
		System.out.println(AutomataParser.statesToString(states, transduction));
	}
	
	
}