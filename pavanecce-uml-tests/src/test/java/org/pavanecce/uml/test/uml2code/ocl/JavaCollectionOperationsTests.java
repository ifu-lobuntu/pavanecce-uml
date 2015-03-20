package org.pavanecce.uml.test.uml2code.ocl;

import org.junit.BeforeClass;
import org.pavanecce.common.test.util.ConstructionCaseExample;

public class JavaCollectionOperationsTests extends AbstractCollectionOperationsTests {
	public JavaCollectionOperationsTests() {
		super("JavaCollectionOperations");
	}

	@Override
	protected void initLanguage() throws Exception {
		JavaTestInit.initJava(helper);
	}

}
