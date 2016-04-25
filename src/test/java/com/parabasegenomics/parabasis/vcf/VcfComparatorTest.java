/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.vcf;

import java.io.FileNotFoundException;
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
public class VcfComparatorTest {
    
    String testVcfFile = "src/main/resources/test.vcf";
    String testBedFile = "src/main/resources/test.bed";
    String truthVcfFile = "src/main/resources/truth.vcf";
    String truthBedFile = "src/main/resources/truth.bed";
    
    VcfLoader testLoader;
    VcfLoader truthLoader;
    
    public VcfComparatorTest() {
        
    }
    
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of loadTruthSet method, of class VcfComparator.
     */
    //@Test
    public void testLoadTruthSet() throws Exception {
        System.out.println("loadTruthSet");
        VcfComparator instance = new VcfComparator();
        instance.loadTruthSet(testVcfFile, testBedFile);
        
    }

    /**
     * Test of loadTestSet method, of class VcfComparator.
     */
    //@Test
    public void testLoadTestSet() throws Exception {
        System.out.println("loadTestSet");
        VcfComparator instance = new VcfComparator();
        instance.loadTestSet(testVcfFile, testBedFile);
        
    }

    /**
     * Test of compare method, of class VcfComparator.
     */
    //@Test
    public void testCompare() {
        System.out.println("compare");
        VcfComparator instance = new VcfComparator();
        instance.compare();
        
    }
    
}
