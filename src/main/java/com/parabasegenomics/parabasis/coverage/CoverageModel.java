/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.coverage;

import com.parabasegenomics.parabasis.vcf.VcfChrXCounter;
import htsjdk.samtools.util.Interval;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import javax.json.JsonArray;

/**
 * Class to provide a statistical description of average coverage performance 
 * of an assay.
 * @author evanmauceli
 */
public class CoverageModel {

    private static final String TAB = "\t";
    
    // a position is a string of the form "chrA:position"
    private String [] positions;
    
    private double [] means;
    private double [] varianceDeviations;
    private double [] coeffOfVariations;
    private int [] counts;
    
    private Integer count;
    private Double threshold;
    
    
    /**
     * Null constructor, methods fill in the stats.
     */
    public CoverageModel() {
        positions = null;
        means = null;
        varianceDeviations = null;
        coeffOfVariations = null;
        count=0;
        threshold=10.;
        counts = null;
    }
    
    /**
     * Initialize the member arrays from a list of intervals.
     * @param intervals 
     */
    public void initialize(List<Interval> intervals) {
        Integer baseCount = countBases(intervals);
        positions = new String [baseCount];
        means = new double [baseCount];
        varianceDeviations = new double [baseCount];
        coeffOfVariations = new double [baseCount];
        counts = new int [baseCount];
        
        count=0;
        threshold=10.;
        
        Integer index=0;
        for (Interval interval : intervals) {
            String contig = interval.getContig();
            Integer startPos = interval.getStart();
            Integer endPos = interval.getEnd();
                
            for (Integer pos=startPos; pos<endPos; pos++) {
                positions[index]
                    = contig + ":" + pos.toString();
                index++;
            }  
        } 
    }
    
    /**
     * Build the member arrays from a list of intervals and bam files.  
     * @param intervals 
     * @param bamArray 
     * @param vcfArray
     * @throws java.io.IOException 
     */
    public void build(
        List<Interval> intervals,
        JsonArray bamArray, 
        JsonArray vcfArray) 
    throws IOException {
        
        initialize(intervals);
        
        String assayName="dummy";
        for (int bamIndex=0; bamIndex<bamArray.size(); bamIndex++) { 
            IntervalCoverageManager coverageManager 
                = new IntervalCoverageManager(assayName,intervals);

            String bamFilepath = bamArray.getString(bamIndex);
            coverageManager.parseBam(new File(bamFilepath));

            Integer readCount = coverageManager.getReadCount();
            Double weight =  1000000000.0/(readCount*75);

            incrementCount();

            VcfChrXCounter vcfChrXCounter 
                = new VcfChrXCounter(vcfArray.getString(bamIndex));
            boolean isMale = vcfChrXCounter.isMale();
            
            Integer index=0;
            for (Interval interval : intervals) {
                String contig = interval.getContig();
                Integer startPos = interval.getStart();
                Integer endPos = interval.getEnd();

                for (Integer pos=startPos; pos<endPos; pos++) {
                    String positionString 
                        = contig + ":" + pos.toString();

                    Double locusCoverage 
                        = weight*coverageManager.getCoverageAt(interval, positionString);
                    if (isMale && contig.equals("chrX")) {
                        locusCoverage *= 2.;
                    }                    
                    updatePosition(locusCoverage, index, positionString);
                    index++;  
                }                          
            } 
        }        
    }
    
    
    /**
     * Create the model from a list of BAM files provided in a JsonArray.
     * @param jsonArray 
     */
    public void createFromList(JsonArray jsonArray) {
        
    }
    
    /**
     * Set the threshold for defining an outlier coverage (by z-score).
     * @param t 
     */
    public void setThreshold(double t) {
        threshold=t;
    }
    
