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
 * The IntervalCoverage class keeps coverage statistics on intervals in an assay,
 * for example, and provides them for use in CNV detection and sequencing 
 * stability assessments.
 * @author evanmauceli
 */
public class IntervalCoverage {
    
    private final Interval interval;
    private int count;
    private final Mean mean;
    private final StandardDeviation standardDeviation;
    private double coefficientOfVariation;
    
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
    }
    
    /**
     * Returns the number of datapoints that are currently held by the object.
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
    public IntervalCoverage update(Integer coverage) {
        if (count == 0) {
  
            mean.increment((double) coverage);
            standardDeviation.increment((double) coverage);
            coefficientOfVariation = 0.0;
            count=1;
        } else {
            count++;
            mean.increment((double) coverage);
            standardDeviation.increment((double) coverage);
            coefficientOfVariation
                = (1+(1/(4*count)))
                * (standardDeviation.getResult()/mean.getResult());
            
            System.out.println(" adf " + count+" "+mean.getResult()+ " "+standardDeviation.getResult() +" "+coefficientOfVariation);
        }       
        return this;    
    }
    
    
}
