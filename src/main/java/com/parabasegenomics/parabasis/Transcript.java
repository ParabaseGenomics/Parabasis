/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis;

import htsjdk.samtools.util.Interval;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import java.util.List;
import java.util.ListIterator;

/**
 * Class to encapsulate what it means to be a transcript. In this case, a "transcript"
 * corresponds to one line from a file containing gene models, such as downloaded
 * from UCSC.
 * 
 * @author evanmauceli
 */
public class Transcript implements Comparable<Transcript> {
    
    private int numberOfExons;
    private final List<Exon> exons;
    private final String transcriptName;
    private final String geneName;
    private final Interval transcriptInterval;
    private final Interval codingInterval;
    private boolean isRC;
    
    private ListIterator<Exon> exonIterator;
    
    /**
     * Constructor.  
     * @param name The transcript name, usually something like "NM_xxx".
     * @param gene The gene name.
     * @param strand The strand the transcript is on. Either "+" (forward strand) 
     *  or "-" (reverse strand).
     * @param numberOfExons The number of exons in the transcript.
     * @param transcriptSpan The genomic coordinates spanned by the transcript.
     * @param codingSpan The genomic coordinates spanned by the coding portion 
     *  of the transcript.
     * @param exonList The list of coordinates of the individual exons in this 
     *  transcript.
     */
    public Transcript(
        String name, 
        String gene,
        String strand,
        int numberOfExons,
        Interval transcriptSpan,
        Interval codingSpan,
        List<Exon> exonList) {
            transcriptName=name;
            geneName=gene;
            transcriptInterval=transcriptSpan;
            codingInterval=codingSpan;
            exons=exonList;
            
            isRC=FALSE;
            if (strand.equals("-")) {
                isRC=TRUE;
            }
        }
    
    /**
     * Basic getter methods.
     */
    public String getTranscriptName() {
        return transcriptName;
    }
    
    public String getGeneName() {
        return geneName;
    }
    
    public boolean isRC() {
        return isRC;
    }
    
    public String getStrand() {
        if (isRC) {
            return "-";
        } else {
            return "+";
        }
    } 
    
    /**
     * Method to sort Transcripts by gene name. Implements Comparable::compareTo
     * leveraging String::compareTo.
     * @param toCompare Comparison transcript.
     * @return int with results of the comparison.
     */
    @Override
    public int compareTo(Transcript toCompare) {
        return (geneName.compareTo(toCompare.getGeneName()));
    }
    
    /**
     * From the transcript's perspective, get the first exon. Leaves 
     * the exonIterator at the next exon in the transcript.
     * @return The first exon of the transcript as an Interval, or null if 
     *  no exons, which in this case would be pathological.
     */
    public Exon get5primeExon() {
       
        // if this is a forward transcript, we've got an iterator to the first
        // exon.  If not, we'll have to go in reverse.
        if (!isRC) {
           exonIterator = exons.listIterator();
           if (exonIterator.hasNext()) {
               return exonIterator.next();
           } else {
               return null;
           }           
        } else {
          exonIterator = exons.listIterator(exons.size());
          if (exonIterator.hasPrevious()) {
              return exonIterator.previous();
          } else {
              return null;
          }   
        }
    }
    
    /**
     * Get the next exon in the transcript reading 5' to 3'.  Must be called 
     * after get5primeExon().
     * 
     * @return The next exon in the transcript, or null if no more. 
     */
    public Exon getNextExon() {
        if (!isRC) {
            if (exonIterator.hasNext()) {
                return exonIterator.next();
            } else {
                return null;
            }
        } else {
            if (exonIterator.hasPrevious()) {
                return exonIterator.previous();
            } else {
                return null;
            }
        }
    }
    
}

