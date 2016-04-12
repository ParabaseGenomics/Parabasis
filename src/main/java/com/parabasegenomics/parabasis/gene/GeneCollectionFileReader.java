/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.gene;

import htsjdk.samtools.util.Interval;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to read a file containing a list of gene models as downloaded from the 
 * UCSC Table Browser.
 * 
 * * The following is the expected schema for the file containing the gene models.
 * Header line: 
 * #bin    name    chrom   strand  txStart txEnd   cdsStart        cdsEnd  exonCount       exonStarts      exonEnds        score   name2   cdsStartStat    cdsEndStat      exonFrames
 * 
 * One transcript per line, with exon starts and ends comma-separated and in 
 * coordinate order.
 * 1       NM_018090       chr1    +       16767166        16786584        16767256        16785385        8       16767166,16770126,16774364,16774554,16775587,16778332,16782312,16785336,        16767348,16770227,16774469,16774636,16775696,16778510,16782388,16786584,        0       NECAP2  cmpl    cmpl    0,2,1,1,2,0,1,2,
 * 
 * @author evanmauceli
 */
public class GeneCollectionFileReader {
      
    private final BufferedReader reader;
    private final File geneModelFile;
    
    final static String comma = ",";
    final static String plus = "+";
    final static String minus = "-";
    final static String hash = "#";
    final static String hap = "hap";
    
    final static int transcriptNameColumn = 1;
    final static int chromosomeColumn = 2;
    final static int strandColumn = 3;
    final static int transcriptStartColumn = 4;
    final static int transcriptEndColumn = 5;
    final static int codingStartColumn = 6;
    final static int codingEndColumn = 7;
    final static int exonCountColumn = 8;
    final static int exonStartsColumn = 9;
    final static int exonEndsColumn = 10;
    final static int geneNameColumn = 12;
    
    
    
    /**
     * Constructor.  Takes the full path to the file with the gene models
     * and creates the BufferedReader.
     * @param file File with the gene models.
     * 
     * @throws FileNotFoundException 
     */
    public GeneCollectionFileReader(String file) 
    throws FileNotFoundException {
        geneModelFile = new File(file);
        reader = new BufferedReader(new FileReader(geneModelFile));   
    }
    
    /**
     * Method to parse the file with the gene models into transcripts and add
     * those to the GeneModel object.
     * 
     * @param transcripts Repository for transcripts read from the gene collection
     * file.
     * @throws IOException 
     */
    public void readFile(List<Transcript> transcripts) 
    throws IOException {

        String line;
        readHeader();
        while (reader.ready()) {
            line = reader.readLine();
            if (!line.isEmpty()) {
                Transcript transcript = convertLineToTranscript(line);
                if (transcript != null) {
                    transcripts.add(transcript);
                }  
            }
        }              
    }
    
