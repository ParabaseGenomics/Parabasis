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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author evanmauceli
 */
public class GeneModelCollectionTest {
    
    private final Interval transcriptInterval = new Interval("chr1",66999824,67000051);
    private final Interval codingInterval = new Interval("chr1",67000041,67000051);
    private final String file = "src/main/resources/refseq_genemodel.txt";
    
    private final List<Exon> exons;
    private List<Exon> secondExonList;
    private final Transcript transcript;
    private final Transcript transcript2;
    
    private final String name = "NM_032291";
    private final String gene = "SGIP1";
    private final String strand = "+";
    private final int exonCount = 2;
    
    public GeneModelCollectionTest() {
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
        
        List<Exon> exons2 = new ArrayList<>();
        exons2.add(new Exon(new Interval("chr1",16767166,16767348)));
        transcript2 
            = new Transcript(
                "NM_001145277",
                "NECAP2",
                "+",
                1,
                new Interval("chr1",16767166,16786584),
                new Interval("chr1",16767256,16785491),
                exons2);
    }

    /**
     * Null Test of isFullLength method, of class GeneModelCollection.
     */
    @Test
    public void testIsFullLengthNull() {
        System.out.println("isFullLength");
        String gene = "FOXE1";
        GeneModelCollection instance = new GeneModelCollection();
        boolean expResult = false;
        boolean result = instance.isFullLength(gene);
        assertEquals(expResult, result);
    }
    
     /**
     * Test of isFullLength method, of class GeneModelCollection.
     */
    @Test
    public void testIsFullLength() {
        System.out.println("isFullLength");
        String gene = "FOXE1_FL";
        GeneModelCollection instance = new GeneModelCollection();
        boolean expResult = true;
        boolean result = instance.isFullLength(gene);
        assertEquals(expResult, result);
    }

    /**
     * Test of createTargets method, of class GeneModelCollection.
     */
    @Test
    public void testCreateTargets() 
    throws Exception {
        System.out.println("createTargets");
        Set<String> genesToTarget = new HashSet<>();
        genesToTarget.add("SGIP1");
        int splicingDistance = 10;
        GeneModelCollection instance = new GeneModelCollection();
        instance.addTranscript(transcript);
        instance.aggregateTranscriptsByGenes();
        List<Interval> expResult = new ArrayList<>();
        expResult.add(new Interval("chr1",66999824,67000051));
        List<Interval> result 
            = instance.createTargets(genesToTarget, splicingDistance);
        assertEquals(expResult, result);
        
    }

    /**
     * Test of readGeneModelCollection method, of class GeneModelCollection.
     */
    @Test
    public void testReadGeneModelCollection() 
    throws Exception {
        System.out.println("readGeneModelCollection");

        GeneModelCollection instance = new GeneModelCollection();
        instance.readGeneModelCollection(file);
       
    }

    /**
     * Test of setName method, of class GeneModelCollection.
     */
    @Test
    public void testSetName() {
        System.out.println("setName");
        String name = "RefSeq";
        String version = "4.0";
        GeneModelCollection instance = new GeneModelCollection();
        instance.setName(name, version);
        String nameBack = instance.getName();
        assertEquals(name,nameBack);
    }

    /**
     * Test of getGeneModels method, of class GeneModelCollection.
     */
    @Test
    public void testGetGeneModels() {
        System.out.println("getGeneModels");
        GeneModelCollection instance = new GeneModelCollection();
        List<GeneModel> expResult = null;
        List<GeneModel> result = instance.getGeneModels();
        assertEquals(expResult, result);
    }

    /**
     * Test of addTranscript method, of class GeneModelCollection.
     */
    @Test
    public void testAddTranscript() {
        System.out.println("addTranscript");
        GeneModelCollection instance = new GeneModelCollection();
        instance.addTranscript(transcript);
        
    }

    /**
     * Test of sortTranscriptsByGeneName method, of class GeneModelCollection.
     */
    @Test
    public void testSortTranscriptsByGeneName() 
    throws IOException {
        System.out.println("sortTranscriptsByGeneName");
        GeneModelCollection instance = new GeneModelCollection();
        instance.addTranscript(transcript);
        instance.addTranscript(transcript2);
        instance.aggregateTranscriptsByGenes();
         List<GeneModel> models = instance.getGeneModels();
         assertEquals("NECAP2",models.get(0).getGeneName());
         assertEquals("SGIP1",models.get(1).getGeneName());
        
    }

    /**
     * Test of aggregateTranscriptsByGenes method, of class GeneModelCollection.
     */
    @Test
    public void testAggregateTranscriptsByGenes() 
    throws Exception {
        System.out.println("aggregateTranscriptsByGenes");
        GeneModelCollection instance = new GeneModelCollection();
        instance.addTranscript(transcript);
        instance.addTranscript(transcript2);
        instance.aggregateTranscriptsByGenes();
        
        List<GeneModel> models = instance.getGeneModels();
        assertEquals(2,models.size());
    }

    
}
