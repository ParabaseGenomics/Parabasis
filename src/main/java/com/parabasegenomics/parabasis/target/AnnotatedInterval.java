/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.target;

import static com.parabasegenomics.parabasis.decorators.AnnotationKeys.GENE_KEY;
import htsjdk.samtools.util.Interval;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author evanmauceli
 */
public class AnnotatedInterval {
    
    private static final String COLON = ":";
    private static final String SEMI_COLON = ";";

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
     * Returns a list of gene names associated with this interval or null if
     * there are none.
     * @return 
     */
    public List<String> getGeneNames() {
        if (annotations == null) {
            return null;
        }
        String geneAnnotation = annotations.get(GENE_KEY);
        if (geneAnnotation==null || geneAnnotation.isEmpty()) {
            return null;
        }   
        
        List<String> geneNames = new ArrayList<>();
        String [] tokens = geneAnnotation.split(SEMI_COLON);
        for (String token : tokens) {
            String geneName = getGeneName(token);
            if (!geneName.isEmpty()) {
                geneNames.add(geneName);
            }
        }
        return geneNames;
        
    }
    
    /**
     * Parses the gene annotation for the gene name. Returns an empty string
     * if not found or gene annotation not applied.
     * @return 
     */
    private String getGeneName(String geneAnnotation) {
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
    
    /**
     * Returns the percentage of the interval covered by the annotation or
     * null if the annotation is not intended to be numerical or does not
     * exist.
     * @param key
     * @return 
     */
    public Double getPercentOfIntervalForAnnotation(String key) {      
        if (key.equals(GENE_KEY)) {
            return null;
        }      
        
        if (annotations.get(key) == null) {
            return null;
        }
        
        Double pct 
            = Double.parseDouble(annotations.get(key)) / (double) length();
            
        return pct;
    }
    
}

