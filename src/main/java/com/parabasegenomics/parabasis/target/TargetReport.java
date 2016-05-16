/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.target;

import static com.parabasegenomics.parabasis.decorators.AnnotationKeys.CAPTURE_KEY;
import static com.parabasegenomics.parabasis.decorators.AnnotationKeys.GC_KEY;
import static com.parabasegenomics.parabasis.decorators.AnnotationKeys.GENE_KEY;
import static com.parabasegenomics.parabasis.decorators.AnnotationKeys.HOM_KEY;
import com.parabasegenomics.parabasis.decorators.CaptureDecorator;
import static com.parabasegenomics.parabasis.decorators.FormatPatterns.percentPattern;
import com.parabasegenomics.parabasis.decorators.GCCountDecorator;
import com.parabasegenomics.parabasis.decorators.GeneModelDecorator;
import com.parabasegenomics.parabasis.gene.Exon;
import com.parabasegenomics.parabasis.gene.GeneModel;
import com.parabasegenomics.parabasis.gene.GeneModelCollection;
import com.parabasegenomics.parabasis.gene.Transcript;
import com.parabasegenomics.parabasis.reporting.Report;
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
    private final static String TAB = "\t";
    private final static String NEWLINE = "\n";
    private final static String pctHomKey = HOM_KEY;
    private final static String geneKey = GENE_KEY;
  
    private final static String formatPattern = percentPattern;
    private final DecimalFormat decimalFormat;
    
    private final Reader utilityReader;
    
    private List<Interval> targetIntervals;
    private List<Interval> captureIntervals;
    private Set<String> targetGenelist;
    
    private final GeneModelCollection geneModelCollection;
    private IndexedFastaSequenceFile referenceSequence;
    private FastaSequenceIndex referenceIndex;
    
    private final List<AnnotatedInterval> annotatedIntervals;
    AnnotatedIntervalManager annotatedIntervalManager;
    private final File targetReportFile;
    private final File summaryReportFile;
    private Report targetReport;
    private Report summaryReport;
    
    
    public TargetReport(String fileToWrite) {
       geneModelCollection = new GeneModelCollection();
       utilityReader = new Reader();
       targetIntervals = new ArrayList<>();
       captureIntervals = new ArrayList<>();
       
       targetGenelist = new HashSet<>();
       
       annotatedIntervals = new ArrayList<>();
       annotatedIntervalManager
            = new AnnotatedIntervalManager();
       
       targetReportFile = new File(fileToWrite + ".report.bed");
       summaryReportFile = new File(fileToWrite+ ".summary.txt");
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

        targetReport.decorateTargets();        
        targetReport.report();
        targetReport.codingSummary();
        targetReport.summary();
        
    }
    
    /**
     * For each target interval, annotate with gene location, gc content, etc.
     * @throws IOException 
     */
    public void decorateTargets() 
    throws IOException {
        
       GeneModelDecorator geneModelDecorator 
           = new GeneModelDecorator(geneModelCollection,targetGenelist);
        
       GCCountDecorator gcPctDecorator = null;
       if (referenceSequence != null) {
           gcPctDecorator = new GCCountDecorator(referenceSequence,referenceIndex);
       }
       
       CaptureDecorator captureDecorator = null;
       if (!captureIntervals.isEmpty()) {
           captureDecorator = new CaptureDecorator(captureIntervals);
       }
       
        for (Interval interval : targetIntervals) {
            AnnotatedInterval annotatedTarget = new AnnotatedInterval(interval);
            if (gcPctDecorator != null) {
                gcPctDecorator.annotate(annotatedTarget);
            } 
            if (captureDecorator != null) { 
                captureDecorator.annotate(annotatedTarget);
            } 

            geneModelDecorator.annotate(annotatedTarget);
                       
            annotatedIntervals.add(annotatedTarget);
        }
    }

    /**
     * Print a target-level report.
     * @throws java.io.IOException
     */
    public void report() 
    throws IOException {
       
        targetReport = new Report(targetReportFile);
       
        for (AnnotatedInterval interval : annotatedIntervals) {
            
            Interval genomicInterval = interval.getInterval();
            String direction = "+";
            if (genomicInterval.isNegativeStrand()) {
                direction = "-";
            }
            
            Double gcPct = interval.getPercentOfIntervalForAnnotation(GC_KEY);
            Double capPct = interval.getPercentOfIntervalForAnnotation(CAPTURE_KEY);
            
            StringBuilder reportLine = new StringBuilder();
            reportLine.append(genomicInterval.getContig());
            reportLine.append(TAB);
            reportLine.append(genomicInterval.getStart());
            reportLine.append(TAB);
            reportLine.append(genomicInterval.getEnd());
            reportLine.append(TAB);
            reportLine.append(direction);
            reportLine.append(TAB);
            reportLine.append(interval.getAnnotation(geneKey));
            reportLine.append(TAB);
            reportLine.append(interval.length());
            reportLine.append(TAB);
            if (capPct != null) {
                reportLine.append(decimalFormat.format(capPct));
                reportLine.append(TAB);
            }
            if (gcPct != null ) {
                reportLine.append(decimalFormat.format(gcPct));   
            }
            //reportLine.append(NEWLINE);
            targetReport.write(reportLine.toString());
            
        }
        targetReport.close();
    }
    
    /**
     * Print a gene-level report summary based on coding regions.
     * @throws IOException 
     */
    public void codingSummary() 
    throws IOException {
        summaryReport = new Report(summaryReportFile);
        
        List<GeneModel> genes = geneModelCollection.selectGeneModels(targetGenelist);
        for (GeneModel gene : genes) {            
            Transcript collapsedTranscript = gene.getCollapsedTranscript();
            int [] markupArray;
            int offset = 0;
            
            if (collapsedTranscript.isNonCoding()) {
               markupArray = new int [collapsedTranscript.getTranscriptInterval().length()]; 
               offset = collapsedTranscript.getTranscriptStart();
            } else {
                markupArray = new int [collapsedTranscript.getCodingInterval().length()];
                offset = collapsedTranscript.getCodingStart();
            }
          
            Exon exon = collapsedTranscript.get5primeExon();
              
            int startBase = 0;
            int endBase = 0;
            
            if (collapsedTranscript.isNonCoding()) {
                startBase = exon.getStart();
                endBase = exon.getEnd();
            } else if (exon.getCodingExon() != null) {
                startBase = exon.getCodingStart();
                endBase = exon.getCodingEnd();
            }

            for (int base = startBase; base<endBase;base++) {
                int index = base-offset;
                markupArray[index] = 1;
            }
            while (collapsedTranscript.hasNextExon()) {
                exon = collapsedTranscript.getNextExon();
                if (exon.getCodingInterval()==null) {
                    continue;
                }
                for (int base = exon.getCodingStart(); base<exon.getCodingEnd();base++) {
                int index = base-offset;
                markupArray[index] = 1;
                }
            }
            
            for (Interval capture : captureIntervals) {
                if (!capture.intersects(collapsedTranscript.getTranscriptInterval())) {
                    continue;
                }
                
                if (capture
                        .intersect(
                            collapsedTranscript.getTranscriptInterval())
                            .length()<=1) {
                    continue;
                }
              
                
                for (int base=capture.getStart(); base<capture.getEnd(); base++) {
                    int index = base-offset;
                    if (index<0 ||index>=markupArray.length) {
                        continue;
                    }
                    if (markupArray[index]<1) {
                        continue;
                    } 
                    markupArray[index]=2;
                }
            }
                
            int numberBasesCaptured = 0;
            int numberBasesInGene = 0;
            for (int index=0; index<markupArray.length; index++) {
                if (markupArray[index]>=1) {
                    numberBasesInGene++;
                }
                if (markupArray[index]==2) {
                    numberBasesCaptured++;
                }
            }
            
            double capturePercent 
                = 100.0*(double) numberBasesCaptured/(double) numberBasesInGene;
            
            StringBuilder reportLine = new StringBuilder();
            reportLine.append(gene.getGeneName());
            reportLine.append(TAB);
            reportLine.append(numberBasesInGene);
            reportLine.append(TAB);
            reportLine.append(numberBasesCaptured);
            reportLine.append(TAB);
            reportLine.append(decimalFormat.format(capturePercent));
            //reportLine.append(NEWLINE);
            summaryReport.write(reportLine.toString());
            
        }
        summaryReport.close();
    }
               
    
    
    public void summary() 
    throws IOException {
        File fullTranscriptReportFile = new File(summaryReportFile.getAbsoluteFile() + ".full");
        Report fullTranscriptReport = new Report(fullTranscriptReportFile);
        
        List<GeneModel> genes = geneModelCollection.selectGeneModels(targetGenelist);
        for (GeneModel gene : genes) {            
            Transcript collapsedTranscript = gene.getCollapsedTranscript();
            int [] markupArray;
            int offset = 0;

            markupArray = new int [collapsedTranscript.getTranscriptInterval().length()]; 
            offset = collapsedTranscript.getTranscriptStart();
            
          
            Exon exon = collapsedTranscript.get5primeExon();
              
            int startBase = 0;
            int endBase = 0;
  
            startBase = exon.getStart();
            endBase = exon.getEnd();
            
            for (int base = startBase; base<endBase;base++) {
                int index = base-offset;
                markupArray[index] = 1;
            }
            while (collapsedTranscript.hasNextExon()) {
                exon = collapsedTranscript.getNextExon();
                if (exon.getInterval()==null) {
                    continue;
                }
                for (int base = exon.getStart(); base<exon.getEnd();base++) {
                int index = base-offset;
                markupArray[index] = 1;
                }
            }
            
            for (Interval capture : captureIntervals) {
                if (!capture.intersects(collapsedTranscript.getTranscriptInterval())) {
                    continue;
                }
                
                if (capture
                        .intersect(
                            collapsedTranscript.getTranscriptInterval())
                            .length()<=1) {
                    continue;
                }
              
                
                for (int base=capture.getStart(); base<capture.getEnd(); base++) {
                    int index = base-offset;
                    if (index<0 ||index>=markupArray.length) {
                        continue;
                    }
                    if (markupArray[index]<1) {
                        continue;
                    } 
                    markupArray[index]=2;
                }
            }
                
            int numberBasesCaptured = 0;
            int numberBasesInGene = 0;
            for (int index=0; index<markupArray.length; index++) {
                if (markupArray[index]>=1) {
                    numberBasesInGene++;
                }
                if (markupArray[index]==2) {
                    numberBasesCaptured++;
                }
            }
            
            double capturePercent 
                = 100.0*(double) numberBasesCaptured/(double) numberBasesInGene;
            
            StringBuilder reportLine = new StringBuilder();
            reportLine.append(gene.getGeneName());
            reportLine.append(TAB);
            reportLine.append(numberBasesInGene);
            reportLine.append(TAB);
            reportLine.append(numberBasesCaptured);
            reportLine.append(TAB);
            reportLine.append(decimalFormat.format(capturePercent));
            //reportLine.append(NEWLINE);
            fullTranscriptReport.write(reportLine.toString());
            
        }
        fullTranscriptReport.close();
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
    
    
}
