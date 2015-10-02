
package ir;

import java.util.HashMap;
import java.util.Iterator;

class DocTermFrequencyIndex implements Index {

	private HashMap<String,Double> normTF = new HashMap<String, Double>();

	private HashMap<String, Integer> termFreq = new HashMap<String, Integer>();

	private int lastDoc = -1;
	private double termsInDoc = 0;

	public void insert( String token, int docID, int offset ){

		// If new doc, add the normalized term freq to the map
		// and update last doc, reset termsInDoc
		if(docID != lastDoc) {

			Double val = 0.0;

			for (String term : termFreq.keySet()){

				val = termFreq.get(term).doubleValue()/termsInDoc;

				if(normTF.containsKey(term)){
					val += normTF.get(term);
					normTF.put(term, val);
				} else {
					normTF.put(term, val);
				}
				//System.out.println("normTermFreq: " + normTermFreq);
			}
			lastDoc = docID;
			termsInDoc = 0;
			/*System.out.println("START REAL TERMFREQ!!!!");
			for(String term: termFreq.keySet()){
				System.out.format("%s : %d\n", term, termFreq.get(term));
			}
			System.out.println("END REAL TERMFREQ!!!!");*/
			termFreq = new HashMap<String, Integer>();
		}

		Integer value = termFreq.get(token);
		if(value == null){
			termFreq.put(token, 1);
		} else {
			value += 1;
			termFreq.put(token,value);
		}
		termsInDoc++;
		

	}
	public Iterator<String> getDictionary(){
		return normTF.keySet().iterator();
	}
	public PostingsList getPostings( String token ){
    	// Won't need this
		return null;
	}
	public PostingsList search( Query query, int queryType, int rankingType, int structureType ){
    	// Wont need this
    	return null;
	}

	public HashMap<String, Double> getNormTF(){
		return normTF;
	}

    // Won't need this
	public void cleanup(){

	}
}