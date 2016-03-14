/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis;

import htsjdk.samtools.util.Interval;

/**
 * Class to encapsulate what it means to be an exon.  Mostly wraps the Interval 
 * class from htsjdk, but also provides a method to get the coding-only portion
 * of an exon.
 * 
 * @author evanmauceli
 */
public class Exon {
    
    private Interval interval;
    
    public Exon(Interval i) {
        interval=i;
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

    
    /**
     * Method to return an Exon object representing only the coding portion
     * of the exon.
     * @param codingIntervalOfTranscript An Interval defining the coding
     * portion of this transcript.  This is read in from the file with the gene
     * models and help by each Transcript.
     * @return An Exon corresponding to the coding portion of this Exon.
     */
    public Exon getCodingExon(Interval codingIntervalOfTranscript) {
            
        if (codingIntervalOfTranscript == null) {
            return null;
        }
        
        if (!interval.intersects(codingIntervalOfTranscript)) {
            return null;
        }

        return(new Exon(interval.intersect(codingIntervalOfTranscript)));
                           
    }


}
