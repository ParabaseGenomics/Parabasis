/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.gene;

import htsjdk.samtools.util.Interval;
import java.io.IOException;
import static java.lang.Boolean.FALSE;
import java.util.ArrayList;
import java.util.List;


//TODO - full length genes
//TODO - id non-coding genes

/**
 * The GeneModel class provides translations from a gene model (represented by
 * a list of transcripts) to genomics coordinates.
 * 
 * @author evanmauceli
 */
public class GeneModel {
    
    private final static String dash = "-";
    private final static String colon = ":";
    
    
    private final List<Transcript> transcripts;   
    private Transcript collapsedTranscript;
    
    private String geneName;
    private int spliceDistance;
    
    /**
     * Constructor
     */
    public GeneModel() {      
        transcripts = new ArrayList<>();  
        spliceDistance = 10;
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
    public void setSpliceDistance(int s) {
        spliceDistance=s;
    }
    
    /**
     * Getter methods
     * @return 
     */
    public int getTranscriptCount() {
        return transcripts.size();
    }
    public String getGeneName() {
        return geneName;
    }
    public Transcript getCollapsedTranscript() {
        return collapsedTranscript;
    }
    public int getSpliceDistance() {
        return spliceDistance;
    }
    
    
    /**
     * Method to collapse all transcripts from a gene into a flat representation 
     * on the reference sequence. Sets the "collapsedTranscript" member variable.
     * Useful for creating a list of genomic targets from a gene list, for
     * example.
     * @throws java.io.IOException
     */
    public void Collapse() 
    throws IOException {
        Interval transcriptSpanInterval = getTranscriptSpanThisGene();
       
        String chromosome = transcriptSpanInterval.getContig();
        
        // rely on java spec to enforce that ints are initialized to zero
        int [] sequenceArray = new int[transcriptSpanInterval.length()];
   
        int offset = transcriptSpanInterval.getStart();
        
        /**
         * loop over transcripts, filling sequenceArray as coding/non-coding
         * which we'll use to collapse the transcripts.
         * 
         * For this exercise, non-coding=1, coding=2 
         * (not covered by an exon = 0)
         * 
         * Once a base is annotated as coding (2), further transcripts won't
         * change this setting.
         * 
         */
        for (Transcript transcript : transcripts) {           
            int codingStart = transcript.getCodingStart();
            int codingEnd = transcript.getCodingEnd();
 
            Exon exon = transcript.get5primeExon();
            if (exon == null) {
                throw new IOException("no 5prime exon.");
            }
            for (int i=exon.getStart(); i<exon.getEnd(); i++) {
                if (i<codingStart || i>codingEnd) {
                    sequenceArray[i-offset] 
                        = Math.max(sequenceArray[i-offset],1);
                } else {
                    sequenceArray[i-offset] = 2;
                }
            }
            
            while (transcript.hasNextExon()) {
                exon = transcript.getNextExon();
                if (exon == null) {
                    throw new IOException("no exon, but one expected.");
                }
                for (int i=exon.getStart(); i<exon.getEnd(); i++) {
                   if (i<codingStart || i>codingEnd) {
                        sequenceArray[i-offset] 
                            = Math.max(sequenceArray[i-offset],1);
                    } else {
                        sequenceArray[i-offset] = 2;
                    }
                }
            }
        }
        
        /**
         * traverse through the sequenceArray, filling out the collapsed
         * transcript
         */
        boolean markup=false;
        int start=0;
        int end=0;
        int codingStart=0;
        int codingEnd=0;
        List<Exon> collapsedExons = new ArrayList<>();
        for (int i=0; i<sequenceArray.length; i++) {
            if (codingStart==0 && sequenceArray[i]==2) {
                codingStart = i+offset;
            }
            if (sequenceArray[i]==2) {
                codingEnd = i+offset;
            }
            
            if (!markup && sequenceArray[i]>0) {
                markup=true;
                start=i+offset;
            } else if (markup && sequenceArray[i]==0) {
                markup=false;
                end=i;
                Exon exon = new Exon(
                    new Interval(
                        chromosome,start,end));
                collapsedExons.add(exon);
            }     
        }
        
        Interval codingInterval 
            = new Interval(chromosome,codingStart,codingEnd);
        
        collapsedTranscript 
            = new Transcript(
                geneName,
                geneName,
                transcripts.get(0).getStrand(),
                collapsedExons.size(),
                transcriptSpanInterval,
                codingInterval,
                collapsedExons);
                
        
    }

    
    /**
     * Method to return the maximum genomic span of this gene given all the 
     * transcripts.
     * @return Returns an Interval with the maximum genomic span for this gene
     * given all the associated transcripts.
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
    
        /**
     * Method to return the maximum genomic span of the coding portion of this
     * gene given all the transcripts.
     * @return 
     * 
     */
    private Interval getCodingSpanThisGene() {
        int index = 0;
        int minCodingStart = transcripts.get(index).getCodingStart();
        int maxCodingEnd = transcripts.get(index).getCodingEnd();
        
        for (; index<transcripts.size(); index++) {
            minCodingStart = Math.min(
                minCodingStart,transcripts.get(index).getCodingStart());
            
            maxCodingEnd = Math.max(
                maxCodingEnd,transcripts.get(index).getCodingEnd());
        }
             
        return new Interval(
            transcripts.get(0).getChromosome(),
            minCodingStart,
            maxCodingEnd);
    }
    
    /**
     * Method to find overlap between the given Interval and a gene model. 
     * 
     * @param interval Takes an Interval.
     * @return Returns a String with the portions of each transcript overlapping
     * the given interval. Returns null if there is no overlap.
     * 
     * TODO: 5', 3' utr where applicable
     * TODO: promoter.
     * 
     * @throws java.io.IOException
     * 
     */
    public String overlap(Interval interval) 
    throws IOException {
        String overlapString = null;
               
        // fail as early as possible
        if (!interval.intersects(collapsedTranscript.getTranscriptInterval())) {
            return overlapString;
        }
        
        int intronStart = 0;
        int intronEnd = 0;
        int intronIndex = 1;
        String chromosome = transcripts.get(0).getChromosome();
        
        StringBuilder overlapStringBuilder = new StringBuilder();
        
        overlapStringBuilder.append(geneName);
        overlapStringBuilder.append(colon);
        for (Transcript transcript : transcripts) {
            if (!transcript.getTranscriptInterval().intersects(interval)) {
                continue;
            }
            
            overlapStringBuilder.append(transcript.getTranscriptName());
            overlapStringBuilder.append(dash);
            
            Exon exon = transcript.get5primeExon();
            if (!transcript.isRC()) {
                intronStart = exon.getEnd();
            } else {
                intronEnd = exon.getStart()-1;
            }
            if (exon.getInterval().intersects(interval)) {
                overlapStringBuilder.append("Exon").append(exon.getName());
                overlapStringBuilder.append(dash);
            }
            
            // loop over any other exons
            while (transcript.hasNextExon()) {
                exon = transcript.getNextExon();
                if (!transcript.isRC()) {
                    intronEnd = exon.getStart()-1;
                } else {
                    intronStart = exon.getEnd();
                }
                
                Interval intron 
                    = new Interval(
                        chromosome, 
                        intronStart,
                        intronEnd,
                        FALSE,
                        Integer.toString(intronIndex));
                
                /**
                 * this comes before the exon overlap test so the string we're  
                 * building runs 5'->3' along the transcript. 
                 */
                if (intron.intersects(interval)) {
                    overlapStringBuilder.append("Intron").append(intron.getName());
                    
                    // check if the gap overlaps a splicing region
                    if (!transcript.isRC()) {
                        if (interval.getStart() <= intron.getStart() 
                            && interval.getEnd() >= intron.getStart()+getSpliceDistance()) {
                            overlapStringBuilder.append("(splicing5p)");
                        } 
                        if (interval.getStart() <= intron.getEnd()
                            && interval.getEnd() >= intron.getEnd()) {
                            overlapStringBuilder.append("(splicing3p)");
                        }
                    } else {
                         if (interval.getStart() <= intron.getStart() 
                            && interval.getEnd() >= intron.getStart()+getSpliceDistance()) {
                            overlapStringBuilder.append("(splicing3p)");
                        } 
                        if (interval.getStart() <= intron.getEnd()
                            && interval.getEnd() >= intron.getEnd()) {
                            overlapStringBuilder.append("(splicing5p)");
                        }
                    }
                    
                    overlapStringBuilder.append(dash);     
                }
                
                if (exon.getInterval().intersects(interval)) {
                    overlapStringBuilder.append("Exon").append(exon.getName());
                    overlapStringBuilder.append(dash);     
                }
                
                // keep this half of the intron around in case there is another
                // exon
                if (!transcript.isRC()) {
                    intronStart = exon.getEnd();
                } else {
                    intronEnd = exon.getStart()-1;
                }
                intronIndex++;
            }
        }
        
        // remove the trailing dash before returning
        overlapStringBuilder.deleteCharAt(overlapStringBuilder.length()-1);
        
        return overlapStringBuilder.toString();
    }
    
    /**
     * Method to find overlap between the given Interval and the coding
     * portion of a gene model. 
     * 
     * @param interval Takes an Interval.
     * @return Returns a String with the portions of each transcript overlapping
     * the given interval. Returns null if there is no overlap.
     * 
     * TODO: 5', 3' utr where applicable
     * TODO: introns, promoter.
     * TODO: splicing
     * TODO: remove trailing dash
     * 
     * @throws java.io.IOException
     * 
     */
    public String overlapCoding(Interval interval) 
    throws IOException {
        String overlapString = null;
               
        // fail as early as possible
        if (!interval.intersects(collapsedTranscript.getCodingInterval())) {
            return overlapString;
        }
        
        StringBuilder overlapStringBuilder = new StringBuilder();
        
        overlapStringBuilder.append(geneName);
        overlapStringBuilder.append(colon);
        for (Transcript transcript : transcripts) {
            if (!transcript.getCodingInterval().intersects(interval)) {
                continue;
            }
            
            overlapStringBuilder.append(transcript.getTranscriptName());
            overlapStringBuilder.append(dash);
            
            Exon exon = transcript.get5primeExon();
            if (exon.getCodingExon().getInterval().intersects(interval)) {
                overlapStringBuilder.append("Exon"+exon.getName());
                overlapStringBuilder.append(dash);
            }
            while (transcript.hasNextExon()) {
                exon = transcript.getNextExon().getCodingExon();
                if (!exon.getInterval().intersects(interval)) {
                    continue;
                }
                overlapStringBuilder.append("Exon"+exon.getName());
                overlapStringBuilder.append(dash);             
            }         
        }
        
        return overlapStringBuilder.toString();
    }
}