    /**
     * Write out the coverage model to a file. The format is:
     * 
     * line1: the number of samples used to make the model
     * line2: the number of positions in the model
     * line3-N: the model: position"\t"mean"\t"std"\t"cv"\n"
     * 
     * @param coverageModelFile
     * @throws IOException 
     */
    public void write(File coverageModelFile) 
    throws IOException {
        try (BufferedWriter writer 
            = new BufferedWriter(new FileWriter(coverageModelFile))) {
            String sampleCount = count.toString();
            writer.write(sampleCount);
            writer.newLine();
            
            Integer intervalsSize = positions.length;
            writer.write(intervalsSize.toString());
            writer.newLine();
            
            for (int index=0; index<intervalsSize; index++) {
                writer.write(positions[index]
                    +"\t"+means[index]
                    +"\t"+varianceDeviations[index]
                    +"\t"+coeffOfVariations[index]
                    +"\t"+counts[index]);
                writer.newLine();
            }
        }
    }
    
    /**
     * Read a CoverageModel from a file.  Directly sets the stats.
     * @param coverageModelFile File containing the coverage model.  
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public void read(File coverageModelFile) 
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
        varianceDeviations = new double [baseCount];
        coeffOfVariations = new double [baseCount];
        counts = new int [baseCount];
        
        Integer index = 0;
        while (reader.ready()) {
            line = reader.readLine();
            if (line.isEmpty()) {
                break;
            }
            String [] tokens = line.split(TAB);
            positions[index]=tokens[0];
            means[index]=Double.parseDouble(tokens[1]);
            varianceDeviations[index]=Double.parseDouble(tokens[2]);
            coeffOfVariations[index]=Double.parseDouble(tokens[3]);
            counts[index]=Integer.parseInt(tokens[4]);
            index++;
        }
        reader.close();
    }
    
    /**
     * Increment the sample count.
     */
    public void incrementCount() {
        count++;
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
    public void updatePosition(double thisCount, int index, String thisPosition) 
    throws IOException {
        if (!arrayInSync(index,thisPosition)) {
            throw new IOException("index out of sync with position: " 
                + index +" "+thisPosition +" "+positions[index]);
        }
        
        if (counts[index]==0) {
            means[index]=thisCount;
            varianceDeviations[index]=0;
            coeffOfVariations[index]=0;
            counts[index]=1;
        } else {
            
            ++counts[index];
            double currentMeanDiff = thisCount-means[index];
            means[index] += (currentMeanDiff)/counts[index];
            
            double updatedMeanDiff = thisCount-means[index];
            varianceDeviations[index] += (currentMeanDiff*updatedMeanDiff)/(counts[index]-1);
            coeffOfVariations[index]
                = (1+(1/(4*counts[index]))) * (Math.sqrt(varianceDeviations[index])/means[index]);
            
        }
        
    }
    
    public boolean isOutlier(double testCount, int index, String thisPosition) 
    throws IOException {
        if (!arrayInSync(index,thisPosition)) {
            throw new IOException("index out of sync with position: " 
                + index +" "+thisPosition +" "+positions[index]);
        }    

        // don't look where the model is inaccurate
        if (means[index]==0 ) {
            return false;
        }
        
        if(varianceDeviations[index]==0) {
            return false;
        }
        
        Double zscore = getZscore(testCount,index);
        if (zscore == null) {
           return false;
       }
       return (Math.abs(zscore)>threshold);      
    }
    
    /**
     * Returns the z-score of the provided coverage given the current
     * state of the model. Returns null if the interval is not held by the model.
     * 
     * A naive interpretation: 
     *  negative zscore = deletion 
     *  positive zscore = gain
     * 
     * @param testCount
     * @param index
     * @return 
     */
    public Double getZscore(double testCount,Integer index) {
        if (index != null) {
            Double mean = means[index];
            Double std = Math.sqrt(varianceDeviations[index]);
            Double zscore 
                = (testCount-mean)/std;
            return (zscore);
        }
        return null;
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
    
    
  /**
     * Ugly hack used to identify the NBDxV1.1 samples for male/female coverage levels
     * adjustments on the X chr.
     * @param file
     * @return 
     */
     public boolean parseBamNameForMF(File file) {
        String fileAsString = file.getName();
        return (fileAsString.contains("AB") 
            || fileAsString.contains("AMI")
            || fileAsString.contains("APHL-7")
            || fileAsString.contains("10844")
            || fileAsString.contains("CSC31206"));
    }

}