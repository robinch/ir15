/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */  

package ir;

import java.util.LinkedList;
import java.io.Serializable;
import java.util.Collections;

/**
 *   A list of postings for a given word.
 */
public class PostingsList implements Serializable {

    /** The postings list as a linked list. */
    private LinkedList<PostingsEntry> list = new LinkedList<PostingsEntry>();

    /**  Number of postings in this list  */
    public int size() {
        return list.size();
    }

    /**  Returns the ith posting */
    public PostingsEntry get( int i ) {
        return list.get( i );
    }

    public LinkedList<PostingsEntry> getList(){
        return list;
    }

    public void add(int docID, int offset){
        if(list.size() == 0){
            list.add(new PostingsEntry(docID));
        }
        
        PostingsEntry pEntry = list.getLast();
        if(pEntry.docID == docID){
            pEntry.add(offset);
        }else{
            list.add(new PostingsEntry(docID));   
            list.getLast().add(offset);
        }
        
    }

    public void add(PostingsEntry pe){
        list.add(pe);
    }

    public void sort(){
        Collections.sort(list);
    }
}



