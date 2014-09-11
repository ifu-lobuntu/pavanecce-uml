package org.pavanecce.uml.jbpm;

import java.io.IOException;
import java.util.HashMap;

import org.drools.compiler.builder.impl.KnowledgeBuilderConfigurationImpl;
import org.drools.compiler.builder.impl.KnowledgeBuilderImpl;
import org.drools.core.xml.ExtensibleXmlParser;
import org.drools.core.xml.SemanticModule;
import org.drools.core.xml.SemanticModules;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.jbpm.process.core.Process;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceConfiguration;
import org.kie.api.io.ResourceType;
import org.kie.internal.assembler.KieAssemblerService;
import org.kie.internal.assembler.KieAssemblers;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.utils.ServiceRegistryImpl;
import org.pavanecce.cmmn.jbpm.xml.handler.CMMNSemanticModule;
import org.pavanecce.cmmn.jbpm.xml.handler.CaseHandler;
import org.pavanecce.uml.common.util.StandaloneLocator;
import org.pavanecce.uml.common.util.UmlResourceSetFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class UmlBuilder implements KieAssemblerService {
	public static final ResourceType UML_RESOURCE_TYPE = ResourceType.addResourceTypeToRegistry("UML", "UML", "src/main/resources", "uml");
	public static final String UML_RESOURCE_SET = "uml.resource.set";
	static {
		ServiceRegistryImpl.getInstance().get(KieAssemblers.class).getAssemblers().put(UML_RESOURCE_TYPE, new UmlBuilder());
	}
	private ResourceSet rst = new UmlResourceSetFactory(new StandaloneLocator()).prepareResourceSet();

	@Override
	public Class<?> getServiceInterface() {
		return KieAssemblerService.class;
	}

	@Override
	public ResourceType getResourceType() {
		return UML_RESOURCE_TYPE;
	}

	@Override
	public void addResource(KnowledgeBuilder kbuilder, Resource resource, ResourceType type, ResourceConfiguration configuration) throws IOException {
		KnowledgeBuilderImpl kb = (KnowledgeBuilderImpl) kbuilder;
		KnowledgeBuilderConfigurationImpl conf = kb.getBuilderConfiguration();
		if (conf.getSemanticModules().getSemanticModule(CMMNSemanticModule.CMMN_URI) == null) {
			conf.addSemanticModule(new CMMNSemanticModule());
		}
		conf.getSemanticModules().getSemanticModule(CMMNSemanticModule.CMMN_URI).addHandler("case", new CaseHandler() {
			@Override
			public Object start(String uri, String localName, Attributes attrs, ExtensibleXmlParser parser) throws SAXException {
				Process process = (Process) super.start(uri, localName, attrs, parser);
				process.setMetaData(UML_RESOURCE_SET, rst);
				return process;
			}

		});
		rst.createResource(URI.createFileURI(resource.getSourcePath())).load(resource.getInputStream(), new HashMap<Object, Object>());

	}
}
