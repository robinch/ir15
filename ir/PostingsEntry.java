/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */  

package ir;

import java.io.Serializable;
import java.util.LinkedList;

public class PostingsEntry implements Comparable<PostingsEntry>, Serializable {

    public int docID;
    public double score;
    private LinkedList<Integer> offsets;

    
    public PostingsEntry(int docID){
        this.docID = docID;
        this.offsets = new LinkedList<Integer>(); 
    }
    /**
     *  PostingsEntries are compared by their score (only relevant 
     *  in ranked retrieval).
     *
     *  The comparison is defined so that entries will be put in 
     *  descending order.
     */
    public int compareTo( PostingsEntry other ) {
        return Double.compare( other.score, score );
    }

    public LinkedList<Integer> getOffsets(){
        return offsets;
    }

    public void add(int offset){

        Integer off = new Integer(offset);
        if (offsets.size() == 0){
            offsets.add(off);
        }
        else if(!(offsets.getLast().compareTo(off) == 0)){
            offsets.add(off);
        }
    }
}


