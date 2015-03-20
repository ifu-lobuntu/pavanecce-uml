package org.pavanecce.uml.test.uml2code.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.UMLFactory;
import org.jbpm.designer.ucd.ClassDiagramProfileImpl;
import org.jbpm.designer.uml.code.metamodel.CodeModel;
import org.jbpm.designer.uml.code.metamodel.CodeTypeReference;
import org.jbpm.designer.uml.codegen.codemodel.DefaultCodeModelBuilder;
import org.jbpm.designer.uml.codegen.codemodel.EmfCodeModelBuilder;
import org.jbpm.designer.uml.codegen.codemodel.UmlCodeModelVisitorAdaptor;
import org.jbpm.designer.uml.codegen.java.JavaCodeGenerator;
import org.junit.Before;
import org.junit.Test;
import org.pavanecce.common.test.util.DummyProgressMonitor;
import org.pavanecce.common.test.util.SourceGeneratingTestHelper;
import org.pavanecce.common.test.util.UmlExample;
import org.pavanecce.common.text.filegeneration.TextFileGenerator;
import org.pavanecce.common.util.IntrospectionUtil;
import org.pavanecce.uml.reverse.java.SimpleUmlGenerator;
import org.pavanecce.uml.reverse.java.sourcemodel.SourceClass;
import org.pavanecce.uml.reverse.java.sourcemodel.reflect.JavaDescriptorFactory;

public class RoundTripTests {
	SourceGeneratingTestHelper  helper;
	private ResourceSetImpl rst;
	private CodeModel codeModel;

	@Before
	public void setup() throws Exception {
		this.rst=new ResourceSetImpl();
		new ClassDiagramProfileImpl().prepareResourceSet(rst);
		URL vdfpFile = getClass().getClassLoader().getResource("VdfpUml.uml");
		URI uri = URI.createURI(vdfpFile.toExternalForm());
		Resource resource = rst.getResource(uri, true);
		final Model model = (Model) resource.getContents().get(0);
		UmlCodeModelVisitorAdaptor adaptor = new UmlCodeModelVisitorAdaptor(importUmlClasses(rst));
		DefaultCodeModelBuilder builder=new EmfCodeModelBuilder();
		adaptor.startVisiting(builder, model);
		codeModel = adaptor.getCodeModel();
		helper =new SourceGeneratingTestHelper(new UmlExample(){
			@Override
			public Model getModel() {
				return model;
			}

			@Override
			public String getTestName() {
				return "RoundTrip";
			}
		});
	}


	@Test
	public void testIt() throws Exception {
		TextFileGenerator tfg = helper.generateSourceCode(codeModel);
		ClassLoader cl = helper.compile(tfg.getNewFiles());
		Class<?> classifierClass = cl.loadClass(Classifier.class.getName());
		Class<?> vdfpClass = cl.loadClass("vdfp_uml.VdfpClassifier");
		assertTrue(classifierClass.isAssignableFrom(vdfpClass));
	}

	private Map<String, Classifier> importUmlClasses(ResourceSet rst) throws Exception {
		Map<String, Classifier> result = new HashMap<String, Classifier>();
		JavaDescriptorFactory jfd = new JavaDescriptorFactory();
		Set<Class<?>> dependencies = IntrospectionUtil.getDependencies(Classifier.class, "org.eclipse.uml2.uml");
		Set<SourceClass> sourceClasses = new HashSet<SourceClass>();
		for (Class<?> class1 : dependencies) {
			sourceClasses.add(jfd.getClassDescriptor(class1));
			((JavaCodeGenerator) helper.getCodeGenerator()).map(new CodeTypeReference(false, ("tmp." + class1.getName()).split("\\.")), class1.getName());
		}
		SimpleUmlGenerator sug = new SimpleUmlGenerator();
		Resource tmpResource = rst.createResource(URI.createFileURI("test-input/tmp.uml"));
		Model tmpModel = UMLFactory.eINSTANCE.createModel();
		tmpModel.setName("tmp");
		tmpResource.getContents().add(tmpModel);
		Collection<Element> generateUml = sug.generateUml(sourceClasses, tmpModel, new DummyProgressMonitor());
		for (Element element : generateUml) {
			if (element instanceof Interface) {
				Interface intf = (Interface) element;
				if (intf.getName().equals("Classifier")) {
					result.put("vdfp_uml::VdfpClassifier", (Classifier) element);
				} else if (intf.getName().equals("ParameterableElement")) {
					assertNotNull(intf.getOwnedAttribute("templateParameter", null));
				}
			}
		}
		Set<Entry<String, Classifier>> entrySet = sug.getClassMap().entrySet();
		for (Entry<String, Classifier> entry : entrySet) {
			String[] split = entry.getValue().getQualifiedName().split("\\:\\:");
			((JavaCodeGenerator)helper.getCodeGenerator()).map(new CodeTypeReference(false, split), entry.getKey());
		}

		return result;
	}

}
