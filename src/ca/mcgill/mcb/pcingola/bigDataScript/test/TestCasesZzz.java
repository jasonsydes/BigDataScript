package ca.mcgill.mcb.pcingola.bigDataScript.test;

import org.junit.Test;

/**
 * Quick test cases when creating a new feature...
 *
 * @author pcingola
 *
 */
public class TestCasesZzz extends TestCasesBase {

	@Test
	public void test15_6() {
		runAndCheck("test/run_15.bds", "li6", "[apple, orange, i=10hihihi]");
	}

	//	@Test
	//	public void test15_checkpoint_par_function_call() {
	//		runAndCheckpoint("test/checkpoint_15.bds", "test/checkpoint_15.chp", "ok", "true");
	//	}
	//
	//	@Test
	//	public void test16_checkpoint_recursive() {
	//		runAndCheckpoint("test/checkpoint_16.bds", "test/checkpoint_16.chp", "fn", "120");
	//	}

}
