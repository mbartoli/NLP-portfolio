package nlp.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;

public class PcfgConstructor {
	// the grammar frequency. the LHS of a grammar is hashed to a hashtable that maps a grammar to its frequency
	private Hashtable<String, Hashtable<GrammarRule,Integer>> frequency = new Hashtable<String, Hashtable<GrammarRule,Integer>>();

	// the number of occurrences of the LHS. This allows us to get the number of occurences of the LHS
	// of a rule in O(1) time when calculating the weight of a grammar,
	private Hashtable<String, Integer> numOccurences = new Hashtable<String, Integer>();
	
	// a list of all the grammars created after converting them into Chomsky Normal form
	private ArrayList<GrammarRule> binaryGrammars = new ArrayList<GrammarRule>();
	
	// a counter to keep track of the current rule number for form 'X1' when creating binary rules
	private Integer leftCounter = 1;
	
	/**
	 * 
	 * @param filename: The filename that holds the parse tree to be parsed
	 */
	public PcfgConstructor(String filename) {
		// input sentences
		//creating File instance to reference text file in Java
		File text = new File(filename);

		//Creating Scanner instnace to read File in Java
		Scanner scnr;
		try {
			scnr = new Scanner(text);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		//Reading each line of file using Scanner class
		while(scnr.hasNextLine()){
			//read in line, create parse tree out of line, and make recursive call to addToTable
			String line = scnr.nextLine();
			ParseTree pt = new ParseTree(line);
			addToTable(pt, false);
		}

		//iterate through the hashtable and calculate the probabilities
		for (String s : frequency.keySet()) {
			Hashtable<GrammarRule, Integer> innerHash = frequency.get(s);
			for (GrammarRule gr : innerHash.keySet()) {
				double weight = innerHash.get(gr) / (double) numOccurences.get(s);
				gr.setWeight(weight);
			}
		}
		
		// convert to Chomsky Normal form and save into binaryGrammars array list
		for (String s : frequency.keySet()) {
			Hashtable<GrammarRule, Integer> innerHash = frequency.get(s);
			for (GrammarRule gr : innerHash.keySet()) {
				convertToChomsky(gr);
			}
		}
	}
	
	/**
	 * 
	 * @param filename the name of the file where all the pcfg grammars should be written to
	 */
	public void getPcfg(String filename) {
		try {
			PrintWriter writer = new PrintWriter(filename);
			
			// iterate through our frequency hash table to get all the occurences
			// and write this into filename
			for (String s : frequency.keySet()) {
				for (GrammarRule gr : frequency.get(s).keySet()) {
					writer.println(gr);
				}
			}
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param filename the name of the file where all the binary pcfg grammars should be written to
	 */
	public void getBinaryPcfg(String filename) {
		try {
			PrintWriter writer = new PrintWriter(filename);
			
			//write every single grammar in our array list to filename
			for (GrammarRule g: binaryGrammars) {
				writer.println(g);
			}
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param pt the parseTree to be added to the frequency hashtable
	 * @param isLexical whether the parseTree has an immediate child that is a terminal
	 */
	private void addToTable(ParseTree pt, boolean isLexical) {
		// if a parseTree is a terminal node, there's no need to add it to the frequency hashtable bceause it will never 
		// be a left hand side of a grammar rule
		if (pt.isTerminal()) {
			return;
		}

		String outerLabel = pt.getLabel();

		// check if our hash table has the outer most label of the parse tree 
		// if it's not contained in the outer most label, we add it to frequency and initialize
		// a new hashtable
		if (!frequency.containsKey(outerLabel)) {
			frequency.put(outerLabel, new Hashtable<GrammarRule, Integer>());
			numOccurences.put(outerLabel, 0);
		}

		// increment the number of occurrences of the specific LHS.			
		numOccurences.put(outerLabel, numOccurences.get(outerLabel) + 1);
		
		// create a grammar rule out of the current parseTree and it's immediate children
		GrammarRule grammar = new GrammarRule(outerLabel, pt.getChildrenLabels(), isLexical);

		// update frequency counts based on the grammar
		if (frequency.get(outerLabel).containsKey(grammar)) {
			Integer oldFrequency = frequency.get(outerLabel).get(grammar);
			frequency.get(outerLabel).put( grammar, oldFrequency + 1);
		} else {
			frequency.get(outerLabel).put(grammar, 1);
		}
		
		// iterate through pt's children and call addToTable on each children
		// We note if a parseTree is lexical if it's children are terminal nodes  
		for (ParseTree child : pt.getChildren()) {
			if (!child.isTerminal() && child.getChild(0).isTerminal()){
				addToTable(child, true);
			} else {
				addToTable(child, false);				
			}
		}
	}
	
	/**
	 * 
	 * @param grammar the grammar that should be added to the binaryGrammars arraylist 
	 */
	private void convertToChomsky(GrammarRule grammar) {
		// if a grammar has 2 or fewer elements, then it is already binarized
		// and can be put straing into the binaryGrammars arrayList
		if (grammar.numRhsElements() <= 2) {
			binaryGrammars.add(grammar);
			return;
		}
		
		ArrayList<String> rightHand = grammar.getRhs();
		
		// for the first rule, input the first two elements of the original right hand side
		// as the children. 
		ArrayList<String> initialRhs = new ArrayList<String>();
		initialRhs.add(rightHand.get(0));
		initialRhs.add(rightHand.get(1));
	
		// create a grammar where the left hand side is the current value of leftCounter and the right hand side 
		// is the arraylist we created above. Since this isn't the last rule we're making out of the original grammar rule,
		// we set the weight to 0 and add it to the grammar arraylist
		GrammarRule newGrammar = new GrammarRule("X" + leftCounter, initialRhs);
		newGrammar.setWeight(1);
		binaryGrammars.add(newGrammar);
		
		// iterate through the rest of the remaining elements in the rhs (except the very last one)
		for (int i = 2; i < grammar.getRhs().size()-1; i++) {
			leftCounter++;

			// temp arraylist that holds that LHS of the last intemediary grammar rule (of form X1) 
			// and one more element from the original right hand side of the grammar rule we're binarizing
			ArrayList<String> temp = new ArrayList<String>();
			temp.add("X" + (leftCounter-1));
			temp.add(rightHand.get(i));
			
			//create new grammar rule based on the current value of leftCounter and the temp arrayList, set the weight
			// to 1 since we know this isn't the final rule we have to make. Add it to the arraylist of grammars
			GrammarRule middleGrammar = new GrammarRule("X" + (leftCounter), temp);
			middleGrammar.setWeight(1);
			binaryGrammars.add(middleGrammar);
		}

		//create the last binary grammar rule from the original grammar. The LHS of this rule will be 
		// the original left hand side of the grammar parameter.
		ArrayList<String> finalRhs = new ArrayList<String>();
		finalRhs.add("X" + (leftCounter));
		finalRhs.add(rightHand.get(rightHand.size()-1));


		GrammarRule finalGrammar = new GrammarRule(grammar.getLhs(), finalRhs);
		finalGrammar.setWeight(grammar.getWeight());
		binaryGrammars.add(finalGrammar);
		
		// increment leftCounter for future calls to convertToChomsky, if any
		leftCounter++;
	}

	public static void main(String[] args) {
		PcfgConstructor test = new PcfgConstructor("example.parsed");
		test.getBinaryPcfg("example.binary.pcfg");
	}
}
