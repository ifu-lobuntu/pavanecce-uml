package org.pavanecce.uml.test.uml2code.ocl;


public class JavascriptEnumerationLiteralTests extends AbstractEnumerationLiteralTests {
	public JavascriptEnumerationLiteralTests() {
		super("JavascriptEnumerationLiterals");
	}


	@Override
	protected void initLanguage() throws Exception {
		JavaScriptTestInit.init(helper);

	}
}
