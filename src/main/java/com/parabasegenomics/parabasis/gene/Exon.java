/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.gene;

import htsjdk.samtools.util.Interval;

/**
 * Class to encapsulate what it means to be an exon.  Mostly wraps the Interval 
 * class from htsjdk, but also provides a method to get the coding-only portion
 * of an exon.
 * 
 * @author evanmauceli
 */
public class Exon {
    
    private final Interval interval;
    private Interval codingInterval;
    
    public Exon(Interval i) {
        interval=i;
        codingInterval=null;
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
    
    
    
    /**
     * Returns the coding-only portion of the exon.
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
