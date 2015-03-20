package org.pavanecce.uml.test.uml2code.ocl;


public class JavascriptCollectTests extends AbstractCollectTests {
	public JavascriptCollectTests() {
		super("JavascriptCollects");
	}

	@Override
	protected void initLanguage() throws Exception {
		JavaScriptTestInit.init(helper);
	}

}
