/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.coverage;

import htsjdk.samtools.util.Interval;
import java.util.ArrayList;
import java.util.List;
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
public class GapsFileReaderTest {
    
    String testFile = "src/main/resources/test.gaps.csv";
    
    public GapsFileReaderTest() {
    }
    
    @Before
    public void setUp() {
        
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of readFile method, of class GapsFileReader.
     * @throws java.lang.Exception
     */
    @Test
    public void testReadFile() throws Exception {
        System.out.println("readFile");
        GapsFileReader instance = new GapsFileReader(testFile);
          
        List<Interval> expResult = new ArrayList<>();
        expResult.add(new Interval("chr1",6484838,6485319));
        expResult.add(new Interval("chr1",6500386,6500510));
        
        List<Interval> result = instance.readFile();
        assertEquals(expResult, result);

    }
    
}
