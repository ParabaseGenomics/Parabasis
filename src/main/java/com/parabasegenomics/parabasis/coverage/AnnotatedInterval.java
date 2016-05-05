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
    
    private static final String GENE_KEY = "GEN";
    private static final String COLON = ":";
    private final Interval interval;
    private final Map<String,String> annotations;
    
    public AnnotatedInterval(Interval val) {
        interval = val;
        annotations = new HashMap<>();
    }
    
    public AnnotatedInterval(AnnotatedInterval toCopy) {
        this.interval = toCopy.getInterval();
        this.annotations = toCopy.getMap();
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
    
    public Map<String,String> getMap() {
        return annotations;
    }
    
    /**
     * Parses the gene annotation for the gene name. Returns an empty string
     * if not found or gene annotation not applied.
     * @return 
     */
    public String getGeneName() {
        if (annotations == null) {
            return "";
        }
        String geneAnnotation = annotations.get(GENE_KEY);
        if (geneAnnotation==null || geneAnnotation.isEmpty()) {
            return "";
        }
      
        int colonIndex = geneAnnotation.indexOf(COLON);
        return geneAnnotation.substring(0,colonIndex);
    }
    
    /**
     * Returns the length of the interval.
     * @return 
     */
    public int length() {
        return (interval.getEnd()-interval.getStart());
    }
}

