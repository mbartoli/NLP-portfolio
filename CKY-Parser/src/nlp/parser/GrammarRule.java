package nlp.parser;
import java.util.*;
import java.util.regex.*;

/**
 * A class representing a PCFG rule, that is:
 *   LHS -> RHS1 RHS2 ... RHSN along with a weight
 *
 * Higher weights are better.  Add weights to combine weights of two rules.
 *
 * The specific format for the rule is:
 *  - non-lexical rule:
 *   LHS -> RHS1 RHS2 ... RHSN[tab][tab]WEIGHT
 * - lexical rule
 *   LHS -> * word[tab][tab]WEIGHT
 * 
 * @author Dave Kauchak
 * @version 2/16/2011
 *
 */
public class GrammarRule {
	private static final Pattern PCFG = Pattern.compile("(\\S+) -> (.*)\t\t([\\d\\.E-]+)");
	private static final String LEXICAL_PREFIX = "* ";
	
	private String lhs;  // the left hand side symbol
	private ArrayList<String> rhs = new ArrayList<String>(); // the set of right hand side symbols
	double weight = 0.0; // the weight of this rule, initially 0.0
	private boolean lexical = false;
	
	/**
	 * Create a new PCFG rule with left as the LHS component, the right hand side components rhs and
	 * a probability of zero
	 * 
	 * @param left the left hand side of the rule
	 * @param rhs the right had side elements of the rule
	 */
	public GrammarRule(String left, ArrayList<String> rhs){
		this(left, rhs, false);
	}
	
	/**
	 * Create a new PCFG rule with specification of whether or not it is a lexical rule
	 * 
	 * @param left the LHS symbol
	 * @param rhs the RHS symbols
	 * @param isLexical whether this is a lexical rule or not
	 */
	public GrammarRule(String left, ArrayList<String> rhs, boolean isLexical){
		this.lhs = left;
		this.rhs = rhs;
		
		if( isLexical ){
			if( rhs.size() != 1 ){
				throw new RuntimeException("Tried to make a lexical PCFG with multiple children");
			}
			
			lexical = true;
		}
	}
	
	/**
	 * Create a new PCFG rule with left as the LHS component, the right hand side components rhs and
	 * a probability
	 * 
	 * @param left the left hand side of the rule
	 * @param rhs the right had side elements of the rule
	 * @param weight the probability of this rul
	 */
	public GrammarRule(String left, ArrayList<String> rhs, double weight){
		this.lhs = left;
		this.rhs = rhs;
		this.weight = weight;
	}
	
	/**
	 * Create a new PCFG rule from it's string representation:
	 * 
	 * LHS -> RHS1 RHS2 ...		weight
	 * 
	 * @param pcfgString
	 */
	public GrammarRule(String pcfgString){		
		Matcher m = PCFG.matcher(pcfgString);
		
		if( m.matches() ){
			lhs = m.group(1);
			
			String rhsString = m.group(2);
			
			if( rhsString.startsWith(LEXICAL_PREFIX) ){
				String[] parts = rhsString.split(" ");
								
				rhs = new ArrayList<String>();
				rhs.add(parts[1]);
				lexical = true;
			}else{
				rhs = new ArrayList<String>(Arrays.asList(rhsString.split(" ")));				
			}
			
			weight = Double.parseDouble(m.group(3));
		}else{
			throw new RuntimeException("Bad PCFG: " + pcfgString);
		}
	}
	
	public boolean isLexical(){
		return lexical;
	}
	
	/**
	 * Get the weight associated with this rule
	 * 
	 * @return the weight associated with this rule
	 */
	public double getWeight(){
		return weight;
	}
		
	/**
	 * Set the weight associated with this rule
	 * 
	 * @param weight the weight associated with this rule
	 */
	public void setWeight(double weight){
		this.weight = weight;
	}
	
	/**
	 * Get the left hand side of this rule
	 * 
	 * @return the left hand side of the rule
	 */
	public String getLhs() {
		return lhs;
	}
	
	/**
	 * Get the list of elements on the right hand side of this rule
	 * 
	 * @return the right hand side elements
	 */
	public ArrayList<String> getRhs() {
		return rhs;
	}
	
	/**
	 * The number of elements on the right hand side
	 * 
	 * @return number of right hand side elements
	 */
	public int numRhsElements(){
		return rhs.size();
	}
	
	/**
	 * A string representation of this node using arrow notation, e.g.:
	 * S -> NP VP
	 */
	public String toString(){
		StringBuffer buffer = new StringBuffer();
		
		buffer.append(lhs);
		buffer.append(" -> ");
		
		// if this is a lexical rule, prefix it with the lexical symbol
		if( lexical ){
			buffer.append(LEXICAL_PREFIX);
		}
		
		for( String s: rhs ){
			buffer.append(s + " ");
		}
		
		// delete the trailing whitespace
		buffer.deleteCharAt(buffer.length()-1);
		
		buffer.append("\t\t" + weight);
		
		return buffer.toString();
	}
	
	/**
	 * The hashcode utilizes the String's hashcode method.
	 */
	public int hashCode(){
		return toString().hashCode();
	}

	/**
	 * Checks if this rule is equal to another object (most frequently another PCFGRule).  Again
	 * We'll use string equality, though the results would be the same if we did it element-wise.
	 */
	public boolean equals(Object o){
		if( o instanceof GrammarRule ){
			return toString().equals(((GrammarRule)o).toString());
		}else{
			return false;
		}
	}	
}