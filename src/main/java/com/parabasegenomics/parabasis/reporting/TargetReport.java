/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.reporting;

import com.parabasegenomics.parabasis.coverage.AnnotatedInterval;
import com.parabasegenomics.parabasis.decorators.GCPctDecorator;
import com.parabasegenomics.parabasis.decorators.GeneModelDecorator;
import com.parabasegenomics.parabasis.gene.GeneModel;
import com.parabasegenomics.parabasis.gene.GeneModelCollection;
import com.parabasegenomics.parabasis.util.Reader;
import htsjdk.samtools.reference.FastaSequenceIndex;
import htsjdk.samtools.reference.IndexedFastaSequenceFile;
import htsjdk.samtools.reference.ReferenceSequence;
import htsjdk.samtools.util.Interval;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author evanmauceli
 */
public class TargetReport {
    
    private final static String pctGCKey = "GC";
    private final static String pctHomKey = "HOM";
    private final static String pctCaptureKey = "CAP";
    private final static String geneKey = "GEN";
  
    private final static String formatPattern = "###.##";
    private final DecimalFormat decimalFormat;
    
    private final Reader utilityReader;
    
    private List<Interval> targetIntervals;
    private List<Interval> captureIntervals;
    private Set<String> targetGenelist;
    
    private final GeneModelCollection geneModelCollection;
    private IndexedFastaSequenceFile referenceSequence;
    private FastaSequenceIndex referenceIndex;
    
    private final List<AnnotatedInterval> annotatedIntervals;
    private final File reportFile;
    
    public TargetReport(String fileToWrite) {
       geneModelCollection = new GeneModelCollection();
       utilityReader = new Reader();
       targetIntervals = new ArrayList<>();
       captureIntervals = new ArrayList<>();
       
       targetGenelist = new HashSet<>();
       
       annotatedIntervals = new ArrayList<>();
       reportFile = null;//new File(fileToWrite);
       referenceSequence = null;
       
       decimalFormat = new DecimalFormat(formatPattern);
    }
    
    
    /**
     * Returns the list of annotated intervals.
     * @return 
     */
    public List<AnnotatedInterval> getAnnotatedIntervals() {
        return annotatedIntervals;
    }
    
    /**
     * Method to set the target intervals from a List of Interval objects.
     * @param intervals 
     */
    public void setTargetIntervals(List<Interval> intervals) {
        targetIntervals = intervals;
    }
    
    /**
     * Method to set the capture intervals from a List of Interval objects.
     * @param intervals 
     */
    public void setCaptureIntervals(List<Interval> intervals) {
        captureIntervals = intervals;
    }
    
    /**
     * Load a collection of RefSeq and GENCODE genes.
     * @param refSeqGeneModelFile RefSeq gene models.
     * @param gencodeGeneModelFile GENCODE gene models.
     * @throws IOException 
     */
    public void loadGeneModelCollection(
        String refSeqGeneModelFile,
        String gencodeGeneModelFile) 
    throws IOException {
        
        geneModelCollection.readGeneModelCollection(refSeqGeneModelFile);
        geneModelCollection.addGeneModelCollection(gencodeGeneModelFile);
        geneModelCollection.aggregateTranscriptsByGenes();
    }
    
    /**
     * Loads the human reference sequence.
     * @param referenceFile File containing the human reference sequence.
     */
    public void loadReferenceSequence(String referenceFile) {
        File humanReference = new File(referenceFile);               
        File referenceIndexFile = new File(humanReference + ".fai");    
        referenceIndex = new FastaSequenceIndex(referenceIndexFile);
        referenceSequence 
            = new IndexedFastaSequenceFile(humanReference,referenceIndex);      
    }
    
    /**
     * Load the target regions.
     * @param targetIntervalFile BED formatted file of target intervals.
     * @throws IOException 
     */
    public void loadTargetFile(String targetIntervalFile) 
    throws IOException {
        targetIntervals = utilityReader.readBEDFile(targetIntervalFile);
    }
    
    /**
     * Load the capture regions.
     * @param captureIntervalFile BED formatted file of capture intervals.
     * @throws IOException 
     */
    public void loadCaptureFile(String captureIntervalFile) 
    throws IOException {
        captureIntervals = utilityReader.readBEDFile(captureIntervalFile);
    }
    
    public void loadTargetGenelist(String targetGenelistFile) 
    throws IOException {
        targetGenelist = utilityReader.readHashSet(targetGenelistFile);
    }
    
    public static void main(String[] args) 
    throws IOException {
        
        String targetIntervalFile = args[0];
        String humanReferenceFile = args[1];
        String refSeqGeneModelFile = args[2];
        String gencodeGeneModelFile = args[3];
        String outputFile = args[4];
        String targetGenelistFile = args[5];
        String captureIntervalFile = null;
        if (args.length == 7) {
            captureIntervalFile = args[6];
        }
        
        TargetReport targetReport = new TargetReport(outputFile);  
        targetReport.loadGeneModelCollection(refSeqGeneModelFile,gencodeGeneModelFile);
        targetReport.loadReferenceSequence(humanReferenceFile);

        targetReport.loadTargetFile(targetIntervalFile);
        if (captureIntervalFile != null) {
            targetReport.loadCaptureFile(captureIntervalFile);
        }
        
        targetReport.loadTargetGenelist(targetGenelistFile);

        targetReport.aggregateData();        
        targetReport.report();
        
    }
    
    /**
     * For each target interval, annotate with gene location, gc content, etc.
     * @throws IOException 
     */
    public void aggregateData() 
    throws IOException {
        
       GeneModelDecorator geneModelDecorator 
           = new GeneModelDecorator(geneModelCollection,targetGenelist);
        
       GCPctDecorator gcPctDecorator = null;
       if (referenceSequence != null) {
           gcPctDecorator = new GCPctDecorator(referenceSequence,referenceIndex);
       }
       
        for (Interval interval : targetIntervals) {
            AnnotatedInterval annotatedTarget = new AnnotatedInterval(interval);
            if (gcPctDecorator != null) {
                gcPctDecorator.annotate(annotatedTarget);
            } else { 
            } 

            geneModelDecorator.annotate(annotatedTarget);
                       
            annotatedIntervals.add(annotatedTarget);
        }
    }
    
    
    /**
     * Returns the percentage of bases in the interval that are "G" or "C".
     * @param interval Interval in which to calculate GC content.
     * @return 
     */
    public double getGCPercentage(Interval interval) {
        
        String chromosome = interval.getContig();
        int startPosition = interval.getStart();
        int endPosition = interval.getEnd();
        ReferenceSequence seq 
            = referenceSequence.getSubsequenceAt(chromosome, startPosition, endPosition);
        
        int gcCount = 0;
        
        byte [] bases = seq.getBases();
        String basesString = new String(bases,Charset.defaultCharset());
        String upperCaseBases = basesString.toUpperCase();
        
        for (int i=0 ; i<upperCaseBases.length(); i++) {
            if (upperCaseBases.charAt(i) == 'C' 
                || upperCaseBases.charAt(i) == 'G') {
                gcCount++;
            }
        }
        
        return (100.0* (double) gcCount/(double)interval.length());       
    }
    
    /**
     * Print a report to stdout.
     */
    public void report() {
        for (AnnotatedInterval interval : annotatedIntervals) {
            System.out.print(interval.getInterval().toString());
            System.out.print("\t");
            System.out.print(interval.getAnnotation(geneKey));
            System.out.print("\t");
            System.out.println(interval.getAnnotation(pctGCKey));          
        }
    }
    
}
