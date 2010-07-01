package ee.stacc.productivity.edsl.crawler;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import ee.stacc.productivity.edsl.cache.CacheService;
import ee.stacc.productivity.edsl.cache.UnsupportedStringOpEx;
import ee.stacc.productivity.edsl.checkers.INodeDescriptor;
import ee.stacc.productivity.edsl.checkers.IStringNodeDescriptor;
import ee.stacc.productivity.edsl.common.logging.ILog;
import ee.stacc.productivity.edsl.common.logging.Logs;
import ee.stacc.productivity.edsl.string.IAbstractString;
import ee.stacc.productivity.edsl.string.IPosition;
import ee.stacc.productivity.edsl.string.StringChoice;
import ee.stacc.productivity.edsl.string.StringConstant;
import ee.stacc.productivity.edsl.string.StringRandomInteger;
import ee.stacc.productivity.edsl.string.StringSequence;

@Deprecated
public class Old_AbstractStringEvaluator {
	private static final String RESULT_FOR_SQL_CHECKER = "@ResultForSQLChecker";
	private static final String SIMPLIFIED_BODY_FOR_SC = "@SimplifiedBodyForSQLChecker";
	private static final ILog LOG = Logs.getLog(Old_AbstractStringEvaluator.class);
	private int maxLevel = 2;
	private boolean supportParameters = true;
	private boolean supportInvocations = true;
	
	private int level;
	private MethodInvocation invocationContext;
	private IJavaElement[] scope;
	
	public static IAbstractString evaluateExpression(Expression node) {
		Old_AbstractStringEvaluator evaluator = 
			new Old_AbstractStringEvaluator(0, null, new IJavaElement[] {ASTUtil.getNodeProject(node)});
		return evaluator.eval(node);
	}
	
	private Old_AbstractStringEvaluator(int level, MethodInvocation invocationContext,
			IJavaElement[] scope) {
		
		if (level > maxLevel) {
			throw new UnsupportedStringOpEx("Analysis level (" + level + ") too deep");
		}
		
		this.level = level;
		this.invocationContext = invocationContext;
		this.scope = scope;
	}
	
	private IAbstractString eval(Expression node) {
		IAbstractString result = CacheService.getCacheService().getAbstractString(PositionUtil.getPosition(node));
		if (result == null) {
//			System.out.println("Eval at: " + PositionUtil.getPosition(node));
			try {
				result = doEval(node);
				CacheService.getCacheService().addAbstractString(PositionUtil.getPosition(node), result);
			} catch (UnsupportedStringOpEx e) {
				CacheService.getCacheService().addUnsupported(PositionUtil.getPosition(node), e.getMessage());
				throw e;
			}
		}
		return result;
	}
	
	private IAbstractString doEval(Expression node) {
		ITypeBinding type = node.resolveTypeBinding();
		assert type != null;
		
		if (type.getName().equals("int")) {
			return new StringRandomInteger(PositionUtil.getPosition(node));
		}
		else if (node instanceof StringLiteral) {
			StringLiteral stringLiteral = (StringLiteral)node;
			StringConstant stringConstant = new StringConstant(PositionUtil.getPosition(node), 
					stringLiteral.getLiteralValue(), stringLiteral.getEscapedValue());
			return stringConstant;
		}
		else if (node instanceof CharacterLiteral) {
			CharacterLiteral characterLiteral = (CharacterLiteral)node;
			StringConstant stringConstant = new StringConstant(PositionUtil.getPosition(node), 
					String.valueOf(characterLiteral.charValue()), characterLiteral.getEscapedValue());
			return stringConstant;
		}
		else if (node instanceof Name) {
			return evalName((Name)node); 
		}
		else if (node instanceof ConditionalExpression) {
			return new StringChoice(PositionUtil.getPosition(node),
					eval(((ConditionalExpression)node).getThenExpression()),
					eval(((ConditionalExpression)node).getElseExpression()));
		}
		else if (node instanceof ParenthesizedExpression) {
			return eval(((ParenthesizedExpression)node).getExpression());
		}
		else if (node instanceof InfixExpression) {
			return evalInfix((InfixExpression)node);
		}
		else if (node instanceof MethodInvocation) {
			MethodInvocation inv = (MethodInvocation)node;
				return evalInvocationResult(inv);
		}
		else if (node instanceof ClassInstanceCreation) {
			
			assert (isStringBuilderOrBuffer(node.resolveTypeBinding()));
			ClassInstanceCreation cic = (ClassInstanceCreation)node;
			if (cic.arguments().size() == 1) {
				Expression arg = (Expression)cic.arguments().get(0);
				// string initializer
				if (arg.resolveTypeBinding().getName().equals("String")) {
					return eval(arg);
				}
				else if (arg.resolveTypeBinding().getName().equals("int")) {
					StringConstant stringConstant = new StringConstant(PositionUtil.getPosition(node), 
							"", "\"\"");
					return stringConstant;
				}
				else { // CharSequence
					throw new UnsupportedStringOpEx("Unknown StringBuilder/Buffer constructor: " 
							+ arg.resolveTypeBinding().getName());
				}
			}
			else {
				assert cic.arguments().size() == 0;
				StringConstant stringConstant = new StringConstant(PositionUtil.getPosition(node), 
						"", "\"\"");
				return stringConstant;
			}
		}
		else {
			throw new UnsupportedStringOpEx
				("getValOf(" + node.getClass().getName() + ")");
		}
	}

