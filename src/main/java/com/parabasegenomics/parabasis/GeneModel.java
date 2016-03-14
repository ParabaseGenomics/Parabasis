/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis;

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
    
 
    /**
     * Method to collapse all transcripts from a gene into a flat representation 
     * on the reference sequence. Sets the "collapsedTranscript" member variable.
     * 
     */
    public void Collapse() 
    throws IOException {
 
        ListIterator<Transcript> transcriptsIteratorBegin 
            = transcripts.listIterator(0);
        ListIterator<Transcript> transcriptsIteratorEnd
            = transcripts.listIterator(1);
        
        if (!transcriptsIteratorBegin.hasNext()) {
            throw new IOException("No transcripts to collapse!");
        }
        
        String thisGeneName = transcripts.get(0).getGeneName();
       
        int [] coordinateArray = new int[getGenomicSpanThisGene(thisGeneName)];
        
    }

    private int getGenomicSpanThisGene(String thisGeneName) {
        return 0;
    }
    
}
