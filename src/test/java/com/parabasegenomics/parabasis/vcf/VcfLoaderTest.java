/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.vcf;

import java.io.FileNotFoundException;
import java.io.IOException;
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
public class VcfLoaderTest {
    
    String testFile = "src/main/resources/test.vcf";
    VcfLoader loader;
    VcfCohort cohort;
    
    public VcfLoaderTest() {
    }
    
    
    @Before
    public void setUp() 
    throws FileNotFoundException, IOException {
        loader = new VcfLoader(testFile);
        loader.loadFile();
        cohort = loader.getCohort();
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of loadPassingVariantsOnly method, of class VcfLoader.
     * @throws java.io.FileNotFoundException
     */
    @Test
    public void testLoadPassingVariantsOnly() 
    throws FileNotFoundException, IOException {
        System.out.println("loadPassingVariantsOnly");
        VcfLoader instance = new VcfLoader(testFile);
        instance.loadPassingVariantsOnly();
        int expected = 13;
        instance.loadFile();
        int actual = instance.getVariantCount();
        assertEquals(expected,actual);
    }

    /**
     * Test of loadFile method, of class VcfLoader.
     */
    @Test
    public void testLoadFile() throws Exception {
        System.out.println("loadFile");
        VcfLoader instance = new VcfLoader(testFile);
        instance.loadFile();
        
    }

    /**
     * Test of parseHeader method, of class VcfLoader.
     */
    @Test
    public void testParseHeader() throws Exception {
        System.out.println("parseHeader");
        VcfLoader instance = new VcfLoader(testFile);
        instance.parseHeader();
       
    }

    /**
     * Test of parseLines method, of class VcfLoader.
     */
    @Test
    public void testParseLines() throws Exception {
        System.out.println("parseLines");
        VcfLoader instance = new VcfLoader(testFile);
        instance.parseHeader();
        instance.parseLines();
        
    }
    
}
