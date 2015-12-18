package csu.common.test;

public class TestString {

	public static void main(String[] args) {
		String str = "abc";
		String str_1 = "abc";
		String result = null;
		if (str.equals(str_1))
			result = " = ";
		else
			result = " != ";
		System.out.println(str + result + str_1);
		
		
		Object o1 = null;
		Object o2 = null;
	//	if (o1.equals(o2))   null pointer
		if(o1 == o2)
			result = " = ";
		else
			result = " != ";
		System.out.println(o1 + result + o2);
		
		Object o3 = new Object();
		Object o4 = new Object();
		if (o3.equals(o4))   
//		if(o3 == o4)
			result = " = ";
		else
			result = " != ";
		System.out.println(o3 + result + o4);
		
	}
}
