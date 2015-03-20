package org.pavanecce.uml.test.uml2code.ocl;


public class JavaExistsTests extends AbstractExistsTests {

	public JavaExistsTests() {
		super("JavaExists");
	}

	@Override
	protected void initLanguage() throws Exception {
		JavaTestInit.initJava(helper);
	}

}
