package cz.cuni.mff.auv.problem;

import org.junit.Test;

public class Test05_Events {
	
	@Test
	public void test() {
		Problem problem = new Problem();
		
		EventsTester tester = new EventsTester(problem);
		
		tester.search(3);
		
		System.out.println();
		
		System.out.println("Applied events: " + tester.events);
		
		System.out.println("--// TEST OK //--");
	}
	
	public static void main(String[] args) {
		Test05_Events test = new Test05_Events();
		
		test.test();
	}

}
