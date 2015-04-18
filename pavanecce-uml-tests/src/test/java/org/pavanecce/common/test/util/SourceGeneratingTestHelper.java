package org.pavanecce.common.test.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.script.ScriptException;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.jbpm.designer.uml.code.metamodel.CodeClassifier;
import org.jbpm.designer.uml.code.metamodel.CodeModel;
import org.jbpm.designer.uml.code.metamodel.CodePackage;
import org.jbpm.designer.uml.codegen.AbstractCodeGenerator;
import org.jbpm.designer.uml.codegen.codemodel.CodeModelBuilder;
import org.jbpm.designer.uml.codegen.codemodel.UmlCodeModelVisitorAdaptor;
import org.jbpm.designer.uml.codegen.java.JavaCodeGenerator;
import org.jbpm.designer.uml.codegen.jpa.AbstractJavaCodeDecorator;
import org.osgi.framework.wiring.BundleWiring;
import org.pavanecce.common.text.filegeneration.TextFileGenerator;
import org.pavanecce.common.text.filegeneration.TextNodeVisitorAdapter;
import org.pavanecce.common.text.workspace.CharArrayTextSource;
import org.pavanecce.common.text.workspace.ProjectNameStrategy;
import org.pavanecce.common.text.workspace.SourceFolder;
import org.pavanecce.common.text.workspace.SourceFolderDefinition;
import org.pavanecce.common.text.workspace.SourceFolderNameStrategy;
import org.pavanecce.common.text.workspace.TextProject;
import org.pavanecce.common.text.workspace.TextProjectDefinition;
import org.pavanecce.common.text.workspace.TextWorkspace;
import org.pavanecce.common.util.FileUtil;
import org.pavanecce.common.util.VersionNumber;
import org.pavanecce.uml.common.util.IFileLocator;
import org.pavanecce.uml.common.util.StandaloneLocator;
import org.pavanecce.uml.ocl2code.OclCodeBuilder;
import org.pavanecce.uml.test.uml2code.test.TestBundle;
import org.pavanecce.uml.uml2code.javascript.JavaScriptGenerator;
import org.phidias.compile.BundleJavaManager;
import org.phidias.compile.ResourceResolver;

public class SourceGeneratingTestHelper extends JavaCompilingTestHelper {
	protected IFileLocator fileLocator = new StandaloneLocator();
	private TextFileGenerator textFileGenerator;

	public SourceGeneratingTestHelper(UmlExample example) {
		super(example);
	}

	@Override
	protected String getTestName() {
		return example.getTestName();
	}

	public Object eval(String s) throws ScriptException {
		return getJavaScriptEngine().eval(s);
	}

	public TextFileGenerator generateSourceCode(CodeModel codeModel) {
		TextWorkspace tw = new TextWorkspace("thisgoesnowhere");
		File outputRoot = calculateTextOutputRoot();
		TextFileGenerator tfg = new TextFileGenerator(outputRoot);
		TextProjectDefinition tfd = new TextProjectDefinition(ProjectNameStrategy.SUFFIX_ONLY, "org.pavanecce.uml.test.domain");
		VersionNumber vn = new VersionNumber("0.0.1");
		TextProject tp = tw.findOrCreateTextProject(tfd, "", vn);
		SourceFolder sf = tp.findOrCreateSourceFolder(new SourceFolderDefinition(SourceFolderNameStrategy.QUALIFIER_ONLY, "gen-src"), "", vn);
		TextNodeVisitorAdapter tnva = new TextNodeVisitorAdapter();
		createText(codeModel, sf);
		tnva.startVisiting(tw, tfg);
		return tfg;
	}

	public String getExtension() {
		if (getCodeGenerator() instanceof JavaCodeGenerator) {
			return ".java";
		} else if (getCodeGenerator() instanceof JavaScriptGenerator) {
			return ".js";
		} else {
			return ".py";

		}
	}

	public void setup(AbstractCodeGenerator codeGenerator, AbstractJavaCodeDecorator... decorators) throws Exception {
		this.setJavaCodeGenerator(codeGenerator);
		this.setup(codeGenerator,decorators);
		this.getAdaptor().startVisiting(new OclCodeBuilder(), example.getModel());
		this.textFileGenerator = generateSourceCode(this.getAdaptor().getCodeModel());
		if (getCodeGenerator() instanceof JavaCodeGenerator) {
			setClassLoader(this.compile(textFileGenerator.getNewFiles()));
		}
	}

