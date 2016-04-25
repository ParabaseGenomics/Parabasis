/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.vcf;

import htsjdk.samtools.util.Interval;
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
public class VcfCohortVariantTest {
    
    String testFile = "src/main/resources/test.vcf";
    String  data = "0/1:7,19:28:87.50:447,0,87:0.731:87";
    VcfLoader loader;
    VcfCohort cohort;
    VcfCohortVariant cohortVariant;
    
    public VcfCohortVariantTest() {
       
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
        
        cohortVariant.addSampleVariant(new VcfSampleVariant(data));
        
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
     * Test of getChromosome method, of class VcfCohortVariant.
     */
    @Test
    public void testGetChromosome() {
        System.out.println("getChromosome");
        String expResult = "chr1";
        String result = cohortVariant.getChromosome();
        assertEquals(expResult, result);
        
    }

    /**
     * Test of getPosition method, of class VcfCohortVariant.
     */
    @Test
    public void testGetPosition() {
        System.out.println("getPosition");
        int expResult = 11854457;
        int result = cohortVariant.getPosition();
        assertEquals(expResult, result);
        
    }

    /**
     * Test of getVariantId method, of class VcfCohortVariant.
     */
    @Test
    public void testGetVariantId() {
        System.out.println("getVariantId");
        String expResult = "rs4846051";
        String result = cohortVariant.getVariantId();
        assertEquals(expResult, result);
        
    }

    /**
     * Test of getReferenceAllele method, of class VcfCohortVariant.
     */
    @Test
    public void testGetReferenceAllele() {
        System.out.println("getReferenceAllele");
        String expResult = "G";
        String result = cohortVariant.getReferenceAllele();
        assertEquals(expResult, result);
        
    }

    /**
     * Test of getAlternateAllele method, of class VcfCohortVariant.
     */
    @Test
    public void testGetAlternateAllele() {
        System.out.println("getAlternateAllele");
        String expResult = "A";
        String result = cohortVariant.getAlternateAllele();
        assertEquals(expResult, result);
        
    }

    /**
     * Test of getQualityField method, of class VcfCohortVariant.
     */
    @Test
    public void testGetQualityField() {
        System.out.println("getQualityField");
        String expResult = "7219.47";
        String result = cohortVariant.getQualityField();
        assertEquals(expResult, result);
        
    }

    /**
     * Test of getFilterField method, of class VcfCohortVariant.
     */
    @Test
    public void testGetFilterField() {
        System.out.println("getFilterField");
        String expResult = "PASS";
        String result = cohortVariant.getFilterField();
        assertEquals(expResult, result);
        
    }

    /**
     * Test of getInfoField method, of class VcfCohortVariant.
     */
    @Test
    public void testGetInfoField() {
        System.out.println("getInfoField");
        String expResult = "AC=2;AF=1.00;AN=2;DP=210;Dels=0.00;FS=0.000;HRun=2;HaplotypeScore=8.2973;MQ=58.76;";
        String result = cohortVariant.getInfoField();
        assertEquals(expResult, result);
        
    }

    /**
     * Test of getFormatKey method, of class VcfCohortVariant.
     */
    @Test
    public void testGetFormatKey() {
        System.out.println("getFormatKey");
        String expResult = "GT:AD:DP:GQ:PL:VF:GQX";
        String result = cohortVariant.getFormatKey();
        assertEquals(expResult, result);
        
    }

    /**
     * Test of addSampleVariant method, of class VcfCohortVariant.
     */
    @Test
    public void testAddSampleVariant() {
        System.out.println("addSampleVariant");
        String result = cohortVariant.getSampleVariant(0).getData();
        assertEquals(data,result);
        assertEquals(1,cohortVariant.getSampleVariantCount());
        
    }

    /**
     * Test of getInfoForKey method, of class VcfCohortVariant.
     */
    @Test
    public void testGetInfoForKey() {
        System.out.println("getInfoForKey");
        String key = "AC";
        InfoFieldParser infoFieldParser = new InfoFieldParser();
        String expResult = "2";
        String result = cohortVariant.getInfoForKey(key, infoFieldParser);
        assertEquals(expResult, result);
        
    }

    /**
     * Test of getEndPosition method, of class VcfCohortVariant.
     */
    @Test
    public void testGetEndPosition() {
        System.out.println("getEndPosition");
        int expResult = 11854458;
        int result = cohortVariant.getEndPosition();
        assertEquals(expResult, result);
        
    }

    /**
     * Test of isSNP method, of class VcfCohortVariant.
     */
    @Test
    public void testIsSNP() {
        System.out.println("isSNP");
        boolean expResult = true;
        boolean result = cohortVariant.isSNP();
        assertEquals(expResult, result);
        
    }

    /**
     * Test of isMNP method, of class VcfCohortVariant.
     */
    @Test
    public void testIsMNP() {
        System.out.println("isMNP");
        boolean expResult = false;
        boolean result = cohortVariant.isMNP();
        assertEquals(expResult, result);
        
    }

    /**
     * Test of isIndel method, of class VcfCohortVariant.
     */
    @Test
    public void testIsIndel() {
        System.out.println("isIndel");
        boolean expResult = false;
        boolean result = cohortVariant.isIndel();
        assertEquals(expResult, result);
       
    }

    /**
     * Test of asInterval method, of class VcfCohortVariant.
     */
    @Test
    public void testAsInterval() {
        System.out.println("asInterval");
        Interval expResult = new Interval("chr1",11854456,11854457);
        Interval result = cohortVariant.asInterval();
        assertEquals(expResult, result);
       
    }

    /**
     * Test of getSampleVariants method, of class VcfCohortVariant.
     */
    @Test
    public void testGetSampleVariants() {
        System.out.println("getSampleVariants");
        VcfSampleVariant var = new VcfSampleVariant(data);
        List<VcfSampleVariant> expResult = new ArrayList<>();
        boolean add = expResult.add(var);
        List<VcfSampleVariant> result = cohortVariant.getSampleVariants();
        assertEquals(expResult.get(0).getData(), result.get(0).getData());
        
    }

    /**
     * Test of getSampleVariantCount method, of class VcfCohortVariant.
     */
    @Test
    public void testGetSampleVariantCount() {
        System.out.println("getSampleVariantCount");
        int expResult = 1;
        int result = cohortVariant.getSampleVariantCount();
        assertEquals(expResult, result);
       
    }

    /**
     * Test of getSampleVariant method, of class VcfCohortVariant.
     */
    @Test
    public void testGetSampleVariant() {
        System.out.println("getSampleVariant");
        int index = 0;
        VcfSampleVariant expResult = new VcfSampleVariant(data);
        VcfSampleVariant result = cohortVariant.getSampleVariant(index);
        assertEquals(expResult.getData(), result.getData());
      
    }

    /**
     * Test of overlaps method, of class VcfCohortVariant.
     */
    //@Test
    public void testOverlaps() {
        System.out.println("overlaps");
        VcfCohortVariant toCompare = null;
        VcfCohortVariant instance = null;
        boolean expResult = false;
        boolean result = instance.overlaps(toCompare);
        assertEquals(expResult, result);
        
    }

    /**
     * Test of compareTo method, of class VcfCohortVariant.
     */
    //@Test
    public void testCompareTo() {
        System.out.println("compareTo");
        VcfCohortVariant toCompare = null;
        VcfCohortVariant instance = null;
        int expResult = 0;
        int result = instance.compareTo(toCompare);
        assertEquals(expResult, result);
       
    }
    
}
