/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.coverage;

import htsjdk.samtools.util.Interval;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

/**
 * The IntervalCoverage class keeps coverage statistics on intervals in an assay
 * and provides them for use in CNV detection and sequencing 
 * stability assessments.
 * @author evanmauceli
 */
public class IntervalCoverage {
    
    private final Interval interval;
    private int count;
    private final Mean mean;
    private final StandardDeviation standardDeviation;
    private double coefficientOfVariation;
    private final double [] coverageArray;
    private final int offset;
    private final Mean [] meanArray;
    private final StandardDeviation [] deviationArray;
    
    /**
     * Construct with an Interval.
     * @param val 
     */
    public IntervalCoverage(Interval val) {
        interval=val;
        count=0;
        standardDeviation = new StandardDeviation();
        mean = new Mean();       
        coefficientOfVariation=0.0;
        coverageArray = new double [interval.length()-1];
        offset = interval.getStart();
        
        meanArray = new Mean [interval.length()-1];
        deviationArray = new StandardDeviation [interval.length()-1];
    }
    
    /**
     * Construct from given quantities.
     * @param val Interval
     * @param c count (int)
     * @param m Mean coverage
     * @param s StandardDeviation
     * @param cv coefficient of variation (double)
     */
    public IntervalCoverage(
        Interval val, 
        int c, 
        Mean m, 
        StandardDeviation s,
        double cv) {       
            interval=val;
            count=c;
            mean=m;
            standardDeviation=s;
            coefficientOfVariation=cv;  
            coverageArray = new double [interval.length()-1];
            offset = interval.getStart();
            
            meanArray = new Mean [interval.length()-1];
            deviationArray = new StandardDeviation [interval.length()-1];
    }

    /**
     * Calculate the statistics from the held coverage array.
     */
    public void summarize() {
        mean.increment(coverage());
        standardDeviation.increment(mean.getResult());
        ++count;
        coefficientOfVariation
                = (1+(1/(4*count)))
                * (standardDeviation.getResult()/mean.getResult());
        
        for (int index=0; index<coverageArray.length; ++index) {
            int position = index+offset;
            this.updateMeanAt(position,coverageAt(position));
            this.updateStdAt(position,coverageAt(position));
        }
        
    }
    
    /**
     * Returns the number of data points that are currently held by the object.
     * @return 
     */
    public int count() {
        return count;
    }
    
    /**
     * Return the coverage at the specified position or -1 if
     * the position is outside of the held interval.
     * @param position
     * @return 
     */
    public double coverageAt(int position) {
        int index=position-offset;
        if (index<0 || index>=coverageArray.length) {
            return -1.;
        }
        return (double) coverageArray[index]/count;
    }
    
    
    /**
     * Returns the total read coverage for this interval.
     * @return 
     */
    private double coverage() {
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
     * Returns the current mean coverage for this interval.
     * @return 
     */
    public double getMean() {
        return mean.getResult();
    }
    
    /**
     * Returns the current standard deviation for this interval.
     * @return 
     */
    public double getStandardDeviation() {
        return standardDeviation.getResult();
    }
    
    /**
     * Returns the unbiased coefficient of variation for this interval.
     * @return 
     */
    public double getCoefficientOfVariation() {
        return coefficientOfVariation;
    }
    
     /**
     * Updates the current statistics with new data.
     * @param coverage
     * @return Returns the current IntervalCoverage object.
     */
    public IntervalCoverage update(Double coverage) {
        if (count == 0) {
            mean.increment(coverage);
            standardDeviation.increment(coverage);
            coefficientOfVariation = 0.0;
            count=1;
        } else {
            count++;
            mean.increment(coverage);
            standardDeviation.increment(coverage);
            coefficientOfVariation
                = (1+(1/(4*count)))
                * (standardDeviation.getResult()/mean.getResult());
        }       
        return this;    
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
     * Update the mean coverage at the given position.  If the specified position
     * is outside of this interval, do nothing.
     * @param position The position in genomic coordinates.
     * @param coverage The coverage to add.
     */
    public void updateMeanAt(int position, double coverage) {
        int index = position-offset;
        if (index<0 || index>=meanArray.length) {
            return;
        }    
        
        meanArray[index].increment(coverage);
    }
    
    /**
     * Update the standard deviation of coverage at the given position.  
     * If the specified position is outside of this interval, do nothing.
     * @param position The position in genomic coordinates.
     * @param coverage The coverage to add.
     */
    public void updateStdAt(int position, double coverage) {
        int index = position-offset;
        if (index<0 || index>=deviationArray.length) {
            return;
        }    
        
        deviationArray[index].increment(coverage);
    }
    
    
    /**
     * Returns the mean coverage at the given position, or null if the given
     * position is outside of this interval.
     * @param position The position in genomic coordinates.
     * @return 
     */
    public Double getMeanAt(int position) {
        int index = position-offset;
        if (index<0 || index>=meanArray.length) {
            return null;
        }    
        return meanArray[index].getResult();
    }
    
     /**
     * Returns the standard deviation of coverage at the given position, or null if the given
     * position is outside of this interval.
     * @param position The position in genomic coordinates.
     * @return 
     */
    public Double getStdAt(int position) {
        int index = position-offset;
        if (index<0 || index>=meanArray.length) {
            return null;
        }    
        return deviationArray[index].getResult();
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
            if ((double) coverageArray[index]/count < threshold) {
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
            if ((double) coverageArray[index]/count <threshold) {
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
