package ee.stacc.productivity.edsl.db;


public class AbstractSQLStructure {
    /*
	
	private ArrayList<String> colNames = new ArrayList<String>();
	private int maxColumnCount = 0;
	private String errorMsg = null;
	private IAbstractString errorAStr = null;
	
	public AbstractSQLStructure(IAbstractString aStr, SQLStringAnalyzer analyzer) {
		
		List<IAbstractString> strList = null; //TODO aStr.getExpandedStrings();
		
		for (IAbstractString expAStr: strList) {
			SQLStructure struct = analyzer.validate(expAStr.toString());
			
			if (struct.errorMsg != null) {
				this.errorMsg = struct.errorMsg;
				this.errorAStr = expAStr;
				break; // TODO collect all errors
			}
			else {
				try {
					maxColumnCount = Math.max(maxColumnCount, 
							struct.resultSetMD.getColumnCount());
					for (int i = 1; i <= struct.resultSetMD.getColumnCount(); i++) {
						String name = struct.resultSetMD.getColumnName(i);
						if (!colNames.contains(name)) {
							colNames.add(name);
						}
					}
				} catch (SQLException e) { // should not occur
					System.err.println("SQLEX: " + e.getMessage());
				}
			}
		}
	}
	
	public boolean hasColumn(String name) {
		return colNames.contains(name);
	}
	
	public int getColumnCount() {
		return maxColumnCount;
	}
	
	public String getErrorMsg() {
		return errorMsg;
	}
	
	public IAbstractString getErrorAStr() {
		return errorAStr;
	}
	*/
}
