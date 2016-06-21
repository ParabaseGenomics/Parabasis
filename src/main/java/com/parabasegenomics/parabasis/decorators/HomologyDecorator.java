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
import java.io.File;
import java.text.DecimalFormat;

/**
 *
 * @author evanmauceli
 */
public class HomologyDecorator implements IntervalDecorator {
    private static final String KEY = HOM_KEY;
    private final static String formatPattern = percentPattern;
    private final DecimalFormat decimalFormat;
    private final File inputFile;
    
    public HomologyDecorator(String file) {
        decimalFormat = new DecimalFormat(formatPattern); 
        inputFile = new File(file);
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
        return -1;
    }
    
}