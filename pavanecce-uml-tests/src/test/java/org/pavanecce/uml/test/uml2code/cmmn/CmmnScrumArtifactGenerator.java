package org.pavanecce.uml.test.uml2code.cmmn;

import java.io.File;
import java.util.Collections;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Model;
import org.pavanecce.common.code.metamodel.CodeModel;
import org.pavanecce.common.test.util.AbstractPotentiallyJavaCompilingTest;
import org.pavanecce.common.text.filegeneration.TextFileGenerator;
import org.pavanecce.common.text.filegeneration.TextNodeVisitorAdapter;
import org.pavanecce.common.text.workspace.ProjectNameStrategy;
import org.pavanecce.common.text.workspace.SourceFolder;
import org.pavanecce.common.text.workspace.SourceFolderDefinition;
import org.pavanecce.common.text.workspace.SourceFolderNameStrategy;
import org.pavanecce.common.text.workspace.TextProject;
import org.pavanecce.common.text.workspace.TextProjectDefinition;
import org.pavanecce.common.text.workspace.TextWorkspace;
import org.pavanecce.common.util.VersionNumber;
import org.pavanecce.uml.common.util.StandaloneLocator;
import org.pavanecce.uml.common.util.UmlResourceSetFactory;
import org.pavanecce.uml.uml2code.cmmn.CmmnTextGenerator;
import org.pavanecce.uml.uml2code.codemodel.CodeModelBuilder;
import org.pavanecce.uml.uml2code.java.JavaCodeGenerator;
import org.pavanecce.uml.uml2code.jpa.JpaCodeDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CmmnScrumArtifactGenerator {
	static Logger logger = LoggerFactory.getLogger(CmmnScrumArtifactGenerator.class);

	public static void main(String[] args) throws Exception {
		UmlResourceSetFactory rsf = new UmlResourceSetFactory(new StandaloneLocator());
		ResourceSet rst = rsf.prepareResourceSet();
		URI uri = URI.createFileURI("/home/ampie/test/jbpm-playground/scrum/src/main/resources/scrum.uml");
		Resource res = rst.createResource(uri);
		res.load(Collections.emptyMap());
		final File outputRoot = new File("/home/ampie/test/jbpm-playground");
		// final File outputRoot = new File("/home/ampie/Code/pavanecce/pavanecce-uml");
		AbstractPotentiallyJavaCompilingTest codeGeneration = new AbstractPotentiallyJavaCompilingTest() {
			@Override
			protected TextFileGenerator generateSourceCode(CodeModel codeModel) {
				TextWorkspace tw = new TextWorkspace("thisgoesnowhere");
				TextFileGenerator tfg = new TextFileGenerator(outputRoot);
				TextProjectDefinition tfd = new TextProjectDefinition(ProjectNameStrategy.SUFFIX_ONLY, "scrum");
				// TextProjectDefinition tfd = new TextProjectDefinition(ProjectNameStrategy.SUFFIX_ONLY,
				// "pavanecce-uml-jbpm");
				VersionNumber vn = new VersionNumber("0.0.1");
				TextProject tp = tw.findOrCreateTextProject(tfd, "", vn);
				SourceFolder sf = tp.findOrCreateSourceFolder(new SourceFolderDefinition(SourceFolderNameStrategy.QUALIFIER_ONLY, "src/main/java"),
						"", vn);
				TextNodeVisitorAdapter tnva = new TextNodeVisitorAdapter();
				createText(codeModel, sf);
				tnva.startVisiting(tw, tfg);
				return tfg;
			}

			@Override
			protected String getTestName() {
				return null;
			}
		};
		codeGeneration.setModel((Model) res.getContents().get(0));
		codeGeneration.setup(new CodeModelBuilder(false), new JavaCodeGenerator(), new JpaCodeDecorator());
		CmmnTextGenerator cmmn = new CmmnTextGenerator(true);
		logger.info(cmmn.generateCaseFileItemDefinitions(codeGeneration.getModel()));
		logger.info(cmmn.generateCaseFileItems((Class) codeGeneration.getModel().getOwnedType("ProductBacklog")));
		logger.info(cmmn.generateCaseFileItems((Class) codeGeneration.getModel().getOwnedType("SprintBacklog")));
	}
}
