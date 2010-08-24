package ee.stacc.productivity.edsl.string;


public interface IAbstractStringVisitor<R, D> {

	R visitStringCharacterSet(StringCharacterSet characterSet, D data);

	R visitStringChoice(StringChoice stringChoice, D data);

	R visitStringConstant(StringConstant stringConstant, D data);

	R visitStringSequence(StringSequence stringSequence, D data);

	R visitStringRepetition(StringRepetition stringRepetition, D data);
	
	R visitStringParameter(StringParameter stringParameter, D data);

}
