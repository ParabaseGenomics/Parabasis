/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.coverage;

import htsjdk.samtools.util.Interval;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author evanmauceli
 */
public class BamCoverageTest {
    
    BamCoverage instance;
    String path = "src/main/resources/test.bam";
    
    public BamCoverageTest() {
        instance = new BamCoverage(path);
    }
    

    /**
     * Test of getCoverage method, of class BamCoverage.
     */
    @Test
    public void testGetCoverage() {
        System.out.println("getCoverage");
        Interval interval = new Interval("chr1",6484847,6485319);
        Integer expResult = 3504;
        Integer result = instance.getCoverage(interval).intValue();
        assertEquals(expResult, result);
        
    }

    /**
     * Test of closeBamFile method, of class BamCoverage.
     */
    @Test
    public void testCloseBamFile() 
    throws Exception {
        System.out.println("closeBamFile");
        instance.closeBamFile();
        
    }
    
}
