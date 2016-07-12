/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.decorators;

import com.parabasegenomics.parabasis.coverage.AssayCoverageModel;
import static com.parabasegenomics.parabasis.decorators.AnnotationKeys.COVERAGE_KEY;
import static com.parabasegenomics.parabasis.decorators.FormatPatterns.percentPattern;
import com.parabasegenomics.parabasis.target.AnnotatedInterval;
import htsjdk.samtools.util.Interval;
import java.io.IOException;
import java.text.DecimalFormat;

/**
 * Annotate an interval with the mean coverage of that interval.  Mean coverage 
 * is held by the AssayCoverageModel. This class provides the annotation framework.
 * @author evanmauceli
 */
public class CoverageDecorator implements IntervalDecorator {
    
    private static final String KEY = COVERAGE_KEY;
    private final static String formatPattern = percentPattern;
    private final DecimalFormat decimalFormat;
    private final AssayCoverageModel coverageModel;
    
    /**
     * Constructor.  
     * @param assayCoverageModel
     * @throws IOException 
     */
    public CoverageDecorator(AssayCoverageModel assayCoverageModel) 
    throws IOException {
        decimalFormat = new DecimalFormat(formatPattern);
        coverageModel = assayCoverageModel; 
    }
    
    /**
     * Annotates the given interval with the mean coverage from the 
     * coverage model.
     * @param annotatedInterval 
     */
    @Override 
    public void annotate(AnnotatedInterval annotatedInterval) {
        annotatedInterval
            .addAnnotation(
                KEY,
                decimalFormat
                    .format(    
                        getCount(annotatedInterval.getInterval())));   
    }
    
   
    /**
     * Returns the coverage of the given interval as an integer to satisfy the
     * requirements of the interface.  Returns -1 if the given interval is not 
     * in the coverage model.  TODO: fix this. 
     * @param interval
     * @return 
     */
    @Override
    public int getCount(Interval interval) {
        Double coverage = coverageModel.getMeanCoverageAt(interval);
        if (coverage != null) {
            return coverage.intValue();
        } else {
            return -1;
        }
    }
    
    /**
     * Returns the identifying string for this decorator.
     * @return 
     */
    @Override
    public String getKey() {
        return COVERAGE_KEY;
    }
}
