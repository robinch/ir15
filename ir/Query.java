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

        // get
        double[] w = getWeights();
        double a = w[0];
        double b = w[1];
        double threshold = w[2];
        System.out.format("Weights! a:%.3f, b:%.3f, threshold:%.3f\n" , w[0], w[1],w[2]);
        //------------

        File f;

        DocTermFrequencyIndex termFreqIndex = new DocTermFrequencyIndex();
        Indexer termExpandIndexer = new Indexer(termFreqIndex);
        HashMap<String,Double> termFreq = termFreqIndex.getNormTF();

        // #terms in the original query (a) and the expander (b)
        int aSize = terms.size();
        int bSize = 0;
        
        // fetch all the terms in the relevant doc and create a normalized doc index
        for (int i = 0; i < 10; i++ ){
            if(docIsRelevant[i]) {
                f = new File(Index.docIDs.get(Integer.toString(results.get(i).docID)));
                //System.out.println(f);
                termExpandIndexer.processFiles2(f);
                bSize++;
            }
        }

        if(bSize == 0) return;

        //Normalize query
        for(int i = 0; i< aSize; i++){
            weights.add(i, weights.get(i)/aSize);
        }

        LinkedList<String> expandedTerms = new LinkedList();
        LinkedList<Double> expandedWeights = new LinkedList();

        double aWeight = 0.0;
        Double bWeight = 0.0;
        double newScore = 0;
        String t = "";

        // Rocchios Algorithm
        // Those terms that already exists
        while(!terms.isEmpty()){
            t = terms.pop();
            aWeight = weights.pop();
            bWeight = termFreq.remove(t);
            bWeight = bWeight == null? 0 : bWeight;
            newScore = a*aWeight/aSize + b*bWeight/bSize;
            if(newScore > threshold){
                expandedTerms.add(t);
                expandedWeights.add(newScore);
            }
        }

        // Those that were not in the original query
        for(String term: termFreq.keySet()){
            bWeight = termFreq.get(term);     
            newScore = b*bWeight/bSize;
            if(newScore > threshold) {
                expandedTerms.add(term);
                expandedWeights.add(newScore);
            }
        }
        
        // Replace the original query and weight with the expanded ones.
        terms = expandedTerms;
        weights = expandedWeights;    
    }

    /**
    *   Returns weights set in weight.txt
    *   if file is not found return default
    *   a-weight = 1,  b-weight = 0.75, threshold = 0.01
    *   index 0 gives a-weight
    *   index 1 gives b-weight
    *   index 2 gives threshold
    */
    private double[] getWeights(){
        double a = 1;
        double b = 0.75;
        double threshold = 0.01;
        String line;
        try{
            BufferedReader br = new BufferedReader(new FileReader("ir/weight.txt"));
            if((line = br.readLine()) != null){
                if((line = br.readLine()) != null){
                    String[] s = line.split("\t");
                    a = Double.parseDouble(s[1].trim());
                    b = Double.parseDouble(s[2].trim());
                    threshold = Double.parseDouble(s[3].trim());
                }
            }
            br.close();
        }
        catch(Exception e){
            e.printStackTrace();
            System.out.format("Error! Will use default values a:%f , b:%f\n", a,b);
        }
        return new double[]{a,b,threshold};
    }
}

    
