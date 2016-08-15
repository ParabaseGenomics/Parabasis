/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.coverage;

import htsjdk.samtools.util.Interval;
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
    private final Mean lowCoverageMean;
    private final Mean mean;
    private final StandardDeviation standardDeviation;
    private double coefficientOfVariation;
    private int [] coverageArray;
    
    /**
     * Construct with an Interval.
     * @param val 
     */
    public IntervalCoverage(Interval val) {
        interval=val;
        count=0;
        standardDeviation = new StandardDeviation();
        mean = new Mean();       
        lowCoverageMean = new Mean();
        coefficientOfVariation=0.0;
        coverageArray = new int [interval.length()];
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
            lowCoverageMean=new Mean();
            coverageArray = new int [interval.length()];
    }
    
    /**
     * Returns the number of data points that are currently held by the object.
     * @return 
     */
    public int count() {
        return count;
    }
    
    /**
     * Returns the Interval held by this object.
     * @return 
     */
    public Interval getInterval() {
        return interval;
    }
    
    /**
     * Returns the current mean count of low coverage bases for this interval.
     * @return 
     */
    public double getLowCoverageMean() {
        return lowCoverageMean.getResult();
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
     * Updates the current mean count of low coverage bases.
     * @param lowCoverageCount
     * @return 
     */
    public IntervalCoverage updateLowCoverage(Double lowCoverageCount) {
        lowCoverageMean.increment(lowCoverageCount);
        
        return this;
    }
    
}
