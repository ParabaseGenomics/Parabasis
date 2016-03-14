/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The GeneModelCollection class provides access to a Reference Sequence collection 
 * of gene models such as RefSeq.
 * 
 * @author evanmauceli
 */
public class GeneModelCollection {
    
    private final List<Transcript> transcripts;
    
    private String modelName;
    private String modelVersion;
    private boolean isSorted;
    
    private Transcript collapsedTranscript;
    
    private GeneCollectionFileReader geneCollectionFileReader;
    
    /**
     * Constructor
     */
    public GeneModelCollection() {      
        transcripts = new ArrayList<>();  
        isSorted = false;       
    }
    
    /**
     * Method to read a collection of gene models from a file.
     * @param file
     * @throws FileNotFoundException 
     */
    public void readGeneModelCollection(String file) 
    throws FileNotFoundException, IOException {
        geneCollectionFileReader 
            = new GeneCollectionFileReader(file);
        
        geneCollectionFileReader.readFile(transcripts);
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
    
    /**
     * Method to sort the transcripts by gene name. Sets the isSorted flag.
     */
    public void sortTranscriptsByGeneName() {
        Collections.sort(transcripts);
        isSorted=true;
    }

}
