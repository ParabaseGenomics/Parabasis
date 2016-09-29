/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.target;

import com.parabasegenomics.parabasis.coverage.AssayCoverageModel;
import com.parabasegenomics.parabasis.coverage.IntervalCoverage;
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
import com.parabasegenomics.parabasis.reporting.AssayReport;
import com.parabasegenomics.parabasis.reporting.GapsTableReport;
import com.parabasegenomics.parabasis.reporting.GeneSummaryReport;
import com.parabasegenomics.parabasis.reporting.TargetReport;
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
public class ReportOnGaps {

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

    private Set<String> targetGenelist;
    
    private final GeneModelCollection geneModelCollection;

    private final List<AnnotatedInterval> annotatedIntervals;
    private List<Interval> targetIntervals;

    private final File gapReportFile;

    private GapsTableReport gapReport;

    private AnnotationSummary annotationSummary;
    private AssayCoverageModel assayCoverageModel;
    private final int splicingDistance;
    private final String assayName;
    private File coverageResourceFile;
    private Double [] coverageThresholds;
    
    
    public ReportOnGaps(String fileToWrite,String name) {
        geneModelCollection = new GeneModelCollection();
        utilityReader = new Reader();
        targetIntervals = new ArrayList<>();
        targetGenelist = new HashSet<>();
       
        annotatedIntervals = new ArrayList<>();
       
        gapReportFile = new File(fileToWrite + ".gap.report.txt");
        
       
        assayCoverageModel = null;     
        splicingDistance = 10;
        assayName = name;      
        decimalFormat = new DecimalFormat(formatPattern);
        coverageResourceFile  = null;
        coverageThresholds=null;    
    }
    
    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
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
        
        
        // TODO: fix anachronism
        String coverageResourceFile = null;
        if (jsonObject.containsKey(BAM)) {
            coverageResourceFile = resourceFile.getAbsolutePath();
        }

        ReportOnGaps reportOnGaps = new ReportOnGaps(outputFile,assayName);  
        reportOnGaps.loadTargetGenelist(targetGenelistFile);
        reportOnGaps.loadGeneModelCollection(refSeqGeneModelFile,gencodeGeneModelFile);        
        reportOnGaps.loadTargetFile(targetIntervalFile);

        if (coverageResourceFile != null) {
            if (jsonObject.containsKey(COVERAGE_THRESHOLD)) {  
                JsonArray thresholdArray = jsonObject.getJsonArray(COVERAGE_THRESHOLD);
                Double [] doubleThresholdArray = new Double [thresholdArray.size()];
                for (int i=0; i<thresholdArray.size(); i++) {
                    doubleThresholdArray[i]= (double) thresholdArray.getInt(i);
                }
                reportOnGaps.setLowCoverageThreshold(doubleThresholdArray);
            }
            reportOnGaps.loadCoverageModel(coverageResourceFile);
        }

        
        reportOnGaps.decorateTargets();    
        reportOnGaps.report();
       
    }
    
    /**
     * For each target interval, annotate with gene location, gc content, etc.
     * @throws IOException 
     */
    public void decorateTargets() 
    throws IOException {
        
       GeneModelDecorator geneModelDecorator 
           = new GeneModelDecorator(geneModelCollection,targetGenelist);
        
    
       CoverageDecorator coverageDecorator = null;
       if (coverageResourceFile != null) {        
           coverageDecorator = new CoverageDecorator(assayCoverageModel);
       }
       
       annotationSummary = new AnnotationSummary();
       annotationSummary.addDecorator(geneModelDecorator);
       
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
       
        gapReport = new GapsTableReport(gapReportFile,coverageThresholds[0]);
        gapReport.setAnnotationSummary(annotationSummary);
        List<IntervalCoverage> intervalCoverages 
            = new ArrayList<>();
        for (Interval targetInterval : targetIntervals) {
            intervalCoverages
                .add(assayCoverageModel
                    .findIntervalCoverage(targetInterval));
        }    
        
        gapReport.reportOn(intervalCoverages);
        gapReport.close();
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
    
    public void loadTargetGenelist(String targetGenelistFile) 
    throws IOException {
        targetGenelist = utilityReader.readHashSet(targetGenelistFile);
    }
    
    public void setLowCoverageThreshold(Double [] thresholdArray) {
        coverageThresholds=thresholdArray;
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
}
