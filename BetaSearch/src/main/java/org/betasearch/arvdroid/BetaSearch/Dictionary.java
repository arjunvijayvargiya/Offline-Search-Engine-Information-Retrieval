package org.betasearch.arvdroid.BetaSearch;

import java.util.LinkedList;

/**
 * @author avijayvargiy
 *Dictionary represents the individual word in a Dictionary and its posting list containing variables of type posting.
 */
public class Dictionary implements Comparable<Dictionary>{
	LinkedList<Posting> list; //It represents postings of that particular word in a dictionary.
	String term; //It represents word or the term.
	int documentFrequency=0; //It represents how many documents that particular word has occured
	public int compareTo(Dictionary o) {
		// TODO Auto-generated method stub
		String comparedSize = o.term;
		if (this.term.compareTo(comparedSize)>0) 
		{
			return 1;
		} 
		else if (this.term.compareTo(comparedSize)<0) 
		{
			return -1;
		} 
		else 
		{
			return 0;
		}
	}
}
