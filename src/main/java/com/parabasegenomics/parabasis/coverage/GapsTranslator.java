/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.coverage;

import com.parabasegenomics.parabasis.gene.GeneModel;
import com.parabasegenomics.parabasis.gene.GeneModelCollection;
import com.parabasegenomics.parabasis.util.Reader;
import htsjdk.samtools.util.Interval;
import java.io.FileNotFoundException;
import java.io.IOException;
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
    private static GeneModelCollection geneModelCollection; 
    private final Reader utilityReader;
    
    private static Set<String> geneNamesInTest;
    
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
        //geneModelCollection.aggregateTranscriptsByGenes();
        
        utilityReader = new Reader();
        geneNamesInTest = utilityReader.readHashSet(genelistFile);
            
    }
    
    public void addGeneModel(String geneModelFileToAdd) 
    throws IOException {
        geneModelCollection.addGeneModelCollection(geneModelFileToAdd);
    }
    
    public static void main(String[] args) 
    throws IOException {     
 
        String gapsFile = args[3];
        String refSeqGeneModelFile = args[0];
        String gencodeGeneModelFile = args[4];
        String genelistFile = args[1];
        GapsTranslator gapsTranslator 
            = new GapsTranslator(gapsFile, refSeqGeneModelFile, genelistFile);
        
        gapsTranslator.addGeneModel(gencodeGeneModelFile);
        geneModelCollection.aggregateTranscriptsByGenes();
  
        int splicingDistance = Integer.valueOf(args[2]);
        List<Interval> targets 
            = geneModelCollection
                .createTargets(
                    geneNamesInTest,
                    splicingDistance);
        
        
        for (Interval target : targets) {
            System.out.println(target);
        }
        
        System.exit(0);
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
