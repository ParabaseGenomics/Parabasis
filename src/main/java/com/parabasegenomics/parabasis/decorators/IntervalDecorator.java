/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.decorators;

    
import com.parabasegenomics.parabasis.target.AnnotatedInterval; 
import htsjdk.samtools.util.Interval;

/**
 *
 * @author evanmauceli
 */
public interface IntervalDecorator {
    
    void annotate(AnnotatedInterval annotatedInterval);
    String getKey();
    int getCount(Interval interval);
    
}


