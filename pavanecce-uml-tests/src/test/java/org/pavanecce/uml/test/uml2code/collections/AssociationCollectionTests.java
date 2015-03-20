package org.pavanecce.uml.test.uml2code.collections;

import javax.script.ScriptException;

import org.jbpm.designer.uml.codegen.codemodel.CodeModelBuilder;
import org.jbpm.designer.uml.codegen.java.JavaCodeGenerator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pavanecce.common.test.util.ConstructionCaseExample;
import org.pavanecce.common.test.util.SourceGeneratingTestHelper;
import org.pavanecce.uml.uml2code.java.AssociationCollectionCodeDecorator;

public class AssociationCollectionTests extends Assert {
	ConstructionCaseExample example = new ConstructionCaseExample("AssociationCollections");
	SourceGeneratingTestHelper helper = new SourceGeneratingTestHelper(example);

	@After
	public void after() {
		helper.after();
	}

	@Before
	public void setup() throws Exception {
		helper.setBuilders(new CodeModelBuilder(true));
		helper.setDecorators(new AssociationCollectionCodeDecorator());
		helper.setup(new JavaCodeGenerator());
	}

	@Test
	public void testManyToManySet() throws Exception {
		eval("WallPlan=Packages.test.WallPlan;");
		eval("RoomPlan=Packages.test.RoomPlan;");
		eval("var roomPlan=new RoomPlan();");
		eval("var wallPlan1=new WallPlan();");
		eval("var wallPlan2=new WallPlan();");
		eval("roomPlan.getWallPlans().add(wallPlan1);");
		eval("roomPlan.getWallPlans().add(wallPlan2);");
		assertEquals(2, eval("roomPlan.getWallPlans().size();"));
		assertEquals(1, eval("wallPlan1.getRoomPlans().size();"));
		assertEquals(1, eval("wallPlan1.getRoomPlans().size();"));
		eval("roomPlan.getWallPlans().remove(wallPlan1);");
		assertEquals(1, eval("roomPlan.getWallPlans().size();"));
		assertEquals(0, eval("wallPlan1.getRoomPlans().size();"));
	}

	@Test
	public void testOneToManySet() throws Exception {
		eval("WallPlan=Packages.test.WallPlan;");
		eval("HousePlan=Packages.test.HousePlan;");
		eval("var housePlan=new HousePlan();");
		eval("var wallPlan1=new WallPlan();");
		eval("var wallPlan2=new WallPlan();");
		eval("housePlan.getWallPlans().add(wallPlan1);");
		eval("housePlan.getWallPlans().add(wallPlan2);");
		assertEquals(2, eval("housePlan.getWallPlans().size();"));
		assertSame(eval("housePlan"), eval("wallPlan1.getHousePlan();"));
		assertSame(eval("housePlan"), eval("wallPlan2.getHousePlan();"));
		eval("housePlan.getWallPlans().remove(wallPlan1);");
		assertEquals(1, eval("housePlan.getWallPlans().size();"));
		assertNull(eval("wallPlan1.getHousePlan();"));
		// Test the other side
		eval("wallPlan1.setHousePlan(housePlan);");
		assertEquals(2, eval("housePlan.getWallPlans().size();"));
		eval("wallPlan1.setHousePlan(null);");
		assertEquals(1, eval("housePlan.getWallPlans().size();"));
	}

	private Object eval(String string) throws ScriptException {
		return helper.eval(string);
	}
}
