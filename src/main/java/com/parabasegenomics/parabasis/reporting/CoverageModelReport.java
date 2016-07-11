/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.reporting;

import com.parabasegenomics.parabasis.coverage.AssayCoverageModel;
import com.parabasegenomics.parabasis.coverage.IntervalCoverage;
import htsjdk.samtools.util.Interval;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

/**
 * 
 * @author evanmauceli
 */
public class CoverageModelReport extends Report {
    private final static String HEADER 
        = "#Assay\t";
    private final static String COL_HEADER
        = "CHR\tBEG\tEND\tCOUNT\tMEAN\tSTD\tCV\n";    

    private final String assayName;
    
    public CoverageModelReport(File file, String name) 
    throws IOException {
        super(file);

        assayName=name;
        
    }
    
    /**
     * Write the coverage model to a text file.
     * @param targetCoverageList
     * @throws IOException 
     */
    public void writeModel(List<IntervalCoverage> targetCoverageList) 
    throws IOException {
   
        this.openForWriting();
        bufferedWriter.write(HEADER);
        bufferedWriter.write(assayName);
        bufferedWriter.write(NEWLINE);
        bufferedWriter.write(COL_HEADER);   
        
       for (IntervalCoverage intervalCoverage : targetCoverageList) {
           StringBuilder builder = new StringBuilder();
           
           Interval interval = intervalCoverage.getInterval();
           builder.append(interval.getContig());
           builder.append(TAB);
           builder.append(interval.getStart());
           builder.append(TAB);
           builder.append(interval.getEnd());
           builder.append(TAB);
           builder.append(intervalCoverage.count());
           builder.append(TAB);
           builder.append(intervalCoverage.getMean());
           builder.append(TAB);
           builder.append(intervalCoverage.getStandardDeviation());
           builder.append(TAB);
           builder.append(intervalCoverage.getCoefficientOfVariation());
           builder.append(NEWLINE);
           
           bufferedWriter.write(builder.toString());
           
        }
    }
    
    /**
     * Read the coverage model from a text file.
     * @return Returns an AssayCoverageModel object initialized from a file.
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public AssayCoverageModel readModel() 
    throws FileNotFoundException, IOException {
        
        this.openForReading();
        
        String name = this.readLine();
        String colHeaders = this.readLine();
     
        AssayCoverageModel model = new AssayCoverageModel(name);
        
        while (this.getReader().ready()) {
            String line = this.readLine();
            String [] tokens = line.split(TAB);
            Interval interval 
                = new Interval(
                    tokens[0], 
                    Integer.valueOf(tokens[1]), 
                    Integer.valueOf(tokens[2]));
            int count = Integer.valueOf(tokens[3]);
            Mean mean = new Mean();
            mean.increment(Double.valueOf(tokens[4]));
            StandardDeviation std = new StandardDeviation();
            std.increment(Double.valueOf(tokens[5]));
            double cv = Double.valueOf(tokens[6]);
           
            model.update(
                new IntervalCoverage(
                    interval,
                    count,
                    mean,
                    std,
                    cv));
            
        }
        return model;
    }
    
 
}
