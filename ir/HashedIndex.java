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
import java.util.LinkedList;


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
	// 
        return index.get(token);

    }


    /**
     *  Searches the index for postings matching the query.
     */
    public PostingsList search( Query query, int queryType, int rankingType, int structureType ) {
	// 
	//  REPLACE THE STATEMENT BELOW WITH YOUR CODE
	//
        int querySize = query.size();
        String[] terms = new String[querySize];
        String s = "";
        for(int i = 0; i < querySize; i++ ){
            s = query.terms.get(i).toLowerCase();
            //if (s.matches("[a-zA-Z0-9]+")) // dont think we need this
            terms[i] = s;
        }

        
        if (terms.length == 1){
            return getPostings(terms[0]);
        }

        PostingsList postList = new PostingsList();

        
        // Simple intersection   
        for(int i = 0; i < terms.length; i++){
            if (i == 0){
                if(queryType == Index.INTERSECTION_QUERY)
                    postList = intersectionQuery(getPostings(terms[0]), getPostings(terms[1]));
                if(queryType == Index.PHRASE_QUERY)
                    postList = phraseSeach(getPostings(terms[0]), getPostings(terms[1]), i+1);
            }else if(i > 1){
                if(queryType == Index.INTERSECTION_QUERY)
                    postList = intersectionQuery(postList, getPostings(terms[i]));
                if(queryType == Index.PHRASE_QUERY)
                    postList = phraseSeach(postList, getPostings(terms[i]), i);
            }
        }
        return postList;
    }

    private PostingsList intersectionQuery(PostingsList p1, PostingsList p2){
        // TODO: Use skip-list in PostingsList to improve speed


        if(p1 == null || p2 == null){
            return null;
        }

        PostingsList postList = new PostingsList();
        int i = 0;
        int j = 0;
        int id1;
        int id2;

        while (i < p1.size() && j < p2.size()){
            id1 = p1.get(i).docID;
            id2 = p2.get(j).docID;

            if(id1 == id2){
                // Only adds 0 as an offset
                postList.add(id1, 0);
                // instead of ++ use a skiplist
                i++;
                j++;
            }else if(id1 < id2){
                i++;
            }else{
                j++;
            }
        }
        return postList;
    }


    private PostingsList phraseSeach(PostingsList p1, PostingsList p2, int offsetDiff){
        
        if(p1 == null || p2 == null){
            return null;
        }

        PostingsList postList = new PostingsList();
        // for loops
        int i = 0;
        int j = 0;


        int id1;
        int id2;
        int k,l;
        LinkedList<Integer> offsets1;
        LinkedList<Integer> offsets2;
        int off1, off2;

        while (i < p1.size() && j < p2.size()){
            id1 = p1.get(i).docID;
            id2 = p2.get(j).docID;

            if(id1 == id2){
                // Do the offset check here
                k = 0;
                l = 0;
                offsets1 = p1.get(i).getOffsets();
                offsets2 = p2.get(j).getOffsets();
                while(k < offsets1.size() && l < offsets2.size()){
                    off1 = offsets1.get(k);
                    off2 = offsets2.get(l);
                    if( (off2 - off1) == offsetDiff){
                        postList.add(id1, off1);
                        k++;
                        l++;
                    }else if(off1 < off2){
                        k++;
                    }else{
                        l++;
                    }
                }
                i++;
                j++;
            }else if(id1 < id2){
                i++;
            }else{
                j++;
            }
        }

        return postList;
    }

    /**
     *  No need for cleanup in a HashedIndex.
     */
    public void cleanup() {
    }
}