    /**
     * Method to convert a line of test to a Transcript which gets added to 
     * the GeneModel.
     * @return Transcript object created from the input String.
     * @param line A single non-header line from the file containing the gene 
     *  models.
     * 
     * @throws IOException 
     */
    protected Transcript convertLineToTranscript(String line) 
    throws IOException {
      
        if (line.startsWith(hash)) {
            throw new IOException("Line starts with # and should not be header.");
        }
        
        // don't attempt to convert empty lines
        if (line.isEmpty()) {
            return null;
        }        
        
        String [] tokens = line.split("\\t");

        String transcriptName = tokens[transcriptNameColumn];
        String geneName = tokens[geneNameColumn];
        String chromosome = tokens[chromosomeColumn];
        
        // Do not allow alternate haplotypes as we're not set up to handle 
        // them.
        if (chromosome.contains(hap)) {
            return null;
        }
        String strand = tokens[strandColumn];
        int transcriptStart
            = Integer.valueOf(tokens[transcriptStartColumn]);
        int transcriptEnd
            = Integer.valueOf(tokens[transcriptEndColumn]);
        int codingStart
            = Integer.valueOf(tokens[codingStartColumn]);
        int codingEnd
            = Integer.valueOf(tokens[codingEndColumn]);

        int exonCount = Integer.valueOf(tokens[exonCountColumn]);
        
        List<Exon> exons 
            = parseExons(
                exonCount,
                chromosome,
                tokens[exonStartsColumn], 
                tokens[exonEndsColumn],
                strand,
                codingStart,
                codingEnd);
        
        boolean isRC = FALSE;
        if (strand.equals(minus)) {
            isRC = TRUE;
        }
        
        Interval transcriptSpan 
            = new Interval(chromosome,transcriptStart,transcriptEnd,isRC,"transcript");
        Interval codingSpan 
            = new Interval(chromosome,codingStart,codingEnd,isRC,"coding");
        
        Transcript transcript 
            = new Transcript(
                transcriptName,
                geneName,
                strand,
                exonCount,
                transcriptSpan,
                codingSpan,
                exons);
            
        return transcript;
    }
    
    
    /**
     * Method to convert a string of exon starts and exons endsto a list of 
     * Interval objects. 
     * @param expectedExonCount Expected number of exons for this transcript.
     * @param chromosome Chromosome this gene is on.
     * @param exonStarts Comma-separated list of exon starting coordindates.
     * @param exonEnds Comma-separated list of exon ending coordinates.
     * @param strand Gene orientation is forward ("+") or rc ("-").
     * @param codingStart Start of coding region for this transcript.
     * @param codingEnd End of coding region for this transcript.
     * 
     * @return Returns a list of Interval objects corresponding to this 
     * transcript's exons.
     * 
     * @throws java.io.IOException Probably could be a better exception...
     */
    protected List<Exon> parseExons(
        int expectedExonCount,
        String chromosome,
        String exonStarts, 
        String exonEnds,
        String strand,
        int codingStart,
        int codingEnd) 
    throws IOException {
        
        List<Exon> returnList = new ArrayList<>();
        
        boolean isRC = FALSE;
        if (strand.equals(minus)) {
            isRC = TRUE;
        }
        
        Interval codingIntervalOfTranscript 
            = new Interval(chromosome,codingStart,codingEnd);
        
        String [] startTokens = exonStarts.split(comma);
        String [] endTokens = exonEnds.split(comma);
        
        if ( (startTokens.length != endTokens.length) 
            || (startTokens.length != expectedExonCount)) {
            throw new IOException("Exon count is not as expected: " 
                + startTokens.length + "\t"
                + endTokens.length + "\t"
                + expectedExonCount);
        }
        
        for (int i=0; i<startTokens.length; i++) {
            int exonStart = Integer.valueOf(startTokens[i]);
            int exonEnd = Integer.valueOf(endTokens[i]);
            String exonName = Integer.toString(i+1);
            if (isRC) {
                Integer index = startTokens.length - i;
                exonName = index.toString();
            }
 
            Interval exonInterval
                = new Interval(chromosome,exonStart,exonEnd,isRC,exonName);
            
            Interval codingExonInterval = null;
            
            Interval overlapInterval = null;
            if (exonInterval.intersects(codingIntervalOfTranscript)) {
                    overlapInterval 
                        = exonInterval.intersect(codingIntervalOfTranscript);
      
                    codingExonInterval 
                        = new Interval(
                            chromosome,
                            overlapInterval.getStart(),
                            overlapInterval.getEnd(),
                            isRC,
                            exonName);
            }
            
            Exon newExon = new Exon(exonInterval);
            newExon.addCodingInterval(codingExonInterval);
            returnList.add(newExon);                      
        }
        
        return returnList;
    }
    
    
    
    /**
     * Method to read the header line of a file.  Currently is not
     * used other than to move the FileReader to the first line containing a 
     * transcript.
     * 
     * @throws IOException 
     */
    protected void readHeader()
    throws IOException {
      
       String line = reader.readLine();
       if (!line.startsWith(hash)) {
           throw new IOException("File does not have a header.");
       }
       
    }
    
    
}
