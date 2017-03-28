package org.betasearch.arvdroid.BetaSearch;

/**
 * @author avijayvargiy
 * Query term is a data structure that holds termname and its frequency in a query.
 * for example, we have a query like "my game is game"
 * a linked list of query term will hold list-><"my",1>-><"game",2>->"is",1>
 * This is helpful in calculating one of the component of cosine similarity algorithm that is Wt(q)
 */
public class QueryTerm{
	String termname;
	int TermFrequency=0;
}
