/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.coverage;

import com.parabasegenomics.parabasis.reporting.CoverageModelReport;
import com.parabasegenomics.parabasis.util.Reader;
import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;
import htsjdk.samtools.util.Interval;
import htsjdk.samtools.util.OverlapDetector;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

/**
 * The AssayCoverageModel class is used to calculate the coverage mean, std, 
 * and CV for a set of intervals and provide basic tools for interacting with 
 * the coverage statistics.
 * @author evanmauceli
 */
public class AssayCoverageModel {
    
    private static final String BAM = "BAM";
    private static final String TARGETS = "TARGETS";
    
    private final String assayName;
    private final List<IntervalCoverage> intervalCoverages;
    private final Reader utilityReader;
    private final Map<String,Integer> modelIntervalIndexMap;
    private final SamReaderFactory samReaderFactory ;
    
    private Double zscoreThreshold;
    private JsonReader jsonReader; 
    private CoverageModelReport report;  
    private SAMFileHeader samFileHeader;     
    private SamReader samReader;
    
    
    public AssayCoverageModel(String name) {
        assayName=name;
        intervalCoverages=new ArrayList<>();
        utilityReader = new Reader();
        modelIntervalIndexMap = new HashMap<>();
        zscoreThreshold=null;
        jsonReader=null;
           
        samReaderFactory 
            = SamReaderFactory.makeDefault()
                .validationStringency(ValidationStringency.SILENT);
    
    }
    
    /**
     * Set the boundary on the z-score. Outliers get special handling.
     * @param threshold 
     */
    public void setThreshold(Double threshold) {
        zscoreThreshold=threshold;
    }
    
    /**
     * Access the interval coverages, mostly for the intervals themselves.
     * @return 
     */
    public List<IntervalCoverage> getIntervals() {
        return intervalCoverages;
    }
    
    /**
     * Return the IntervalCoverage object for the provided interval.
     * @param interval
     * @return 
     */
    public IntervalCoverage findIntervalCoverage(Interval interval) {
        String keyString = stringifyInterval(interval);
        return intervalCoverages.get(modelIntervalIndexMap.get(keyString));     
    }
    
    /**
     * Is the provided coverage an outlier given the current state of the model?
     * @param intervalCoverage The coverage value to compare to the model.
     * @return Returns true if the provided coverage is outside of the 
     * current model distribution, false otherwise. Returns false if the 
     * provided interval is not held by the model.
     * 
     */
    public boolean isOutlier(IntervalCoverage intervalCoverage) {
       Double zscore = getZscore(intervalCoverage);
       if (zscore == null) {
           return false;
       }
       return (Math.abs(zscore)>zscoreThreshold);
    }
    
    /**
     * Returns the z-score of the provided interval coverage given the current
     * state of the model. Returns null if the interval is not held by the model.
     * 
     * A naive interpretation: 
     *  negative zscore = deletion 
     *  positive zscore = gain
     * 
     * @param intervalCoverage
     * @return 
     */
    public Double getZscore(IntervalCoverage intervalCoverage) {
        Integer index = hasIntervalAt(intervalCoverage);
        if (index != null) {
            IntervalCoverage coverageModel = intervalCoverages.get(index);
            Double mean = coverageModel.getMean();
            Double std = coverageModel.getStandardDeviation();
            Double zscore 
                = (intervalCoverage.getMean()-mean)/std;
            return (zscore);
        }
        return null;
    }
    
    /**
     * Does the current model show signs of wildly varying coverage for any
     * of the held intervals?
     * @return Returns a list of outliers.  If none, returns an empty list.
     */
    public List<IntervalCoverage> findOutliers() {
        List<IntervalCoverage> outliers = new ArrayList<>();
        for (IntervalCoverage intervalCoverage : intervalCoverages) {
            if (isOutlier(intervalCoverage)) {
                outliers.add(intervalCoverage);
            }
        }
        return outliers;
    }
    
    /**
     * 
     * @return Returns the name of the assay for this model.
     */
    public String getAssayName() {
        return assayName;
    }
      
