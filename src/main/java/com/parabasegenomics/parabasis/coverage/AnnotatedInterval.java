/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.coverage;

import htsjdk.samtools.util.Interval;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author evanmauceli
 */
public class AnnotatedInterval {
    
    private final Interval interval;
    private final Map<String,String> annotations;
    
    public AnnotatedInterval(Interval val) {
        interval = val;
        annotations = new HashMap<>();
    }
    
    /**
     * Add an annotation
     * @param key Name of the annotation.
     * @param value Value of the annotation.
     */
    public void addAnnotation(String key, String value) {
        annotations.put(key, value);
    }
    
    // get methods
    public String getAnnotation(String key) {
        return annotations.get(key);
    }
    public Interval getInterval() {
        return interval;
    }
    
}
