package org.pavanecce.uml.test.uml2code.ocl;

import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.Set;

import javax.script.ScriptException;

import org.junit.BeforeClass;
import org.pavanecce.common.test.util.ConstructionCaseExample;
import org.pavanecce.common.test.util.SourceGeneratingTestHelper;
import org.pavanecce.uml.uml2code.javascript.JavaScriptGenerator;

public class JavascriptOneTests extends AbstractOneTests {
	public JavascriptOneTests() {
		super("JavascriptOne");
	}
	@Override
	protected void initLanguage() throws Exception {
		JavaScriptTestInit.init(helper);

	}

}
