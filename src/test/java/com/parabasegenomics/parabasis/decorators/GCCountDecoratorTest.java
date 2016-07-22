/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.decorators;

import static com.parabasegenomics.parabasis.decorators.AnnotationKeys.GC_KEY;
import com.parabasegenomics.parabasis.target.AnnotatedInterval;
import htsjdk.samtools.reference.FastaSequenceIndex;
import htsjdk.samtools.reference.IndexedFastaSequenceFile;
import htsjdk.samtools.util.Interval;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 *
 * @author evanmauceli
 */
public class GCCountDecoratorTest {
    
     private static final String annotation = "81";
    
    private static final Interval interval 
        = new Interval("chr1",0,100);
    
    private final IndexedFastaSequenceFile referenceSequence;
    private final FastaSequenceIndex referenceIndex;
    private final AnnotatedInterval annotatedInterval;
    private final GCCountDecorator instance;
    List<Interval> intervals;
    
    public GCCountDecoratorTest() {
        intervals = new ArrayList<>();
        intervals.add(interval);
        annotatedInterval = new AnnotatedInterval(interval);
        annotatedInterval.addAnnotation(GC_KEY,annotation);
        
        File humanReference = new File("src/main/resources/hg19sample.fasta");               
        File referenceIndexFile = new File(humanReference + ".fai");    
        referenceIndex = new FastaSequenceIndex(referenceIndexFile);
        referenceSequence 
            = new IndexedFastaSequenceFile(humanReference,referenceIndex);
        
        instance = new GCCountDecorator(referenceSequence,referenceIndex);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }
   
    /**
     * Test of annotate method, of class CaptureDecorator.
     */
    @Test
    public void testAnnotate() {
        System.out.println("annotate");
        instance.annotate(annotatedInterval);
    }

    /**
     * Test of getKey method, of class CaptureDecorator.
     */
    @Test
    public void testGetKey() {
        System.out.println("getKey");
        String expResult = GC_KEY;
        String result = instance.getKey();
        assertEquals(expResult, result);
    }

    /**
     * Test of getCount method, of class CaptureDecorator.
     */
    @Test
    public void testGetCount() {
        System.out.println("getCount");
        int expResult = 81;
        int result = instance.getCount(interval);
        assertEquals(expResult, result);
    }
    
    
}
