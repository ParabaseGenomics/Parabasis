/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.gene;

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
    
    private final List<GeneModel> genes;
    
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
        genes = new ArrayList<>();
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
        sortTranscriptsByGeneName();
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
     * Method that returns the list of gene models held by the collection.
     * @return 
     */
    public List<GeneModel> getGeneModels() {
        return genes;
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
    protected void sortTranscriptsByGeneName() {
        Collections.sort(transcripts);
        isSorted=true;
    }

    /**
     * Method to iterate through the transcripts, pulling out those belonging to
     * the same gene and collecting in separate GeneModel objects.
     * 
     * This class holds the GeneModel objects in a member List.
     * @throws java.io.IOException
     */
    public void aggregateTranscriptsByGenes() 
    throws IOException {
        int beginIndex = 0;
        int endIndex = 0;
        
        if (transcripts.isEmpty()) {
            throw new IOException("No transcripts found to aggregate by gene.");
        }
        
        /**
         * The list of transcripts must be sorted (sorting by gene name is the
         * default for the Transcript class) before proceeding.
         */
        if (!isSorted) {
            sortTranscriptsByGeneName();
        }
        
        int transcriptCount = transcripts.size();
        
        /**
         * traverse the list of transcripts by gene, aggregating into separate
         * GeneModel objects according to the gene name.
         */       
        while (beginIndex < transcriptCount) {
            String thisGene = transcripts.get(beginIndex).getGeneName();
            GeneModel geneModel = new GeneModel();
            geneModel.setGeneName(thisGene);
            endIndex = beginIndex+1;
            
            while (endIndex < transcriptCount 
                && transcripts.get(endIndex).getGeneName().equals(thisGene)) {
                geneModel.addTranscript(transcripts.get(endIndex));
        
                endIndex++;
            }
        
            beginIndex = endIndex;
            
        }
   
    }
}
