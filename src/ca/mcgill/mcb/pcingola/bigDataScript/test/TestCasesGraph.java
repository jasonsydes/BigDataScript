package ca.mcgill.mcb.pcingola.bigDataScript.test;

import org.junit.Test;

/**
 * Test cases aboug graph construction and 'implicit wait' commands
 * 
 * @author pcingola
 *
 */
public class TestCasesGraph extends TestCasesBase {

	@Test
	public void test01() {
		runAndCheck("test/graph_01.bds", "output", "IN\nTASK 1\nTASK 2\n");
	}

	@Test
	public void test02() {
		runAndCheck("test/graph_02.bds", "output", "IN\nTASK 1\nTASK 2\nTASK 3\n");
	}

}