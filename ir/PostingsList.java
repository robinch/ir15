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
import java.io.*;

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

	public void score(int rankingType){
		double weight = 0.01;
		String line;
		try{
			BufferedReader br = new BufferedReader(new FileReader("ir/weight.txt"));
			while((line = br.readLine()) != null){
				weight = new Double(line);
			}
			br.close();
		}
		catch(Exception e){
			e.printStackTrace();
			System.out.println("Error! Will use default value:" + weight);
		}


		for(PostingsEntry pe: list){
			switch(rankingType){
				case(Index.TF_IDF):
				pe.score = tf_idfScore(pe);
				break;
				case(Index.PAGERANK):
				pe.score = pagerankScore(pe);
				break;
				case(Index.COMBINATION):
				pe.score = combinationScore(pe, weight);
				break;
			}
		}
	}

	public double tf_idfScore(PostingsEntry pe){
		int tf = pe.getOffsets().size();
		double idf = Math.log(Index.docIDs.size()/size());
		int len = Index.docLengths.get(Integer.toString(pe.docID));
		return tf*idf/len;
	}

	public double pagerankScore(PostingsEntry pe){	
		String s = Index.docIDs.get(Integer.toString(pe.docID));
		s = s.replace("./davisWiki/", "");
		s = s.replace(".f", "");
		return Index.docRanking.get(s);
	}

	public double combinationScore(PostingsEntry pe, double weight){
		// higher weight gives tf_idf more impact, lower gives pr.
		double tfScore = tf_idfScore(pe);
		double prScore = pagerankScore(pe);
		return weight*tfScore+(1 - weight)*10*prScore; // THis will be changed
	}


	public void add(PostingsEntry pe){
		list.add(pe);
	}

	public void sort(){
		Collections.sort(list);
	}
}



