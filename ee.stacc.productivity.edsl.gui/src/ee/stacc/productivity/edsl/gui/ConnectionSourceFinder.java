package ee.stacc.productivity.edsl.gui;

import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.SharedASTProvider;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

import ee.stacc.productivity.edsl.common.logging.ILog;
import ee.stacc.productivity.edsl.common.logging.Logs;
import ee.stacc.productivity.edsl.conntracker.ConnectionDescriptor;
import ee.stacc.productivity.edsl.conntracker.ConnectionTracker;
import ee.stacc.productivity.edsl.crawler.PositionUtil;
import ee.stacc.productivity.edsl.string.IPosition;

public class ConnectionSourceFinder implements IEditorActionDelegate {
	private static final ILog LOG = Logs.getLog(ConnectionSourceFinder.class);
	
	private ISelection selection;
	private IEditorPart targetEditor;


	public void run(IAction action) {
		try {
			doit();
		} catch (Exception e) {
			LOG.exception(e);
		}
	}
	
	void doit() throws Exception {
		//System.out.println("-- Editor action --" + selection.toString());
		
		assert selection instanceof ITextSelection;
		ITextSelection textSel = (ITextSelection) selection;
		
		ITypeRoot root = JavaUI.getEditorInputTypeRoot(targetEditor.getEditorInput());
		CompilationUnit ast = SharedASTProvider
			.getAST(root, SharedASTProvider.WAIT_YES, null);
		
		ASTNode node = NodeFinder.perform(ast, textSel.getOffset(), textSel.getLength());
		if (node == null) {
			LOG.error("ERROR: Did not find node");
		}
		else if (node instanceof Expression) {
			LOG.message("###############################");
			LOG.message("Selection is : " + node.getClass().getName());
			LOG.message("Connection source is: ");
			
			ConnectionDescriptor desc = ConnectionTracker.getConnectionDescriptor((Expression)node); 
			LOG.message(desc.getPos().getPath() + ": " 
				+ PositionUtil.getLineNumber(desc.getPos()) + ", DESC: " + desc);
		} 
		else {
			LOG.message("Selection is not expression, but: "
					+ node.getClass().getName());
		}
	}
	
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}
	
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		this.targetEditor = targetEditor;
	}
}
