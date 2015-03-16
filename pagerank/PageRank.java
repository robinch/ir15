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

       computePagerank( noOfDocs );

       // Monte Carlo methods
       monteCarlo1(noOfDocs);
       monteCarlo2(noOfDocs);
       monteCarlo3(noOfDocs);
       monteCarlo4(noOfDocs);
       monteCarlo5(noOfDocs);
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
    void computePagerank( int numberOfDocs ) {
	//
	//   YOUR CODE HERE
	//

        System.out.println("Power Iteration!");
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
        printResult(xs);
        System.out.println("----------------------");
    }


    private void printResult(double[] x){ 
        class Pair implements Comparable<Pair>{
            String name;
            double score;
            Pair(String name, double score){
                this.name = name;
                this.score = score;
            }
            public int compareTo( Pair other ) {
                return Double.compare( other.score, score );
            }
        }

        List<Pair> list = new ArrayList<Pair>(x.length);
        for(int i = 0; i < x.length; i++){
            list.add(new Pair(docName[i], x[i]));
        }
        
        Collections.sort(list);

        for(int i = 0; i < 50; i++){
            System.out.format("%d: %s %.8f%n", i+1, list.get(i).name, list.get(i).score);
        }
    }


    void monteCarlo1(int numberOfDocs){
        System.out.println("Monte Carlo 1");
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
        
        printResult(docs);
        System.out.println("----------------------");
    }


    void monteCarlo2(int numberOfDocs){
        System.out.println("Monte Carlo 2");
        double[] docs = new double[numberOfDocs];
        int m = 5; // number of times the walk is done from a doc
        int randomDoc;
        int nrOfSteps;

        for (int i = 0; i < numberOfDocs; i++){
            randomDoc = i;
            for (int j = 0; j < m; j++){
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
        printResult(docs);
        System.out.println("----------------------");
    } 


    void monteCarlo3(int numberOfDocs){
        System.out.println("Monte Carlo 3");
        double[] docs = new double[numberOfDocs];
        int m = 5; // number of times the walk is done from a doc
        int randomDoc;
        int nrOfSteps;
        int counter = 0;

        for (int i = 0; i < numberOfDocs; i++){
            randomDoc = i;
            nrOfSteps = rnd.nextInt(10);
            counter += (nrOfSteps + 1);
            for (int j = 0; j < m; j++){
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

        printResult(docs);
        System.out.println("----------------------");
    }


    void monteCarlo4(int numberOfDocs){
        System.out.println("Monte Carlo 4");
        double[] docs = new double[numberOfDocs];
        int m = 5; // number of times the walk is done from a doc
        int randomDoc;
        int counter = 0;

        for (int i = 0; i < numberOfDocs; i++){
            randomDoc = i;
            for (int j = 0; j < m; j++){
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

        printResult(docs);
        System.out.println("----------------------");
    }


    void monteCarlo5(int numberOfDocs){
        System.out.println("Monte Carlo 5");
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

        printResult(docs);
        System.out.println("----------------------");
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

    public static void main( String[] args ) {
       if ( args.length != 1 ) {
           System.err.println( "Please give the name of the link file" );
       }
       else {
           new PageRank( args[0] );
       }
   }
}
