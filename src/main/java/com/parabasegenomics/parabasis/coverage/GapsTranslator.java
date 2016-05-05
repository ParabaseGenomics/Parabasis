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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
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
    
    /**
     * Write targets to a file.
     * @param file
     * @param targets
     * @throws IOException 
     */
    public static void writeTargets(File file, List<Interval> targets) 
    throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        
        for (Interval target : targets) {
            
            String strand = "+";
            if (target.isNegativeStrand()) {
                strand = "-";
            }
            int length = Math.abs(target.getStart()-target.getEnd());
            String targetString = 
                target.getContig()
                + "\t"
                + target.getStart()
                + "\t"
                + target.getEnd()
                + "\t"
                + strand
                + "\t"
                + target.getName()
                + "\t"
                + Integer.toString(length);
                
            writer.write(targetString);
            writer.newLine();
            //System.out.println(target.toString());
        }
        writer.close();
            
    }
    
    public static void main(String[] args) 
    throws IOException {     
 
        String refSeqGeneModelFile = args[0];
        String genelistFile = args[1];
        int splicingDistance = Integer.valueOf(args[2]);
        String gapsFile = args[3];
        String gencodeGeneModelFile = args[4];
        String targetsFile = args[5];
        String codingTargetsFile = args[6];

        GapsTranslator gapsTranslator 
            = new GapsTranslator(gapsFile, refSeqGeneModelFile, genelistFile);
        
        gapsTranslator.addGeneModel(gencodeGeneModelFile);
        geneModelCollection.aggregateTranscriptsByGenes();
  
        
        List<Interval> targets 
            = geneModelCollection
                .createTargets(
                    geneNamesInTest,
                    splicingDistance);
        
        writeTargets(new File(targetsFile),targets);
              
        List<Interval> codingTargets
            = geneModelCollection
                .createCodingTargets(
                    geneNamesInTest, 
                    splicingDistance);

        writeTargets(new File(codingTargetsFile),codingTargets);
        
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
