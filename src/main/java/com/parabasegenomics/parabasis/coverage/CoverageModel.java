/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.coverage;

import htsjdk.samtools.util.Interval;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * Class to provide a statistical description of average coverage performance 
 * of an assay.
 * @author evanmauceli
 */
public class CoverageModel {

    private static final String TAB = "\t";
    
    private final String [] positions;
    private final double [] means;
    private final double [] standardDeviations;
    private final double [] coeffOfVariations;
    
    private int count;
    
    /**
     * Creates an empty CoverageModel from a list of intervals.  Uses the 
     * updatePosition(...) method to fill the stats.
     * @param intervals 
     */
    public CoverageModel(List<Interval> intervals) {
        
        Integer baseCount = countBases(intervals);
        positions = new String [baseCount];
        means = new double [baseCount];
        standardDeviations = new double [baseCount];
        coeffOfVariations = new double [baseCount];
        
        count=0;
        
    }
    
    /**
     * Read a CoverageModel from a file.  Directly sets the stats, unlike the 
     * constructor that takes a list of intervals.
     * @param coverageModelFile File containing the coverage model.  
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public CoverageModel(File coverageModelFile) 
    throws FileNotFoundException, IOException {
        BufferedReader reader 
            = new BufferedReader(new FileReader(coverageModelFile));
        
        Integer baseCount = 0;
        String line="";
        
        //first line is the count of samples used for the stats
        if (reader.ready()) {
            line = reader.readLine();           
            this.count = Integer.parseInt(line);
        }
        // fail early on a bad model file
        if (this.count==0) {
            throw new IOException("count equals 0: "+line);
        }
        
        //second line is the total number of lines in the file equivalent to 
        //the "baseCount" from a list of intervals.
        if (reader.ready()) {
            line = reader.readLine();           
            baseCount = Integer.parseInt(line);
       
            
        }
        
        // fail early on a bad model file
        if (baseCount.equals(0)) {
            throw new IOException("base count equals 0: "+this.count+" "+line);
        }
        positions = new String [baseCount];
        means = new double [baseCount];
        standardDeviations = new double [baseCount];
        coeffOfVariations = new double [baseCount];
        
        Integer index = 0;
        while (reader.ready()) {
            line = reader.readLine();
            String [] tokens = line.split(TAB);
            positions[index]=tokens[0];
            means[index]=Double.parseDouble(tokens[1]);
            standardDeviations[index]=Double.parseDouble(tokens[2]);
            coeffOfVariations[index]=Double.parseDouble(tokens[3]);
        }
        
    }
    
    /**
     * Updates the mean, standard deviation and coeff. of variation at the provided
     * position with the provided value.  Calculations based on the 
     * commons.apache.org Mean and StandardDeviation classes.
     * @param thisCount the coverage at the given position used to update the model
     * @param index the index into the local array (TODO: fix this)
     * @param thisPosition the genomic position where the coverage was measured,
     * in the "chr:position" format.  Used to ensure the "index" points to the
     * correct array element.  If not, throw an IOException.
     * @throws IOException 
     */
    public void updatePosition(int thisCount, int index, String thisPosition) 
    throws IOException {
        if (!arrayInSync(index,thisPosition)) {
            throw new IOException("index out of sync with position: " 
                + index +" "+thisPosition +" "+positions[index]);
        }
        
        count++;
        if (count==1) {
            means[index]=thisCount;
            standardDeviations[index]=0;
            coeffOfVariations[index]=0;
        } else {
            means[index] += (thisCount-means[index])/count;
            
            standardDeviations[index] 
                += ( (thisCount-means[index])*(thisCount-means[index]) )
                    *( (count-1)/count );
            
            coeffOfVariations[index]
                = (1+(1/(4*count))) * (standardDeviations[index]/means[index]);
        }
    }
    
    /**
     * Check that the index into the local arrays is in sync with the given 
     * position.
     * @param index
     * @param thisPosition
     * @return Returns true is the provided index is in sync with the local
     * arrays.
     */
    private boolean arrayInSync(int index,String thisPosition) {
        return (positions[index].equals(thisPosition));
    }
    
    /**
     * Returns the number of bases in the given intervals.
     * @param intervals
     * @return 
     */
    private Integer countBases(List<Interval> intervals) {
        Integer number=0;
        for (Interval interval : intervals) {
            number += (interval.getEnd()-interval.getStart());
        }
        return number;
    } 
    
    
}
