package org.pavanecce.uml.ocl2code.creators;

import java.util.List;

import org.eclipse.ocl.uml.OperationCallExp;
import org.eclipse.uml2.uml.Operation;
import org.jbpm.designer.uml.code.metamodel.CodeClassifier;
import org.jbpm.designer.uml.code.metamodel.CodeExpression;
import org.jbpm.designer.uml.code.metamodel.CodeParameter;
import org.jbpm.designer.uml.code.metamodel.expressions.BinaryOperatorExpression;
import org.jbpm.designer.uml.code.metamodel.expressions.NotExpression;

public class EnumerationOperationCallCreator extends AbstractOperationCallCreator {

	public EnumerationOperationCallCreator(CodeClassifier myClass) {
		super(myClass);
	}

	public CodeExpression makeOperCall(OperationCallExp exp, CodeExpression source, List<CodeExpression> args, Operation referedOp, List<CodeParameter> params) {
		if (referedOp != null) {
			if (referedOp.getName().equals("=")) {
				return new BinaryOperatorExpression(source, "${equals}", args.get(0));
			} else if (referedOp.getName().equals("<>")) {
				return new NotExpression(new BinaryOperatorExpression(source, "${equals}", args.get(0)));
			} else {
				return commonOperations(source, referedOp);
			}
		}
		return null;
	}
}
