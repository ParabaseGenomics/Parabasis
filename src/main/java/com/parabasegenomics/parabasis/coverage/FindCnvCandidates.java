/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.coverage;

import static com.parabasegenomics.parabasis.decorators.AnnotationKeys.GENE_KEY;
import com.parabasegenomics.parabasis.decorators.GeneModelDecorator;
import com.parabasegenomics.parabasis.gene.GeneModelCollection;
import com.parabasegenomics.parabasis.reporting.CoverageModelReport;
import com.parabasegenomics.parabasis.target.AnnotatedInterval;
import com.parabasegenomics.parabasis.util.Reader;
import htsjdk.samtools.util.Interval;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
        String geneModelFile = args[3];
        String gencodeModelFile = args[4];
        String genelistFile = args[5];
        
        
        GeneModelCollection geneModelCollection = new GeneModelCollection();
        geneModelCollection.readGeneModelCollection(geneModelFile);
        geneModelCollection.addGeneModelCollection(gencodeModelFile);
        geneModelCollection.aggregateTranscriptsByGenes();
        
        AssayCoverageModel coverageModel = new AssayCoverageModel(assayName);
        File initFile = new File(initFilepath);
        File modelFile = new File(initFilepath + ".model");
        
        if (!modelFile.exists()) {    
            coverageModel.initializeFromResourceFile(new File(initFilepath));
            coverageModel.writeToFile(modelFile);
        } else {
            CoverageModelReport coverageModelReport 
                = new CoverageModelReport(modelFile,assayName);
            coverageModel = coverageModelReport.readModel();
        }
        coverageModel.setThreshold(2.0);
                
        
        Reader utilityReader = new Reader();
        Set<String> geneNamesInTest = utilityReader.readHashSet(genelistFile);
        GeneModelDecorator geneModelDecorator 
            = new GeneModelDecorator(geneModelCollection,geneNamesInTest);

        
        BamCoverage bamCoverage = new BamCoverage(bamFilepath);
        
        List<AnnotatedInterval> candidates=new ArrayList<>();
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
                 
                 candidates.add(new AnnotatedInterval(interval));
            }
            
        }
      
        for (AnnotatedInterval candidate : candidates) {
            geneModelDecorator.annotate(candidate);
            System.out.println(
                "outlier: "
                + candidate.getInterval().getContig()
                + TAB
                + candidate.getInterval().getStart()
                + TAB
                + candidate.getInterval().getEnd()
                + TAB
                + candidate.getAnnotation(GENE_KEY));
        }
     
    }
 
}
