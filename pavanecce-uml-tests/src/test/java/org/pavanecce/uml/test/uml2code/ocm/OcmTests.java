package org.pavanecce.uml.test.uml2code.ocm;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.script.ScriptContext;

import org.apache.jackrabbit.commons.cnd.CndImporter;
import org.apache.jackrabbit.core.TransientRepository;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.AnnotationMapperImpl;
import org.apache.jackrabbit.ocm.reflection.ReflectionUtils;
import org.jbpm.designer.uml.code.metamodel.CodeClass;
import org.jbpm.designer.uml.code.metamodel.CodeClassifier;
import org.jbpm.designer.uml.code.metamodel.CodePackage;
import org.jbpm.designer.uml.code.metamodel.documentdb.DocumentNamespace;
import org.jbpm.designer.uml.codegen.java.JavaCodeGenerator;
import org.jbpm.designer.uml.codegen.ocm.CndTextGenerator;
import org.jbpm.designer.uml.codegen.ocm.DocumentModelBuilder;
import org.jbpm.designer.uml.codegen.ocm.OcmCodeDecorator;
import org.jbpm.designer.uml.codegen.ocm.UmlDocumentModelFileVisitorAdaptor;
import org.junit.Before;
import org.pavanecce.common.ocm.ObjectContentManagerFactory;
import org.pavanecce.common.ocm.OcmObjectPersistence;
import org.pavanecce.common.test.util.ConstructionCaseExample;
import org.pavanecce.common.util.FileUtil;
import org.pavanecce.uml.test.uml2code.jpa.AbstractPersistenceTest;
import org.pavanecce.uml.uml2code.java.AssociationCollectionCodeDecorator;

public class OcmTests extends AbstractPersistenceTest {

	public OcmTests() {
		super("OcmPersistence");
	}

	@SuppressWarnings("rawtypes")
	@Before
	public  void setup() throws Exception {
		FileUtil.deleteRoot(new File("./repository"));
		System.setProperty("java.naming.factory.initial", "bitronix.tm.jndi.BitronixInitialContextFactory");
		example = new ConstructionCaseExample("OcmPersistence");
		helper.setDecorators(new AssociationCollectionCodeDecorator(), new OcmCodeDecorator());
		helper.generateCode(new JavaCodeGenerator());
		List<Class> classes = getClasses();
		File outputRoot = helper.calculateBinaryOutputRoot();
		File testCndFile = generateCndFile(outputRoot, new CndTextGenerator());
		Repository tr = new TransientRepository();
		Session session = tr.login(new SimpleCredentials("admin", "admin".toCharArray()));
		CndImporter.registerNodeTypes(new FileReader(testCndFile), session);
		session.getRootNode().addNode("ConstructionCaseCollection");
		session.save();
		// session.logout();
		ReflectionUtils.setClassLoader(helper.getClassLoader());
		ObjectContentManagerFactory objectContentManagerFactory = new ObjectContentManagerFactory(tr, "admin", "admin", new AnnotationMapperImpl(classes), null);
		OcmObjectPersistence hibernatePersistence = new OcmObjectPersistence(objectContentManagerFactory);
		helper.initScriptingEngine();
		helper.getJavaScriptContext().setAttribute("p", hibernatePersistence, ScriptContext.ENGINE_SCOPE);
	}

	protected File generateCndFile(File outputRoot, CndTextGenerator cndTextGenerator) {
		UmlDocumentModelFileVisitorAdaptor a = new UmlDocumentModelFileVisitorAdaptor();
		DocumentModelBuilder docBuilder = new DocumentModelBuilder();
		a.startVisiting(docBuilder, example.getModel());

		DocumentNamespace documentModel = docBuilder.getDocumentModel();
		File testCndFile = new File(outputRoot, "test.cnd");
		FileUtil.write(testCndFile, cndTextGenerator.generate(documentModel));
		return testCndFile;
	}

	@SuppressWarnings("rawtypes")
	private List<Class> getClasses() throws Exception {
		List<Class> result = new ArrayList<Class>();
		addMappedClasses(result, helper.getAdaptor().getCodeModel(), (JavaCodeGenerator) helper.getCodeGenerator(), helper.getClassLoader());
		return result;
	}

	@SuppressWarnings("rawtypes")
	protected static void addMappedClasses(List<Class> classes, CodePackage codePackage, JavaCodeGenerator jcg, ClassLoader cl) throws Exception {
		for (CodeClassifier cc : codePackage.getClassifiers().values()) {
			if (cc instanceof CodeClass) {
				classes.add(cl.loadClass(jcg.toQualifiedName(cc.getTypeReference())));
			}
		}
		Collection<CodePackage> values = codePackage.getChildren().values();
		for (CodePackage child : values) {
			addMappedClasses(classes, child, jcg, cl);
		}
	}

}
