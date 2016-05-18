/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.decorators;

import static com.parabasegenomics.parabasis.decorators.AnnotationKeys.CAPTURE_KEY;
import static com.parabasegenomics.parabasis.decorators.AnnotationKeys.GENE_KEY;
import com.parabasegenomics.parabasis.gene.GeneModelCollection;
import com.parabasegenomics.parabasis.target.AnnotatedInterval;
import com.parabasegenomics.parabasis.util.Reader;
import htsjdk.samtools.util.Interval;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author evanmauceli
 */
public class GeneModelDecoratorTest {
   
    private static final String annotation = "100";
    
    private static final Interval interval 
        = new Interval("chr1",0,100);
    
    private final AnnotatedInterval annotatedInterval;
    private GeneModelDecorator instance;
    List<Interval> intervals;
    
    Reader reader;
    private final String file;
    private final GeneModelCollection geneModelCollectionInstance;
    
    public GeneModelDecoratorTest() 
    throws IOException {
        intervals = new ArrayList<>();
        intervals.add(interval);
        annotatedInterval = new AnnotatedInterval(interval);
        annotatedInterval.addAnnotation(GENE_KEY,annotation);
        
        file = "src/main/resources/refseq_genemodel.txt";
        geneModelCollectionInstance = new GeneModelCollection();
        geneModelCollectionInstance.readGeneModelCollection(file);
        geneModelCollectionInstance.aggregateTranscriptsByGenes();
        
        Set<String> genesToTarget = new HashSet<>();
        genesToTarget.add("ESPN");
        
        instance 
            = new GeneModelDecorator(geneModelCollectionInstance,genesToTarget);
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
        String expResult = GENE_KEY;
        String result = instance.getKey();
        assertEquals(expResult, result);
    }

    /**
     * Test of getCount method, of class CaptureDecorator.
     */
    @Test
    public void testGetCount() {
        System.out.println("getCount");
        int expResult = 0;
        int result = instance.getCount(interval);
        assertEquals(expResult, result);
    }
    
}
