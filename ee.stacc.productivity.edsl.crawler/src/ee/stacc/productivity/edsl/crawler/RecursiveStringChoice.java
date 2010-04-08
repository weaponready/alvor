package ee.stacc.productivity.edsl.crawler;

import ee.stacc.productivity.edsl.string.IAbstractString;
import ee.stacc.productivity.edsl.string.IAbstractStringVisitor;
import ee.stacc.productivity.edsl.string.IPosition;

public class RecursiveStringChoice implements IAbstractString {
	private IAbstractString base;
	private Object recKey; // pointer to a parent node of this node
	
	public RecursiveStringChoice(IAbstractString base, Object recKey) {
		this.base = base;
		this.recKey = recKey;
	}

	public IAbstractString getBase() {
		return base;
	}
	
	public Object getRecKey() {
		return recKey;
	}
	
	@Override
	public <R, D> R accept(
			IAbstractStringVisitor<? extends R, ? super D> visitor, D data) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IPosition getPosition() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isEmpty() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String toString() {
		return "RecChoice(" + base.toString() + ", " + recKey.hashCode() + ")";
	}

}
