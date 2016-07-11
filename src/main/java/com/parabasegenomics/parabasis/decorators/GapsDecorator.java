/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.decorators;

import com.parabasegenomics.parabasis.coverage.BamCoverage;
import static com.parabasegenomics.parabasis.decorators.AnnotationKeys.GAPS_KEY;
import static com.parabasegenomics.parabasis.decorators.FormatPatterns.percentPattern;
import com.parabasegenomics.parabasis.target.AnnotatedInterval;
import htsjdk.samtools.util.Interval;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

/**
 *
 * @author evanmauceli
 */
public class GapsDecorator implements IntervalDecorator {
    private static final String KEY = GAPS_KEY;
    private final static String formatPattern = percentPattern;
    private final DecimalFormat decimalFormat;
    private final BamCoverage bamCoverage;
    private final Integer coverageThreshold;
    
    /**
     * Constructor.  
     * @param bamFilepath
     * @param threshold
     * @throws IOException 
     */
    public GapsDecorator(String bamFilepath,Integer threshold) 
    throws IOException {
        decimalFormat = new DecimalFormat(formatPattern);
        bamCoverage 
                = new BamCoverage(bamFilepath);
        coverageThreshold=threshold;
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
     * Returns the count of bases in the provided interval with coverage less 
     * than the given threshold.
     * @param interval
     * @return 
     */
    @Override
    public int getCount(Interval interval) {
        int count = 0;
        List<Interval> lowCoverageIntervals
            = bamCoverage.getLowCoverage(interval,coverageThreshold);
        for (Interval lowCoverageInterval : lowCoverageIntervals) {
            count += lowCoverageInterval.length();
        }
        return count;
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
