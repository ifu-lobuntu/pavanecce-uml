package org.pavanecce.uml.test.uml2code.ocl;

import org.jbpm.designer.uml.codegen.java.JavaCodeGenerator;
import org.junit.BeforeClass;
import org.pavanecce.common.test.util.ConstructionCaseExample;
import org.pavanecce.common.test.util.SourceGeneratingTestHelper;
import org.pavanecce.uml.uml2code.java.AssociationCollectionCodeDecorator;

public class JavaOneTests extends AbstractOneTests {
	public JavaOneTests() {
		super("JavaOne");
	}
	@Override
	protected void initLanguage() throws Exception {
		JavaTestInit.initJava(helper);
	}

}
