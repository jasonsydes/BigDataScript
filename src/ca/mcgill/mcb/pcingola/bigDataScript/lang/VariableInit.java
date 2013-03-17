package ca.mcgill.mcb.pcingola.bigDataScript.lang;

import org.antlr.v4.runtime.tree.ParseTree;

import ca.mcgill.mcb.pcingola.bigDataScript.run.BigDataScriptThread;
import ca.mcgill.mcb.pcingola.bigDataScript.run.RunState;
import ca.mcgill.mcb.pcingola.bigDataScript.scope.Scope;
import ca.mcgill.mcb.pcingola.bigDataScript.scope.ScopeSymbol;
import ca.mcgill.mcb.pcingola.bigDataScript.util.CompilerMessage.MessageType;
import ca.mcgill.mcb.pcingola.bigDataScript.util.CompilerMessages;

/**
 * Variable declaration
 * 
 * @author pcingola
 */
public class VariableInit extends BigDataScriptNode {

	String varName;
	Expression expression;

	public static VariableInit get(String name) {
		VariableInit vi = new VariableInit(null, null);
		vi.varName = name;
		return vi;
	}

	public VariableInit(BigDataScriptNode parent, ParseTree tree) {
		super(parent, tree);
	}

	public Expression getExpression() {
		return expression;
	}

	public String getVarName() {
		return varName;
	}

	@Override
	protected void parse(ParseTree tree) {
		varName = tree.getChild(0).getText();
		expression = (Expression) factory(tree, 2); // Node 1 is '=', we need node 2
	}

	/**
	 * Run 
	 */
	@Override
	protected RunState runStep(BigDataScriptThread csThread) {
		if (expression != null) {
			Scope scope = csThread.getScope();
			ScopeSymbol ssym = scope.getSymbol(varName);
			Object value = expression.eval(csThread);
			value = ssym.getType().cast(value);
			ssym.setValue(value);
		}

		return RunState.OK;
	}

	public void setExpression(Expression expression) {
		this.expression = expression;
	}

	@Override
	protected void typeCheck(Scope scope, CompilerMessages compilerMessages) {
		// Variable type
		ScopeSymbol varSym = scope.getSymbol(varName, true);
		Type varType = null;
		if (varSym != null) varType = varSym.getType();

		// Calculate expression type
		if (expression != null) {
			Type exprRetType = expression.returnType(scope);

			// Compare types
			if ((varType == null) || (exprRetType == null)) {
				// Variable not found, nothing else to do
			} else if (!exprRetType.canCast(varType)) {
				// We cannot cast expression's type to variable's type: Error
				compilerMessages.add(this, "Cannot cast " + exprRetType + " to " + varType, MessageType.ERROR);
			}
		}
	}

}