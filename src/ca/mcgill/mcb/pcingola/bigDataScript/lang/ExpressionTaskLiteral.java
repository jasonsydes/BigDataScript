package ca.mcgill.mcb.pcingola.bigDataScript.lang;

import org.antlr.v4.runtime.tree.ParseTree;

/**
 * A 'task' statement (to execute a command line in a node)
 *
 * @author pcingola
 */
public class ExpressionTaskLiteral extends ExpressionTask {

	public static final String TASK_STR = "task";

	public ExpressionTaskLiteral(BigDataScriptNode parent, ParseTree tree) {
		super(parent, tree);
	}

	@Override
	protected void parse(ParseTree tree) {
		// Get task literal
		String valueStr = tree.getText();
		if (valueStr.startsWith(TASK_STR)) valueStr = tree.getText().substring(TASK_STR.length());
		valueStr = valueStr.trim();

		ExpressionSys sys = new ExpressionSys(this, null);
		sys.setCommands(valueStr);
		statement = sys;
	}

}
