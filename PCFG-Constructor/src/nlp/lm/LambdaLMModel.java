package nlp.lm;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * @author Aloke
 *
 */
public class LambdaLMModel implements LMModel {
	Hashtable<String, Integer> unigramFrequency = new Hashtable<String, Integer>();
	Hashtable<String, Hashtable<String, Integer>> bigramFrequency = new Hashtable<String, Hashtable<String, Integer>>();
	Hashtable<String, Hashtable<String, Double>> bigramProbability = new Hashtable<String, Hashtable<String, Double>>();
	HashSet<String> vocabulary = new HashSet<String>();
	double lambda;
	int wordCount = 0;

	public LambdaLMModel(String filename, double lambda) {
		this.lambda = lambda;
		// set the value of unk to be 0 before we do anything
		unigramFrequency.put("unk", 0);
		unigramFrequency.put("<s>", 0);
		unigramFrequency.put("</s>", 0);
		bigramFrequency.put("unk", new Hashtable<String, Integer>());
		bigramFrequency.put("<s>", new Hashtable<String, Integer>());
		bigramFrequency.put("</s>", new Hashtable<String, Integer>());
		vocabulary.add("unk");
		vocabulary.add("<s>");
		vocabulary.add("</s>");

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
            String line = scnr.nextLine();
            // split on spaces and put into arraylist, add start and end character
            ArrayList<String> words = new ArrayList<String>(Arrays.asList(line.split(" ")));
            words.add(0, "<s>");
            words.add("</s>");
            
            
            // make two passes through words. The first calculates the unigram counts. The second 
            // calcualtes the bigram counts
            
            for(int i = 0; i < words.size(); i++) {
            	wordCount++;
            	if (!unigramFrequency.containsKey(words.get(i))) {
            		// incremeent unk, set current word to be 0 
            		unigramFrequency.put("unk", unigramFrequency.get("unk") +1);
            		unigramFrequency.put(words.get(i), 0);
            		// put the word as the key in the hash table of hash tables
            		bigramFrequency.put(words.get(i), new Hashtable<String, Integer>());
            		words.set(i, "unk");
            		
            	} else {
            		unigramFrequency.put(words.get(i), unigramFrequency.get(words.get(i)) +1);
            		vocabulary.add(words.get(i));
            	}
            }
            
            for (int i = 0; i < words.size() -1; i++) {
            	if (!bigramFrequency.get(words.get(i)).containsKey(words.get(i+1))) {
            		bigramFrequency.get(words.get(i)).put(words.get(i+1), 1);
            	} else {
            		Integer oldValue = bigramFrequency.get(words.get(i)).get(words.get(i+1));
            		bigramFrequency.get(words.get(i)).put(words.get(i+1), oldValue+1);
            	}
            }
        }

        // iterate through bigram frequency to calculate the bigram probability
        for(Map.Entry<String, Hashtable<String, Integer>> entry : bigramFrequency.entrySet()){
        	bigramProbability.put(entry.getKey(), new Hashtable<String,Double>());
        	
        	// iterate through the inner for loop
        	for (Map.Entry<String, Integer> innerEntry : entry.getValue().entrySet()) {
        		/* the bigram probability, P(b|a) is by definition the count of (ab) / count(a)
        		 * However, since we're doing discounting, the new formula is P(b|a) = (count(ab) + lamda)/(count(a) + # unique words)
        		 * We know that the number of unique words <=> unigramFrequence.size()
        		 */
        		double resultingProbability = (innerEntry.getValue() + lambda) / ((double)(unigramFrequency.get(entry.getKey()) + lambda*vocabulary.size()));
        		bigramProbability.get(entry.getKey()).put(innerEntry.getKey(), resultingProbability);
        	}
        }
	}

	public double logProb(ArrayList<String> sentWords) {
		double sum = 0;
		ArrayList<String> wordsWithSymbols = sentWords;
		//add <s> and </s>
		wordsWithSymbols.add(0, "<s>");
		wordsWithSymbols.add("</s>");
		for(int i=0; i < wordsWithSymbols.size()-1; i++) {
			sum = sum + Math.log10(getBigramProb(wordsWithSymbols.get(i),wordsWithSymbols.get(i+1)));
		}
		return sum;
	}

	public double getPerplexity(String filename) {
		double sum = 0;
		File text = new File(filename);

        //Creating Scanner instnace to read File in Java
        Scanner scnr;
		try {
			scnr = new Scanner(text);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
		while(scnr.hasNextLine()){
			String line = scnr.nextLine();
	        // split on spaces and put into arraylist, add start and end character
			ArrayList<String> currentString = new ArrayList<String>(Arrays.asList(line.split(" ")));
            sum = sum + logProb(currentString);
        }
		return (-1 * sum) / (double) wordCount;
	}
	
	public double getBigramProb(String first, String second) {
		// globally replace any unknown characters with unk
		if (!vocabulary.contains(first)) {
			first = "unk";
		}
		if (!vocabulary.contains(second)) {
			second = "unk";
		}
		if (bigramProbability.containsKey(first)) {
			if (bigramProbability.get(first).containsKey(second)) {
				return bigramProbability.get(first).get(second);
			} 
			// calculate probability on the fly
			double probability = lambda / (double) (unigramFrequency.get(first) + lambda*vocabulary.size());
			return probability;
		} else {
			// think about this more-- do we need to include unigramFrequency?
			return lambda / ((double) vocabulary.size() + unigramFrequency.get(first));
		}
	}
	
	public static void main(String[] args) {
		LambdaLMModel lm = new LambdaLMModel("training.txt", .1);
		System.out.println(".1: "+ lm.getPerplexity("development.txt"));
		
		lm = new LambdaLMModel("training.txt", .01);
		System.out.println(".01: " + lm.getPerplexity("development.txt"));
		
		lm = new LambdaLMModel("training.txt", .001);
		System.out.println(".001: " + lm.getPerplexity("development.txt"));
		
		lm = new LambdaLMModel("training.txt", .0001);
		System.out.println(".0001: " + lm.getPerplexity("development.txt"));
		
		lm = new LambdaLMModel("training.txt", .00001);
		System.out.println(".00001: " + lm.getPerplexity("development.txt"));
		
		lm = new LambdaLMModel("training.txt", .000001);
		System.out.println(".000001: " + lm.getPerplexity("development.txt"));
		
		lm = new LambdaLMModel("training.txt", .0000001);
		System.out.println(".0000001: " + lm.getPerplexity("development.txt"));
		
		lm = new LambdaLMModel("training.txt", .00000001);
		System.out.println(".00000001: " + lm.getPerplexity("development.txt"));
	}
}
