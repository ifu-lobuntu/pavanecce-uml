package org.pavanecce.uml.test.uml2code.ocl;

import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.Set;

import javax.script.ScriptException;

import org.jbpm.designer.uml.codegen.codemodel.CodeModelBuilder;
import org.pavanecce.common.test.util.SourceGeneratingTestHelper;
import org.pavanecce.uml.ocl2code.OclCodeBuilder;
import org.pavanecce.uml.uml2code.javascript.JavaScriptGenerator;

public class JavaScriptTestInit {

	public static void init(SourceGeneratingTestHelper helper) throws Exception {
		helper.setBuilders(new CodeModelBuilder(true), new OclCodeBuilder());
		helper.generateCode(new JavaScriptGenerator());
		helper.initScriptingEngine();
		Set<File> newFiles = helper.getTextFileGenerator().getNewFiles();
		evaluateResource(helper, "underscore.js");
		evaluateResource(helper, "backbone.js");
		for (File file : newFiles) {
			helper.getJavaScriptEngine().eval(new FileReader(file));
		}
	}

	protected static void evaluateResource(SourceGeneratingTestHelper helper, String name2) throws ScriptException {
		helper.getJavaScriptEngine().eval(new InputStreamReader(JavascriptCollectTests.class.getClassLoader().getResourceAsStream(name2)));
	}
}
