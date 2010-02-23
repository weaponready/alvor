package ee.stacc.productivity.edsl.crawler;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.dom.ASTNode;

import ee.stacc.productivity.edsl.checkers.IPositionDescriptor;
import ee.stacc.productivity.edsl.checkers.IStringNodeDescriptor;
import ee.stacc.productivity.edsl.string.IAbstractString;
import ee.stacc.productivity.edsl.string.StringConstant;

public class StringNodeDescriptor extends NodeDescriptor implements IStringNodeDescriptor, IStringPositionStorage {

	private IAbstractString abstractValue;
	private final Map<StringConstant, IPositionDescriptor> positionMap = new HashMap<StringConstant, IPositionDescriptor>();
	private final Map<StringConstant, String> escapedValuesMap = new HashMap<StringConstant, String>();

	public StringNodeDescriptor(ASTNode node, IFile file, int lineNumber,
			int charStart, int charLength, IAbstractString abstractValue) {
		super(node, file, lineNumber, charStart, charLength);
		this.abstractValue = abstractValue;
	}
	
	public IAbstractString getAbstractValue() {
		return this.abstractValue;
	}
	
	public void setAbstractValue(IAbstractString abstractValue) {
		this.abstractValue = abstractValue;
	}
	
	@Override
	public void setPositionInformation(StringConstant literal, IPositionDescriptor descriptor, String escapedValue) {
		positionMap.put(literal, descriptor);
		escapedValuesMap.put(literal, escapedValue);
	}
	
	@Override
	public IPositionDescriptor getPosition(StringConstant literal) {
		return positionMap.get(literal);
	}

	@Override
	public String getEscapedValue(StringConstant literal) {
		return escapedValuesMap.get(literal);
	}
	
}
