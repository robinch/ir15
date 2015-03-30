/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2012
 */  

import java.util.*;
import java.io.*;
import java.util.Arrays;
import java.util.Random;
import java.text.*;

public class PageRank{

	/**  
	 *   Maximal number of documents. We're assuming here that we
	 *   don't have more docs than we can keep in main memory.
	 */
	final static int MAX_NUMBER_OF_DOCS = 200000; // org value 2000000

	/**
	 *   Mapping from document names to document numbers.
	 */
	Hashtable<String,Integer> docNumber = new Hashtable<String,Integer>();

	/**
	 *   Mapping from document numbers to document names
	 */
	String[] docName = new String[MAX_NUMBER_OF_DOCS];

	/**  
	 *   A memory-efficient representation of the transition matrix.
	 *   The outlinks are represented as a Hashtable, whose keys are 
	 *   the numbers of the documents linked from.<p>
	 *
	 *   The value corresponding to key i is a Hashtable whose keys are 
	 *   all the numbers of documents j that i links to.<p>
	 *
	 *   If there are no outlinks from i, then the value corresponding 
	 *   key i is null.
	 */
	Hashtable<Integer,Hashtable<Integer,Boolean>> link = new Hashtable<Integer,Hashtable<Integer,Boolean>>();

	/**
	 *   The number of outlinks from each node.
	 */
	int[] out = new int[MAX_NUMBER_OF_DOCS];

	/**
	 *   The number of documents with no outlinks.
	 */
	int numberOfSinks = 0;

	/**
	 *   The probability that the surfer will be bored, stop
	 *   following links, and take a random jump somewhere.
	 */
	final static double BORED = 0.15; // org valyue 0.15

	/**
	 *   Convergence criterion: Transition probabilities do not 
	 *   change more that EPSILON from one iteration to another.
	 */
	final static double EPSILON = 0.00001;   // org value 0.0001

	/**
	 *   Never do more than this number of iterations regardless
	 *   of whether the transistion probabilities converge or not.
	 */
	final static int MAX_NUMBER_OF_ITERATIONS = 1000;   //org value 1000


	/**
	* Used to make a random walk
	*/
	private Random rnd = new Random(System.currentTimeMillis());
	final static int MAX_NR_OF_STEPS = 50;
	
	/* --------------------------------------------- */


	public PageRank( String filename ) {
		int noOfDocs = readDocs( filename );

		long start;
		long stop;

	   // Exact method
		System.out.println("Power Iteration!");
		start = System.nanoTime();
		double[] pr = computePagerank( noOfDocs );
		stop = System.nanoTime();
		System.out.format("Time taken: %d ms%n", (stop-start)/1000000);
		Integer[] sortedIndexes = sortedIndexArray(pr);
		printResult(pr, sortedIndexes);
		System.out.println("-----------------------");

	   // write to file
		writeToFile("ranks.txt","articleTitles.txt", pr);

	   // Monte Carlo methods
		for (int m = 1; m < 11; m++){
			System.out.format("Square diffs N = %d, (m = %d)%n", noOfDocs*m, m);
			start = System.nanoTime();
			double[] mc1 = monteCarlo1(noOfDocs, noOfDocs*m);
			stop = System.nanoTime();
			System.out.format("Time taken: %d ms (mc1)%n", (stop-start)/1000000);
			start = System.nanoTime();
			double[] mc2 = monteCarlo2(noOfDocs, m);       
			stop = System.nanoTime();
			System.out.format("Time taken: %d ms (mc2)%n", (stop-start)/1000000);
			start = System.nanoTime();
			double[] mc3 = monteCarlo3(noOfDocs, m); 
			stop = System.nanoTime();
			System.out.format("Time taken: %d ms (mc3)%n", (stop-start)/1000000);
			start = System.nanoTime();
			double[] mc4 = monteCarlo4(noOfDocs, m);      
			stop = System.nanoTime();
			System.out.format("Time taken: %d ms (mc4)%n", (stop-start)/1000000);
			start = System.nanoTime();
			double[] mc5 = monteCarlo5(noOfDocs, noOfDocs*m);
			stop = System.nanoTime();
			System.out.format("Time taken: %d ms (mc5)%n", (stop-start)/1000000);
			printSquareDiff(sortedIndexes, pr, mc1, mc2, mc3, mc4, mc5);
			System.out.println("----------------------");
		}
	}


