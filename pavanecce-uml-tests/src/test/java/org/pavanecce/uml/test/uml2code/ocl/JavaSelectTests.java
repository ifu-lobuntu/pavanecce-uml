package org.pavanecce.uml.test.uml2code.ocl;


public class JavaSelectTests extends AbstractSelectTests {
	public JavaSelectTests() {
		super("JavaSelects");
	}

	@Override
	protected void initLanguage() throws Exception {
		JavaTestInit.initJava(helper);
	}
}