	private IAbstractString evalName(Name node) {
		// can be SimpleName or QualifiedName
		Statement stmt = ASTUtil.getContainingStmt(node);
		if (stmt == null) {
			assert ((IVariableBinding)node.resolveBinding()).isField();
			return evalField(node);
		} else {
			// TODO this statement can modify this var 
			return evalVarBefore(node, stmt);
		}
	}
	
	private IAbstractString evalInfix(InfixExpression expr) {
		if (expr.getOperator() == InfixExpression.Operator.PLUS) {
			List<IAbstractString> ops = new ArrayList<IAbstractString>();
			ops.add(eval(expr.getLeftOperand()));
			ops.add(eval(expr.getRightOperand()));
			for (Object operand: expr.extendedOperands()) {
				ops.add(eval((Expression)operand));
			}
			return new StringSequence(PositionUtil.getPosition(expr), ops);
		}
		else {
			throw new UnsupportedStringOpEx
				("getValOf( infix op = " + expr.getOperator() + ")");
		}
	}
	
	private IAbstractString evalVarAfterIf(Name name, IfStatement stmt) {
		IAbstractString ifVal = evalVarAfter(name, stmt.getThenStatement());
		IAbstractString elseVal = null;
		
		if (stmt.getElseStatement() != null) {
			elseVal = evalVarAfter(name, stmt.getElseStatement());
		} else {
			elseVal = evalVarBefore(name, stmt);
		}
		
		if (ifVal.equals(elseVal)) {
			return ifVal;
		} else {
			return new StringChoice(PositionUtil.getPosition(stmt), ifVal, elseVal);
		}
	}
	
	private IAbstractString evalVarAfterDecl(Name name,
			VariableDeclarationStatement stmt) {
		IVariableBinding var = (IVariableBinding) name.resolveBinding();
		
		// May include declarations for several variables
		for (int i=stmt.fragments().size()-1; i>=0; i--) {
			VariableDeclaration vDec = (VariableDeclaration)stmt.fragments().get(i);
			
			if (vDec.getName().resolveBinding().isEqualTo(var)) {
				return eval(vDec.getInitializer());
			}
		}
		return evalVarBefore(name, stmt);
	}
	
	private IAbstractString evalVarAfterAss(Name name, 
			ExpressionStatement stmt) {
		assert stmt.getExpression() instanceof Assignment;

		IVariableBinding var = (IVariableBinding) name.resolveBinding();
		
		// TODO StringBuilder variable can be assigned also
		
		Assignment ass = (Assignment)stmt.getExpression();
		
		if (ass.getLeftHandSide() instanceof SimpleName
			&& ((SimpleName)ass.getLeftHandSide()).resolveBinding().isEqualTo(var)) {
			
			IAbstractString rhs = eval(ass.getRightHandSide());
			
			if (ass.getOperator() == Assignment.Operator.ASSIGN) {
				return rhs;
			}
			else if (ass.getOperator() == Assignment.Operator.PLUS_ASSIGN) {
				return new StringSequence(PositionUtil.getPosition(ass), evalVarBefore(name, stmt), rhs);
			}
			else {
				throw new UnsupportedStringOpEx("getVarValAfterAss: unknown operator");
			}
		}
		else { // wrong assignment, this statement doesn't change var (hopefully :)
			return evalVarBefore(name, stmt);
		}
	}
	
