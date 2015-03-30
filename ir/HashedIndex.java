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
import java.util.Arrays;
import java.io.*;



/**
 *   Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndex implements Index {

    //Debug
    boolean d = false;

    /** The index as a hashtable. */
    private HashMap<String,PostingsList> index = new HashMap<String,PostingsList>();
    

    public HashedIndex(){
        readRankingFile("pagerank/ranks.txt");

    }

    private void readRankingFile(String path){
        String line;
        String[] s;
        try{
            BufferedReader br = new BufferedReader(new FileReader(path));
            while((line = br.readLine()) != null){
                s = line.split(";");
                Index.docRanking.put(s[0], new Double(s[1]));
            }
            br.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     *  Inserts this token in the index.
     */
    public void insert( String token, int docID, int offset ) {
	//
	//  YOUR CODE HERE
	//  
        PostingsList postList = index.get(token);
        if(postList == null){
            postList = new PostingsList();
            index.put(token, postList);
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
        // for (String s: docIDs.keySet()){
        //     System.out.format("Key: %s Value:%s", s, docIDs.get(s));
        
        // }
        String[] terms = new String[query.size()];
        String s = "";
        for(int i = 0; i < query.size(); i++ ){
            s = query.terms.get(i).toLowerCase();
            //if (s.matches("[a-zA-Z0-9]+")) // dont think we need this
            terms[i] = s;
        }

        
        

        PostingsList postList = new PostingsList();

        
        if (terms.length == 1){
            if(queryType == Index.RANKED_QUERY)
            {
                postList = rankedQuery(getPostings(terms[0]), rankingType);
            }else{
                postList = getPostings(terms[0]);
            }
        }else {
            for(int i = 0; i < terms.length; i++){
                switch(queryType){
                    case (Index.INTERSECTION_QUERY):
                    if(i == 0) postList = intersectionQuery(getPostings(terms[0]), getPostings(terms[1]));
                    if(i > 1) postList = intersectionQuery(postList, getPostings(terms[i]));
                    break;
                    case (Index.PHRASE_QUERY):
                    if(i == 0) postList = phraseQuery(getPostings(terms[0]), getPostings(terms[1]), i+1);
                    if(i > 1) postList = phraseQuery(postList, getPostings(terms[i]), i);
                    break;
                    case (Index.RANKED_QUERY):
                    if(i == 0) postList = rankedQuery(rankedQuery(getPostings(terms[0]), rankingType), getPostings(terms[1]), rankingType);
                    if(i > 1) postList = rankedQuery(postList, getPostings(terms[i]), rankingType);
                    break;
                }
            }
        }

        if (queryType == Index.RANKED_QUERY && postList != null){
            postList.sort();
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


    private PostingsList phraseQuery(PostingsList p1, PostingsList p2, int offsetDiff){

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


    private PostingsList rankedQuery(PostingsList pl, int rankingType){
        if(pl == null) return null;
        pl.score(rankingType); 
        return pl;
    }

    private PostingsList rankedQuery(PostingsList p1, PostingsList p2, int rankingType){
        p2 = rankedQuery(p2, rankingType);

        if(p1 == null || p2 == null){
            return null;
        }

        PostingsList postList = new PostingsList();
        int i = 0;
        int j = 0;
        PostingsEntry pe1;
        PostingsEntry pe2;

        while (i < p1.size() && j < p2.size()){
            // insection search 
            pe1 = p1.get(i);
            pe2 = p2.get(j);
            if(pe1.docID == pe2.docID){
                // PostingsEntry pe = new PostingsEntry(pe1.docID);
                pe1.score = pe1.score + pe2.score;
                postList.add(pe1);               
                i++;
                j++;
            }else if(pe1.docID < pe2.docID){
                postList.add(pe1);
                i++;
            }else{
                postList.add(pe2);
                j++;
            }
        }

         // if one of the list is finished
        if(i == p1.size() ){
            while(j < p2.size()){
                pe2 = p2.get(j);
                postList.add(pe2);
                j++;
            }
            return postList;
        }else{
            while(i < p1.size()){
                pe1 = p1.get(i);
                postList.add(pe1);
                i++;
            }
            return postList;
        }
    }

    /**
     *  No need for cleanup in a HashedIndex.
     */
    public void cleanup() {
    }
}
