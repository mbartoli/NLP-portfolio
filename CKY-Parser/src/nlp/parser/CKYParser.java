package nlp.parser;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;

public class CKYParser {
	private Hashtable<String, GrammarRule> binaryRules = new Hashtable<String, GrammarRule>();
	private Hashtable<String, GrammarRule> unaryRules = new Hashtable<String, GrammarRule>();
	private Hashtable<String, ArrayList<GrammarRule>> lexicalRules = new Hashtable<String, ArrayList<GrammarRule>>();
	
	
	public CKYParser(String filename) {
		File file = new File(filename);
		Scanner sc;

		try {
			sc = new Scanner(file);			
			
			while (sc.hasNextLine()) {
				String currentLine = sc.nextLine();
				GrammarRule rule = new GrammarRule(currentLine);
				
				// depending on the type of rule, we update the proper hashtable with the rule
				if (rule.isLexical()) {
					if (lexicalRules.containsKey(rule.getRhs().get(0))) {
						lexicalRules.get(rule.getRhs().get(0)).add(rule);
					} else {
						ArrayList<GrammarRule> rules = new ArrayList<GrammarRule>();
						rules.add(rule);
						lexicalRules.put(rule.getRhs().get(0), rules);
					}
				} else {
					if (rule.getRhs().size() > 1) {
						// binary rule
						binaryRules.put(rule.getRhs().toString(), rule);
					} else {
						unaryRules.put(rule.getRhs().get(0), rule);
					}
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public String parse(String sentence) {
		String[] words = sentence.split(" ");
		int numWords = words.length;
		// build cky table
		CKYEntry[][] theTable = new CKYEntry[numWords][numWords];
		
		//populate table for lexical rules
		for(int j=0, i=0; i < theTable.length; i++, j++) {
			theTable[i][j] = new CKYEntry(i, j);

			//find all the lexical Rules with our current word
			ArrayList<GrammarRule> allRules = lexicalRules.get(words[j]);

			// add all the rules to the entry
			for (GrammarRule gr: allRules) {
				add(gr, theTable[i][j], 0.0, -1, -1);
			}
		}
		
		// fill in the rest of the table
		for (int j = 0; j < numWords; j++) {
			for (int i = j-1; i >= 0; i--) {
				theTable[i][j] = new CKYEntry(i,j);
				for (int n = j; n > i; n--) {
					possibleGrammar(theTable[i][j], theTable[i][n-1], theTable[n][j]);
				}
			}
		}
		
		for (int i = 0; i < numWords; i++) {
			for (int j = 0; j < numWords; j++) {
				System.out.println(theTable[i][j]);
			}
		}
		return "";
	}
	
	private void possibleGrammar(CKYEntry original, CKYEntry left, CKYEntry right) {
		for (String leftSide : left.allRules()) {
			for (String rightSide : right.allRules()) {
				// create a new arraylist with two elements with left side as the first and 
				// right side as the second
				ArrayList<String> rhs = new ArrayList<String>();
				rhs.add(leftSide);
				rhs.add(rightSide);
				if (binaryRules.containsKey(rhs.toString())) {
					add(binaryRules.get(rhs.toString()), original, left.getWeight(leftSide)+right.getWeight(rightSide), left.getJ(), right.getI());
				}
			}
		}
	}
	private void add(GrammarRule rule, CKYEntry entry, Double originalWeight, int leftPointer, int rightPointer){
		Double sum = originalWeight + rule.weight;
		if (entry.containsRule(rule.getLhs())) {
			if (sum > entry.getWeight(rule.getLhs())) {
				entry.addRule(rule.getLhs(), sum, leftPointer, rightPointer);
			} else {
				return;
			}
		}
		GrammarRule currentRule = rule;
		entry.addRule(rule.getLhs(), sum, leftPointer, rightPointer);
		while (unaryRules.containsKey(currentRule.getLhs())) {
			GrammarRule newRule = unaryRules.get(currentRule.getLhs());
			sum += newRule.weight;
			
			if (entry.containsRule(newRule.getLhs())) {
				if (sum > entry.getWeight(rule.getLhs())) {
					entry.addRule(newRule.getLhs(), sum, leftPointer, rightPointer);
					entry.addUnigramPointer(newRule.getLhs(), newRule.getRhs().get(0));
				}
			} else {
				entry.addRule(newRule.getLhs(), sum, leftPointer, rightPointer);
				entry.addUnigramPointer(newRule.getLhs(), newRule.getRhs().get(0));
			}
			currentRule = newRule;
		}
	}

	public static void main(String[] args) {
		CKYParser p = new CKYParser("example.pcfg");
		p.parse("Mary likes giant programs .");
	}
}
