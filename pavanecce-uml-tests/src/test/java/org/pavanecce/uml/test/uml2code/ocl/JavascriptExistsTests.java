package org.pavanecce.uml.test.uml2code.ocl;


public class JavascriptExistsTests extends AbstractExistsTests {
	public JavascriptExistsTests() {
		super("JavascriptExists");
	}

	@Override
	protected void initLanguage() throws Exception {
		JavaScriptTestInit.init(helper);

	}

}
