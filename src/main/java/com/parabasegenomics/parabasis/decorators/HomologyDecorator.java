/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.decorators;

import static com.parabasegenomics.parabasis.decorators.AnnotationKeys.HOM_KEY;
import static com.parabasegenomics.parabasis.decorators.FormatPatterns.percentPattern;
import com.parabasegenomics.parabasis.target.AnnotatedInterval;
import htsjdk.samtools.util.Interval;
import java.text.DecimalFormat;
import java.util.List;

/**
 *
 * @author evanmauceli
 */
public class HomologyDecorator implements IntervalDecorator {
    private static final String KEY = HOM_KEY;
    private final static String formatPattern = percentPattern;
    private final DecimalFormat decimalFormat;
    private final List<Interval> uniqKmerIntervals;
    
    public HomologyDecorator(List<Interval> intervals) {
        decimalFormat = new DecimalFormat(formatPattern);       
        uniqKmerIntervals = intervals;
    }
    
    /**
     * Annotates the given interval with the count of uniq kmers.
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
     * 
     * @return Returns the identifying string for this decorator.
     */
     @Override
    public String getKey() {
        return KEY;
    }
 
    
     @Override
    public int getCount(Interval interval) {
        
        int overlapCount = 0;
        for (Interval uniqKmerInterval : uniqKmerIntervals) {
            if (!uniqKmerInterval.intersects(interval)) {
                continue;
            }
            
            // TODO. This ugly hack is implemented to deal with the fact that
           // we're using 0-based, open intervals, but htsjdk Interval methods
           // are expecting 1-based, closed intervals.  Therefore, abutting
           // intervals are counted as overlapping, when that should not be the case.
           if (uniqKmerInterval
                .intersect(interval)
                .length()<=1) {
               continue;
           } 
           
           overlapCount += (uniqKmerInterval.getIntersectionLength(interval)-1);          
       }
       
       return overlapCount; 
    }
    
}