/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Hedvig Kjellström, 2012
 */  

package ir;

import java.util.LinkedList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Arrays;
import java.io.*;

public class Query {
    
    public LinkedList<String> terms = new LinkedList<String>();
    public LinkedList<Double> weights = new LinkedList<Double>();

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

        // Can be adjusted
        double a = 1;
        double b = 0.75;
        double threshold = 7e-7;
        //------------

        File f;

        DocTermFrequencyIndex termFreqIndex = new DocTermFrequencyIndex();
        Indexer termExpandIndexer = new Indexer(termFreqIndex);
        HashMap<String,Double> termFreq = termFreqIndex.getNormTF();

        // #terms in the original query and the expander
        int aSize = terms.size();
        int bSize = 0;
        
        // fetch all the terms in the relevant dock and creat a normalized dock index
        for (int i = 0; i < 10; i++ ){
            if(docIsRelevant[i]) {
                f = new File(Index.docIDs.get(Integer.toString(results.get(i).docID)));
                System.out.println(f);
                termExpandIndexer.processFiles(f);
            }
        }

        bSize = termFreq.size();

        // Just for debuging
        System.out.println(Arrays.toString(docIsRelevant));
        for (int i = 0; i < terms.size(); i++){
            System.out.format("%s : %f, ", terms.get(i), weights.get(i));
        }


        System.out.println();

        System.out.format("Size of expanding query: %d\n",termFreq.size());

        for(String term: termFreq.keySet()){
            if(terms.contains(term)){
                System.out.println("HELLO!!!");
            }
        }
        //----------------------

        LinkedList<String> expandedTerms = new LinkedList();
        LinkedList<Double> expandedWeights = new LinkedList();

        double aWeight = 0;
        Double bWeight = 0.0;
        double newScore = 0;
        String t = "";

        System.out.println("weights size:" + weights.size());



        System.out.println("all weights");
        for(String term : termFreq.keySet()){
            System.out.println(termFreq.get(term));
        }


        System.out.println("Old terms");
        // Those terms that already exists
        while(!terms.isEmpty()){
            t = terms.pop();
            System.out.println(t);
            aWeight = weights.pop();
            System.out.println("aWeight: " + aWeight);
            System.out.println("termFreqSize" + termFreq.size());
            bWeight = termFreq.remove(t);
            bWeight = bWeight == null? 0 : bWeight;
            System.out.println("bWeight: " + bWeight);
            newScore = a*aWeight/aSize + b*bWeight/bSize;
            if(newScore > threshold){
                expandedTerms.add(t);
                expandedWeights.add(newScore);
            }
        }


        System.out.println("expand terms");
        for(String term: termFreq.keySet()){
            bWeight = termFreq.get(term);     
            newScore = b*bWeight/bSize;
            System.out.println("newscore: " + newScore);
            if(newScore > threshold) {
                expandedTerms.add(term);
                expandedWeights.add(newScore);
            }
        }

        terms = expandedTerms;
        weights = expandedWeights;

        System.out.println("First doc length: " + Index.docLengths.get("4"));

        System.out.println("Expanded query!");
        System.out.println(Arrays.toString(terms.toArray()));
    }
}

    
