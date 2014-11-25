package nlp.parser;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;

public class CKYEntry {
	private Hashtable<String, Double> rules = new Hashtable<String, Double>();
	private Hashtable<String, ArrayList<Integer>> backPointers = new Hashtable<String, ArrayList<Integer>>();
	private Hashtable<String, String> unigramPointer = new Hashtable<String, String>();
	
	private int i;
	private int j;

	public CKYEntry(int i, int j) {
		this.i = i;
		this.j = j;
	}
	
	public Set<String> allRules() {
		return rules.keySet();
	}
	
	public boolean containsRule(String rule) {
		return rules.containsKey(rule);
	}
	
	public void addUnigramPointer(String lhs, String rhs) {
		unigramPointer.put(lhs, rhs);
	}
	
	public Double getWeight(String rule) {
		return rules.get(rule);
	}
	public void addRule(String rule, Double weight, int leftPointer, int rightPointer) {
		rules.put(rule, weight);
		
		ArrayList<Integer> pointers = new ArrayList<Integer>();
		pointers.add(leftPointer);
		pointers.add(rightPointer);
		
		backPointers.put(rule, pointers);
	}
	
	public Integer getI() {
		return i;
	}
	
	public Integer getJ() {
		return j;
	}
	
	@Override
	public String toString() {
		return i +"," + j + " : " +rules.toString() + " " + backPointers.toString() + " " + unigramPointer.toString();
	}
}
