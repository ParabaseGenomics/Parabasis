/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.coverage;

import com.parabasegenomics.parabasis.target.AnnotatedInterval;
import htsjdk.samtools.util.Interval;
import java.util.List;
import java.util.Map;
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
public class AnnotatedIntervalTest {
    
    private static final String annotation 
        = "IFT172:NM_015662-Exon1-Intron1(splicing5p);";
    
    private static final Interval interval 
        = new Interval("chr1",0,100);
    
    private AnnotatedInterval instance;
    
    public AnnotatedIntervalTest() {
        instance = new AnnotatedInterval(interval);
        instance.addAnnotation("GEN",annotation);
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of addAnnotation method, of class AnnotatedInterval.
     */
    @Test
    public void testAddAnnotation() {
        System.out.println("addAnnotation");
        String key = "GEN";
        String value = annotation;
        instance.addAnnotation(key, value);
        
    }

    /**
     * Test of getAnnotation method, of class AnnotatedInterval.
     */
    @Test
    public void testGetAnnotation() {
        System.out.println("getAnnotation");
        String key = "GEN";  
        String expResult = annotation;
        String result = instance.getAnnotation(key);
        assertEquals(expResult, result);
        
    }

    /**
     * Test of getInterval method, of class AnnotatedInterval.
     */
    @Test
    public void testGetInterval() {
        System.out.println("getInterval");
        Interval expResult = interval;
        Interval result = instance.getInterval();
        assertEquals(expResult, result);
       
    }

    /**
     * Test of getMap method, of class AnnotatedInterval.
     */
    //@Test
    public void testGetMap() {
        System.out.println("getMap");
        Map<String, String> expResult = null;
        Map<String, String> result = instance.getMap();
        assertEquals(expResult, result);
        
    }

    /**
     * Test of getGeneName method, of class AnnotatedInterval.
     */
    @Test
    public void testGetGeneName() {
        System.out.println("getGeneName");
        String expResult = "IFT172";
        List<String> result = instance.getGeneNames();
        assertEquals(expResult, result.get(0));
       
    }

    /**
     * Test of length method, of class AnnotatedInterval.
     */
    @Test
    public void testLength() {
        System.out.println("length");
        int expResult = 100;
        int result = instance.length();
        assertEquals(expResult, result);
        
    }
    
}
