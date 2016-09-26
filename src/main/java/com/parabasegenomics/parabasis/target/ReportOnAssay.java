/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.target;

import com.parabasegenomics.parabasis.coverage.AssayCoverageModel;
import static com.parabasegenomics.parabasis.decorators.AnnotationKeys.COVERAGE_KEY;
import static com.parabasegenomics.parabasis.decorators.AnnotationKeys.GENE_KEY;
import static com.parabasegenomics.parabasis.decorators.AnnotationKeys.HOM_KEY;
import com.parabasegenomics.parabasis.decorators.CaptureDecorator;
import com.parabasegenomics.parabasis.decorators.CoverageDecorator;
import static com.parabasegenomics.parabasis.decorators.FormatPatterns.percentPattern;
import com.parabasegenomics.parabasis.decorators.GCCountDecorator;
import com.parabasegenomics.parabasis.decorators.GapsDecorator;
import com.parabasegenomics.parabasis.decorators.GeneModelDecorator;
import com.parabasegenomics.parabasis.decorators.HomologyDecorator;
import com.parabasegenomics.parabasis.gene.GeneModelCollection;
import com.parabasegenomics.parabasis.reporting.GeneSummaryReport;
import com.parabasegenomics.parabasis.reporting.TargetReport;
import com.parabasegenomics.parabasis.reporting.AssayReport;
import com.parabasegenomics.parabasis.util.Reader;
import htsjdk.samtools.reference.FastaSequenceIndex;
import htsjdk.samtools.reference.IndexedFastaSequenceFile;
import htsjdk.samtools.util.Interval;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

/**
 *
 * @author evanmauceli
 */
public class ReportOnAssay {
    private final static String TAB = "\t";
    private final static String UNDERSCORE = "_";
    private final static String NEWLINE = "\n";
    private final static String pctHomKey = HOM_KEY;
    private final static String geneKey = GENE_KEY;
  
    //to parse from json file specifying the resources for main
    private static final String BAM = "BAM";
    private static final String TARGETS = "TARGETS";
    private static final String CODING = "CODINGTARGETS";
    private static final String CAPTURE = "CAPTURE";
    private static final String ASSAY = "ASSAY";
    private static final String HUMAN_REFERENCE = "REFERENCE";
    private static final String REFSEQ = "REFSEQ";
    private static final String GENCODE = "GENCODE";
    private static final String UNIQUE_KMERS = "UNIQMERS";
    private static final String GENELIST = "GENELIST";
    private static final String OUTPUT = "OUTPUT";
    private static final String COVERAGE_THRESHOLD = "THRESHOLD";
    
    private final static String formatPattern = percentPattern;
    private final DecimalFormat decimalFormat;
    
    private final Reader utilityReader;
    