	/* --------------------------------------------- */


	/**
	 *   Reads the documents and creates the docs table. When this method 
	 *   finishes executing then the @code{out} vector of outlinks is 
	 *   initialised for each doc, and the @code{p} matrix is filled with
	 *   zeroes (that indicate direct links) and NO_LINK (if there is no
	 *   direct link. <p>
	 *
	 *   @return the number of documents read.
	 */
	int readDocs( String filename ) {
		int fileIndex = 0;
		try {
			System.err.print( "Reading file... " );
			BufferedReader in = new BufferedReader( new FileReader( filename ));
			String line;
			while ((line = in.readLine()) != null && fileIndex<MAX_NUMBER_OF_DOCS ) {
				int index = line.indexOf( ";" );
				String title = line.substring( 0, index );
				Integer fromdoc = docNumber.get( title );
		//  Have we seen this document before?
				if ( fromdoc == null ) {    
			// This is a previously unseen doc, so add it to the table.
					fromdoc = fileIndex++;
					docNumber.put( title, fromdoc );
					docName[fromdoc] = title;
				}
		// Check all outlinks.
				StringTokenizer tok = new StringTokenizer( line.substring(index+1), "," );
				while ( tok.hasMoreTokens() && fileIndex<MAX_NUMBER_OF_DOCS ) {
					String otherTitle = tok.nextToken();
					Integer otherDoc = docNumber.get( otherTitle );
					if ( otherDoc == null ) {
			// This is a previousy unseen doc, so add it to the table.
						otherDoc = fileIndex++;
						docNumber.put( otherTitle, otherDoc );
						docName[otherDoc] = otherTitle;
					}
			// Set the probability to 0 for now, to indicate that there is
			// a link from fromdoc to otherDoc.
					if ( link.get(fromdoc) == null ) {
						link.put(fromdoc, new Hashtable<Integer,Boolean>());
					}
					if ( link.get(fromdoc).get(otherDoc) == null ) {
						link.get(fromdoc).put( otherDoc, true );
						out[fromdoc]++;
					}
				}
			}
			if ( fileIndex >= MAX_NUMBER_OF_DOCS ) {
				System.err.print( "stopped reading since documents table is full. " );
			}
			else {
				System.err.print( "done. " );
			}
		// Compute the number of sinks.
			for ( int i=0; i<fileIndex; i++ ) {
				if ( out[i] == 0 )
					numberOfSinks++;
			}
		}
		catch ( FileNotFoundException e ) {
			System.err.println( "File " + filename + " not found!" );
		}
		catch ( IOException e ) {
			System.err.println( "Error reading file " + filename );
		}
		System.err.println( "Read " + fileIndex + " number of documents" );
		return fileIndex;
	}


	/* --------------------------------------------- */


	/*
	 *   Computes the pagerank of each document.
	 */
	double[] computePagerank( int numberOfDocs ) {
	//
	//   YOUR CODE HERE
	//
		// intialise x and xs
		double[] x = new double[numberOfDocs];
		double[] xs = new double[numberOfDocs];
		double diffSum = 0;
		double jPart = BORED/numberOfDocs;;
		double sinkFactors = 0;        
		int iterations = 0;
		xs[0] = 1;
		for (int i = 0; i < MAX_NUMBER_OF_ITERATIONS; i++){
			diffSum = 0;
			x = xs;
			xs = new double[numberOfDocs];
			sinkFactors = 0;

			for (int j = 0; j < numberOfDocs; j++ ){
				// adds the cP part
				// is a sink
				if(out[j] == 0){
					sinkFactors += x[j];
				} else { // is not a sink
					double cPart = (1-BORED)/out[j];
					for(Integer l: link.get(j).keySet()){
						xs[l] += x[j]*cPart;
					}
				}
			}

			// adds the rest
			for (int j = 0; j < numberOfDocs; j++ ){
				xs[j] += jPart;
				xs[j] += sinkFactors * (1-BORED)/(numberOfDocs-1);
				if(out[j] == 0){
					xs[j] =  xs[j] - x[j]*(1-BORED)/(numberOfDocs-1);
				}
				diffSum = Math.max(diffSum, Math.abs(xs[j] - x[j]));
			}

			// Checks if the largest difference is smaller than epsilon
			if(diffSum <= EPSILON){
				System.out.println("EPSILON!!!");
				break;
			}
			iterations++;

		}
		System.out.println("iterations: " + iterations);
		return xs;

	}

