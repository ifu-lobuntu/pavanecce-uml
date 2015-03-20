package org.pavanecce.uml.ocl2code.creators;

import org.jbpm.designer.uml.code.metamodel.CodePackage;
import org.jbpm.designer.uml.code.metamodel.CodePackageReference;

public class UtilityCreator {
	static private final ThreadLocal<CodePackageReference> utilPath = new ThreadLocal<CodePackageReference>();
	static private final ThreadLocal<CodePackage> utilPack = new ThreadLocal<CodePackage>();

	public UtilityCreator() {
		super();
	}

	public static CodePackageReference getUtilPathName() {
		return utilPath.get().getCopy();
	}

	public static void setUtilPathName(CodePackageReference name) {
		utilPath.set(name);
	}

	public static CodePackage getUtilPack() {
		return utilPack.get();
	}
}
