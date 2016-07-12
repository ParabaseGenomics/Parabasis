/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.decorators;

import com.parabasegenomics.parabasis.coverage.AssayCoverageModel;
import static com.parabasegenomics.parabasis.decorators.AnnotationKeys.GAPS_KEY;
import static com.parabasegenomics.parabasis.decorators.FormatPatterns.percentPattern;
import com.parabasegenomics.parabasis.target.AnnotatedInterval;
import htsjdk.samtools.util.Interval;
import java.io.IOException;
import java.text.DecimalFormat;

/**
 *
 * @author evanmauceli
 */
public class GapsDecorator implements IntervalDecorator {
    private static final String KEY = GAPS_KEY;
    private final static String formatPattern = percentPattern;
    private final DecimalFormat decimalFormat;

    private final AssayCoverageModel coverageModel;
    
    /**
     * Constructor.  
     * @param assayCoverageModel
     * @throws IOException 
     */
    public GapsDecorator(AssayCoverageModel assayCoverageModel) 
    throws IOException {
        decimalFormat = new DecimalFormat(formatPattern);
        coverageModel = assayCoverageModel; 
    }
    
    /**
     * Annotates the given interval with the count of bases having coverage
     * less then threshold.
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
     * Returns the count of bases with coverage below threshold in the provided 
     * interval.
     * @param interval
     * @return 
     */
    @Override
    public int getCount(Interval interval) {
        int count = 0;
        Double coverage = coverageModel.getLowCoverageCountAt(interval);
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
        return KEY;
    } 
}
