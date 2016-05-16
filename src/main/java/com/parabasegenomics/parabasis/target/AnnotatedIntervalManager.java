/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.target;

import static com.parabasegenomics.parabasis.decorators.AnnotationKeys.GENE_KEY;
import static com.parabasegenomics.parabasis.decorators.FormatPatterns.percentPattern;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author evanmauceli
 */
public class AnnotatedIntervalManager {
    private final static String formatPattern = percentPattern;
    private final DecimalFormat decimalFormat;
    
    private List<AnnotatedInterval> intervals;
    private final Map<String,Integer> annotationCountMap;
    private final Set<String> geneNamesHeldByManager;
    
    public AnnotatedIntervalManager() {
        intervals = new ArrayList<>();
        annotationCountMap = new HashMap<>();
        geneNamesHeldByManager= new HashSet<>();
        
        decimalFormat = new DecimalFormat(formatPattern);
    }

    /**
     * Add an interval to the list.
     * @param interval 
     */
    public void addInterval(AnnotatedInterval interval) {
        intervals.add(interval);
    }
    
    public void setIntervals(List<AnnotatedInterval> list) {
        intervals=list;
    }
    
    public void summarize() {
    }
  
    
    public void aggregate() 
    throws IOException {     
        if (intervals==null) {
            throw new IOException("aggregating without adding intervals.");
        }
        Map<String,String> annotationMap = intervals.get(0).getMap();
        Set<String> keys = annotationMap.keySet();
        
        getGeneNames();
        
        for (String thisGene : geneNamesHeldByManager) {
            List<AnnotatedInterval> intervalsThisGene
                = getIntervalsForThisGene(thisGene);
            if (intervalsThisGene==null) {
                System.out.println("Serious issue here! Cannot find gene "
                    + thisGene);
            }
            int geneLength=0;
            for (AnnotatedInterval interval : intervalsThisGene) { 
                
                // this shouldn't happen but sometimes does
                if (interval.getGeneNames().isEmpty()) {
                    continue;
                }
                geneLength += interval.length();
                for (String key : keys) {
                    // don't want the gene annotation
                    if (key.equals(GENE_KEY)) {
                        continue;
                    }
                    int count = Integer.parseInt(interval.getAnnotation(key));
                    if (annotationCountMap.containsKey(key)) {
                        int currentCount = annotationCountMap.get(key);
                        int newCount = currentCount + count;           
                        annotationCountMap.replace(key, newCount);
                    } else {
                        annotationCountMap.put(key, count);
                    }              
                }
            }
        
            report(thisGene,geneLength);
            annotationCountMap.clear();
            
        }     
    }
    
    private void report(String gene, int length) {
        System.out.print(gene);
        System.out.print("\t");
        double pct=0.;
        Set<String> keys = annotationCountMap.keySet();
        for (String key : keys) {
            int count = annotationCountMap.get(key);
            pct = 100.0 * (double) count/(double) length;
            System.out.print(decimalFormat.format(pct));
            System.out.print("\t");
        }
        System.out.println("");
        
    }
    
    
    
    /**
     * Returns the intervals associated with the requested gene, or null
     * if none.
     * @param gene
     * @return 
     */
    private List<AnnotatedInterval> getIntervalsForThisGene(String gene) {       
        if (gene == null) {
            return null;
        }
        if (intervals==null) {
            return null;
        }
        
        int index = 0;
        while (index < intervals.size() 
            && !(intervals.get(index).getGeneNames().contains(gene))) {
            index++;
        }
        int startIndex = index;
        while (index<intervals.size()
            && intervals.get(index).getGeneNames().contains(gene)) {
            index++;
        }
        int endIndex = index;
        if (startIndex>=endIndex) {
            return null;
        }
        return intervals.subList(startIndex, endIndex);
        
    }
    
    /**
     * Get all the genes held in this collection of AnnotatedInterval objects.
     */
    private void getGeneNames() {
        for (AnnotatedInterval interval : intervals) {
            List<String> names = interval.getGeneNames();
            if (names!=null) {
                for (String name : names) {
                    geneNamesHeldByManager.add(name);   
                }
            }
        }
    }
    
}