	/**
	*	End-point with random start
	*/
	double[] monteCarlo1(int numberOfDocs, int n){
		double[] docs = new double[numberOfDocs];
		int nrOfSteps;
		int doc;
		int counter;
		int randomDoc = rnd.nextInt(numberOfDocs);
		for (int i = 0; i < n; i++){
			nrOfSteps = rnd.nextInt(MAX_NR_OF_STEPS);
			for(int j = 0; j < nrOfSteps; j++){
				randomDoc = randomStep(randomDoc, numberOfDocs);
			}
			docs[randomDoc]++;
		}
		for (int i = 0; i < numberOfDocs; i++){
			docs[i] = docs[i]/n;
		}

		return docs;

	}


	/**
	* End-point with cyclic start
	*/
	double[] monteCarlo2(int numberOfDocs, int m){
		double[] docs = new double[numberOfDocs];
		int randomDoc;
		int nrOfSteps;

		for (int i = 0; i < numberOfDocs; i++){
			for (int j = 0; j < m; j++){
				randomDoc = i;
				nrOfSteps = rnd.nextInt(MAX_NR_OF_STEPS);
				for (int k = 0; k < nrOfSteps; k++){
					randomDoc = randomStep(randomDoc, numberOfDocs);
				}
				docs[randomDoc]++;
			}
		}

		for (int i = 0; i < numberOfDocs; i++){
			docs[i] = docs[i]/(numberOfDocs*m);
		}
		return docs;

	} 

	/**
	* Complete path with cyclic start
	*/
	double[] monteCarlo3(int numberOfDocs, int m){
		double[] docs = new double[numberOfDocs];
		int randomDoc;
		int nrOfSteps;
		int counter = 0;

		for (int i = 0; i < numberOfDocs; i++){
			nrOfSteps = rnd.nextInt(MAX_NR_OF_STEPS);
			counter += (nrOfSteps + 1);
			for (int j = 0; j < m; j++){
				randomDoc = i;
				docs[randomDoc]++;
				for (int k = 0; k < nrOfSteps; k++){
					randomDoc = randomStep(randomDoc, numberOfDocs);
					docs[randomDoc]++;
				}
			}
		}

		for (int i = 0; i < numberOfDocs; i++){
			docs[i] = docs[i]/(counter*m);
		}

		return docs;
	}


	/**
	*	Complete path stopping at dangling node, cyclic start
	*/
	double[] monteCarlo4(int numberOfDocs, int m){
		double[] docs = new double[numberOfDocs];
		int randomDoc;
		int visits = 0;

		for (int i = 0; i < numberOfDocs; i++){
			for (int j = 0; j < m; j++){
				randomDoc = i;
				docs[randomDoc]++;
				visits++;
				while(!isDangling(randomDoc)){
					randomDoc = randomStep(randomDoc, numberOfDocs);
					docs[randomDoc]++;
					visits++;
				}
			}
		}

		for (int i = 0; i < numberOfDocs; i++){
			docs[i] = docs[i]/(visits);
		}

		return docs;
	}


	/**
	* Compelte path stopping at dangling node, random start
	*/
	double[] monteCarlo5(int numberOfDocs, int n){
		double[] docs = new double[numberOfDocs];
		int randomDoc;
		int visits = 0; 
		for (int i = 0; i < n; i++){
			randomDoc = rnd.nextInt(numberOfDocs);
			docs[randomDoc]++;
			visits++;
			while(!isDangling(randomDoc)){
				randomDoc = randomStep(randomDoc, numberOfDocs);
				docs[randomDoc]++;
				visits++;
			}
		}

		for (int i = 0; i < numberOfDocs; i++){
			docs[i] = docs[i]/(visits);
		}

		return docs;

	}

