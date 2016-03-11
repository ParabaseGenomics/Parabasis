/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis;

import java.util.ArrayList;
import java.util.List;

/**
 * The GeneModel class provides translations from gene models (such as RefSeq) 
 * to genomic coordinates and back again.
 * 
 * @author evanmauceli
 */
public class GeneModel {
    
    private List<Transcript> transcripts;
    
    private String modelName;
    private String modelVersion;
   
    private Transcript collapsedTranscript;
    
    /**
     * Constructor
     */
    public GeneModel() {
        
        transcripts = new ArrayList<Transcript>();  
    }
    
    /**
     * Method to set the name of the gene models, i.e. "RefSeq" or "Ensembl" and
     * the version.
     * 
     * @param name 
     * @param version
     */
    public void setName(String name, String version) {
        modelName = name;
        modelVersion = version;
    }
    
    /**
     * Add a new transcript to the list being held by the model.
     * @param transcript 
     */
    public void addTranscript(Transcript transcript) {
        transcripts.add(transcript);
    }
    
}
