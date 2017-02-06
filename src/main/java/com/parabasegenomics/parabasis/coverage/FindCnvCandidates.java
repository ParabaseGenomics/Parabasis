/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.coverage;

import static com.parabasegenomics.parabasis.decorators.AnnotationKeys.GENE_KEY;
import static com.parabasegenomics.parabasis.decorators.AnnotationKeys.HOM_KEY;
import com.parabasegenomics.parabasis.util.Reader;
import com.parabasegenomics.parabasis.vcf.VcfChrXCounter;
import htsjdk.samtools.util.Interval;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;


/**
 *
 * @author evanmauceli
 */
public class FindCnvCandidates {
 
    private final static String TAB = "\t";
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
    private static final String VCF = "VCF";
    
    /**
     * @param args the command line arguments
     * @throws java.io.FileNotFoundException
     */
    public static void main(String[] args) 
    throws FileNotFoundException, IOException {
        File modelResourceFile = new File(args[0]);
        File testResourceFile = new File(args[1]);
        Double threshold = new Double(args[2]);
        
        JsonReader reader 
            = Json.createReader(new FileReader(modelResourceFile.getAbsolutePath()));
        JsonObject jsonObject = reader.readObject();
           
        String targetIntervalFile = null;
        if (jsonObject.containsKey(TARGETS)) {
           targetIntervalFile = jsonObject.getString(TARGETS);
        } else {
            throw new IOException(
                "Must specify a targets file in the json resource");
        }
       
        JsonArray bamArray = jsonObject.getJsonArray(BAM);
        JsonArray vcfArray = jsonObject.getJsonArray(VCF);
        
        String assayName = "genericAssay";
        if (jsonObject.containsKey(ASSAY)) {
            assayName = jsonObject.getString(ASSAY);
        }

        JsonReader testReader 
            = Json.createReader(new FileReader(testResourceFile.getAbsolutePath()));
        JsonObject testJsonObject = testReader.readObject();
           
        String vcfFile;
        VcfChrXCounter vcfChrXCounter = null;
        if (testJsonObject.containsKey(VCF)) {
            vcfFile = testJsonObject.getJsonArray(VCF).getString(0);
            vcfChrXCounter = new VcfChrXCounter(vcfFile);
        } else {
            throw new IOException(
                "Must specify a vcf file for this sample in the json resource");
        }
   
        Reader utilityReader = new Reader();
        List<Interval> targetIntervals
            = utilityReader.readBEDFile(targetIntervalFile);
        
        CoverageModel coverageModel = new CoverageModel();
        File coverageModelFile 
            = new File(modelResourceFile.getAbsolutePath()+".model");
        if (coverageModelFile.exists()) {
            coverageModel.read(coverageModelFile);
        } else {
            coverageModel.build(targetIntervals,bamArray,vcfArray);
            coverageModel.write(coverageModelFile);
        }
       
       // load the test bam
       IntervalCoverageManager testCoverageManager
           = new IntervalCoverageManager(assayName,targetIntervals);
       
      
        JsonArray testBamArray = testJsonObject.getJsonArray(BAM);
        String testBamFilepath = testBamArray.getString(0);
        testCoverageManager.parseBam(new File(testBamFilepath));
        
        Integer readCount = testCoverageManager.getReadCount();
        Double testWeight =  1000000000.0/(readCount*75); 
        if (vcfChrXCounter.isMale())  {
            testWeight *=2 ;
        }       
        coverageModel.setThreshold(threshold);
        
        
        Integer index=0;
        for (Interval interval : targetIntervals) {
            String contig = interval.getContig();
            Integer startPos = interval.getStart();
            Integer endPos = interval.getEnd();

            for (Integer pos=startPos; pos<endPos; pos++) {
                String positionString 
                    = contig + ":" + pos.toString();

                Double locusCoverage 
                    = testWeight*testCoverageManager
                        .getCoverageAt(interval, positionString);

                Integer nextPos = pos+1;
                //if (coverageModel
                //    .isOutlier(locusCoverage,index,positionString)) {
                  if (true)  {
                    System.err.println(
                    contig
                    +"\t"
                    +pos.toString()
                    +"\t"
                    + nextPos.toString()
                    +"\t"
                    +locusCoverage.toString()
                    +"\t"
                    +coverageModel.getZscore(locusCoverage, index));

                }
                index++;
            }
        }
    }
 
   

    
    
}
