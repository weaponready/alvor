package ee.stacc.productivity.edsl.crawler;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.search.SearchMatch;


public class ASTUtil {
	public static final String ORIGINAL_I_COMPILATION_UNIT = "OriginalICompilationUnit";

	public static TypeDeclaration getContainingTypeDeclaration(ASTNode node) {
		ASTNode result = node;
		while (result != null && ! (result instanceof TypeDeclaration)) {
			result = result.getParent();
		}
		return (TypeDeclaration)result;
	}
	
	public static MethodDeclaration getMethodDeclarationByName(TypeDeclaration typeDecl,
			String methodName) {
		for (MethodDeclaration method: typeDecl.getMethods()) {
			if (method.getName().getIdentifier().equals(methodName)) {
				return method;
			}
		}
		throw new IllegalArgumentException("Method '" + methodName + "' not found");
	}

	public static boolean varIsUsedIn(IVariableBinding var, Expression expr) {
		if (expr instanceof MethodInvocation) {
			MethodInvocation inv = (MethodInvocation) expr;
			if (inv.getExpression() != null && varIsUsedIn(var, inv.getExpression())) {
				return true;
			}
			else {
				for (Object arg : inv.arguments()) {
					if (varIsUsedIn(var, (Expression) arg)) {
						return true;
					}
				}
				return false;
			}
		}
		else if (expr instanceof Name) {
			return ((Name) expr).resolveBinding().isEqualTo(var);
		}
		else if (expr instanceof InfixExpression) {
			InfixExpression inf = (InfixExpression) expr;
			if (varIsUsedIn(var, inf.getLeftOperand())) {
				return true;
			}
			if (varIsUsedIn(var, inf.getRightOperand())) {
				return true;
			}
			for (Object o : inf.extendedOperands()) {
				if (varIsUsedIn(var, (Expression)o)) {
					return true;
				}
			}
			return false;
		}
		else if (expr instanceof StringLiteral 
				|| expr instanceof NumberLiteral
				|| expr instanceof BooleanLiteral) {
			return false;
		}
		else {
			throw new UnsupportedStringOpEx("Checking whether var is mentioned. "
					+ "Unsupported expression: "
					+ expr.getClass());
		}
	}
	
	public static int getNodeLineNumber(SearchMatch match, ASTNode node) {
		if (node.getRoot() instanceof CompilationUnit) {
			return ((CompilationUnit)node.getRoot()).getLineNumber(match.getOffset());
		}
		else {
			return -1;
		}
	}
	
	public static Statement getPrevStmt(Statement node) {
		//LOG.message("getPrevStmt: " + node.getClass().getName());
		
		if (node.getParent() instanceof Block) {
			Block block = (Block) node.getParent();
			int i = block.statements().indexOf(node);
			
			if (i == 0) { // this is first in block, eg. this block is done
				return getPrevStmt(block);
			} else {
				return (Statement)block.statements().get(i-1);
			}
		} 
		else if (node.getParent() instanceof MethodDeclaration) {
			return null;
		}
		else if (node.getParent() instanceof IfStatement) {
			return getPrevStmt((IfStatement)node.getParent());
		}
		else if (node.getParent() instanceof TryStatement) {
			return getPrevStmt((TryStatement)node.getParent());
		}
		else { 
			throw new UnsupportedStringOpEx("getPrevStatement(" + node.getClass().getName() 
				+ ", parent is " + node.getParent().getClass().getName() + ")");
		}
	}
	
	
	public static Statement getContainingStmt(ASTNode node) {
		assert node != null;
		
		if (node.getParent() instanceof Statement) {
			return (Statement)node.getParent();
		}
		else {
			ASTNode parent = node.getParent();
			if (parent == null) {
				return null;
			}
			return getContainingStmt(parent);
		}
	}
	
