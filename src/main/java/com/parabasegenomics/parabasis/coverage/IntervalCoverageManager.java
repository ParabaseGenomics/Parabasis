/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.coverage;

import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;
import htsjdk.samtools.util.Interval;
import htsjdk.samtools.util.OverlapDetector;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to manage a list of IntervalCoverage objects.  Used to manage coverage
 * of many intervals making up an assay (or test). 
 * @author evanmauceli
 */
public class IntervalCoverageManager {
    
    private final String assayName;
    private final List<IntervalCoverage> intervalCoverages;
    private final Map<String,Integer> modelIntervalIndexMap;
    private final SamReaderFactory samReaderFactory ;
    
    private OverlapDetector targetOverlapDetector;
    private SAMFileHeader samFileHeader;     
    private SamReader samReader;
    
    
    public IntervalCoverageManager(String assay, List<Interval> intervals) {
        assayName=assay;
        intervalCoverages=new ArrayList<>();
        modelIntervalIndexMap = new HashMap<>();
        samReaderFactory 
            = SamReaderFactory.makeDefault()
                .validationStringency(ValidationStringency.SILENT);
        
        targetOverlapDetector = new OverlapDetector<>(0,0);
        targetOverlapDetector.addAll(intervals,intervals);
        
        // create a new IntervalCoverage object for each target in the assay
        // set the mapping of interval to index in list
        Integer index=0;
        for (Interval interval : intervals) {
            intervalCoverages.add(new IntervalCoverage(interval));
            modelIntervalIndexMap.put(stringifyInterval(interval), index);
            index++;
        }
        
    }
    
    /**
     * Reset the manager with the provided intervals.
     * @param intervals
     */
    public void reset(List<Interval> intervals) {
        intervalCoverages.clear();
        modelIntervalIndexMap.clear();
        targetOverlapDetector = new OverlapDetector<>(0,0);
        
        targetOverlapDetector.addAll(intervals,intervals);
        Integer index=0;
        for (Interval interval : intervals) {
            intervalCoverages.add(new IntervalCoverage(interval));
            modelIntervalIndexMap.put(stringifyInterval(interval), index);
            index++;
        }
    }
    
    /**
     * Access the held interval coverage objects.
     * @return 
     */
    public final List<IntervalCoverage> getIntervals() {
        return intervalCoverages;
    }
    
    /**
     * Returns the coverage of the provided interval.
     * @param interval
     * @return 
     */
  
    public Double getCoverage(Interval interval) {
        String keyString = stringifyInterval(interval);
        Integer index = modelIntervalIndexMap.get(keyString); 
        if (index == null) {
            return null;
        } else {
            return intervalCoverages.get(index).getAverageCoverage();
        }      
    }
    
    
    /**
     * Returns the coverage at the specified position.
     * @param interval
     * @param position
     * @return 
     */
    public Double getCoverageAt(Interval interval, String position) {
        String keyString = stringifyInterval(interval);
        Integer index = modelIntervalIndexMap.get(keyString); 
        if (index == null) {
            return null;
        } else {           
            int chrCoordinate = parseStringPosition(position);
            return intervalCoverages
                .get(index)
                .getCoverageAt(chrCoordinate);
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
     * Read through a bam file record by record, updating the appropriate IntervalCoverage 
     * objects as we go.  This method ignores the following reads:
     *      Fails vendor quality 
     *      Not primary alignment
     *      Unmapped read
     *      Duplicate read
     * @param bamFile Bam file to parse.
     * @throws java.io.IOException
     */
    public void parseBam(File bamFile) 
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
                = targetOverlapDetector.getOverlaps(recordInterval);

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
    }
    
    /**
     * Returns the position in a chromosome as an int from the provided string.
     * @param positionString
     * @return 
     */
    private int parseStringPosition(String positionString) {
        return ( Integer
            .parseInt(
                positionString
                    .substring(positionString.indexOf(":")+1)) );
    }
    
    /**
     * Produce a String from the given Interval object.  The String needs to be
     * unique avoid collisions when searching for held Intervals.
     * @param interval
     * @return Returns a unique (for this set of Intervals) String identifying
     * this Interval.
     */
    private String stringifyInterval(Interval interval) {
        StringBuilder builder = new StringBuilder();
        builder.append(interval.getContig());
        builder.append(":");
        builder.append(interval.getStart());
        builder.append("-");
        builder.append(interval.getEnd());
        return (builder.toString());
    }
    
}
