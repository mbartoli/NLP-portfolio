import java.util.*;

class MatchSSN {
	public static boolean q1a1(String myString) {
		return (myString.matches("[0-9]{9}|[0-9]{3}-[0-9]{2}-[0-9]{4}"));
	public static boolean q1a2(String myString) {
		return (myString.matches("([0-6][0-9][0-9]|7[0-6][0-9]|77[0-2])([0-9]{6}|-[0-9]{2}-[0-9]{4})"));
	}
	public static void main(String[] args) {
		String myString1 = "123-45-6789";
		String myString2 = "771444490";
		String myString3 = "123-456789";
		String myString4 = "123456890";
		System.out.println(q1a2(myString1));
		System.out.println(q1a2(myString2));
		System.out.println(q1a2(myString3));
		System.out.println(q1a2(myString4));
	}
}