	/**
	* Makes a random walk from walk to
	* another doc that it links to
	* Also has the probability BORED
	* to make a jump to any document
	*/
	private int randomStep(int doc, int numberOfDocs){   

		int randomDoc;
		// Check if bored or sink
		if(rnd.nextDouble() <= BORED || out[doc] == 0){
			randomDoc = rnd.nextInt(numberOfDocs);
		} else {
			LinkedList<Integer> linkList = new LinkedList<Integer>();
			linkList.addAll(link.get(doc).keySet());
			// Gets a random doc from the docs list of links
			randomDoc = linkList.get(rnd.nextInt(linkList.size()));
		}
		return randomDoc;
	}

	boolean isDangling(int doc){
		return out[doc] == 0;
	}


	/* --------------------------------------------- */

	/**
	* Prints the top 50 results
	*/
	private void printResult(double[] x, Integer[] orderedList){ 
		NumberFormat f = new DecimalFormat("0.######E0");
		for(int i = 0; i < 50; i++){
			System.out.format("%d:\t%s\t%s%n", i+1, docName[orderedList[i]], f.format(x[orderedList[i]]));
		}
	}

	/**
	* Prints the square difference between the power iteration ranks and the monte carlo ranks
	*/
	void printSquareDiff(Integer[] sortedIndexes, double[] pi, double[] mc1, double[] mc2, double[] mc3, double[] mc4, double[] mc5){
		System.out.println("mc1\t\tmc2\t\tmc3\t\tmc4\t\tmc5");
		NumberFormat f = new DecimalFormat("0.######E0");  // the decimalformat that will be printed
		List<double[]> mcs = new ArrayList<double[]>(5);
		mcs.add(mc1);
		mcs.add(mc2);
		mcs.add(mc3);
		mcs.add(mc4);
		mcs.add(mc5);
		int topIndex, bottomIndex;
		double[] topDiffs = new double[mcs.size()];
		double[] bottomDiffs = new double[mcs.size()];
		double diff = 0;
		for (int i = 0; i < 50; i++){
			topIndex = sortedIndexes[i];
			bottomIndex = sortedIndexes[sortedIndexes.length - 1 - i];
			for(int j = 0; j < mcs.size(); j++){
				//top diffs
				diff = pi[topIndex] - mcs.get(j)[topIndex];
				diff *= diff;
				topDiffs[j] += diff;
				// bottendiffs
				diff = pi[bottomIndex] - mcs.get(j)[bottomIndex];
				diff *= diff;
				bottomDiffs[j] = diff;
			}
		}
		System.out.format("%s\t%s\t%s\t%s\t%s\t(top 50)%n",f.format(topDiffs[0]), f.format(topDiffs[1]), f.format(topDiffs[2]), f.format(topDiffs[3]), f.format(topDiffs[4]));
		System.out.format("%s\t%s\t%s\t%s\t%s\t(bottom 50)%n",f.format(bottomDiffs[0]), f.format(bottomDiffs[1]), f.format(bottomDiffs[2]), f.format(bottomDiffs[3]), f.format(bottomDiffs[4]));      
	}


	  /**
	  * Return a maped Integer array that is sorted by the 
	  * given array ranking
	  * The returned integer array has the document ids as values
	  * @array ranked array
	  */
	  private Integer[] sortedIndexArray(double[] array){
	  	final double[] list = array;
	  	Integer[] sorted = new Integer[array.length];
	  	for(int i = 0; i < array.length; i++){
	  		sorted[i] = i;
	  	}
	  	Arrays.sort(sorted, new Comparator<Integer>() {
	  		@Override
	  		public int compare(Integer i, Integer j){
	  			return Double.compare(list[j], list[i]);
	  		}
	  	});
	  	return sorted;
	  }

	/**
	* Reads from a file that maps id to document name then 
	* Takes the power iteration ranking and writes it to a
	* Semicolon seperated file
	* @writeTo write to file
	* @readFrom read article titles and ids from
	* @ranks array with ranks
	*/
	public void writeToFile(String writeTo, String readFrom, double[] ranks){
		String line;
		String[] s = new String[2];

		try{
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(writeTo)));
			BufferedReader br = new BufferedReader(new FileReader(readFrom));

			while((line = br.readLine()) != null){
				s = line.split(";");
				writer.println(s[1]+ ";" + ranks[docNumber.get(s[0])]);
			}
			writer.close();
			br.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}

	}

	public static void main( String[] args ) {
		if ( args.length != 1 ) {
			System.err.println( "Please give the name of the link file" );
		}
		else {
			new PageRank( args[0] );
		}
	}
}
