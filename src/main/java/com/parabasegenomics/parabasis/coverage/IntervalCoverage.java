/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.coverage;

import htsjdk.samtools.util.Interval;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to hold the coverage at each base in an Interval for a single sample.
 * stability assessments.
 * @author evanmauceli
 */
public class IntervalCoverage {
    
    private final Interval interval;
    private final double [] coverageArray;
    private final int offset;
    
    /**
     * Construct with an Interval.
     * @param val 
     */
    public IntervalCoverage(Interval val) {
        interval=val;
        coverageArray = new double [interval.length()-1];
        offset = interval.getStart();
    }
    
    /**
     * Return the coverage at the specified position or -1 if
     * the position is outside of the held interval.
     * @param position
     * @return 
     */
    public double getCoverageAt(int position) {
        int index=position-offset;
        if (index<0 || index>=coverageArray.length) {
            return -1.;
        }
        return (double) coverageArray[index];
    }
    
    
    /**
     * Returns the total read coverage for this interval.
     * @return 
     */
    public double getAverageCoverage() {
        double coverageCount=0.0;
        for (int index=0; index<coverageArray.length; ++index) {
            coverageCount += coverageArray[index];
        }
        return coverageCount;
    }
    
    /**
     * Returns the Interval held by this object.
     * @return 
     */
    public Interval getInterval() {
        return interval;
    }
    
    /**
     * Returns the minimum coverage value in the interval.
     * @return 
     */
    public double getMin() {
        double min=1000000.;
        for (int index=0; index<coverageArray.length; ++index) {
            if (coverageArray[index]<min) {
                min=coverageArray[index];
            }
        }
        return min;
    }
    
    /**
     * Returns the maximum coverage value in the interval.
     * @return 
     */
    public double getMax() {
        double max=-1.;
        for (int index=0; index<coverageArray.length; ++index) {
            if (coverageArray[index]>max) {
                max=coverageArray[index];
            }
        }
        return max;
    }
    
    /**
     * Increment the coverage count at the specified position. If the specified
     * position is outside of this interval, do nothing.
     * @param position 
     */
    public void incrementCoverageCount(int position) {
        int index = position-offset;
        if (index<0 || index>=coverageArray.length) {
            return;
        }
        ++coverageArray[index];
    }
    

    /**
     * Returns the current count of low coverage bases for this interval.
     * @param threshold The threshold defining "low coverage".  The number of
     * low coverage bases in the interval is the count of bases that have coverage 
     * less than the threshold.
     * @return 
     */
    public double getLowCoverageCount(Double threshold) {
        int lowCount = 0;
        for (int index=0;index<coverageArray.length; ++index) {
            if ((double) coverageArray[index] < threshold) {
                //lowCount+=coverageArray[index];
                ++lowCount;
            }
        }
        return (double) lowCount;
    }
    
    /**
     * Returns a list of intervals within this interval where the coverage is
     * below the provided threshold.
     * @param threshold
     * @return 
     */
    public List<Interval> getLowCoverageIntervals(Double threshold) {
        List<Interval> lowCoverageIntervalList = new ArrayList<>();
        int start = 0;
        int end = 0;
        boolean inGap=false;
        for (int index=0; index<coverageArray.length; ++index) {
            if ((double) coverageArray[index] < threshold) {
                if (!inGap) {
                    start = index;
                    end = start+1;
                    inGap=true;
                } else {
                    ++end;
                }
            } else {
                if (inGap) {
                    Interval gapInterval 
                        = new Interval(
                            interval.getContig(),
                            start+offset,
                            end+offset);
                    lowCoverageIntervalList.add(gapInterval);
                    start=0;
                    end=0;
                }
                inGap=false;   
            }  
        }
        if (inGap) {
            Interval gapInterval 
                = new Interval(
                    interval.getContig(),
                    start+offset,
                    end+offset);
            lowCoverageIntervalList.add(gapInterval);
         }   
           
        return lowCoverageIntervalList;
    }
    
}
