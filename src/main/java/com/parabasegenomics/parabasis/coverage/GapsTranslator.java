/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.coverage;

import com.parabasegenomics.parabasis.gene.GeneModel;
import com.parabasegenomics.parabasis.gene.GeneModelCollection;
import htsjdk.samtools.util.Interval;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author evanmauceli
 */
public class GapsTranslator {
    
    private final GapsFileReader gapsFileReader;
    private final GeneModelCollection geneModelCollection;
    
    public GapsTranslator(String gapsFile, String geneModelFile) 
    throws FileNotFoundException, IOException {
        gapsFileReader = new GapsFileReader(gapsFile);        
        geneModelCollection = new GeneModelCollection();
        // read the transcripts
        geneModelCollection.readGeneModelCollection(geneModelFile);
        // aggregate transcripts by gene
        geneModelCollection.aggregateTranscriptsByGenes();
    }
    
    public void grab() 
    throws IOException {
        List<String> overlaps = null;
        
        List<Interval> gaps = gapsFileReader.readFile();
        for (Interval gap : gaps ) {
            boolean foundGap = false;
            
            List<GeneModel> genes = geneModelCollection.getGeneModels();
            for (GeneModel gene : genes) {
                String overlapWithGene = gene.overlap(gap);
                if (overlapWithGene != null) {
                    boolean add = overlaps.add(overlapWithGene);
                    foundGap=true;
                }
            }
            if (!foundGap) {
                // problem
            } 
           
            
        }
        
    }
    
    
}
