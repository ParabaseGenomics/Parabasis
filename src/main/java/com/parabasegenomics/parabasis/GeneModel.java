/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis;

import htsjdk.samtools.util.Interval;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

/**
 * The GeneModel class provides translations from a gene model (represented by
 * a list of transcripts) to genomics coordinates.
 * 
 * @author evanmauceli
 */
public class GeneModel {
    
    private final List<Transcript> transcripts;   
    private Transcript collapsedTranscript;
    
    private String geneName;
    
    /**
     * Constructor
     */
    public GeneModel() {      
        transcripts = new ArrayList<>();  
    }
    
    /**
     * Add a new transcript to the list being held by the model.
     * @param transcript 
     */
    public void addTranscript(Transcript transcript) {
        transcripts.add(transcript);
    }
    
    public void setGeneName(String name) {
        geneName=name;
    }
    
    /**
     * Method to collapse all transcripts from a gene into a flat representation 
     * on the reference sequence. Sets the "collapsedTranscript" member variable.
     * 
     */
    public void Collapse() {
        Interval transcriptSpanInterval = getTranscriptSpanThisGene();
        
        // rely on java spec to enforce that ints are initialized to zero
        int [] sequenceArray = new int[transcriptSpanInterval.length()];
        
        int offset = transcriptSpanInterval.getStart();
        for (int base = transcriptSpanInterval.getStart(); 
                base < transcriptSpanInterval.getEnd(); 
                base++) {
            
            int sequenceArrayIndex = base - offset;
            for (Transcript transcript : transcripts) {
                
                
                
            }
            
        }
        
        
    }

    
    /**
     * Method to return the maximum genomic span of this gene given all the 
     * transcripts.
     * @return Returns an Interval with the maximum genomic span given the
     * associated transcripts.
     * 
     */
    private Interval getTranscriptSpanThisGene() {
        int index = 0;
        int minTranscriptStart = transcripts.get(index).getTranscriptStart();
        int maxTranscriptEnd = transcripts.get(index).getTranscriptEnd();
        
        for (; index<transcripts.size(); index++) {
            minTranscriptStart = Math.min(
                minTranscriptStart,transcripts.get(index).getTranscriptStart());
            
            maxTranscriptEnd = Math.max(
                maxTranscriptEnd,transcripts.get(index).getTranscriptEnd());
        }
             
        return new Interval(
            transcripts.get(0).getChromosome(),
            minTranscriptStart,
            maxTranscriptEnd);
    }
    
}
