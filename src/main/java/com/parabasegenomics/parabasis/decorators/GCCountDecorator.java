/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.decorators;

import static com.parabasegenomics.parabasis.decorators.AnnotationKeys.GC_KEY;
import static com.parabasegenomics.parabasis.decorators.FormatPatterns.percentPattern;
import com.parabasegenomics.parabasis.target.AnnotatedInterval; 
import htsjdk.samtools.reference.FastaSequenceIndex;
import htsjdk.samtools.reference.IndexedFastaSequenceFile;
import htsjdk.samtools.reference.ReferenceSequence;
import htsjdk.samtools.util.Interval;
import java.nio.charset.Charset;
import java.text.DecimalFormat;

/**
 *
 * @author evanmauceli
 */
public class GCCountDecorator implements IntervalDecorator {
    
    private static final String KEY = GC_KEY;
    
    private final IndexedFastaSequenceFile referenceSequence;
    private final FastaSequenceIndex referenceIndex; 
    
    private final static String formatPattern = percentPattern;
    private final DecimalFormat decimalFormat;
    
    public GCCountDecorator(
        IndexedFastaSequenceFile reference,
        FastaSequenceIndex index) {
            referenceSequence=reference;
            referenceIndex=index;
            
            decimalFormat = new DecimalFormat(formatPattern);
    }
    
    
    /**
     * Annotates the given interval with the %GC of the interval.
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
    
    /**
     * Returns the GC count in the given interval.
     * @param interval
     * @return 
     */
    @Override
    public int getCount(Interval interval) {
        
        String chromosome = interval.getContig();
        int startPosition = interval.getStart();
        int endPosition = interval.getEnd();

        ReferenceSequence seq 
            = referenceSequence.getSubsequenceAt(chromosome, startPosition, endPosition-1);
        
        int gcCount = 0;
        
        byte [] bases = seq.getBases();
        String basesString = new String(bases,Charset.defaultCharset());
        String upperCaseBases = basesString.toUpperCase();
        
        for (int i=0 ; i<upperCaseBases.length(); i++) {
            if (upperCaseBases.charAt(i) == 'C' 
                || upperCaseBases.charAt(i) == 'G') {
                gcCount++;
            }
        }
        
        return gcCount;        
    }
}
