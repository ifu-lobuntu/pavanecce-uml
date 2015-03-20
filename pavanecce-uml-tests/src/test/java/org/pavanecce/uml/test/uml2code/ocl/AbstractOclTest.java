package org.pavanecce.uml.test.uml2code.ocl;

import javax.script.ScriptException;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.pavanecce.common.test.util.ConstructionCaseExample;
import org.pavanecce.common.test.util.SourceGeneratingTestHelper;

public abstract class AbstractOclTest extends Assert {
	protected ConstructionCaseExample example;
	protected SourceGeneratingTestHelper helper;

	public AbstractOclTest(String name) {
		example=new ConstructionCaseExample(name);
		helper=new SourceGeneratingTestHelper(example);
	}
	@Before
	public void before() throws Exception{
		addOcl();
		initLanguage();
	}
	protected abstract void addOcl();
	protected abstract void initLanguage() throws Exception;
	@After
	public void after() {
		helper.after();
	}

	protected final static <T> EList<T> emptyList(Class<T> class1) {
		BasicEList<T> result = new BasicEList<T>();
		return result;
	}

	protected final static <T> EList<T> list(@SuppressWarnings("unchecked") T... t) {
		BasicEList<T> result = new BasicEList<T>();
		for (T t2 : t) {
			result.add(t2);
		}
		return result;
	}

	protected Object eval(String s) throws ScriptException {
		return helper.getJavaScriptEngine().eval(s);
	}

	public void assertEquals(int i, Object val) {
		if (val instanceof Number) {
			assertEquals(i, ((Number) val).intValue());
		} else {
			super.assertEquals(i, val);
		}
	}

}