	public static boolean invocationMayUseDeclaration (MethodInvocation inv, MethodDeclaration decl) {
		assert inv.getName().getIdentifier().equals(decl.getName().getIdentifier());
		
		if (inv.arguments().size() != decl.parameters().size()) {
			return false;
		}
		
		// not working
		/*
		ITypeBinding invType = inv.resolveTypeBinding();
		ITypeBinding declType = ASTUtil.getContainingTypeDeclaration(decl).resolveBinding();
		if (! declType.isSubTypeCompatible(invType)) {
			return false;
		}
		*/
		
		// finally compare parameter types
		ITypeBinding[] invPTypes = inv.resolveMethodBinding().getParameterTypes();
		ITypeBinding[] declPTypes = decl.resolveBinding().getParameterTypes();
		
		for (int i = 0; i < invPTypes.length; i++) {
			if (!invPTypes[i].isEqualTo(declPTypes[i])) {
				return false;
			}
		}
		
		return true;
		
		
		// Approach 2
		// Does not work for some reason
		//return invBinding.isEqualTo(declBinding) || declBinding.overrides(invBinding);
	}
	
	public static TagElement getJavadocTag(Javadoc javadoc, String name) {
		if (javadoc == null) {
			return null;
		}
		for (Object element : javadoc.tags()) {
			TagElement tag = (TagElement)element;
			if (tag != null && name.equals(tag.getTagName())) {
				return tag;
			}
		}
		return null;
	}
	
	public static String getTagElementText(TagElement tag) {
		if (tag.fragments().size() == 1 
				&& tag.fragments().get(0) instanceof TextElement) {
			TextElement textElement = (TextElement)tag.fragments().get(0);
			return textElement.getText();
		} else {
			return null;
		}
	}
	
	public static IJavaProject getNodeProject(ASTNode node) {
		assert node.getRoot() instanceof CompilationUnit;
		CompilationUnit cUnit = (CompilationUnit)node.getRoot();
		return cUnit.getJavaElement().getJavaProject();
	}

	public static Statement getLastStmt(Block block) {
		return (Statement)block.statements().get(block.statements().size()-1);
	}
	
	public static String getMethodClassName(MethodDeclaration method) {
		assert (method.getParent() instanceof TypeDeclaration);
		TypeDeclaration typeDecl = (TypeDeclaration)method.getParent();
		ITypeBinding classBinding = typeDecl.resolveBinding();
		return classBinding.getQualifiedName();
	}
	
	public static MethodDeclaration getContainingMethodDeclaration(ASTNode node) {
		ASTNode result = node;
		while (result != null && ! (result instanceof MethodDeclaration)) {
			result = result.getParent();
		}
		return (MethodDeclaration)result;
	}
	
	// NB! uses 0-based indexing
	public static int getParamIndex0(MethodDeclaration method, IBinding param) {
		int i = 0;
		for (Object elem: method.parameters()) {
			SingleVariableDeclaration decl = (SingleVariableDeclaration)elem;
			if (decl.resolveBinding().isEqualTo(param)) {
				return i;
			}
			i++;
		}
		return -1;
	}
	
	// NB! uses 0-based indexing
	public static int getArgumentIndex0(MethodInvocation inv, IBinding var) {
		int i = 0;
		for (Object elem: inv.arguments()) {
			if (elem instanceof Name 
					&& ((Name)elem).resolveBinding().isEqualTo(var)) {
				return i;
			}
			i++;
		}
		return -1;
	}
	
	public static VariableDeclaration getVarDeclFragment(VariableDeclarationStatement stmt, Name name) {
		for (Object frag : stmt.fragments()) {
			VariableDeclaration vDec = (VariableDeclaration)frag;
			
			if (sameBinding(vDec.getName(), name)) {
				return vDec;
			}
		}
		return null;
	}
	
	public static CompilationUnit getCompilationUnit(ASTNode node) {
		return (CompilationUnit)node.getRoot();
	}
	
	public static ICompilationUnit getICompilationUnit(ASTNode node) {
		return (ICompilationUnit)getCompilationUnit(node).getJavaElement();
	}
	
	public static boolean sameBinding(Expression exp, Name name) {
		return sameBinding(exp, name.resolveBinding());
	}
	
	public static boolean sameBinding(Expression exp, IBinding var) {
		return (exp instanceof Name)
			&& ((Name)exp).resolveBinding().isEqualTo(var);
	}
	
	/*
	private static IFile getNodeFile(ASTNode node) {
		assert node.getRoot() instanceof CompilationUnit;
		CompilationUnit cUnit = (CompilationUnit)node.getRoot();
		return (IFile)cUnit.getTypeRoot().getResource();
	}
	*/
}