	private IAbstractString evalVarAfter(Name name, Statement stmt) {
		//IVariableBinding var = (IVariableBinding) name.resolveBinding();
		//LOG.message("getVarValAfter: var=" + var.getName()
		//		+ ", stmt="+ stmt.getClass().getName());
		
		if (stmt instanceof ExpressionStatement) {
			Expression expr = ((ExpressionStatement)stmt).getExpression(); 
			if (expr instanceof Assignment) {
				return evalVarAfterAss(name, (ExpressionStatement)stmt);
			}
			else if (expr instanceof MethodInvocation) {
				return evalVarAfterMethodInvStmt(name, (ExpressionStatement)stmt);
			}
			else {
				throw new UnsupportedStringOpEx
					("getVarValAfter(_, ExpressionStatement." + expr.getClass() + ")");
			}
		}
		else if (stmt instanceof VariableDeclarationStatement) {
			return evalVarAfterDecl(name, (VariableDeclarationStatement)stmt);
		}
		else if (stmt instanceof IfStatement) {
			return evalVarAfterIf(name, (IfStatement)stmt);
		}
		else if (stmt instanceof Block) {
			Block block = (Block)stmt;
			if (block.statements().isEmpty()) {
				Statement prevStmt = ASTUtil.getPrevStmt(stmt);
				return evalVarAfter(name, prevStmt);
			}
			return evalVarAfter(name, ASTUtil.getLastStmt(block));
		}
		else if (stmt instanceof ReturnStatement) {
			return evalVarBefore(name, stmt);
		}
		else if (stmt instanceof ForStatement) {
			return evalVarAfterFor(name, (ForStatement)stmt);
		}
		else { // other kind of statement
			throw new UnsupportedStringOpEx("getVarValAfter(var, " + stmt.getClass().getName() + ")");
		} 
	}
	
	private IAbstractString evalVarAfterFor(Name name, ForStatement stmt) {
		throw new UnsupportedStringOpEx("Loops not supported yet");
	}

	private IAbstractString evalField(Name node) {
		IVariableBinding var = (IVariableBinding) node.resolveBinding();
		VariableDeclarationFragment frag = NodeSearchEngine
			.findFieldDeclarationFragment(scope, var.getDeclaringClass().getErasure().getQualifiedName() 
				+ "." + var.getName());
	
		FieldDeclaration decl = (FieldDeclaration)frag.getParent();
		if ((decl.getModifiers() & Modifier.FINAL) == 0) {
			throw new UnsupportedStringOpEx("Only final fields are supported");
			// TODO create option with initalizer and AnyString
		}
		return eval(frag.getInitializer());
	}
	
	private IAbstractString evalVarAfterMethodInvStmt(Name name,
			ExpressionStatement stmt) {
		assert (stmt.getExpression() instanceof MethodInvocation);
		
		IVariableBinding var = (IVariableBinding) name.resolveBinding();
		
		if (isStringBuilderOrBuffer(var.getType())) {
			MethodInvocation inv = (MethodInvocation)stmt.getExpression();
			
			// TODO: check that it's really chain of sb.append("...").append("...")
			// and nothing else
			if (isStringBuilderOrBuffer(stmt.getExpression().resolveTypeBinding())
					&& builderChainIsStartedByVar(inv, var)) {
				return eval(inv);
			}
			// if it's passed as argument then track changes to it
			else if (ASTUtil.getArgumentIndex0(inv, var) > -1) {
				return evalInvocationArgOut(inv, ASTUtil.getArgumentIndex0(inv, var)+1);
			}
			else if (ASTUtil.varIsUsedIn(var, inv)) {
				throw new UnsupportedStringOpEx(
						"Var '" + var.getName() + "' used (possibly modified) in unsupported construct");
			}
			else { // SB is not changed in this statement
				return evalVarBefore(name, stmt);
			}
		}
		else { // variable is of type String
			// it cannot be changed here  
			return evalVarBefore(name, stmt);
		}
	}
	
	
	/**
	 * Gives the value of arg at position=argumentIndex after this invocation
	 * @param inv
	 * @param argumentIndex
	 * @return
	 */
	private IAbstractString evalInvocationArgOut(MethodInvocation inv,
			int argumentIndex) {
		return evalInvocationResultOrArgOut(inv, argumentIndex); 
	}

