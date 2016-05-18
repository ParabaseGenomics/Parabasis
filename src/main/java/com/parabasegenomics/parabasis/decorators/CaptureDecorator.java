/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.decorators;

import static com.parabasegenomics.parabasis.decorators.AnnotationKeys.CAPTURE_KEY;
import static com.parabasegenomics.parabasis.decorators.FormatPatterns.percentPattern;
import com.parabasegenomics.parabasis.target.AnnotatedInterval;
import htsjdk.samtools.util.Interval;
import java.text.DecimalFormat;
import java.util.List;

/**
 *
 * @author evanmauceli
 */
public class CaptureDecorator implements IntervalDecorator {
    private static final String KEY = CAPTURE_KEY;
    private final static String formatPattern = percentPattern;
    private final DecimalFormat decimalFormat;
    
    private final List<Interval> captureIntervals;
    
   public CaptureDecorator(List<Interval> intervals) {
       captureIntervals = intervals;
       decimalFormat = new DecimalFormat(formatPattern);    
   }

   /**
    * Annotates the given interval with the percentage for which probes were
    * able to be designed.
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
   public String getKey() {
       return KEY;
   }
   
   /**
    * Returns the number of bases of the target over which probes were able to 
    * be designed.
    * @param interval
    * @return 
    */
    @Override
    public int getCount(Interval interval) {
       
       int overlapCount = 0;
       for (Interval capture : captureIntervals) {
           if (!capture.intersects(interval)) {
               continue;
           } 
               
           // TODO. This ugly hack is implemented to deal with the fact that
           // we're using 0-based, open intervals, but htsjdk Interval methods
           // are expecting 1-based, closed intervals.  Therefore, abutting
           // genes are counted as overlapping, when that should not be the case.
           if (capture
                .intersect(interval)
                .length()<=1) {
               continue;
           } 
           
           overlapCount += (capture.getIntersectionLength(interval)-1);          
       }
       
       return overlapCount;
   }

} 
    
   