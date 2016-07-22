/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.target;

import static com.parabasegenomics.parabasis.decorators.AnnotationKeys.GENE_KEY;
import com.parabasegenomics.parabasis.decorators.GeneModelDecorator;
import com.parabasegenomics.parabasis.decorators.IntervalDecorator;
import com.parabasegenomics.parabasis.gene.GeneModelCollection;
import htsjdk.samtools.util.Interval;
import java.io.IOException;
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
public class AnnotationSummaryTest {
    
    private final String file;
    private final GeneModelCollection geneModelCollectionInstance;
    private final AnnotationSummary instance;
    private final GeneModelDecorator decorator;
    
    public AnnotationSummaryTest() 
    throws IOException {
        instance = new AnnotationSummary();
        Set<String> genesToTarget = new HashSet<>();
        genesToTarget.add("ESPN");
        
        file = "src/main/resources/refseq_genemodel.txt";
        geneModelCollectionInstance = new GeneModelCollection();
        geneModelCollectionInstance.readGeneModelCollection(file);
        geneModelCollectionInstance.aggregateTranscriptsByGenes();
        
        decorator 
            = new GeneModelDecorator(geneModelCollectionInstance,genesToTarget);        
        instance.addDecorator(decorator);
        
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of heldKeys method, of class AnnotationSummary.
     * @throws java.io.IOException
     */
    @Test
    public void testHeldKeys() 
    throws IOException {
        System.out.println("heldKeys");
       
        List<String> expResult = new ArrayList<>();
        expResult.add(GENE_KEY);
        List<String> result = instance.heldKeys();
        assertEquals(expResult, result);
        
    }

    /**
     * Test of hasDecorator method, of class AnnotationSummary.
     */
    @Test
    public void testHasDecorator() {
        System.out.println("hasDecorator");
        
        boolean expResult = true;
        boolean result = instance.hasDecorator(GENE_KEY);
        assertEquals(expResult, result);
    }

    /**
     * Test of getDecorator method, of class AnnotationSummary.
     */
    @Test
    public void testGetDecorator() {
        System.out.println("getDecorator");
        
        IntervalDecorator expResult = decorator;
        IntervalDecorator result = instance.getDecorator(GENE_KEY);
        assertEquals(expResult, result);
        
    }

    /**
     * Test of addDecorator method, of class AnnotationSummary.
     */
    @Test
    public void testAddDecorator() {
        System.out.println("addDecorator");
        AnnotationSummary summary = new AnnotationSummary();
        summary.addDecorator(decorator);
        
    }

    /**
     * Test of annotate method, of class AnnotationSummary.
     */
    @Test
    public void testAnnotate() {
        System.out.println("annotate");
        Interval interval = new Interval("chr1",6484847,6485319);
        List<Interval> intervals = new ArrayList<>();
        intervals.add(interval);

        String expResult = "ESPN:NM_031475-Exon1-Intron1(splicing5p);";
        List<AnnotatedInterval> result = instance.annotate(intervals);
        assertEquals(expResult, result.get(0).getAnnotation(GENE_KEY));
        
    }

    /**
     * Test of annotateOne method, of class AnnotationSummary.
     */
    @Test
    public void testAnnotateOne() {
        System.out.println("annotateOne");
        Interval interval = new Interval("chr1",6484847,6485319);;
        String expResult = "ESPN:NM_031475-Exon1-Intron1(splicing5p);";
        AnnotatedInterval result = instance.annotateOne(interval);
        assertEquals(expResult, result.getAnnotation(GENE_KEY));
        
    }

   
    
}
