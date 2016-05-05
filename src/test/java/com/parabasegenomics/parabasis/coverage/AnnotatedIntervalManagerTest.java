/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.coverage;

import htsjdk.samtools.util.Interval;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author evanmauceli
 */
public class AnnotatedIntervalManagerTest {
    
    private static final String annotation = "IFT172:NM_015662-Exon1-Intron1(splicing5p);";
    
    public AnnotatedIntervalManagerTest() {
   
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of addInterval method, of class AnnotatedIntervalManager.
     */
    @Test
    public void testAddInterval() {
        System.out.println("addInterval");
        AnnotatedInterval interval
            =new AnnotatedInterval(new Interval("chr1",100,200));
      
        AnnotatedIntervalManager instance 
            = new AnnotatedIntervalManager();
        instance.addInterval(interval);
        
    }
    
}
