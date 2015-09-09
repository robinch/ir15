
package ir;

import java.util.HashMap;
import java.util.Iterator;

class DocTermFrequencyIndex implements Index {

	private HashMap<String,Double> normTotalTermFreq = new HashMap<String, Double>();

	private HashMap<String, Integer> termFreq = new HashMap<String, Integer>();

	private int lastDoc = -1;
	private int termsInDoc = 0;

	public void insert( String token, int docID, int offset ){

		Integer oldValue;
		// If new doc, add the normalized term freq to the map
		// and update last doc, reset termsInDoc
		if(docID != lastDoc) {

			double normTermFreq;

			for (String term : termFreq.keySet()){

				normTermFreq = termFreq.get(term)/termsInDoc;

				if(normTotalTermFreq.containsKey(term)){
					normTotalTermFreq.put(term, 
						normTotalTermFreq.get(term) + 
						normTermFreq);
				} else {
					normTotalTermFreq.put(term, normTermFreq);
				}
			}
			lastDoc = docID;
			termsInDoc = 0;
			termFreq = new HashMap<String, Integer>();
		}

		oldValue = termFreq.get(token);
		if(oldValue == null){
			termFreq.put(token, 1);
		} else {
			termFreq.put(token, oldValue++);
		}
		

	}
	public Iterator<String> getDictionary(){
		return normTotalTermFreq.keySet().iterator();
	}
	public PostingsList getPostings( String token ){
    	// Won't need this
		return null;
	}
	public PostingsList search( Query query, int queryType, int rankingType, int structureType ){
    	// Wont need this
    	return null;
	}

	public HashMap<String, Double> getNormTotalTermFreq(){
		return normTotalTermFreq;
	}

    // Won't need this
	public void cleanup(){

	}
}