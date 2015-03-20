package org.pavanecce.uml.test.uml2code.ocl;

import javax.script.ScriptException;

import org.jbpm.designer.uml.codegen.codemodel.CodeModelBuilder;
import org.jbpm.designer.uml.codegen.java.JavaCodeGenerator;
import org.pavanecce.common.test.util.SourceGeneratingTestHelper;
import org.pavanecce.uml.ocl2code.OclCodeBuilder;
import org.pavanecce.uml.uml2code.java.AssociationCollectionCodeDecorator;

public class JavaTestInit {

	public static void initJava(SourceGeneratingTestHelper helper) throws Exception{
		helper.setBuilders(new CodeModelBuilder(true),new OclCodeBuilder());
		helper.setDecorators(new AssociationCollectionCodeDecorator());
		helper.generateCode(new JavaCodeGenerator());
		helper.initScriptingEngine();
		helper.eval("ConstructionCase=Packages.test.ConstructionCase;");
		helper.eval("HousePlan=Packages.test.HousePlan;");
		helper.eval("House=Packages.test.House;");
		helper.eval("WallPlan=Packages.test.WallPlan;");
		helper.eval("RoomPlan=Packages.test.RoomPlan;");
		helper.eval("HouseStatus=Packages.test.HouseStatus;");
	}

}
