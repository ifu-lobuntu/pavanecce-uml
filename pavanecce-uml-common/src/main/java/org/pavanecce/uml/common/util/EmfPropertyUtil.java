package org.pavanecce.uml.common.util;

import static org.pavanecce.uml.common.util.PersistentNameUtil.getPersistentName;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.ocl.expressions.CollectionKind;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.DataType;
import org.eclipse.uml2.uml.EnumerationLiteral;
import org.eclipse.uml2.uml.MultiplicityElement;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.TypedElement;
public class EmfPropertyUtil extends org.jbpm.designer.uml.codegen.util.EmfPropertyUtil {

	public static boolean isMarkedAsPrimaryKey(Property property) {
		Classifier owner = getOwningClassifier(property);
		for (Stereotype st : owner.getAppliedStereotypes()) {
			if (st.getMember("primaryKey") != null) {
				return property.equals(owner.getValue(st, "primaryKey"));
			}
		}
		return false;
	}

	public static boolean isDimension(TypedElement te) {
		return fullfillsRoleInCube(te, "DIMENSION");
	}

	public static boolean isMeasure(TypedElement te) {
		return fullfillsRoleInCube(te, "MEASURE");
	}

	private static boolean fullfillsRoleInCube(TypedElement te, String role) {
		if (EmfPropertyUtil.isMany(te)) {
			return false;
		}
		for (Stereotype st : te.getAppliedStereotypes()) {
			Property roleInCube = st.getAttribute("roleInCube", null);
			if (roleInCube != null) {
				EnumerationLiteral l = (EnumerationLiteral) te.getValue(st, "roleInCube");
				return l.getName().toUpperCase().equals(role);
			}
		}
		return false;
	}

	public static CollectionKind getCollectionKind(MultiplicityElement exp) {
		if (exp == null) {
			return null;
		}
		if (exp.isMultivalued()) {
			if (exp.isOrdered()) {
				if (exp.isUnique()) {
					return CollectionKind.ORDERED_SET_LITERAL;
				} else {
					return CollectionKind.SEQUENCE_LITERAL;
				}
			} else {
				if (exp.isUnique()) {
					return CollectionKind.SET_LITERAL;
				} else {
					return CollectionKind.BAG_LITERAL;
				}
			}
		}
		return null;
	}

	public static boolean isQualifier(Property p) {
		EObject container = EmfElementFinder.getContainer(p);
		if (container instanceof Classifier) {
			Classifier c = (Classifier) container;
			List<Property> propertiesInScope = EmfPropertyUtil.getEffectiveProperties(c);
			for (Property property : propertiesInScope) {
				if (property.getOtherEnd() != null) {
					for (Property q : property.getOtherEnd().getQualifiers()) {
						if (p.getName().equals(q.getName())) {
							return true;
						}
					}
				}
			}
		} else {
			System.out.println();
		}
		return false;
	}

	public static Property getBackingPropertyForQualifier(Property q) {
		Classifier type = (Classifier) q.getAssociationEnd().getType();
		for (Property property : getEffectiveProperties(type)) {
			if (property.getName().equals(q.getName())) {
				return property;
			}
		}
		return null;
	}

	public static Property findTargetPropertyByReferencedColumnName(Property toOne, String referencedColumnName) {
		Property primaryKey = getPrimaryKey((Classifier) toOne.getType());
		if (primaryKey != null) {
			String persistentName = getPersistentName(primaryKey);
			if (persistentName.equals(referencedColumnName)) {
				return primaryKey;
			} else if (primaryKey.getType() instanceof DataType) {
				List<Property> pkProperties = getEffectiveProperties((Classifier) primaryKey.getType());
				for (Property pkProperty : pkProperties) {
					if (getPersistentName(pkProperty).equals(referencedColumnName)) {
						return pkProperty;
					}
				}
			}
		}
		for (Property property : getEffectiveProperties((Classifier) toOne.getType())) {
			if (isMarkedAsPrimaryKey(property)) {
				if (EmfClassifierUtil.isSimpleType(property.getType()) && getPersistentName(property).equals(referencedColumnName)) {
					return property;
				} else if (property.getType() instanceof DataType) {
					List<Property> pkProperties = getEffectiveProperties((Classifier) property.getType());
					for (Property pkProperty : pkProperties) {
						if (getPersistentName(pkProperty).equals(referencedColumnName)) {
							return pkProperty;
						}
					}
				}

			}
		}
		return null;
	}

	public static Property getPrimaryKey(Classifier owner) {
		for (Stereotype st : owner.getAppliedStereotypes()) {
			if (st.getMember("primaryKey") != null) {
				return (Property) owner.getValue(st, "primaryKey");
			}
		}
		return null;
	}

}
