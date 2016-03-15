/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.gene;

import com.parabasegenomics.parabasis.gene.Transcript;
import com.parabasegenomics.parabasis.gene.Exon;
import htsjdk.samtools.util.Interval;
import java.io.IOException;
import static java.lang.Boolean.FALSE;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author evanmauceli
 */
public class TranscriptTest {
  
    private final String name = "NM_032291";
    private final String gene = "SGIP1";
    private final String strand = "+";
    private final int exonCount = 2;
    private final Interval transcriptInterval = new Interval("chr1",66999824,67210768);
    private final Interval codingInterval = new Interval("chr1",67000041,67208778);
    
    private List<Exon> exons;
    private Transcript transcript;
    
    public TranscriptTest() {
         
    }
    
    @Before
    public void setUp() {
        
        exons = new ArrayList<>();
        exons.add(new Exon(new Interval("chr1",66999824,67000051,FALSE,"1")));
        exons.add(new Exon(new Interval("chr1",67091529,67091593,FALSE,"2")));
        
        transcript
            = new Transcript(
                name,
                gene,
                strand,
                exonCount,
                transcriptInterval,
                codingInterval,
                exons);
        
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test that we can get the correct first exon when the transcript has
     * a forward orientation.
     */
    @Test
    public void testGet5primeExonFW() {
        System.out.println("get 5' exon");
        Exon firstExon = transcript.get5primeExon();
        
        int expectedStart = 66999824;
        int expectedEnd = 67000051;
        Assert.assertEquals(expectedStart, firstExon.getStart());
        Assert.assertEquals(expectedEnd,firstExon.getEnd());
        Assert.assertEquals("1",firstExon.getName());
        
    }
    
    /**
     * Test that we can get the next exon in the transcript.  Note the call
     * to get5primeExon() to set up the iterator.
     */
    @Test
    public void testGetNextExonFW() 
    throws IOException {
        System.out.println("get next forward exon");
        
        transcript.get5primeExon();
        
        Exon nextExon = transcript.getNextExon();
        
        int expectedStart = 67091529;
        int expectedEnd = 67091593;
        
        Assert.assertEquals(expectedStart, nextExon.getStart());
        Assert.assertEquals(expectedStart, nextExon.getStart());
        Assert.assertEquals(expectedEnd,nextExon.getEnd());
        Assert.assertEquals("2",nextExon.getName());
        
        Assert.assertEquals(null,transcript.getNextExon());
    }
    
    
    /**
     * Test that we can get the correct first exon when the transcript has
     * a reverse orientation.
     */
    @Test
    public void testGet5primeExonRC() {
        System.out.println("get 5' exon reverse transcript");
       
        List<Exon> rcExons = new ArrayList<>();
        rcExons.add(new Exon(new Interval("chr1",66999824,67000051,FALSE,"2")));
        rcExons.add(new Exon(new Interval("chr1",67091529,67091593,FALSE,"1")));
        
        Transcript t = new Transcript(
            name,gene,"-",exonCount,transcriptInterval,codingInterval,rcExons);
        
        Exon firstExon = t.get5primeExon();
        
        int expectedStart = 67091529;
        int expectedEnd = 67091593;
        
        Assert.assertEquals(expectedStart, firstExon.getStart());
        Assert.assertEquals(expectedStart, firstExon.getStart());
        Assert.assertEquals(expectedEnd,firstExon.getEnd());
        Assert.assertEquals("1",firstExon.getName());
    }
    
    /**
     * Test that comparison by gene name is working for a List of Transcript 
     * objects.
     */
    @Test
    public void testCompareTo() {
        System.out.println("comparator check");
        
        Transcript t = new Transcript(
            name,"NOTAGENE","-",exonCount,transcriptInterval,codingInterval,exons);
        
        List<Transcript> transcripts = new ArrayList<>();
        transcripts.add(transcript);
        transcripts.add(t);
        
        Collections.sort(transcripts);
        
        Assert.assertEquals(
            transcripts.get(0).getGeneName(),
            t.getGeneName());
        
    }
    
    @Test
    public void testCompareToBadOrder() {
        System.out.println("comparator check (bad order)");
        
        Transcript t = new Transcript(
            name,"NOTAGENE","-",exonCount,transcriptInterval,codingInterval,exons);
        
        List<Transcript> transcripts = new ArrayList<>();
        transcripts.add(t);
        transcripts.add(transcript);
        
        Collections.sort(transcripts);
        
        Assert.assertEquals(
            transcripts.get(0).getGeneName(),
            t.getGeneName());
        
    }
    
}
