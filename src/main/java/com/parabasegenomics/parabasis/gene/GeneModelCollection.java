/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.gene;

import com.parabasegenomics.parabasis.util.Reader;
import htsjdk.samtools.util.Interval;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The GeneModelCollection class provides access to a Reference Sequence collection 
 * of gene models such as RefSeq.
 * 
 * @author evanmauceli
 */
public class GeneModelCollection {
    
    private final String FULL_LENGTH = "_FL";
    
    private final List<Transcript> transcripts;  
    private final List<GeneModel> genes;
    
    private String modelName;
    private String modelVersion;
    private boolean isSorted;
    
    private Transcript collapsedTranscript;
    
    private GeneCollectionFileReader geneCollectionFileReader;
    private Reader utilityReader;
    
    /**
     * Constructor
     */
    public GeneModelCollection() {      
        transcripts = new ArrayList<>();  
        isSorted = false;    
        genes = new ArrayList<>();
    }
    
       
    /**
     * Full length genes have "_FL" appended to the end of the gene name
     * in the list of genes to target.
     * @param gene Name of the gene.
     * @return Returns true if the gene has "_FL" appended to the 
     * end (full-length), false otherwise.
     */
    public boolean isFullLength(String gene) {
        return (gene.contains(FULL_LENGTH));
    }
    
    /**
     * Turns the given list of genes into a list of sequencing targets.
     * 
     * @param genesToTarget Input list of genes to target.
     * @param splicingDistance Distance in bp from exon/intron boundary to look
     * for splicing effects. 10 bp is standard.
     * @return Returns a list of targets as chr, start, end.
     * @throws java.io.IOException
     */
    public List<Interval> createTargets(
        Set<String> genesToTarget, 
        int splicingDistance) 
    throws IOException {
       
        List<Interval> targets = new ArrayList<>();
        
        for (String gene : genesToTarget) {
            String realGeneName = gene;
            if (isFullLength(gene)) {
                realGeneName = gene.substring(0,gene.length()-3);
            }
            
            //System.out.println("gene " + gene +" "+realGeneName);
            int index=0;
            for (; index<genes.size(); index++) {
                if (genes.get(index).getGeneName().equals(realGeneName)) {
                    break;
                }
            }
            
            if (index==genes.size()) {
                System.out.println("Cannot find " + realGeneName + " in models.");
                continue;
                //throw new IOException("Cannot find " + realGeneName + " in models.");
            }
            
            if (isFullLength(gene)) {
                GeneModel geneModel = genes.get(index);
                Transcript transcript = geneModel.getCollapsedTranscript();
                if (transcript == null) {
                    geneModel.Collapse();
                }
                
                targets.add(
                    genes
                        .get(index)
                        .getCollapsedTranscript()
                        .getTranscriptInterval());
                            
            } else {
                GeneModel geneModel = genes.get(index);
                Transcript transcript = geneModel.getCollapsedTranscript();
                if (transcript == null) {
                    geneModel.Collapse();
                    transcript = geneModel.getCollapsedTranscript();
                }
                       
                String chromosome = transcript.getChromosome();
                Exon exon = transcript.get5primeExon();
                String name = realGeneName + "_" + exon.getName();
                
                int start = exon.getStart();
                int end = exon.getEnd();
                if (transcript.getExonCount() > 1) {
                    if (transcript.isRC()) {
                        start -= splicingDistance;
                    } else {
                        end += splicingDistance;
                    }
                }
                
                //System.out.println("adding 5ptarget " + chromosome +" "+start+" "+end);
                targets.add(new Interval(chromosome,start,end,transcript.isRC(),name));
                
                while (transcript.hasNextExon()) {
                    exon = transcript.getNextExon();
                    name = realGeneName + "_" + exon.getName();
                    if (!transcript.is3primeExon()) {
                        start = exon.getStart() - splicingDistance;
                        end = exon.getEnd() + splicingDistance;
                         //System.out.println("adding target " + chromosome +" "+start+" "+end);
                        targets
                            .add(new Interval(chromosome,start,end,transcript.isRC(),name));
                    } else {
                        start = exon.getStart();
                        end = exon.getEnd();
                        if (transcript.isRC()) {
                            end += splicingDistance;
                        } else {
                            start -= splicingDistance;
                        }
                         //System.out.println("adding 3ptarget " + chromosome +" "+start+" "+end);
                        targets
                            .add(new Interval(chromosome,start,end,transcript.isRC(),name)); 
                    }                   
                } 
            }
         }
                   
        return targets;
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
    
    public void addGeneModelCollection(String file) 
    throws FileNotFoundException, IOException {
       List<Transcript> transcriptsToAdd = new ArrayList<>();
       geneCollectionFileReader 
            = new GeneCollectionFileReader(file);
        
        geneCollectionFileReader.readFile(transcriptsToAdd);
        
        Set<String> genesAlreadyLoaded = new HashSet<>();
        for (Transcript transcript : transcripts) {
            genesAlreadyLoaded.add(transcript.getGeneName());
        }
        
        for (Transcript transcriptToAdd : transcriptsToAdd) {
            if (!genesAlreadyLoaded.contains(transcriptToAdd.getGeneName())) {
               transcripts.add(transcriptToAdd);
            }
        }
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
     * Return the name of the gene model, i.e. "RefSeq".
     * @return 
     */
    public String getName() {
        return modelName;
    }
    /**
     * Method that returns the list of gene models held by the collection.
     * @return 
     */
    public List<GeneModel> getGeneModels() {
        if (!genes.isEmpty()) {
            return genes;
        } else {
            return null;
        }
    }
    
    /**
     * Returns gene models for the requested list of genes.
     * @param genesToSelect The list of gene names to collect and return.
     * @return 
     */
    public List<GeneModel> selectGeneModels(Set<String> genesToSelect) {
        List<GeneModel> selected = new ArrayList<>();
        
        for (GeneModel gene : genes) {
            if (genesToSelect.contains(gene.getGeneName())) {
                selected.add(gene);
            }
        }
        
        return selected;
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
            geneModel.addTranscript(transcripts.get(beginIndex));
            endIndex = beginIndex+1;
            
            while (endIndex < transcriptCount 
                && transcripts.get(endIndex).getGeneName().equals(thisGene)) {
                geneModel.addTranscript(transcripts.get(endIndex));

                endIndex++;
            }
        
            genes.add(geneModel);           
            beginIndex = endIndex;
            
        }
   
    }
}
