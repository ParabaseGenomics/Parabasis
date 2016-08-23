/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.gene;

import htsjdk.samtools.util.Interval;
import java.util.ArrayList;
import java.util.List;
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
    private final List<Interval> codingIntervals;
    
    public Exon(Interval i) {
        interval=i;
        codingIntervals=new ArrayList<>();
    }
    
    /**
     * Copy constructor.
     * @param toCopy 
     */
    public Exon(Exon toCopy) {
        this.interval = toCopy.getInterval();
        this.codingIntervals = toCopy.getCodingIntervals();
    }
    
    public void addCodingInterval(Interval i) {
        codingIntervals.add(i);
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
    
    public String getName() {
        return interval.getName();
    }
    public String getChromosome() {
        return interval.getContig();
    }
    public Interval getInterval() {
        return interval;
    } 
    
    public int getNumberOfCodingIntervals() {
        return codingIntervals.size();
    }
    public Interval getCodingInterval(int index) {
        return codingIntervals.get(index);
    }
    
    public List<Interval> getCodingIntervals() {
        return codingIntervals;
    }
    public int getLength() {
        return (interval.getEnd()-interval.getStart());
    }
    public int getCodingLength() {
        int codingLength=0;
        for (int i=0; i<codingIntervals.size(); i++) {
            codingLength 
                += (codingIntervals.get(i).getEnd()-codingIntervals.get(i).getStart());
        }
        return codingLength;
    }
   
    
    /**
     * Returns the coding-only portion of the exon or an empty array if the exon is 
     * completely non-coding.
     * @return 
     */
    public List<Exon> getCodingExons() {
        List<Exon> codingExons = new ArrayList<>();
        if (!codingIntervals.isEmpty()) {
            for (Interval codingInterval : codingIntervals) {
                codingExons.add(
                    new Exon(codingInterval));          
            }
        }
        return codingExons;
    }
    
    public Exon getCodingExon(int index) {
        return new Exon(getCodingInterval(index));
    }
    
}
