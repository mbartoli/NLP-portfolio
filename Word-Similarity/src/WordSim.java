/**
 * WordSim class that computes word similarities
 * @author Aloke Desai, Mike Bartoli
 * Assignment 5
 * 10/30/14
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Scanner;

public class WordSim {
	// frequency of a given word in all the documents
	private Hashtable<String, Integer> frequency = new Hashtable<String, Integer>();
	
	// the number of documents a word appears in 
	private Hashtable<String, Integer> docFrequency = new Hashtable<String, Integer>();
	
	private HashSet<String> stopWords = new HashSet<String>();
	private Integer numDocuments = 0;
	private Integer wordCount = 0;
	
	// a hastable the hashes a word to an occurrence vector (another hashtable which has a letter and its frequency) 
	private Hashtable<String, Hashtable<String, Double>> occurrences = new Hashtable<String, Hashtable<String, Double>>();

	/**
	 *A word similarity calculator that computes the top 10 most similar words based on an inputted word, 
	 *similarity-measure type, and distance measure
	 * 
	 * 
	 * @param stopList the filename of the stoplist
	 * @param sentences the filename of the sentences that we use to compute similarity
	 * @param inputFile the input file that on each line has the word, the similarity measure, and distance type
	 *
	 */
	public WordSim(String stopList, String sentences, String inputFile) {
		File file = new File(stopList);
		Scanner sc;

		// add stop words to hashset
		try {
			sc = new Scanner(file);
			while (sc.hasNextLine()) {
				stopWords.add(sc.nextLine());
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		File sentenceFile = new File(sentences);
		Scanner sentenceScanner;
		
		try {
			sentenceScanner = new Scanner(sentenceFile);
			while (sentenceScanner.hasNextLine()) {
				numDocuments += 1;
				
				String currentSentence = sentenceScanner.nextLine();
				String[] sentence = currentSentence.split(" ");
				ArrayList<String> prunedSentence = new ArrayList<String>();

				for (String word: sentence) {
					String currentWord = word.toLowerCase();
					
					// only update wordcount and prunedSentence if the word is't a stop
					//word and only contains letters
					if (!stopWords.contains(currentWord) && currentWord.matches("[a-z]+")) {
						wordCount += 1;
						prunedSentence.add(currentWord);
					}
				}
				
				// a hashset for words we've seen in the current document.
				// we do this so that we don't overcount document frequeny
				HashSet<String> wordsSeen = new HashSet<String>();
				
				// update frequency based on the pruned sentences list
				for (int i = 0; i < prunedSentence.size(); i++) {
					// lowercase word
					String currentWord = prunedSentence.get(i).toLowerCase();
					// update frequency
					if (frequency.containsKey(currentWord)) {
						frequency.put(currentWord, frequency.get(currentWord)+1);
					}  else {
						frequency.put(currentWord, 1);
					}
					
					
					//update document frequency
					if (!wordsSeen.contains(currentWord)) {
						if (docFrequency.containsKey(currentWord)) {
							docFrequency.put(currentWord, docFrequency.get(currentWord)+1);
						} else {
							docFrequency.put(currentWord, 1);
						}
						
						wordsSeen.add(currentWord);
					}

					// create cooccurrence vector and add to occurrences hashtable
					addToOccurrence(i, prunedSentence);
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//print statistics
		System.out.println(frequency.size() + " unique words");
		System.out.println(wordCount + " word occurrences");
		System.out.println(numDocuments + " sentences/lines/documents");
		System.out.println();

		// calculate weights for each line in input file
		File input = new File(inputFile);
		Scanner inputScanner;

		try {
			inputScanner = new Scanner(input);
			// read through input file and print out similarity measure type and the top
			// 10 most similar words
			while (inputScanner.hasNextLine()) {
				String currentLine = inputScanner.nextLine();
				String[] params = currentLine.split("\t");
				System.out.println("SIM: " + currentLine);
				System.out.println(calculateTopWeights(params[0], params[1], params[2]));
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Adds the two words to the left and right of i to the occurrences hashtable 
	 * 
	 * @param i the index of a word in the pruned sentences array
	 * @param sentences an array list of words (the current document after pruning)
	 */
	private void addToOccurrence(int i, ArrayList<String> sentences) {
		String word = sentences.get(i);
		if (i == 1) {
			addToHash(word, sentences.get(0));
		} else if (i > 1) {
			addToHash(word, sentences.get(i-1));
			addToHash(word, sentences.get(i-2));
		}
		
		if (i == sentences.size() - 2) {
			addToHash(word, sentences.get(i+1));
		} else if (i < sentences.size() - 2){
			addToHash(word,sentences.get(i+1));
			addToHash(word, sentences.get(i+2));
		}
	}
	
	/**
	 * Adds the occurrence to the occurrences hashtable with the word as the key
	 * 
	 * @param word the word that is the key for occurrences
	 * @param occurrence a word that appears within the context of the word
	 */
	private void addToHash(String word, String occurrence) {
		if (!occurrences.containsKey(word)) {
			occurrences.put(word, new Hashtable<String, Double>());
		}
		if (!occurrences.get(word).containsKey(occurrence)) {
			occurrences.get(word).put(occurrence, 1.0);
		} else {
			occurrences.get(word).put(occurrence, occurrences.get(word).get(occurrence) + 1.0);
		}
	}
	
	/**
	 * Normalizes a co-occurrence vector using L2 normalization
	 * 
	 * @param h a cocurrence vector (represented sparsely using a hashtable)
	 * @return a normalized co-occurrence hashtable where the sum of each component sums to 1
	 */
	public Hashtable<String, Double> normalize(Hashtable<String, Double> h) {
		double norm = 0.0;
		for (String s : h.keySet()) {
			norm += Math.pow( h.get(s), 2);
		}
		norm = Math.sqrt(norm);
		
		Hashtable<String, Double> output = new Hashtable<String, Double>();
		for (String s: h.keySet()) {
			output.put(s, (h.get(s)) / norm);
		}

		return output;
	}
	
	/**
	 * updates a co-occurrence based on term-frequency weighting
	 * 
	 * @param h a co-occurrence vector
	 * @return the vector itself since tfWeight doesn't not alter the original co-occurrence vector
	 */
	public Hashtable<String, Double> tfWeight(Hashtable<String, Double> h) {
		return h;
	}
	
	/**
	 * updates a vector by each component's IDF value
	 * 
	 * @param h a co-occurrence vector
	 * @return a vector where each term is multiplied by its IDF value
	 */
	public Hashtable<String, Double> tfIdfWeight(Hashtable<String, Double> h) {
		double n = (double) numDocuments;
		Hashtable<String, Double> output = new Hashtable<String, Double>();
		for (String s : h.keySet()) {
			Double idf = Math.log10(n /(double) docFrequency.get(s));
			output.put(s, h.get(s) * idf);
		}
		return output;
	}
	
	/**
	 * updates a vector by each copmonent's PMI value
	 * 
	 * @param h a co-occurrence vector
	 * @param word the word the co-occurrence vector is the context of
	 * @return a vector where each term is the PMI(word, term)
	 */
	public Hashtable<String, Double> pmiWeight(Hashtable<String, Double> h, String word) {
		Hashtable<String, Double> output = new Hashtable<String, Double>();
		double probWord = frequency.get(word) / (double) wordCount;
		for (String key : h.keySet()) {
			double numerator = occurrences.get(word).get(key) / (double) wordCount;
			double probKey = frequency.get(key) / (double) wordCount;
			double weight = Math.log10(numerator / (probKey * probWord));
			output.put(key, weight);
		}
		
		return output;
	}
	
	/**
	 * the L2Distance between two vectors
	 * @param vector1 a vector represented as a hashtable
	 * @param vector2 a vector represented as a hashtable
	 * @return the L2Distance between the two vectors
	 */
	public Double l2Distance(Hashtable<String, Double> vector1, Hashtable<String, Double> vector2) {
		Double distance = 0.0;
		
		for (String word : vector1.keySet()) {
			if (vector2.containsKey(word)) {
				distance += Math.pow((vector1.get(word) - vector2.get(word)), 2);
			} else {
				distance += Math.pow(vector1.get(word), 2);
			}
		}

		for (String word: vector2.keySet()) {
			if (!vector1.containsKey(word)) {
				distance += Math.pow(vector2.get(word),2);
			}
		}
		return Math.sqrt(distance);
	}
	
	/**
	 * the L1Distance between two vectors
	 * @param vector1 a vector represented as a hashtable
	 * @param vector2 a vector represented as a hashtable
	 * @return the L1Distance between the two vectors
	 */
	public Double l1Distance(Hashtable<String, Double> vector1, Hashtable<String, Double> vector2) {
		Double distance = 0.0;
		
		for (String word : vector1.keySet()) {
			if (vector2.containsKey(word)) {
				distance += Math.abs((vector1.get(word) - vector2.get(word)));
			} else {
				distance += vector1.get(word);
			}
		}

		for (String word: vector2.keySet()) {
			if (!vector1.containsKey(word)) {
				distance += vector2.get(word);
			}
		}
		return distance;
	}
	
	/**
	 * the cosine distance between two vectors
	 * @param vector1 a vector represented as a hashtable
	 * @param vector2 a vector represented as a hashtable
	 * @return the cosine distance between the two vectors
	 */
	public Double cosineDistance(Hashtable<String, Double> vector1, Hashtable<String, Double> vector2) {
		Double distance = 0.0;

		for (String word: vector1.keySet()) {
			if (vector2.containsKey(word)) {
				distance += vector1.get(word) * vector2.get(word);
			}
		}
		return distance;
	}
	
	/**
	 * calculates the top words based on the similarity measure and weighting
	 * @param word the word we find similar words for
	 * @param weighting the weighting method (either TF, TFIDF, or PMI)
	 * @param simMeasure the distance measure (either L1, EUCLIDEAN, or COSINE)
	 * @return a string with the top 10 most similar words and with proper formatting
	 */
	public String calculateTopWeights(String word, String weighting, String simMeasure) {
		if (!occurrences.containsKey(word)) {
			return "ERROR";
		}
		Hashtable<String, Double> vector = occurrences.get(word);
		
		// weight vector
		if (weighting.equals("TF")) {
			vector = tfWeight(vector);
		} else if (weighting.equals("TFIDF")) {
			vector = tfIdfWeight(vector);
		} else if (weighting.equals("PMI")) {
			vector = pmiWeight(vector, word);
		} else {
			return "ERROR";
		}
		
		// normalize vector
		vector = normalize(vector);
		
		ArrayList<WordWeight> similarities = new ArrayList<WordWeight>();
		// compare this normalize vector to all other words
		for (String s : frequency.keySet()) {
			if (frequency.get(s) >= 3 && !s.equals(word)) {
				Hashtable<String, Double> currentVector = occurrences.get(s);

				// weight vector
				if (weighting.equals("TF")) {
					currentVector = tfWeight(currentVector);
				} else if (weighting.equals("TFIDF")) {
					currentVector = tfIdfWeight(currentVector);
				} else if (weighting.equals("PMI")) {
					// handle case
					currentVector = pmiWeight(currentVector, s);
				} else {
					return "ERROR";
				}
				
				// normalize vector
				currentVector = normalize(currentVector);
				
				Double distance = 0.0;
				// calculate distance 
				if (simMeasure.equals("L1")) {
					distance = l1Distance(currentVector, vector);
				} else if (simMeasure.equals("EUCLIDEAN")) {
					distance = l2Distance(currentVector, vector);
				} else if (simMeasure.equals("COSINE")) {
					distance = cosineDistance(currentVector, vector);
				} else {
					return "ERROR";
				}
				
				similarities.add(new WordWeight(s, distance));
			}
		}
		// sort the arraylist and only output the 10 most similar words
		Collections.sort(similarities);
		StringBuffer output = new StringBuffer();
		
		// for L1 and euclidean, lower is better, for PMI and COSINE, higher is better
		if (simMeasure.equals("L1") || simMeasure.equals("EUCLIDEAN")) {
			for (int i = 0; i < 10; i++) {
				if (i >= similarities.size()) {
					break;
				}
				output.append(similarities.get(i).word + "\t" + similarities.get(i).weight + "\n");
			}
		} else {
			for (int i =  similarities.size() - 1; i >= similarities.size() - 10; i--) {
				if (i < 0) {
					break;
				}
				output.append(similarities.get(i).word + "\t" + similarities.get(i).weight + "\n");
			}
		}
		return output.toString();
	}
	public static void main(String[] args) {
		if (args.length < 3) {
			System.out.println("not enough args");
		} 
		WordSim test = new WordSim(args[0], args[1] , args[2]);
	}
}
