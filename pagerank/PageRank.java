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
    private int nrOfWalks;
    
    /* --------------------------------------------- */


    public PageRank( String filename ) {
       int noOfDocs = readDocs( filename );

       nrOfWalks = noOfDocs;

       // Exact method
        System.out.println("Power Iteration!");
       double[] pr = computePagerank( noOfDocs );
       Integer[] sortedIndexes = sortedIndexArray(pr);
       printResult(pr, sortedIndexes);
       System.out.println("-----------------------");

       // Monte Carlo methods
       
       double[] mc1 = monteCarlo1(noOfDocs);
       double[] mc2 = monteCarlo2(noOfDocs);       
       double[] mc3 = monteCarlo3(noOfDocs);      
       double[] mc4 = monteCarlo4(noOfDocs);      
       double[] mc5 = monteCarlo5(noOfDocs);
       
       printSquareDiff(sortedIndexes, pr, mc1, mc2, mc3, mc4, mc5);

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

    double[] monteCarlo1(int numberOfDocs){
        double[] docs = new double[numberOfDocs];
        int nrOfSteps;
        int doc;
        int counter;
        int randomDoc = rnd.nextInt(numberOfDocs);
        for (int i = 0; i < nrOfWalks; i++){
            nrOfSteps = rnd.nextInt(10);
            for(int j = 0; j < nrOfSteps; j++){
                randomDoc = randomStep(randomDoc, numberOfDocs);
            }
            docs[randomDoc]++;
        }
        for (int i = 0; i < numberOfDocs; i++){
            docs[i] = docs[i]/nrOfWalks;
        }
        
        return docs;
        
    }


    double[] monteCarlo2(int numberOfDocs){
        double[] docs = new double[numberOfDocs];
        int m = 5; // number of times the walk is done from a doc
        int randomDoc;
        int nrOfSteps;

        for (int i = 0; i < numberOfDocs; i++){
            for (int j = 0; j < m; j++){
                randomDoc = i;
                nrOfSteps = rnd.nextInt(10);
                for (int k = 0; k < nrOfSteps; k++){
                    randomDoc = randomStep(randomDoc, numberOfDocs);
                }
                docs[randomDoc]++;
            }
        }

        for (int i = 0; i < numberOfDocs; i++){
            docs[i] = docs[i]/(nrOfWalks*m);
        }
        return docs;
    
    } 


    double[] monteCarlo3(int numberOfDocs){
        double[] docs = new double[numberOfDocs];
        int m = 5; // number of times the walk is done from a doc
        int randomDoc;
        int nrOfSteps;
        int counter = 0;

        for (int i = 0; i < numberOfDocs; i++){
            nrOfSteps = rnd.nextInt(10);
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


    double[] monteCarlo4(int numberOfDocs){
        double[] docs = new double[numberOfDocs];
        int m = 5; // number of times the walk is done from a doc
        int randomDoc;
        int counter = 0;

        for (int i = 0; i < numberOfDocs; i++){
            randomDoc = i;
            for (int j = 0; j < m; j++){
                randomDoc = i;
                docs[randomDoc]++;
                counter++;
                while(!isDangling(randomDoc)){
                    randomDoc = randomStep(randomDoc, numberOfDocs);
                    docs[randomDoc]++;
                    counter++;
                }
            }
        }

        for (int i = 0; i < numberOfDocs; i++){
            docs[i] = docs[i]/(counter);
        }

        return docs;
    }


    double[] monteCarlo5(int numberOfDocs){
        double[] docs = new double[numberOfDocs];
        int randomDoc;
        int counter = 0; 
        for (int i = 0; i < nrOfWalks; i++){
            randomDoc = rnd.nextInt(numberOfDocs);
            docs[randomDoc]++;
            counter++;
            while(!isDangling(randomDoc)){
                randomDoc = randomStep(randomDoc, numberOfDocs);
                docs[randomDoc]++;
                counter++;
            }
        }

        for (int i = 0; i < numberOfDocs; i++){
            docs[i] = docs[i]/(counter);
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

    private void printResult(double[] x, Integer[] orderedList){ 
        for(int i = 0; i < 50; i++){
            System.out.format("%d: %s %.8f%n", i+1, docName[orderedList[i]], x[orderedList[i]]);
        }
    }

    void printSquareDiff(Integer[] sortedIndexes, double[] pi, double[] mc1, double[] mc2, double[] mc3, double[] mc4, double[] mc5){
        System.out.println("Square Diffs");
        System.out.println("Name\tExact\t\tmc1\t\tmc2\t\tmc3\t\tmc4\t\tmc5");
        List<double[]> mcs = new ArrayList<double[]>(5);
        mcs.add(mc1);
        mcs.add(mc2);
        mcs.add(mc3);
        mcs.add(mc4);
        mcs.add(mc5);

        int index;
        double[] diffs = new double[6];
        for (int i = 0; i < 50; i++){
            index = sortedIndexes[i];
            diffs[0] = pi[index];
            for(int j = 0; j < mcs.size(); j++){
                diffs[j+1] = pi[index] - mcs.get(j)[index];
                diffs[j+1] *= diffs[j+1];
            }
            System.out.format("%s\t%.8f\t%.8f\t%.8f\t%.8f\t%.8f\t%.8f%n", docName[index], diffs[0], diffs[1], diffs[2], diffs[3], diffs[4], diffs[5]);
        }
        return;
    }



    private Integer[] sortedIndexArray(double[] array){
        final double[] list = array;
        Integer[] sorted = new Integer[array.length];
        for(int i = 0; i < array.length; i++){
            sorted[i] = i;
        }
        Arrays.sort(sorted, new Comparator<Integer>() {
            @Override
            public int compare(Integer i, Integer j){
                double d = (list[j] - list[i]);
                if(d < 0 ) return -1;
                if(d == 0) return 0;
                else return 1;
            }
        });
        return sorted;
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
