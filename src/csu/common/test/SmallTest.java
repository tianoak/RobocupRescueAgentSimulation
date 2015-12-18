package csu.common.test;

public class SmallTest {
	public static void main(String[] args)  {
    	SmallTest st = null;
    	SmallTest s = new SmallTest();
        System.out.println(st instanceof SmallTest);
        System.out.println(s instanceof SmallTest);
        System.out.println(st == s);
        System.out.println(s.equals(st));
	}
}