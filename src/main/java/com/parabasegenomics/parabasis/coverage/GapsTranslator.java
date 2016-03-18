/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.coverage;

import com.parabasegenomics.parabasis.gene.GeneModel;
import com.parabasegenomics.parabasis.gene.GeneModelCollection;
import htsjdk.samtools.util.Interval;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * TODO: summary table!!!
 * TODO: intersect gaps with targets!!!
 * 
 * @author evanmauceli
 */
public class GapsTranslator {
    
    private final GapsFileReader gapsFileReader;
    private final GeneModelCollection geneModelCollection;
    private final BufferedReader reader;   
    private final File testGenelistFile;
    
    private final Set<String> geneNamesInTest;
    
    /**
     * Translate the gaps.csv file produced by the MiSeq (regions of assay with 
     * coverage <20x) to two files: 1) a per-gene summary table ; 2) a detailed
     * view of where the gaps are in a gene.
     * @param gapsFile The gaps.csv file produced by the MiSeq.
     * @param geneModelFile A file containing the gene models used to give 
     *          context to the gaps.
     * @param genelistFile A list of gene names corresponding to the particular
     *          test being run for this sample.
     * 
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public GapsTranslator(String gapsFile, String geneModelFile, String genelistFile) 
    throws FileNotFoundException, IOException {
        gapsFileReader = new GapsFileReader(gapsFile);  
        
        // read in the transcript models and aggregate by gene
        geneModelCollection = new GeneModelCollection();
        geneModelCollection.readGeneModelCollection(geneModelFile);
        geneModelCollection.aggregateTranscriptsByGenes();
        
        testGenelistFile = new File(genelistFile);
        reader = new BufferedReader(new FileReader(testGenelistFile));          
        geneNamesInTest = new HashSet<>();
            
    }
    
    /**
     * Method to read in a list of gene names (one name per line, no header) from
     * a file into a HashSet.
     * 
     * @throws IOException 
     */
    protected void readGeneNamesFromFile() 
    throws IOException {
        while (reader.ready()) {
            geneNamesInTest.add(reader.readLine());
        }
    }
    
    public void grab() 
    throws IOException {
        List<String> overlaps = null;
        
        List<Interval> gaps = gapsFileReader.readFile();
        for (Interval gap : gaps ) {
            boolean foundGap = false;
            
            List<GeneModel> genes = geneModelCollection.getGeneModels();
            for (GeneModel gene : genes) {
                
                // we only want to look at genes in the test definition, and
                // since the gaps file based on the assay, we filter any genes
                // on the assay not in the test.
                if (!geneNamesInTest.contains(gene.getGeneName())) {
                    continue;
                }
                               
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