    public void readFromFile(File file) 
    throws IOException {
        report = new CoverageModelReport(file,null);
        report.readModel();
        report.close();
    }
    
    public void writeToFile(File file) 
    throws IOException {
        report = new CoverageModelReport(file,assayName);
        report.writeModel(intervalCoverages);
        report.close();
    }
    
    private String stringifyInterval(Interval interval) {
        StringBuilder builder = new StringBuilder();
        builder.append(interval.getContig());
        builder.append(":");
        builder.append(interval.getStart());
        builder.append("-");
        builder.append(interval.getEnd());
        return (builder.toString());
    }
    
    /**
     * Update the held model with the provided IntervalCoverage object. If
     * the provided interval is not in the model, add it in.
     * 
     * TODO: check for outlier coverage before updating the model.
     * 
     * @param intervalCoverage 
     */
    public void update(IntervalCoverage intervalCoverage) {
        Integer index = hasIntervalAt(intervalCoverage);
        if (index != null) {
            intervalCoverages.get(index).update(intervalCoverage.getMean());
        } else {
            intervalCoverages.add(intervalCoverage);
            modelIntervalIndexMap.put(
                stringifyInterval(intervalCoverage.getInterval()), intervalCoverages.size()-1);
            intervalCoverages.get(intervalCoverages.size()-1).update(intervalCoverage.getMean());
        }
    }
    
    /**
     * Returns the average coverage for the given interval, or null
     * if the given interval is not in the model.
     * @param interval
     * @return 
     */
    public Double getMeanCoverageAt(Interval interval) {
        String keyString = stringifyInterval(interval);
        Integer index = modelIntervalIndexMap.get(keyString); 
        if (index == null) {
            return null;
        } else {
            return intervalCoverages.get(index).getMean();
        }   
    }
    
    /**
     * Returns the average count of low coverage bases for the given interval, 
     * or null if the given interval is not in the model.
     * @param interval
     * @param threshold Return if count is less than this number.
     * @return 
     */
    public Double getLowCoverageCountAt(Interval interval, Double threshold) {
        String keyString = stringifyInterval(interval);
        Integer index = modelIntervalIndexMap.get(keyString); 
        if (index == null) {
            return null;
        } else {
            return intervalCoverages
                .get(index)
                .getLowCoverageCount(threshold);
            
        }   
    }
    
    /**
     * If the provided IntervalCoverage object is in the list held by the model,
     * returns the index into the list or null if not found.
     * @param intervalCoverageToFind
     * @return 
     */
    private Integer hasIntervalAt(IntervalCoverage intervalCoverageToFind) {
        Interval interval = intervalCoverageToFind.getInterval();
        String keyString = stringifyInterval(interval);
        return modelIntervalIndexMap.get(keyString); 
    }
    