	private boolean builderChainIsStartedByVar(Expression node, IVariableBinding var) {
		assert isStringBuilderOrBuffer(node.resolveTypeBinding());
		if (node instanceof SimpleName) {
			return ((SimpleName) node).resolveBinding().isEqualTo(var);
		}
		else if (node instanceof MethodInvocation) {
			return builderChainIsStartedByVar(((MethodInvocation)node).getExpression(), var);
		}
		else {
			throw new UnsupportedStringOpEx("unknown construction in builderChain: " 
					+ node.getClass());
		}
	}
	
	/**
	 * For getting the result of normal (ie. not special cases) invocation, 
	 * the argument values are not computed before analyzing method body. 
	 * Another AbstractStringEvaluator is created for that method body and
	 * before starting the analysis, it's invocationContext is set to that invocation -
	 * this basically switches evaluator to context-sensitive mode -- ie. when it needs
	 * value of method argument then it only looks into invocationContext 
	 * (as opposed to looking for all callsites of this method).
	 * 

	 * @param inv
	 * @return
	 */
	private IAbstractString evalInvocationResult(MethodInvocation inv) {
		if (inv.getExpression() != null
				&& isStringBuilderOrBuffer(inv.getExpression().resolveTypeBinding())) {
			if (inv.getName().getIdentifier().equals("toString")) {
				return eval(inv.getExpression());
			}
			else if (inv.getName().getIdentifier().equals("append")) {
				return new StringSequence(
						PositionUtil.getPosition(inv), 
						eval(inv.getExpression()),
						eval((Expression)inv.arguments().get(0)));
			}
			else {
				throw new UnsupportedStringOpEx("StringBuilder/Buffer, method=" 
						+ inv.getName().getIdentifier(),
						PositionUtil.getPosition(inv)); 
			}
		}
		else  {
			return evalInvocationResultOrArgOut(inv, -1);
		}			
	}
	
	private IAbstractString evalInvocationResultOrArgOut(MethodInvocation inv,
			int argumentIndex) {
		if (! supportInvocations) {
			throw new UnsupportedStringOpEx("Method call");
		}

		if (! supportInvocations) {
			throw new UnsupportedStringOpEx("Method call");
		}
		final Old_AbstractStringEvaluator evaluatorWithNewContext = 
			new Old_AbstractStringEvaluator(level+1, inv, scope);

		List<MethodDeclaration> decls = NodeSearchEngine.findMethodDeclarations(scope, inv);
		
		if (decls.size() == 0) {
			throw new UnsupportedStringOpEx("Possible problem, no declarations found for: " + inv.toString());
		}
		else {
			List<IAbstractString> choices = new ArrayList<IAbstractString>();
			for (MethodDeclaration decl: decls) {
				if (argumentIndex == -1) {
					choices.add(evaluatorWithNewContext.getMethodReturnValue(decl));
				}
				else {
					choices.add(evaluatorWithNewContext.getMethodArgOutValue
							(decl, argumentIndex));
				}
			}
			if (choices.size() == 1) {
				return choices.get(0);
			}
			else {
				return new StringChoice(PositionUtil.getPosition(inv), choices);
			}
//			List<IFile> allFilesInScope = NodeSearchEngine.getAllFilesInScope(scope);
//			String signature = getErasedSignature(inv.resolveMethodBinding());
//			
//			IScopedCache<String, IAbstractString> methodReturnValueCache = CacheService.getCacheService().getMethodReturnValueCache();
//			CachedSearcher<String, IAbstractString> searcher = new CachedSearcher<String, IAbstractString>() {
//				
//				@Override
//				protected void performSearchInScope(List<IJavaElement> scopeToSearchIn,
//						String key, List<? super IAbstractString> values) {
//			List<IAbstractString> abstractStrings = new ArrayList<IAbstractString>();
//			List<MethodDeclaration> decls = NodeSearchEngine.findMethodDeclarations(scope, inv);
//			for (MethodDeclaration decl: decls) {
//				abstractStrings.add(evaluatorWithNewContext.getMethodReturnValue(decl));
//			}
			
//			searcher.performCachedSearch(allFilesInScope, methodReturnValueCache, signature, abstractStrings);
			
//			if (abstractStrings.size() == 1) {
//				return abstractStrings.get(0);
//			} else {
//				return new StringChoice(PositionUtil.getPosition(inv), abstractStrings);
//			}
		}
	}

