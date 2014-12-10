package org.pavanecce.uml.uml2code.cmmn;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.LiteralUnlimitedNatural;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Type;
import org.pavanecce.common.util.NameConverter;
import org.pavanecce.uml.common.util.EmfAssociationUtil;
import org.pavanecce.uml.common.util.EmfClassifierUtil;
import org.pavanecce.uml.common.util.EmfPropertyUtil;
import org.pavanecce.uml.uml2code.AbstractTextGenerator;

public class CmmnTextGenerator extends AbstractTextGenerator {
	protected Deque<String> elementStack = new ArrayDeque<String>();
	private boolean qualifiedCaseFileItemIds = false;

	public CmmnTextGenerator() {
	}

	public CmmnTextGenerator(boolean qualifiedCaseFileItemIds) {
		super();
		this.qualifiedCaseFileItemIds = qualifiedCaseFileItemIds;
	}

	public String generateCaseFileItems(Class... caseFileItemClasses) {
		pushNewStringBuilder();
		pushPadding("      ");
		openElement("cmmn:caseFileModel").startContent();

		for (Class caseFileItemClass : caseFileItemClasses) {
			appendCaseFileItem(caseFileItemClass.getName(),
					caseFileItemClass, "ExactlyOne");
		}
		closeElement();
		popPadding();
		return popStringBuilder().toString();
	}

	public String generateCaseFileItemDefinitions(Package pkg) {
		pushNewStringBuilder();
		pushPadding("      ");
		EList<Type> ownedTypes = pkg.getOwnedTypes();
		for (Type type : ownedTypes) {
			if (isCaseFileItemDefinition(type)) {
				openElement("cmmn:caseFileItemDefinition");
				appendAttribute("name", type.getName() + "Definition");
				appendAttribute("id",
						NameConverter.decapitalize(type.getName())
								+ "DefinitionId");
				appendAttribute("definitionType",
						"http://www.omg.org/spec/CMMN/DefinitionType/UMLClass");
				appendAttribute("structureRef", type.getQualifiedName());
				startContent();
				closeElement();
			}
		}
		for (Package child : pkg.getNestedPackages()) {
			generateCaseFileItemDefinitions(child);
		}

		return popStringBuilder().toString();
	}

	private boolean isCaseFileItemDefinition(Type type) {
		return (type instanceof Class && !((Class) type).isAbstract())
				|| (type instanceof Association && EmfAssociationUtil
						.isClass((Association) type));
	}

	protected void appendCaseFileItems(String parentCaseFileItemName,
			List<Property> props) {
		for (Property property : props) {
			String name = property.getName();
			Type type = property.getType();
			String multiplicity = multiplicity(property);
			if (property.isComposite()) {
				appendCaseFileItem(name, type,
						multiplicity);
			}
		}
	}

	private void appendCaseFileItem(String name, Type type, String multiplicity) {
		openElement("cmmn:caseFileItem");
		appendAttribute("name", name);
		appendAttribute("id", NameConverter.decapitalize(type.getName())
				+ "FileItemId");
		appendAttribute("definitionRef",
				NameConverter.decapitalize(type.getName()) + "DefinitionId");
		if (type instanceof Classifier) {
			StringBuilder targetRefs = new StringBuilder();
			for (Property child : EmfPropertyUtil
					.getEffectiveProperties((Classifier) type)) {
				if (!child.isComposite()
						&& EmfClassifierUtil.isPersistent(child.getType())
						&& (child.getOtherEnd() == null || !child.getOtherEnd()
								.isComposite())) {
					targetRefs.append(NameConverter.decapitalize(child
							.getType().getName()) + "FileItemId");
					targetRefs.append(" ");
				}
			}

			if (targetRefs.length() != 0) {
				appendAttribute("targetRefs", targetRefs.toString());
			}
		}
		appendAttribute("multiplicity", multiplicity);
		startContent();
		if (type instanceof Classifier) {
			openElement("cmmn:children").startContent();
			appendCaseFileItems(name,
					EmfPropertyUtil.getEffectiveProperties((Classifier) type));
			closeElement();
		}
		closeElement();
	}

	private String multiplicity(Property property) {
		if (property.getLower() == 0) {
			if (property.getUpper() == LiteralUnlimitedNatural.UNLIMITED) {
				return "ZeroOrMore";
			} else if (property.getUpper() == 1) {
				return "ZeroOrOne";
			}
		} else if (property.getLower() == 1) {
			if (property.getUpper() == LiteralUnlimitedNatural.UNLIMITED) {
				return "OneOrMore";
			} else if (property.getUpper() == 1) {
				return "ExactlyOne";
			}
		}
		return "Unspecified";
	}

	private void appendAttribute(String name, String value) {
		sb.append(" ");
		sb.append(name);
		sb.append("=\"");
		sb.append(value);
		sb.append("\"");

	}

	private CmmnTextGenerator startContent() {
		sb.append(">");
		endLine();
		pushPadding(padding + "  ");
		return this;
	}

	private CmmnTextGenerator closeElement() {
		popPadding();
		sb.append(padding);
		sb.append("</");
		sb.append(elementStack.pop());
		sb.append(">");
		endLine();
		return this;
	}

	private CmmnTextGenerator openElement(String name) {
		sb.append(padding);
		sb.append("<");
		sb.append(name);
		elementStack.push(name);
		return this;
	}

	@Override
	public CmmnTextGenerator append(String string) {
		return (CmmnTextGenerator) super.append(string);
	}

	public CmmnTextGenerator endLine() {
		append("\n");
		return this;
	}

}
