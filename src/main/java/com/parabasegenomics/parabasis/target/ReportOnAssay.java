/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.target;

import static com.parabasegenomics.parabasis.decorators.AnnotationKeys.GENE_KEY;
import static com.parabasegenomics.parabasis.decorators.AnnotationKeys.HOM_KEY;
import com.parabasegenomics.parabasis.decorators.CaptureDecorator;
import com.parabasegenomics.parabasis.decorators.CoverageDecorator;
import static com.parabasegenomics.parabasis.decorators.FormatPatterns.percentPattern;
import com.parabasegenomics.parabasis.decorators.GCCountDecorator;
import com.parabasegenomics.parabasis.decorators.GeneModelDecorator;
import com.parabasegenomics.parabasis.gene.Exon;
import com.parabasegenomics.parabasis.gene.GeneModel;
import com.parabasegenomics.parabasis.gene.GeneModelCollection;
import com.parabasegenomics.parabasis.gene.Transcript;
import com.parabasegenomics.parabasis.reporting.GeneSummaryReport;
import com.parabasegenomics.parabasis.reporting.Report;
import com.parabasegenomics.parabasis.reporting.TargetReport;
import com.parabasegenomics.parabasis.reporting.AssayReport;
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
public class ReportOnAssay {
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
    private final File targetReportFile;
    private final File codingTargetReportFile;
    private final File assayReportFile;
    private final File summaryReportFile;
    private final File codingSummaryReportFile;
    private final File codingAssayReportFile;
    private TargetReport targetReport;
    private TargetReport codingTargetReport;
    private AssayReport assayReport;
    private GeneSummaryReport summaryReport;
    private GeneSummaryReport codingSummaryReport;
    private AnnotationSummary annotationSummary;
    private final int splicingDistance;
    private final String assayName;
    private String coverageResourceFile;
    
    public ReportOnAssay(String fileToWrite, String name) {
       geneModelCollection = new GeneModelCollection();
       utilityReader = new Reader();
       targetIntervals = new ArrayList<>();
       captureIntervals = new ArrayList<>();
       
       targetGenelist = new HashSet<>();
       
       annotatedIntervals = new ArrayList<>();
       
       assayReportFile = new File(fileToWrite + ".assay.report.txt");
       targetReportFile = new File(fileToWrite + ".report.bed");
       codingTargetReportFile = new File(fileToWrite + ".coding.report.bed");
       summaryReportFile = new File(fileToWrite+ ".summary.txt");
       codingSummaryReportFile = new File(fileToWrite + ".coding.summary.txt");
       codingAssayReportFile = new File(fileToWrite + ".coding.assay.report.txt");
       
       referenceSequence = null;      
       splicingDistance = 10;
       assayName = name;      
       decimalFormat = new DecimalFormat(formatPattern);
       coverageResourceFile  = null;

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
    
    public void setResourceFile(String resourceFile) {
        coverageResourceFile = resourceFile;
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
        String assayName = args[6];
        String captureIntervalFile = null;
        if (args.length == 8) {
            captureIntervalFile = args[7];
        }
        String coverageResourceFile = null;
        if (args.length == 9) {
            coverageResourceFile = args[8];
        }
        
        ReportOnAssay reportOnAssay = new ReportOnAssay(outputFile,assayName);  
        reportOnAssay.loadGeneModelCollection(refSeqGeneModelFile,gencodeGeneModelFile);
        reportOnAssay.loadReferenceSequence(humanReferenceFile);

        reportOnAssay.loadTargetFile(targetIntervalFile);
        if (captureIntervalFile != null) {
            reportOnAssay.loadCaptureFile(captureIntervalFile);
        }
        if (coverageResourceFile != null) {
            reportOnAssay.setResourceFile(coverageResourceFile);
        }
        
        reportOnAssay.loadTargetGenelist(targetGenelistFile);
        reportOnAssay.decorateTargets();        
        reportOnAssay.report();
       
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
       
       annotationSummary = new AnnotationSummary();
       annotationSummary.addDecorator(geneModelDecorator);
       
       if (captureDecorator != null) {
           annotationSummary.addDecorator(captureDecorator);
       }
       if (gcPctDecorator != null) {
           annotationSummary.addDecorator(gcPctDecorator);
       }    
       
       CoverageDecorator coverageDecorator = null;
       if (coverageResourceFile != null) {
           annotationSummary.addDecorator(coverageDecorator);
       }
    }

    /**
     * Print a target-level report.
     * @throws java.io.IOException
     */
    public void report() 
    throws IOException {      
        targetReport = new TargetReport(targetReportFile);
        targetReport.setAnnotationSummary(annotationSummary);
        targetReport.reportOn(targetIntervals);
        targetReport.close();
        
        assayReport = new AssayReport(assayReportFile,assayName);
        assayReport.setAnnotationSummary(annotationSummary);
        assayReport.reportOn(targetIntervals);
        assayReport.close();
        
        codingTargetReport = new TargetReport(codingTargetReportFile);
        codingTargetReport.setAnnotationSummary(annotationSummary);
        List<Interval> codingIntervals
            = geneModelCollection
                .createCodingTargets(targetGenelist,splicingDistance);
        codingTargetReport.reportOn(codingIntervals);
        codingTargetReport.close();
          
        AssayReport codingAssayReport = new AssayReport(codingAssayReportFile,assayName);
        codingAssayReport.setAnnotationSummary(annotationSummary);
        codingAssayReport.reportOn(codingIntervals);
        codingAssayReport.close();
   
        
        summaryReport = new GeneSummaryReport(summaryReportFile);
        summaryReport.setAnnotationSummary(annotationSummary);      
        for (String gene : targetGenelist) {
            Set<String> geneToTarget = new HashSet<>();
            geneToTarget.add(gene);
            List<Interval> intervals 
                = geneModelCollection
                    .createTargets(geneToTarget, splicingDistance);
            summaryReport.reportOn(intervals, gene);
        }
        summaryReport.close();
       
        codingSummaryReport = new GeneSummaryReport(codingSummaryReportFile);
        codingSummaryReport.setAnnotationSummary(annotationSummary);
        for (String gene : targetGenelist) {
            Set<String> geneToTarget = new HashSet<>();
            geneToTarget.add(gene);
            List<Interval> intervals 
                = geneModelCollection
                    .createCodingTargets(geneToTarget, splicingDistance);
            codingSummaryReport.reportOn(intervals, gene);
        }
        codingSummaryReport.close();     
    }
    
    /**
     * Print a gene-level report summary based on coding regions.
     * @throws IOException 
     */
    public void codingSummary() 
    throws IOException {
        summaryReport = new GeneSummaryReport(summaryReportFile);
        
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
            int splicing = 10;
            
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
               
    
    /**
     * Print a transcript-level summary report.
     * @throws IOException 
     */
    public void summary() 
    throws IOException {
        File fullTranscriptReportFile = new File(summaryReportFile.getAbsoluteFile() + ".full");
        Report fullTranscriptReport = new GeneSummaryReport(fullTranscriptReportFile);
        
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
