/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 *   Additions: Hedvig Kjellström, 2012-14
 */  


package ir;

import java.util.HashMap;
import java.util.Iterator;


/**
 *   Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndex implements Index {

    //Debug
    boolean d = false;

    /** The index as a hashtable. */
    private HashMap<String,PostingsList> index = new HashMap<String,PostingsList>();
    int counter = 0;


    /**
     *  Inserts this token in the index.
     */
    public void insert( String token, int docID, int offset ) {
	//
	//  YOUR CODE HERE
	//
        
        //check if key exists
        //if it exists add tocID to 
        PostingsList postList = index.get(token);
        if(postList == null){
            postList = new PostingsList();
            index.put(token, postList);
            counter++;
            if(counter % 10000 == 0) System.out.println(counter);
        }
        postList.add(docID, offset);
    }


    /**
     *  Returns all the words in the index.
     */
    public Iterator<String> getDictionary() {
	// 
	//  REPLACE THE STATEMENT BELOW WITH YOUR CODE
	//
        return index.keySet().iterator();
    }


    /**
     *  Returns the postings for a specific term, or null
     *  if the term is not in the index.
     */
    public PostingsList getPostings( String token ) {
	// 
	//  REPLACE THE STATEMENT BELOW WITH YOUR CODE
	// //
 //        if(d){
 //            System.out.println(index.size());
 //            System.out.println("Token: " + token);
 //            System.out.println("PostinList: " + index.get(token));
 //            System.out.println("Printing all keys!");
 //            Iterator<String> itr = getDictionary();
 //            while(itr.hasNext()){
 //                System.out.println(itr.next());
 //            }

                
 //        }

        return index.get(token);

    }


    /**
     *  Searches the index for postings matching the query.
     */
    public PostingsList search( Query query, int queryType, int rankingType, int structureType ) {
	// 
	//  REPLACE THE STATEMENT BELOW WITH YOUR CODE
	//
        String term = query.terms.get(0);
        return getPostings(term);

    }


    /**
     *  No need for cleanup in a HashedIndex.
     */
    public void cleanup() {
    }
}