	protected void createText(SourceFolder sf, Collection<CodeClassifier> values) {
		for (CodeClassifier codeClassifier : values) {
			String javaSource = getCodeGenerator().toClassifierDeclaration(codeClassifier);
			List<String> path = codeClassifier.getPackage().getPath();
			path.add(codeClassifier.getName() + getExtension());
			sf.findOrCreateTextFile(path).setTextSource(new CharArrayTextSource(javaSource.toCharArray()));
		}
	}

	public TextFileGenerator getTextFileGenerator() {
		return textFileGenerator;
	}

	protected void createText(CodePackage codeModel, SourceFolder sf) {
		Collection<CodeClassifier> values = codeModel.getClassifiers().values();
		for (CodeClassifier codeClassifier : values) {
			String javaSource = getCodeGenerator().toClassifierDeclaration(codeClassifier);
			List<String> path = codeClassifier.getPackage().getPath();
			path.add(codeClassifier.getName() + getExtension());
			sf.findOrCreateTextFile(path).setTextSource(new CharArrayTextSource(javaSource.toCharArray()));
		}
		for (CodePackage codePackage : codeModel.getChildren().values()) {
			createText(codePackage, sf);
		}
	}

	void compileInOsgi(Set<File> set, File destination) {
		try {
			// compiler options
			List<String> options = new ArrayList<String>();

			options.add("-proc:none"); // don't process annotations (typical for
										// jsps)
										// options.add("-verbose"); // Phidias
										// adds to the default verbose
			// // output
			options.add("-d");
			options.add(destination.getCanonicalPath());
			JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();

			// Diagnostics provide details about all errors/warnings observed
			// during compilation
			DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

			// the standard file manager knows how to load libraries from disk
			// and from the runtime
			StandardJavaFileManager standardFileManager = javaCompiler.getStandardFileManager(diagnostics, null, null);

			// Were using source in string format (possibly generated
			// dynamically)
			// JavaFileObject[] sourceFiles = { new
			// StringJavaFileObject(javaSourceName, javaSource) };

			// the OSGi aware file manager
			Iterable<? extends JavaFileObject> fos = standardFileManager.getJavaFileObjects(set.toArray(new File[set.size()]));
			BundleJavaManager bundleFileManager = new BundleJavaManager(TestBundle.bundle, standardFileManager, options);
			// OPtional::
			{
				// *** New since 0.1.7 ***
				// A new, optional, ResourceResolver API

				// This is optional as a default implementation is automatically
				// provided with using the exact logic below.

				ResourceResolver resourceResolver = new ResourceResolver() {
					@Override
					public URL getResource(BundleWiring bundleWiring, String name) {
						return bundleWiring.getBundle().getResource(name);
					}

					@Override
					public Collection<String> resolveResources(BundleWiring bundleWiring, String path, String filePattern, int options) {

						// Use whatever magic you like here to provide
						// additional
						// resolution, such as overcoming the fact that the
						// system.bundle won't return resources from the parent
						// classloader, even when those are exported by
						// framework
						// extension bundles

						return bundleWiring.listResources(path, filePattern, options);
					}
				};

				bundleFileManager.setResourceResolver(resourceResolver);
			}

			// get the compilation task
			CompilationTask compilationTask = javaCompiler.getTask(null, bundleFileManager, diagnostics, options, null, fos);

			bundleFileManager.close();

			// perform the actual compilation
			if (compilationTask.call()) {
				// Success!

				return;
			}

			// Oh no, we got errors, list them
			for (Diagnostic<?> dm : diagnostics.getDiagnostics()) {
				logger.error("COMPILE ERROR: " + dm);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public ClassLoader compile(Set<File> set) throws IOException {
		ClassLoader newClassLoader = null;
		File destination = calculateBinaryOutputRoot();
		FileUtil.deleteAllChildren(destination);
		if (Thread.currentThread().getContextClassLoader() instanceof URLClassLoader) {
			logger.info("!!!!!!!!!!!!!!!!!!!!Compiling in Standalone architecture!!!!!!!!!!!!!!!!!!");
			newClassLoader = compileInStandalone(set, destination);
		} else {
			logger.info("!!!!!!!!!!!!!!!!!!!!Compiling in OSGi architecture!!!!!!!!!!!!!!!!!!");
			compileInOsgi(set, destination);
			newClassLoader = new URLClassLoader(new URL[] { destination.toURI().toURL() }, getClass().getClassLoader());
		}
		return newClassLoader;
	}

}