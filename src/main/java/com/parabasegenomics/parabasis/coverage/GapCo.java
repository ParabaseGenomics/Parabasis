/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.coverage;

import static com.parabasegenomics.parabasis.decorators.AnnotationKeys.GENE_KEY;
import static com.parabasegenomics.parabasis.decorators.FormatPatterns.percentPattern;
import com.parabasegenomics.parabasis.decorators.GeneModelDecorator;
import com.parabasegenomics.parabasis.gene.GeneModelCollection;
import com.parabasegenomics.parabasis.target.AnnotatedInterval;
import com.parabasegenomics.parabasis.util.Reader;
import com.parabasegenomics.parabasis.vcf.VcfCohort;
import com.parabasegenomics.parabasis.vcf.VcfCohortVariant;
import com.parabasegenomics.parabasis.vcf.VcfLoader;
import htsjdk.samtools.util.Interval;
import htsjdk.samtools.util.OverlapDetector;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

/**
 *
 * @author evanmauceli
 */
public class GapCo {

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
    
    private static DecimalFormat decimalFormat;
    private final static String formatPattern = percentPattern;
    
    public GapCo(String fileToWrite, String name) {
         
    }
    /**
     * @param args the command line arguments
     * @throws java.io.FileNotFoundException
     */
    public static void main(String[] args) 
    throws FileNotFoundException, IOException {        
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
        
        decimalFormat = new DecimalFormat(formatPattern);
        
        String vcfFile = null;
        if (jsonObject.containsKey(VCF)) {
            vcfFile = jsonObject.getString(VCF);
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
        
        Double lowCoverageThreshold=0.;
        if (jsonObject.containsKey(COVERAGE_THRESHOLD)) {          
                lowCoverageThreshold = (double) jsonObject.getInt(COVERAGE_THRESHOLD);
        }

        Reader utilityReader = new Reader();
        
        GeneModelCollection geneModelCollection = new GeneModelCollection();
        geneModelCollection.readGeneModelCollection(refSeqGeneModelFile);
        geneModelCollection.addGeneModelCollection(gencodeGeneModelFile);
        geneModelCollection.aggregateTranscriptsByGenes();
        Set<String> geneNamesInTest = utilityReader.readHashSet(targetGenelistFile);
        GeneModelDecorator geneModelDecorator 
            = new GeneModelDecorator(geneModelCollection,geneNamesInTest);
        
        
        VcfLoader vcfLoader = new VcfLoader(vcfFile);
        vcfLoader.loadFile();
        System.out.println("load vcf: "+vcfLoader.getCohort().getVariantCount());
        final VcfCohort vcfCohort = vcfLoader.getCohort();
        
        
        List<Interval> intervals =utilityReader.readBEDFile(targetIntervalFile);        
        AssayCoverageModel lowCoverageModel = new AssayCoverageModel(assayName+"_lc");
        
        OverlapDetector targetOverlapDetector = new OverlapDetector<>(0,0);
        targetOverlapDetector.addAll(intervals,intervals);
        
        JsonArray bamArray = jsonObject.getJsonArray(BAM);
        for (int index=0; index<bamArray.size(); index++) {    
            String bamFilepath = bamArray.getString(index);
            File bamFile = new File(bamFilepath);
            
            String thisResourceFile 
                = bamFilepath.substring(0,bamFilepath.indexOf(".bam")-3) 
                + ".resources.json";
            AssayCoverageModel assayCoverageModel = new AssayCoverageModel(assayName);
            assayCoverageModel.initializeFromResourceFileAndIntervals(new File(thisResourceFile),intervals);
            
            int variantCount = vcfCohort.getVariantCount();
            for (int i=0; i<variantCount; i++) {
                final VcfCohortVariant variant = vcfCohort.getCohortVariant(i);
                String contig = variant.getChromosome();
                int start = variant.getPosition();
                int end = variant.getEndPosition();
                
                Interval variantInterval = new Interval(contig,start,end);
                Collection<Interval> overlappingTargets 
                    = targetOverlapDetector.getOverlaps(variantInterval);
                Interval [] thisIntervalArray = overlappingTargets.toArray(new Interval [1]);
                final IntervalCoverage intervalCoverage 
                    = assayCoverageModel.findIntervalCoverage(thisIntervalArray[0]);
                System.out.println("Variant:\t"
                    + bamFile.getName() + "\t"
                    + variant.getChromosome()
                    +"\t"
                    +variant.getPosition()
                    +"\t"
                    +variant.getVariantId()
                    +"\t"
                    +variant.getReferenceAllele()
                    +"\t"
                    +variant.getAlternateAllele()
                    +"\t"
                    +variant.getQualityField()
                    +"\t"
                    + variant.getFilterField()
                    +"\t"
                    +variant.getInfoField()
                    +"\t"
                    + intervalCoverage.coverageAt(variant.getPosition()));
                
                
            }
            
            
            List<IntervalCoverage> modelIntervals = assayCoverageModel.getIntervals();
            for (IntervalCoverage intervalCoverage : modelIntervals) {

                Interval interval = intervalCoverage.getInterval();
                
                Double lowCoverageCount = intervalCoverage.getLowCoverageCount(lowCoverageThreshold);
                double lowCoveragePct = 100.0 * lowCoverageCount/(double) (interval.length()-1);           
                
                Mean m = new Mean();

                m.increment(lowCoveragePct);
                StandardDeviation d = new StandardDeviation();
                d.increment(m.getResult());
                IntervalCoverage ic 
                    = new IntervalCoverage(
                        interval,
                        0,
                        m,
                        d,
                        0.);

                lowCoverageModel.update(ic);
                
            }
        }
        
        int i=0;
        List<IntervalCoverage> coverages = lowCoverageModel.getIntervals();
        for (IntervalCoverage intervalCoverage : coverages) {

            Interval interval = intervalCoverage.getInterval();
            int l = interval.length()-1;

            AnnotatedInterval ai = new AnnotatedInterval(interval);
            geneModelDecorator.annotate(ai);

            List<Interval> intervalCoverageLow 
                = intervalCoverage.getLowCoverageIntervals(lowCoverageThreshold);
            System.out.print(
                ai.getInterval().getContig()
                +"\t"
                +ai.getInterval().getStart()
                +"\t"
                +ai.getInterval().getEnd()
                +"\t"
                + l
                +"\t"
                +decimalFormat
                    .format(lowCoverageModel.getIntervals().get(i).getMean())
                +"\t"
                +decimalFormat
                    .format(lowCoverageModel.getIntervals().get(i).getStandardDeviation())
                +"\t"
                +decimalFormat
                    .format(lowCoverageModel.getIntervals().get(i).getCoefficientOfVariation())
                +"\t");
            for (Interval lci : intervalCoverageLow) {
                System.out.print(lci.getContig()+":"+lci.getStart()+"-"+lci.getEnd()+";");
            }
            System.out.print(
                "\t"
                +ai.getAnnotation(GENE_KEY));         
            System.out.println("");
           ++i;
        }
    }
    
    
}
            
        

