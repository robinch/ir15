/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Hedvig Kjellström, 2012
 */  

package ir;

import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.Arrays;
import java.io.*;

public class Query {
    
    public LinkedList<String> terms = new LinkedList<String>();
    public LinkedList<Double> weights = new LinkedList<Double>();
    private DocTermFrequencyIndex termFreq;
    private Indexer termIndexer;

    /**
     *  Creates a new empty Query 
     */
    public Query() {
    }
	
    /**
     *  Creates a new Query from a string of words
     */
    public Query( String queryString  ) {
	StringTokenizer tok = new StringTokenizer( queryString );
	while ( tok.hasMoreTokens() ) {
	    terms.add( tok.nextToken() );
	    weights.add( new Double(1) );
	}    
    }
    
    /**
     *  Returns the number of terms
     */
    public int size() {
	return terms.size();
    }
    
    /**
     *  Returns a shallow copy of the Query
     */
    public Query copy() {
	Query queryCopy = new Query();
	queryCopy.terms = (LinkedList<String>) terms.clone();
	queryCopy.weights = (LinkedList<Double>) weights.clone();
	return queryCopy;
    }
    
    /**
     *  Expands the Query using Relevance Feedback
     */
    public void relevanceFeedback( PostingsList results, boolean[] docIsRelevant, Indexer indexer ) {
	// results contain the ranked list from the current search
	// docIsRelevant contains the users feedback on which of the 10 first hits are relevant
	
	//
	//  YOUR CODE HERE
	//
        termFreq = new DocTermFrequencyIndex();
        indexer = new Indexer(termFreq);

        double a = 1;
        double b = 0.75;

        for (int i = 0; i < 10; i++ ){
            if(docIsRelevant[i]) {
                indexer.processFiles(new File(Index.docIDs.get(Integer.toString(results.get(i).docID)).substring(1)));
            }
        }
        System.out.println(Arrays.toString(docIsRelevant));
        for (int i = 0; i < terms.size(); i++){
            System.out.format("%s : %f\n", terms.get(i), weights.get(i));
        }
    }
}

    
