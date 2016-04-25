/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.vcf;

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
public class VcfSampleVariantTest {
    
    String data;
    
    public VcfSampleVariantTest() {
        data = "0/1:7,19:28:87.50:447,0,87:0.731:87";
    }
    
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getData method, of class VcfSampleVariant.
     */
    @Test
    public void testGetData() {
        System.out.println("getData");
        VcfSampleVariant instance = new VcfSampleVariant(data);
        String expResult = data;
        String result = instance.getData();
        assertEquals(expResult, result);
        
    }

    /**
     * Test of isNoCall method, of class VcfSampleVariant.
     */
    @Test
    public void testIsNoCall() {
        System.out.println("isNoCall");
        String referenceAllele = "G";
        String alternateAllele = "GT";
        VcfSampleVariant instance = new VcfSampleVariant(data);
        boolean expResult = false;
        boolean result = instance.isNoCall(referenceAllele, alternateAllele);
        assertEquals(expResult, result);
        
    }

    /**
     * Test of isHomozygous method, of class VcfSampleVariant.
     */
    @Test
    public void testIsHomozygous() {
        System.out.println("isHomozygous");
        String referenceAllele = "G";
        String alternateAllele = "GT";
        VcfSampleVariant instance = new VcfSampleVariant(data);;
        boolean expResult = false;
        boolean result = instance.isHomozygous(referenceAllele, alternateAllele);
        assertEquals(expResult, result);
        
    }

    /**
     * Test of isSnp method, of class VcfSampleVariant.
     */
    @Test
    public void testIsSnp() {
        System.out.println("isSnp");
        String referenceAllele = "G";
        String alternateAllele = "GT";
        VcfSampleVariant instance = new VcfSampleVariant(data);
        boolean expResult = false;
        boolean result = instance.isSnp(referenceAllele, alternateAllele);
        assertEquals(expResult, result);
        
    }

    /**
     * Test of getField method, of class VcfSampleVariant.
     */
    @Test
    public void testGetField() {
        System.out.println("getField");
        String fieldName = "AD";
        String key = "GT:AD:DP:GQ:PL:VF:GQX";
        VcfSampleVariant instance = new VcfSampleVariant(data);
        String expResult = "7,19";
        String result = instance.getField(fieldName, key);
        assertEquals(expResult, result);
        
    }

    /**
     * Test of getGenotype method, of class VcfSampleVariant.
     */
    @Test
    public void testGetGenotype() {
        System.out.println("getGenotype");
        String referenceAllele = "G";
        String alternateAllele = "GT";
        VcfSampleVariant instance =  new VcfSampleVariant(data);
        String expResult = "G:GT";
        String result = instance.getGenotype(referenceAllele, alternateAllele);
        assertEquals(expResult, result);
        
    }
    
}
