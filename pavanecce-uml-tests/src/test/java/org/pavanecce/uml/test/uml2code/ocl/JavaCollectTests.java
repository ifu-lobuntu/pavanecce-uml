package org.pavanecce.uml.test.uml2code.ocl;


public class JavaCollectTests extends AbstractCollectTests {
	public JavaCollectTests() {
		super("JavaCollects");
	}


	@Override
	protected void initLanguage() throws Exception {
		JavaTestInit.initJava(helper);
	}

}
