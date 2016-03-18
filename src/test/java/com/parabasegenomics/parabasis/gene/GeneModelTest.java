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
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author evanmauceli
 */
public class GeneModelTest {
    
    private final Interval transcriptInterval = new Interval("chr1",66999824,67000051);
    private final Interval codingInterval = new Interval("chr1",67000041,67000051);
    
    private final Interval secondTranscriptInterval 
        = new Interval("chr1",67091529,67091593);
    private final Interval secondCodingInterval
        = new Interval("chr1",67091529,67091559);
    
    
    private List<Exon> exons;
    private List<Exon> secondExonList;
    private Transcript transcript;
    private Transcript secondTranscript;
    private Transcript collapsedTranscript;
    
    private final String name = "NM_032291";
    private final String gene = "SGIP1";
    private final String strand = "+";
    private final int exonCount = 2;

    
    public GeneModelTest() {
    }
    
    @Before
    public void setUp() {
        exons = new ArrayList<>();
        exons.add(new Exon(new Interval("chr1",66999824,67000051,FALSE,"1")));
        //exons.add(new Exon(new Interval("chr1",67091529,67091593,FALSE,"2")));
        
        transcript
            = new Transcript(
                name,
                gene,
                strand,
                1,
                transcriptInterval,
                codingInterval,
                exons);
        
        secondExonList = new ArrayList<>();
        secondExonList.add(new Exon(new Interval("chr1",67091529,67091593,FALSE,"2")));
            
        secondTranscript
            = new Transcript(
                "NM_032292",
                gene,
                strand,
                1,
                secondTranscriptInterval,
                secondCodingInterval,
                secondExonList);
        
       Interval collapsedTranscriptInterval
           = new Interval("chr1",66999824,67091593);
       Interval collapsedCodingInterval
           = new Interval("chr1",67000041,67091559);
       
       List<Exon> collapsedExons = new ArrayList<>();
       collapsedExons.add(new Exon(new Interval("chr1",66999824,67000051,FALSE,"1")));
       collapsedExons.add(new Exon(new Interval("chr1",67091529,67091593,FALSE,"3")));
        collapsedTranscript 
            = new Transcript(
                name,
                gene,
                strand,
                2,
                collapsedTranscriptInterval,
                collapsedCodingInterval,
                collapsedExons);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of addTranscript method, of class GeneModel.
     */
    @Test
    public void testAddTranscript() {
        System.out.println("addTranscript");
        GeneModel instance = new GeneModel();
        instance.addTranscript(transcript);
        
        int expected = 1;
        int actual = instance.getTranscriptCount();
        
        Assert.assertEquals(expected, actual);
    }

    /**
     * Test of setGeneName method, of class GeneModel.
     */
    @Test
    public void testSetGeneName() {
        System.out.println("setGeneName");
        GeneModel instance = new GeneModel();
        instance.setGeneName(name);
        
        Assert.assertEquals(name, instance.getGeneName());
    }

    /**
     * Test of Collapse method, of class GeneModel.
     */
    @Test
    public void testCollapse() 
    throws IOException {
        System.out.println("Collapse");
        GeneModel instance = new GeneModel();
        instance.addTranscript(transcript);
        instance.addTranscript(secondTranscript);
        instance.Collapse();
       
        Transcript actual = instance.getCollapsedTranscript();
        Transcript expected = collapsedTranscript;
        
        Assert.assertEquals(expected.getTranscriptStart(), actual.getTranscriptStart());
        Assert.assertEquals(expected.getTranscriptEnd(), actual.getTranscriptEnd());
        Assert.assertEquals(expected.getCodingStart(), actual.getCodingStart());
        Assert.assertEquals(expected.getCodingEnd(), actual.getCodingEnd());
        Assert.assertEquals(2, expected.getExonCount());
    }
    
    @Test
    public void testOverlap() 
    throws IOException {
        System.out.println("overlap");
        GeneModel instance = new GeneModel();
        instance.setGeneName(gene);
        instance.addTranscript(transcript);
        instance.addTranscript(secondTranscript);
        instance.Collapse();
          
        Interval intervalToOverlap  = new Interval("chr1",66999700,67091540);
        
        String actual = instance.overlap(intervalToOverlap);
        String expected = "SGIP1:NM_032291-Exon1-NM_032292-Exon2";
         Assert.assertEquals(expected,actual);
        
    }
     
    @Test
    public void testOverlapWithIntron() 
    throws IOException {
        System.out.println("overlap");
        GeneModel instance = new GeneModel();
        instance.setGeneName(gene);
        instance.addTranscript(collapsedTranscript);
        instance.Collapse();
          
        Interval intervalToOverlap  = new Interval("chr1",66999700,67091540);
        
        String actual = instance.overlap(intervalToOverlap);
        String expected = "SGIP1:NM_032291-Exon1-Intron1(splicing5p)(splicing3p)-Exon3";
        
        Assert.assertEquals(expected,actual);
        
    }   
    
}
