/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.vcf;

import java.io.FileNotFoundException;
import java.io.IOException;
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
public class VcfCohortTest {
    
    String testFile = "src/main/resources/test.vcf";
    VcfLoader loader;
    VcfCohort cohort;
    VcfCohortVariant cohortVariant;
        
    public VcfCohortTest() {
 
        cohortVariant = new VcfCohortVariant(
            "chr1",
            11854457,
            "rs4846051",
            "G",
            "A",
            "7219.47",
            "PASS",
            "AC=2;AF=1.00;AN=2;DP=210;Dels=0.00;FS=0.000;HRun=2;HaplotypeScore=8.2973;MQ=58.76;",
            "GT:AD:DP:GQ:PL:VF:GQX");      
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
     * Test of getVariants method, of class VcfCohort.
     */
    @Test
    public void testGetVariants() {
        System.out.println("getVariants");
        List<VcfCohortVariant> result = cohort.getVariants();
        assertEquals(17, result.size());
        
    }

    /**
     * Test of getSamples method, of class VcfCohort.
     */
    @Test
    public void testGetSamples() {
        System.out.println("getSamples");
        List<String> expResult = new ArrayList<>();
        expResult.add("L375");
        List<String> result = cohort.getSamples();
        assertEquals(expResult, result);
        
    }

    /**
     * Test of getSampleCount method, of class VcfCohort.
     */
    @Test
    public void testGetSampleCount() {
        System.out.println("getSampleCount");
        int expResult = 1;
        int result = cohort.getSampleCount();
        assertEquals(expResult, result);
       
    }

    /**
     * Test of getVariantCount method, of class VcfCohort.
     */
    @Test
    public void testGetVariantCount() {
        System.out.println("getVariantCount");
        int expResult = 17;
        int result = cohort.getVariantCount();
        assertEquals(expResult, result);
        
    }

    /**
     * Test of addCohortVariant method, of class VcfCohort.
     */
    @Test
    public void testAddCohortVariant() {
        System.out.println("addCohortVariant");
        VcfCohortVariant variant = null;
        VcfCohort instance = new VcfCohort();
        instance.addCohortVariant(cohortVariant);
        
    }

    /**
     * Test of addSample method, of class VcfCohort.
     */
    @Test
    public void testAddSample() {
        System.out.println("addSample");
        String sample = "L375";
        VcfCohort instance = new VcfCohort();
        instance.addSample(sample);
        String result = instance.getSampleName(0);
        assertEquals(sample,result);
    }

    /**
     * Test of getSampleName method, of class VcfCohort.
     */
    @Test
    public void testGetSampleName() {
        System.out.println("getSampleName");
        int index = 0;
        String expResult = "L375";
        String result = cohort.getSampleName(index);
        assertEquals(expResult, result);
        
    }

    /**
     * Test of getCohortVariant method, of class VcfCohort.
     */
    @Test
    public void testGetCohortVariant() {
        System.out.println("getCohortVariant");
        int index = 14;
        VcfCohortVariant expResult = cohortVariant;
        VcfCohortVariant result = cohort.getCohortVariant(index);
        assertEquals(expResult.getPosition(), result.getPosition());
       
    }
    
}