    private List<Interval> targetIntervals;
    private List<Interval> captureIntervals;
    private List<Interval> codingIntervals;
    private List<Interval> uniqKmerIntervals;
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
    private AssayCoverageModel assayCoverageModel;
    private final int splicingDistance;
    private final String assayName;
    private File coverageResourceFile;
    private Double [] coverageThresholds;
    
    
    public ReportOnAssay(String fileToWrite, String name) {
        geneModelCollection = new GeneModelCollection();
        utilityReader = new Reader();
        targetIntervals = new ArrayList<>();
        captureIntervals = new ArrayList<>();
        uniqKmerIntervals = new ArrayList<>();
        targetGenelist = new HashSet<>();
       
        annotatedIntervals = new ArrayList<>();
       
        assayReportFile = new File(fileToWrite + ".assay.report.txt");
        targetReportFile = new File(fileToWrite + ".target.report.bed");
        codingTargetReportFile = new File(fileToWrite + ".coding.target.report.bed");
        summaryReportFile = new File(fileToWrite+ ".gene.report.txt");
        codingSummaryReportFile = new File(fileToWrite + ".coding.gene.report.txt");
        codingAssayReportFile = new File(fileToWrite + ".coding.assay.report.txt");
       
        assayCoverageModel = null;
        referenceSequence = null;      
        splicingDistance = 10;
        assayName = name;      
        decimalFormat = new DecimalFormat(formatPattern);
        coverageResourceFile  = null;
        coverageThresholds=null;    
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
     * Method to set up the AssayCoverageModel from the given resources json 
     * file.
     * @param resourceFilepath
     * @throws IOException 
     */
    public void loadCoverageModel(String resourceFilepath) 
    throws IOException {
        assayCoverageModel = new AssayCoverageModel(assayName);
        coverageResourceFile = new File(resourceFilepath);
        assayCoverageModel.initializeFromResourceFile(coverageResourceFile);  
    }
    
    /**
     * Re-create the coverage model to accurately report on the coding-only
     * targets.  Will throw an IOException if the coverage model has not 
     * already been initialized.
     * @throws java.io.IOException
     */
    public void resetCoverageModel() 
    throws IOException {
        assayCoverageModel
            .initializeFromResourceFileAndIntervals(
                coverageResourceFile,
                codingIntervals);
    }
    
    /**
     * Method to set the capture intervals from a List of Interval objects.
     * @param intervals 
     */
    public void setCaptureIntervals(List<Interval> intervals) {
        captureIntervals = intervals;
    }
    
    /**
     * Load a collection of RefSeq and GENCODE genes.  Will also create the list
     * of coding only intervals within the transcripts.
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
        codingIntervals = geneModelCollection
                .createCodingTargets(targetGenelist,splicingDistance);
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
     * Loads the "coding" regions.
     * @param codingTargetIntervalFile
     * @throws IOException 
     */
    public void loadCodingTargetFile(String codingTargetIntervalFile) 
    throws IOException {
        codingIntervals = utilityReader.readBEDFile(codingTargetIntervalFile);
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
    
    public void loadUniqKmerFile(String uniqKmerIntervalFile) 
    throws IOException {
        uniqKmerIntervals = utilityReader.readBEDFile(uniqKmerIntervalFile);
    }
    
    public void loadTargetGenelist(String targetGenelistFile) 
    throws IOException {
        targetGenelist = utilityReader.readHashSet(targetGenelistFile);
    }
    
    public void setLowCoverageThreshold(Double [] thresholdArray) {
        coverageThresholds=thresholdArray;
    }
    
    public static void main(String[] args) 
    throws IOException {
        
        File resourceFile = new File(args[0]);
        JsonReader reader 
            = Json.createReader(new FileReader(resourceFile.getAbsolutePath()));
        JsonObject jsonObject = reader.readObject();
           
        String targetIntervalFile = null;
        if (jsonObject.containsKey(TARGETS)) {
           targetIntervalFile = jsonObject.getString(TARGETS);
        } else {
            throw new IOException(
                "Must specify a targets file in the json resource");
        }
        
        String codingTargetIntervalFile = null;
        if (jsonObject.containsKey(CODING)) {
            codingTargetIntervalFile = jsonObject.getString(CODING);
        } 
        
        String targetGenelistFile = null;
        if (jsonObject.containsKey(GENELIST)) {
            targetGenelistFile = jsonObject.getString(GENELIST);
        } else {
            throw new IOException(
                "Must specify a genelist file in the json resource");
        }
        
        String assayName = "genericAssay";
        if (jsonObject.containsKey(ASSAY)) {
            assayName = jsonObject.getString(ASSAY);
        }
        
        String humanReferenceFile = null;
        if (jsonObject.containsKey(HUMAN_REFERENCE)) {
            humanReferenceFile = jsonObject.getString(HUMAN_REFERENCE);
        }
        
        String refSeqGeneModelFile = null;
        if (jsonObject.containsKey(REFSEQ)) {
            refSeqGeneModelFile=jsonObject.getString(REFSEQ);
        }
        
        String gencodeGeneModelFile = null;
        if (jsonObject.containsKey(GENCODE)) {
           gencodeGeneModelFile=jsonObject.getString(GENCODE);
        }
        String outputFile = "./default";
        if (jsonObject.containsKey(OUTPUT)) {
            outputFile = jsonObject.getString(OUTPUT);
        }
        
        String captureIntervalFile = null;
        if (jsonObject.containsKey(CAPTURE)) {
            captureIntervalFile = jsonObject.getString(CAPTURE);
        }
        
        String uniqKmerIntervalFile = null;
        if (jsonObject.containsKey(UNIQUE_KMERS)) {
            uniqKmerIntervalFile = jsonObject.getString(UNIQUE_KMERS);
        }
        
        // TODO: fix anachronism
        String coverageResourceFile = null;
        if (jsonObject.containsKey(BAM)) {
            coverageResourceFile = resourceFile.getAbsolutePath();
        }

        ReportOnAssay reportOnAssay = new ReportOnAssay(outputFile,assayName);  
        reportOnAssay.loadTargetGenelist(targetGenelistFile);
        reportOnAssay.loadGeneModelCollection(refSeqGeneModelFile,gencodeGeneModelFile);
        
        if (humanReferenceFile != null) {
            reportOnAssay.loadReferenceSequence(humanReferenceFile);
        }
        
        reportOnAssay.loadTargetFile(targetIntervalFile);
        if (codingTargetIntervalFile != null) {
            reportOnAssay.loadCodingTargetFile(codingTargetIntervalFile);
        }
        if (captureIntervalFile != null) {
            reportOnAssay.loadCaptureFile(captureIntervalFile);
        }
        if (coverageResourceFile != null) {
            if (jsonObject.containsKey(COVERAGE_THRESHOLD)) {  
                JsonArray thresholdArray = jsonObject.getJsonArray(COVERAGE_THRESHOLD);
                Double [] doubleThresholdArray = new Double [thresholdArray.size()];
                for (int i=0; i<thresholdArray.size(); i++) {
                    doubleThresholdArray[i]= (double) thresholdArray.getInt(i);
                }
                reportOnAssay.setLowCoverageThreshold(doubleThresholdArray);
            }
            reportOnAssay.loadCoverageModel(coverageResourceFile);
        }
        if (uniqKmerIntervalFile != null) {
            reportOnAssay.loadUniqKmerFile(uniqKmerIntervalFile);
        }
        
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
       
       HomologyDecorator homologyDecorator = null;
       if (!uniqKmerIntervals.isEmpty()) {
           homologyDecorator = new HomologyDecorator(uniqKmerIntervals);
       }
       
       CoverageDecorator coverageDecorator = null;
       if (coverageResourceFile != null) {        
           coverageDecorator = new CoverageDecorator(assayCoverageModel);
       }
       
       annotationSummary = new AnnotationSummary();
       annotationSummary.addDecorator(geneModelDecorator);
       
       if (captureDecorator != null) {
           annotationSummary.addDecorator(captureDecorator);
       }
       if (gcPctDecorator != null) {
           annotationSummary.addDecorator(gcPctDecorator);
       }    
       if (homologyDecorator != null) {
           annotationSummary.addDecorator(homologyDecorator);
       }     
       if (coverageDecorator != null) {
           annotationSummary.addDecorator(coverageDecorator);
       }
       if (coverageResourceFile != null) {
           for (int i=0; i<coverageThresholds.length; i++) {
               GapsDecorator gapsDecorator 
                   = new GapsDecorator(assayCoverageModel,coverageThresholds[i]);
            annotationSummary.addDecorator(gapsDecorator);
           }
       }
    }

    /**
     * Print a target-level report.
     * @throws java.io.IOException
     */
    public void report() 
    throws IOException {      
        
        // full reports first: target-level, gene-level, assay-level
        targetReport = new TargetReport(targetReportFile);
        targetReport.setAnnotationSummary(annotationSummary);
        targetReport.reportOn(targetIntervals);
        targetReport.close();
        
        summaryReport = new GeneSummaryReport(summaryReportFile);
        summaryReport.setAnnotationSummary(annotationSummary);      
        for (String gene : targetGenelist) {
            List<Interval> intervals 
                = selectIntervalsForGene(gene,targetIntervals);
     
            summaryReport.reportOn(intervals, gene);
        }
        summaryReport.close();
          
        assayReport = new AssayReport(assayReportFile,assayName);
        assayReport.setAnnotationSummary(annotationSummary);
        assayReport.reportOn(targetIntervals);
        assayReport.close();
               
        /**
        // coding-only reports. have to reset the coverage if we're reporting
        // on coverage
        // TODO: fix this break in the reporting paradigm
        if (coverageResourceFile != null) {
            resetCoverageModel();
            CoverageDecorator codingCoverageDecorator
                = new CoverageDecorator(assayCoverageModel);
            annotationSummary.replaceDecorator(COVERAGE_KEY, codingCoverageDecorator);          
        }
              

        codingTargetReport = new TargetReport(codingTargetReportFile);
        codingTargetReport.setAnnotationSummary(annotationSummary);
        codingTargetReport.reportOn(codingIntervals);
        codingTargetReport.close();
          
        codingSummaryReport = new GeneSummaryReport(codingSummaryReportFile);
        codingSummaryReport.setAnnotationSummary(annotationSummary);
        for (String gene : targetGenelist) {
            List<Interval> intervals
                = selectIntervalsForGene(gene,codingIntervals);
            codingSummaryReport.reportOn(intervals, gene);
        }
        codingSummaryReport.close();    
        
        AssayReport codingAssayReport = new AssayReport(codingAssayReportFile,assayName);
        codingAssayReport.setAnnotationSummary(annotationSummary);
        codingAssayReport.reportOn(codingIntervals);
        codingAssayReport.close();
        **/
    }   

    /**
     * Select intervals from the provided list that match a particular name.
     * @param name
     * @param intervals
     * @return 
     */
    public List<Interval> selectIntervalsForGene(
        String name, 
        List<Interval> intervals) {

        List<Interval> selectedIntervals = new ArrayList<>();
        for (Interval interval : intervals) {
            String intervalName = interval.getName();
            String geneName = intervalName;
            int underscoreIndex = intervalName.indexOf(UNDERSCORE);
            if (underscoreIndex != -1) {
                geneName 
                    = intervalName
                        .substring(0,underscoreIndex);
            }
            
            if (geneName.equals(name)) {
                selectedIntervals.add(interval);
            }
        }

        return selectedIntervals;
    }
}


