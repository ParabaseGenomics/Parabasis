/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.coverage;

import htsjdk.samtools.util.Interval;
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
public class IntervalCoverageTest {
    
    private final IntervalCoverage instance;
    private final Interval interval;
    
    public IntervalCoverageTest() {
        interval = new Interval("chr1",1,100);
        instance = new IntervalCoverage(interval);
        instance.update(12.0);
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
     * Test of count method, of class IntervalCoverage.
     */
    @Test
    public void testCount() {
        System.out.println("count");
        int expResult = 1;
        int result = instance.count();
        assertEquals(expResult, result);
    }

    /**
     * Test of getInterval method, of class IntervalCoverage.
     */
    @Test
    public void testGetInterval() {
        System.out.println("getInterval");
        Interval expResult = interval;
        Interval result = instance.getInterval();
        assertEquals(expResult, result);
    }

    /**
     * Test of getMean method, of class IntervalCoverage.
     */
    @Test
    public void testGetMean() {
        System.out.println("getMean");
        double expResult = 12.0;
        double result = instance.getMean();
        assertEquals(expResult, result, 0.0);
      
    }

    /**
     * Test of getStandardDeviation method, of class IntervalCoverage.
     */
    @Test
    public void testGetStandardDeviation() {
        System.out.println("getStandardDeviation");
        double expResult = 0.0;
        double result = instance.getStandardDeviation();
        assertEquals(expResult, result, 0.0);
      
    }

    /**
     * Test of getCoefficientOfVariation method, of class IntervalCoverage.
     */
    @Test
    public void testGetCoefficientOfVariation() {
        System.out.println("getCoefficientOfVariation");
        double expResult = 0.0;
        double result = instance.getCoefficientOfVariation();
        assertEquals(expResult, result, 0.0);
        
    }

    /**
     * Test of update method, of class IntervalCoverage.
     */
    @Test
    public void testUpdate() {
        System.out.println("update");
        Double coverage = 10.0;
        IntervalCoverage result = instance.update(coverage);
        Double observedMean = result.getMean();
        Double observedStd = result.getStandardDeviation();
        Double observedCV = result.getCoefficientOfVariation();
        
        Double expectedMean = 11.0;
        Double expectedStd = 1.41;
        Double expectedCV = 0.14;
     
        assertEquals(expectedMean, observedMean,0.05);
        assertEquals(expectedStd, observedStd,0.05);
        assertEquals(expectedCV, observedCV,0.05);
       
    }

  
}
