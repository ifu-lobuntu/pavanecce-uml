package org.pavanecce.uml.ocl2code.creators;

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.emf.common.util.EList;
import org.eclipse.ocl.uml.TupleType;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.DataType;
import org.eclipse.uml2.uml.Property;
import org.jbpm.designer.uml.code.metamodel.CodeClass;
import org.jbpm.designer.uml.code.metamodel.CodeField;
import org.jbpm.designer.uml.code.metamodel.CodeMethod;
import org.jbpm.designer.uml.code.metamodel.CodePackage;
import org.jbpm.designer.uml.code.metamodel.CodeVisibilityKind;
import org.jbpm.designer.uml.code.metamodel.expressions.BinaryOperatorExpression;
import org.jbpm.designer.uml.code.metamodel.expressions.PortableExpression;
import org.jbpm.designer.uml.code.metamodel.expressions.TypeExpression;
import org.jbpm.designer.uml.code.metamodel.expressions.TypeExpressionKind;
import org.jbpm.designer.uml.code.metamodel.statements.CodeIfStatement;
import org.jbpm.designer.uml.code.metamodel.statements.PortableStatement;
import org.jbpm.designer.uml.codegen.StdlibMap;
import org.pavanecce.common.util.NameConverter;
import org.pavanecce.uml.ocl2code.common.UmlToCodeMaps;
import org.pavanecce.uml.ocl2code.maps.ClassifierMap;
import org.pavanecce.uml.ocl2code.maps.TupleTypeMap;

public class TupleTypeCreator {
	private String standardClassComment = "TupleType implementation generated by Octopus";
	private HashMap<String, CodeClass> tupleTypes = null;
	private UmlToCodeMaps codeMaps;

	public TupleTypeCreator(UmlToCodeMaps CodeUtil) {
		this.codeMaps = CodeUtil;
		tupleTypes = new HashMap<String, CodeClass>();
	}

	public CodeClass make(TupleType in, CodePackage tuplePack) {
		String key = "";
		// build key from the types of the parts, in alphabetical order
		TupleTypeMap TUPLE = codeMaps.buildTupleTypeMap(in);
		String[] typeNames = TUPLE.get_typenames();
		for (int i = 0; i < typeNames.length; i++) {
			key = key + "#" + typeNames[i];
		}
		CodeClass result = tupleTypes.get(key);
		if (result != null) { // found the type; it already exists.
			return result;
		}
		result = priv_make(in, tuplePack);
		tupleTypes.put(key, result);
		return result;
	}

	private CodeClass priv_make(TupleType in, CodePackage tuplePack) {
		TupleTypeMap tubple = codeMaps.buildTupleTypeMap(in);
		CodeClass created = new CodeClass(tubple.getClassName(), tuplePack);
		created.setComment(standardClassComment);
		Iterator<?> it = tubple.sort_parts().iterator();
		while (it.hasNext()) {
			Property decl = (Property) it.next();
			ClassifierMap mapper = codeMaps.buildClassifierMap((Classifier) decl.getType());
			make_attribute(created, decl, mapper);
			createGetter(decl, mapper).setDeclaringClass(created);
		}
		return created;
	}

	private CodeMethod createGetter(Property decl, ClassifierMap mapper) {
		CodeMethod oper = new CodeMethod(getGetterName(decl));
		oper.setReturnType(mapper.javaTypePath());
		oper.setVisibility(CodeVisibilityKind.PUBLIC);
		oper.setResultInitialValue(getFieldName(decl));
		return oper;
	}

	private CodeField make_attribute(CodeClass cls, Property decl, ClassifierMap mapper) {
		CodeField field = new CodeField(cls, getFieldName(decl));
		field.setVisibility(CodeVisibilityKind.PRIVATE);
		field.setType(mapper.javaTypePath());
		return field;
	}

	private String getFieldName(Property decl) {
		return NameConverter.decapitalize(decl.getName());
	}

	private String getGetterName(Property decl) {
		return "get" + NameConverter.capitalize(decl.getName());
	}

	protected CodeMethod equal_op(CodeClass currentClass, DataType in) {
		TupleTypeMap map = codeMaps.buildTupleTypeMap((TupleType) in);
		CodeMethod oper = new CodeMethod("equals");
		oper.addParam("tuple", StdlibMap.Object);
		oper.setDeclaringClass(currentClass);
		oper.setReturnType(StdlibMap.Bool);
		CodeIfStatement ifIsInstance = new CodeIfStatement(oper.getBody(), new TypeExpression(map.javaTypePath(), TypeExpressionKind.IS_KIND,
				new PortableExpression("tuple")));
		new PortableStatement(ifIsInstance.getElseBlock(), "return false");
		new CodeField(ifIsInstance.getThenBlock(), "other", map.javaTypePath()).setInitialization(new TypeExpression(map.javaDefaultTypePath(),
				TypeExpressionKind.AS_TYPE, new PortableExpression("tuple")));
		EList<Property> oclProperties = ((TupleType) in).oclProperties();
		for (Property property : oclProperties) {
			String name = getGetterName(property) + "()";
			CodeIfStatement ifNotEquals = new CodeIfStatement(ifIsInstance.getThenBlock(), new BinaryOperatorExpression(new PortableExpression(name),
					"${notEquals}", new PortableExpression("other." + name)));
			new PortableStatement(ifNotEquals.getElseBlock(), "return false");
		}
		new PortableStatement(oper.getBody(), "return true");
		return oper;
	}
}
