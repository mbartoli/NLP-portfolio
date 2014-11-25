/**
 * 
 * @author Aloke Desai and Mike Bartoli
 * Assignment 5
 * A wrapper class that stores a tuple of a weight and a word
 * 
 */
public class WordWeight implements Comparable<WordWeight> {
	public Double weight;
	public String word;
	
	/**
	 * stores the weight and the word
	 * @param word the word to store
	 * @param weight the weight to store
	 */
	public WordWeight(String word, Double weight) {
		this.weight = weight;
		this.word = word;
	}
	
	/**
	 * returns the string representation of the function
	 */
	public String toString() {
		return word + " => " + weight;
	}

	/**
	 * The implementation of compareTo that we use for sorting the wordWeight
	 * 
	 * @param o the other wordWeight to compare to
	 * @return 1 if the current wordweight is greater than o, 0 if they are equal, -1 
	 * if the current wordweight is less than o
	 */
	@Override
	public int compareTo(WordWeight o) {
		return weight.compareTo(o.weight);
	}
}
