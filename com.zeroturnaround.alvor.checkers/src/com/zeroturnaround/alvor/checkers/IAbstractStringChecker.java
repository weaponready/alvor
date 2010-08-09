package com.zeroturnaround.alvor.checkers;

import java.util.List;
import java.util.Map;

public interface IAbstractStringChecker {
	void checkAbstractStrings(List<IStringNodeDescriptor> descriptors, ISQLErrorHandler errorHandler, Map<String, Object> options);
}
