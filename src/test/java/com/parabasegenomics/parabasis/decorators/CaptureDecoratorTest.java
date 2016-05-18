/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.decorators;

import static com.parabasegenomics.parabasis.decorators.AnnotationKeys.CAPTURE_KEY;
import com.parabasegenomics.parabasis.target.AnnotatedInterval;
import htsjdk.samtools.util.Interval;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author evanmauceli
 */
public class CaptureDecoratorTest {
    
    private static final String annotation = "100";
    
    private static final Interval interval 
        = new Interval("chr1",0,100);
    
    private final AnnotatedInterval annotatedInterval;
    private CaptureDecorator instance;
    List<Interval> intervals;
    
    public CaptureDecoratorTest() {
        intervals = new ArrayList<>();
        intervals.add(interval);
        annotatedInterval = new AnnotatedInterval(interval);
        annotatedInterval.addAnnotation(CAPTURE_KEY,annotation);
        
        instance = new CaptureDecorator(intervals);
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
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
        String expResult = CAPTURE_KEY;
        String result = instance.getKey();
        assertEquals(expResult, result);
    }

    /**
     * Test of getCount method, of class CaptureDecorator.
     */
    @Test
    public void testGetCount() {
        System.out.println("getCount");
        int expResult = 100;
        int result = instance.getCount(interval);
        assertEquals(expResult, result);
    }
    
}
