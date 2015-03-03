/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2012
 */  

import java.util.*;
import java.io.*;
import java.util.Arrays;

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

    
    /* --------------------------------------------- */


    public PageRank( String filename ) {
     int noOfDocs = readDocs( filename );
     computePagerank( noOfDocs );
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
        // intialise x and xs
        double[] x = new double[numberOfDocs];
        double[] xs = new double[numberOfDocs];
        double diffSum = 0;
        xs[0] = 1;
        double sum = 0;
        for (int i = 0; i < MAX_NUMBER_OF_ITERATIONS; i++){
            diffSum = 0;
            
            x = xs;
            xs = new double[numberOfDocs];
            sum = 0;
            for (int j = 0; j < numberOfDocs; j++ ){
             
                sum += x[j];    
                // is a sink
                if(out[j] == 0){
                    for(int k = 0; k < xs.length; k++){
                        if(k != j){
                            // adds this value to all but it's own index
                            xs[k] += x[j]*(1-BORED)/(numberOfDocs-1);    
                        }   
                    }
                } else { // i not sink
                    for(Integer l: link.get(j).keySet()){
                        // System.out.format("j = %d keys: %d%n", j, l);
                        xs[l] += x[j]*(1-BORED)/out[j];
                    }
                }
            }
            for (int j = 0; j < numberOfDocs; j++ ){
                xs[j] += BORED/numberOfDocs;
                diffSum = Math.max(diffSum, Math.abs(xs[j] - x[j]));
            }
            

            if(diffSum <= EPSILON){
                System.out.println("EPSILON!!!");
                printResult(xs, true);
                return;
            }
            System.out.println("iteration: "+ i);
        }
        printResult(xs, true);
        // printStuff();

    }


    private void printResult(double[] x, boolean sort){ 
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
        if(sort) Collections.sort(list);

        for(int i = 0; i < 60; i++){
            System.out.format("%d: %s %f%n", i+1, list.get(i).name, list.get(i).score);
        }
        // to print them in order I need to to either
        // add them in a structure that keeps track of the 
        // original position in the array or 
        // iterate thorugh the array 50 times to find and 
        // print them one by one
        // sturcture has the O(nLogn)
        // iterate O(50n)



    }


    private void printStuff(){

        for(Integer i : link.keySet()){
            System.out.println("From: " +i);
            for(Integer j : link.get(i).keySet()){
                System.out.println("\tTo: " +j);
            }    
            System.out.println("Nr of links: " + out[i]);
        }

        for (int i = 0; i < docName.length; i++){
            System.out.format(" docName[%d] = %s : %d\n", i, docName[i], docNumber.get(docName[i] ));
        }
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