	private IAbstractString getMethodArgOutValue(MethodDeclaration decl,
			int argumentIndex) {
		// TODO: at first look for javadoc annotation for this arg
		
		SingleVariableDeclaration paramDecl = 
				(SingleVariableDeclaration)decl.parameters().get(argumentIndex-1);
		
		return evalVarAfter(paramDecl.getName(), decl.getBody());
		
		
		//throw new UnsupportedStringOpEx("getMethodArgOutValue");
	}

	/**
	 * If decl has special annotations then return patched and reparsed version
	 * of the method
	 * 
	 * @param decl
	 * @return
	 * 
	 */
	@Deprecated
	MethodDeclaration simplifyMethodDeclaration__(MethodDeclaration decl) {
		TagElement tag = ASTUtil.getJavadocTag(decl.getJavadoc(), SIMPLIFIED_BODY_FOR_SC);
		if (tag == null) {
			return decl; // can't simplify
		}
		String tagText = ASTUtil.getTagElementText(tag);
		if (tagText == null) {
			throw new UnsupportedStringOpEx("Problem reading "+ SIMPLIFIED_BODY_FOR_SC);
		}
		
		// replace method body with given string and reparse
		try {
			Block newBody = (Block)ASTTransformer.patchAndReParse(decl.getBody(), tagText);
			MethodDeclaration newDecl = (MethodDeclaration)newBody.getParent();
		
			// remove annotation from new version to avoid recursion, TODO test
			newDecl.getJavadoc().delete();
			return newDecl;
		} catch (ParseException e) {
			throw new UnsupportedStringOpEx("Reparsing declaration. ParseException: "
					+ e.toString());
		} catch (JavaModelException e) {
			throw new UnsupportedStringOpEx("Reparsing declaration. JavaModelException: "
					+ e.toString());
		}
	}
	
	private IAbstractString getMethodReturnValue(MethodDeclaration decl) {
		
		// if it has @ResultForSQLChecker in JAVADOC then return this
		IAbstractString javadocResult = getMethodReturnValueFromJavadoc(decl);
		if (javadocResult != null) {
			return javadocResult;
		}
		// TODO: if ResultForSQLChecker is specified in the configuration file ...
		
		assert decl != null;
		
		// find all return statements
		final List<ReturnStatement> returnStmts = new ArrayList<ReturnStatement>();
		ASTVisitor visitor = new ASTVisitor() {
			@Override
			public boolean visit(ReturnStatement node) {
				returnStmts.add(node);
				return true;
			}
		};
		decl.accept(visitor);
		
		// get choice out of different return expressions
		
		List<IAbstractString> options = new ArrayList<IAbstractString>();
		for (ReturnStatement ret: returnStmts) {
			options.add(eval(ret.getExpression()));
		}
		return new StringChoice(PositionUtil.getPosition(decl), options);
	}
	
	private IAbstractString getMethodReturnValueFromJavadoc(MethodDeclaration decl) {
		// TODO: allow also specifying result as regex
		
		if (decl.getJavadoc() == null) {
			return null;
		}
		TagElement tag = ASTUtil.getJavadocTag(decl.getJavadoc(), RESULT_FOR_SQL_CHECKER);
		
		if (tag != null) {
			String tagText = ASTUtil.getTagElementText(tag);
			if (tagText == null) {
				throw new UnsupportedStringOpEx("Problem reading " + RESULT_FOR_SQL_CHECKER);
			} else {
				//return new StringConstant(tagText);
				return new StringConstant(PositionUtil.getPosition(tag), 
						tagText, '"'+tagText+'"');
			}
		}
		else {
			return null;
		}
	}
	
