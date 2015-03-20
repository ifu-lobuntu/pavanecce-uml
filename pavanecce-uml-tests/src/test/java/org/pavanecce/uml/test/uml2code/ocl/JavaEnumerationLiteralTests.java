package org.pavanecce.uml.test.uml2code.ocl;


public class JavaEnumerationLiteralTests extends AbstractEnumerationLiteralTests {
	public JavaEnumerationLiteralTests() {
		super("JavaEnumerationLiterals");
	}

	@Override
	protected void initLanguage() throws Exception {
		JavaTestInit.initJava(helper);
	}

}
