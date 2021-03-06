package org.pavanecce.uml.ocl2code;

import java.util.Collections;
import java.util.SortedSet;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.OpaqueExpression;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Property;
import org.jbpm.designer.uml.code.metamodel.CodeClass;
import org.jbpm.designer.uml.code.metamodel.CodeClassifier;
import org.jbpm.designer.uml.code.metamodel.CodeConstructor;
import org.jbpm.designer.uml.code.metamodel.CodeExpression;
import org.jbpm.designer.uml.code.metamodel.CodeMethod;
import org.jbpm.designer.uml.code.metamodel.CodePackage;
import org.jbpm.designer.uml.code.metamodel.CodeParameter;
import org.jbpm.designer.uml.code.metamodel.statements.AssignmentStatement;
import org.jbpm.designer.uml.codegen.codemodel.DefaultCodeModelBuilder;
import org.pavanecce.uml.common.ocl.OpaqueExpressionContext;
import org.pavanecce.uml.common.util.emulated.OclContextFactory;
import org.pavanecce.uml.ocl2code.common.UmlToCodeMaps;
import org.pavanecce.uml.ocl2code.creators.ExpressionCreator;
import org.pavanecce.uml.ocl2code.maps.OperationMap;
import org.pavanecce.uml.ocl2code.maps.PropertyMap;
import org.eclipse.uml2.uml.Package;
public class OclCodeBuilder extends DefaultCodeModelBuilder {
	private OclContextFactory oclContextFactory;
	private UmlToCodeMaps codeMaps;

	@Override
	public void initialize(SortedSet<Package> models, CodePackage codeModel) {
		super.initialize(models, codeModel);
		ResourceSet rst = models.iterator().next().eResource().getResourceSet();
		this.oclContextFactory = new OclContextFactory(rst);
		this.codeMaps = new UmlToCodeMaps(oclContextFactory.getLibrary(), oclContextFactory.getTypeResolver());

	}

	@Override
	public void visitProperty(Property property, CodeClassifier codeClass) {
		if (property.getDefaultValue() instanceof OpaqueExpression) {
			PropertyMap map = codeMaps.buildStructuralFeatureMap(property);
			OpaqueExpressionContext ctx = oclContextFactory.getOclExpressionContext((OpaqueExpression) property.getDefaultValue());
			ExpressionCreator ec = new ExpressionCreator(codeMaps, codeClass);
			CodeExpression codeExpression = ec.makeExpression(ctx, property.isStatic(), Collections.<CodeParameter> emptyList());
			if (property.isDerived()) {
				CodeMethod getter = codeClass.getMethod(map.getter(), Collections.<CodeParameter> emptyList());
				getter.setResultInitialValue(codeExpression);
			} else if (codeClass instanceof CodeClass) {
				CodeConstructor constr = ((CodeClass) codeClass).findOrCreateConstructor(Collections.<CodeParameter> emptyList());
				new AssignmentStatement(constr.getBody(), map.fieldname(), codeExpression);
			}
		}
	}

	@Override
	public void visitOperation(Operation operation, CodeClassifier codeClass) {
		if (operation.getBodyCondition() != null && operation.getBodyCondition().getSpecification() instanceof OpaqueExpression) {
			OperationMap map = codeMaps.buildOperationMap(operation);
			OpaqueExpressionContext ctx = oclContextFactory.getOclExpressionContext((OpaqueExpression) operation.getBodyCondition().getSpecification());
			ExpressionCreator ec = new ExpressionCreator(codeMaps, codeClass);
			CodeMethod getter = codeClass.getMethod(map.javaOperName(), map.javaParamTypePaths());
			CodeExpression codeExpression = ec.makeExpression(ctx, operation.isStatic(), getter.getParameters());
			getter.setResultInitialValue(codeExpression);
		}
	}
}
