/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.gene;

import htsjdk.samtools.util.Interval;
import java.util.Map;
import java.util.TreeMap;

/**
 * Class to encapsulate what it means to be an exon.  Mostly wraps the Interval 
 * class from htsjdk, but also provides a method to get the coding-only portion
 * of an exon.
 * 
 * TODO: add draw() method
 * TODO: add value member, methods
 * 
 * @author evanmauceli
 */
public class Exon {
    
    private final Interval interval;
    private Interval codingInterval;
    private final Map<String, String> valuesMap;
    
    
    public Exon(Interval i) {
        interval=i;
        codingInterval=null;
        valuesMap = new TreeMap<>();
    }
    
    /**
     * Copy constructor.
     * @param toCopy 
     */
    public Exon(Exon toCopy) {
        this.interval = toCopy.getInterval();
        this.codingInterval = toCopy.getCodingInterval();
        this.valuesMap = toCopy.getValuesMap();
    }
    
    public void addCodingInterval(Interval i) {
        codingInterval=i;
    }
    
    /**
     * Getter methods.  Mostly defer to Interval methods of same name.
     * 
     * @return 
     */    
    public int getStart() {
        return interval.getStart();
    }
    public int getEnd() {
        return interval.getEnd();
    }
    public int getCodingStart() {
        return codingInterval.getStart();
    }
    public int getCodingEnd() {
        return codingInterval.getEnd();
    }
    public String getName() {
        return interval.getName();
    }
    public String getChromosome() {
        return interval.getContig();
    }
    public Interval getInterval() {
        return interval;
    } 
    public Interval getCodingInterval() {
        return codingInterval;
    }
    public int getLength() {
        return (interval.getEnd()-interval.getStart());
    }
    public int getCodingLength() {
        return (codingInterval.getEnd()-interval.getStart());
    }
    
    public String getValue(String key) {
        return valuesMap.get(key);
    }
    public Map<String,String> getValuesMap() {
        return valuesMap;
    }
    
    /**
     * Method to add a value to this exon, along with a descriptor(key). This is 
     * intended to act as a weak but flexible decorator.
     * @param key A short descriptor of the associated value.
     * @param value A number associated with this exon.  It might be GC content.
     * It might be low coverage percentage. It might be something else.
     */
    public void addKeyValuePair(String key, String value) {
        valuesMap.put(key, value);
    }
    
    
    /**
     * Returns the coding-only portion of the exon or null if the exon is 
     * completely non-coding.
     * @return 
     */
    public Exon getCodingExon() {
        if (codingInterval != null) {
            return new Exon(codingInterval);
        } else {
            return null;
        }
    }
    
}