	public static List<INodeDescriptor> evaluateMethodArgumentAtCallSites
			(Collection<NodeRequest> requests,
					IJavaElement[] scope, int level) {
		String levelPrefix = "";
		for (int i = 0; i < level; i++) {
			levelPrefix += "    ";
		}

		
		assert LOG.message(levelPrefix + "###########################################");
		assert LOG.message(levelPrefix + "searching: ");
		for (NodeRequest nodeRequest : requests) {
			assert LOG.message(nodeRequest);
		}
		
		// find value from all call-sites
		Collection<IPosition> argumentPositions = NodeSearchEngine.findArgumentNodes
			(scope, requests);
		
		List<INodeDescriptor> result = new ArrayList<INodeDescriptor>();
		for (IPosition sr: argumentPositions) {

				try {
					IAbstractString abstractString = CacheService.getCacheService().getAbstractString(sr);
					
					if (abstractString == null) {
						assert LOG.message(levelPrefix + "    file: " + sr.getPath() + ", line: "
								+ PositionUtil.getLineNumber(sr));
						assert LOG.message("*SR: " + sr);
						ASTNode node = NodeSearchEngine.getASTNode(sr);
						assert LOG.message("*NODE: " + node);
						
						Expression arg = (Expression)node;// NodeSearchEngine.getASTNode(sr);
						Old_AbstractStringEvaluator evaluator = 
							new Old_AbstractStringEvaluator(level, null, scope);
						abstractString = evaluator.eval(arg);
					}
					result.add(new StringNodeDescriptor(sr, abstractString));
					
				} catch (UnsupportedStringOpEx e) {
					assert LOG.message(levelPrefix + "UNSUPPORTED: " + e.getMessage());
					assert LOG.message(levelPrefix + "    file: " + sr.getPath() + ", line: " 
							/*+ sr.getLineNumber()*/);
					result.add(new UnsupportedNodeDescriptor(sr, 
							"Un---supported SQL construction: " + e.getMessage() + " at " + PositionUtil.getLineString(sr)));
				}
			
		}
		return result;
	}
	
	private IAbstractString evalVarBefore(Name name, Statement stmt) {
		IVariableBinding var = (IVariableBinding) name.resolveBinding();
		Statement prevStmt = ASTUtil.getPrevStmt(stmt);
		if (prevStmt == null) {
			// no previous statement, must be beginning of method declaration
			if (var.isField()) {
				return evalField(name);
			}
			else if (var.isParameter()) {
				if (! supportParameters) {
					throw new UnsupportedStringOpEx("eval Parameter");
				}
				MethodDeclaration method = ASTUtil.getContainingMethodDeclaration(stmt);
				int paramIndexPlus1 = ASTUtil.getParamIndex0(method, var)+1;
				
				if (this.invocationContext != null) {
					// TODO: check that invocation context matches
					Old_AbstractStringEvaluator nextLevelEvaluator = 
						new Old_AbstractStringEvaluator(level+1, null, scope);
					
					return nextLevelEvaluator.eval
						((Expression)this.invocationContext.arguments().get(paramIndexPlus1-1));
				}
				else {
					List<INodeDescriptor> descList = 
						Old_AbstractStringEvaluator.evaluateMethodArgumentAtCallSites(
								Collections.singleton(
										new NodeRequest(
												ASTUtil.getMethodClassName(method), 
												method.getName().toString(),
												paramIndexPlus1)), 
							this.scope, this.level + 1);
					
					List<IAbstractString> choices = new ArrayList<IAbstractString>();
					
					if (choices.size() == 0) {
						throw new UnsupportedStringOpEx("Possible problem, no callsites found for: "
								+ method.getName());
					}
					
					for (INodeDescriptor choiceDesc: descList) {
						if (choiceDesc instanceof IStringNodeDescriptor) {
							choices.add(((IStringNodeDescriptor)choiceDesc).getAbstractValue());
						}
					}
					return new StringChoice(PositionUtil.getPosition(name), choices);
				}
			}
			else {
				throw new UnsupportedStringOpEx
					("getVarValBefore: not param, not field, kind=" + var.getKind());
			}
		}
		else {
			return evalVarAfter(name, prevStmt);
		}
	}
	
	private static boolean isStringBuilderOrBuffer(ITypeBinding typeBinding) {
		return typeBinding.getQualifiedName().equals("java.lang.StringBuffer")
		|| typeBinding.getQualifiedName().equals("java.lang.StringBuilder");
	}
	
}