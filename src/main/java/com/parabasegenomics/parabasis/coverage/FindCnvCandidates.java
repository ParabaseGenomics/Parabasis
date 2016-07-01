/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.coverage;

import com.parabasegenomics.parabasis.reporting.CoverageModelReport;
import com.parabasegenomics.parabasis.util.Reader;
import htsjdk.samtools.util.Interval;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author evanmauceli
 */
public class FindCnvCandidates {

    private final static String TAB = "\t";
        
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    throws IOException {
        String assayName = args[0];
        String initFilepath = args[1];
        String bamFilepath = args[2];
    
        AssayCoverageModel coverageModel = new AssayCoverageModel(assayName);
        File initFile = new File(initFilepath);
        File modelFile = new File(initFilepath + ".model");
        
        if (!modelFile.exists()) {    
            coverageModel.initialize(new File(initFilepath));
            coverageModel.writeToFile(modelFile);
        } else {
            CoverageModelReport coverageModelReport 
                = new CoverageModelReport(modelFile,assayName);
            coverageModel = coverageModelReport.readModel();
        }
        coverageModel.setThreshold(2.0);
                
        
        Reader utilityReader = new Reader();
        
        BamCoverage bamCoverage = new BamCoverage(bamFilepath);
        
        List<IntervalCoverage> modelIntervals = coverageModel.getIntervals();
        for (IntervalCoverage modelIntervalCoverage : modelIntervals) {
            Interval interval = modelIntervalCoverage.getInterval();
            System.out.print("checking interval:\t" 
                + interval.getContig()
                +"\t"
                + interval.getStart()
                +"\t"
                + interval.getEnd());
            
            // coverage from the BAM file
            IntervalCoverage intervalCoverage = new IntervalCoverage(interval);
            intervalCoverage.update(bamCoverage.getCoverage(interval));
            System.out.print("\t" + bamCoverage.getCoverage(interval));
            System.out.print("\t"+intervalCoverage.getMean());
            System.out.print("\t" + coverageModel.getZscore(intervalCoverage));
            System.out.println(TAB+coverageModel.getMeanCoverageAt(interval));

            
            if (coverageModel.isOutlier(intervalCoverage)) {
                 System.out.println("outlier: " 
                + interval.getContig()
                + TAB
                + interval.getStart()
                + TAB
                + interval.getEnd());
            }
            
        }
      
     
    }
 
}