    /**
     * Initialize the current model from a JSON file containing a list of BAM
     * files (with the json key "BAM") and a file containing a list of genomic 
     * targets (with the json key "TARGETS"
     * @param file Input json file defining the necessary resources.
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public void initializeFromResourceFile(File file) 
    throws FileNotFoundException, IOException {
       
        jsonReader = Json.createReader(new FileReader(file.getAbsolutePath()));
        JsonObject jsonObject = jsonReader.readObject();
             
        JsonArray bamArray = jsonObject.getJsonArray(BAM);
              
        String targetsFilepath=null;
        if (jsonObject.containsKey(TARGETS)) {
           targetsFilepath = jsonObject.getString(TARGETS);
        }
        List<Interval> intervals = utilityReader.readBEDFile(targetsFilepath);
        OverlapDetector targetOverlapDetector = new OverlapDetector<>(0,0);
        targetOverlapDetector.addAll(intervals,intervals);
        
        // create a new IntervalCoverage object for each target in the assay
        // set the mapping of interval to index in list
        Integer index=0;
        for (Interval interval : intervals) {
            intervalCoverages.add(new IntervalCoverage(interval));
            modelIntervalIndexMap.put(stringifyInterval(interval), index);
            index++;
        }

        for (index=0; index<bamArray.size(); index++) {    
            String bamFilepath = bamArray.getString(index);
            File bamFile = new File(bamFilepath);
            parseBam(bamFile,targetOverlapDetector);
        }
    }
    
    /**
     * Initial the current model from a json resource file and the provided 
     * list of intervals.  This overrides the intervals specified in the 
     * json file.
     * @param file
     * @param intervals
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public void initializeFromResourceFileAndIntervals(
        File file,
        List<Interval> intervals) 
    throws FileNotFoundException, IOException {
       
        intervalCoverages.clear();
        modelIntervalIndexMap.clear();
        
        jsonReader = Json.createReader(new FileReader(file.getAbsolutePath()));
        JsonObject jsonObject = jsonReader.readObject();
             
        JsonArray bamArray = jsonObject.getJsonArray(BAM);
              
        String targetsFilepath=null;
        if (jsonObject.containsKey(TARGETS)) {
           targetsFilepath = jsonObject.getString(TARGETS);
        }
        OverlapDetector targetOverlapDetector = new OverlapDetector<>(0,0);
        targetOverlapDetector.addAll(intervals,intervals);
        
        // create a new IntervalCoverage object for each target in the assay
        // set the mapping of interval to index in list
        Integer index=0;
        for (Interval interval : intervals) {
            intervalCoverages.add(new IntervalCoverage(interval));
            modelIntervalIndexMap.put(stringifyInterval(interval), index);
            index++;
        }

        for (index=0; index<bamArray.size(); index++) {    
            String bamFilepath = bamArray.getString(index);
            File bamFile = new File(bamFilepath);
            parseBam(bamFile,targetOverlapDetector);
        }
    }
    
    /**
     * Read through a bam file record by record, updating the appropriate IntervalCoverage 
     * objects as we go.  This method ignores the following reads:
     *      Fails vendor quality 
     *      Not primary alignment
     *      Unmapped read
     *      Duplicate read
     * @param bamFile Bam file to parse.
     * @param overlapDetector
     * @throws java.io.IOException
     */
    public void parseBam(File bamFile,OverlapDetector overlapDetector) 
    throws IOException {
        samReader = samReaderFactory.open(bamFile);

        SAMRecordIterator samRecordIterator = samReader.iterator();
        SAMRecord samRecord;
        while (samRecordIterator.hasNext()) {
            samRecord = samRecordIterator.next();
            if (samRecord.getReadFailsVendorQualityCheckFlag()) {
                continue;
            }
            if (samRecord.getNotPrimaryAlignmentFlag()) {
                continue;
            } 
            if (samRecord.getReadUnmappedFlag()) {
                continue;
            }
            if (samRecord.getDuplicateReadFlag()) {
                continue;
            }

            Interval recordInterval 
                = new Interval(
                    samRecord.getReferenceName(),
                    samRecord.getAlignmentStart(),
                    samRecord.getAlignmentEnd());

            Collection<Interval> overlappingTargets 
                = overlapDetector.getOverlaps(recordInterval);

            for (Interval overlappingTarget : overlappingTargets) {
                String overlappingTargetAsString 
                    = stringifyInterval(overlappingTarget);
                if (!modelIntervalIndexMap
                    .containsKey(overlappingTargetAsString)) {
                    throw new IllegalArgumentException(
                        "Could not find interval in model: "
                        + overlappingTargetAsString);
                }
                int indexIntoIntervalCoverages 
                    = modelIntervalIndexMap.get(stringifyInterval(overlappingTarget));

                final IntervalCoverage intervalCoverage 
                    = intervalCoverages.get(indexIntoIntervalCoverages);
                for (int position = samRecord.getAlignmentStart(); 
                         position <= samRecord.getAlignmentEnd();
                        ++position) {
                    // the -1 is to convert from htsjdk intervals to ours
                    intervalCoverage.incrementCoverageCount(position-1);
                }
            }              
        }        
        samReader.close();
        // a small hack to fit the new way of interacting with an IntervalCoverage
        // (per base) with the old way (per target)
        for (IntervalCoverage intervalCoverage : intervalCoverages) {
            intervalCoverage.summarize();
        }
    }
     
}
   

   
