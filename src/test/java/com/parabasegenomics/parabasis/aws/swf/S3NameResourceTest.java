/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.aws.swf;

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
public class S3NameResourceTest {
    
    S3NameResource instance;
    public S3NameResourceTest() {
        
        instance 
            = new S3NameResource(
            "parabase.genomics.sandbox",
            "NBDxV1.1/NA12878A/2016-07-25-15:21:42/160718_M03281_0050_000000000-ANMNN/NA12878A_S2.vcf.gz");
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getBucket method, of class S3NameResource.
     */
    @Test
    public void testGetBucket() {
        System.out.println("getBucket");
        
        String expResult = "parabase.genomics.sandbox";
        String result = instance.getBucket();
        assertEquals(expResult, result);
    }

    /**
     * Test of getKey method, of class S3NameResource.
     */
    @Test
    public void testGetKey() {
        System.out.println("getKey");
        String expResult
            = "NBDxV1.1/NA12878A/2016-07-25-15:21:42/160718_M03281_0050_000000000-ANMNN/NA12878A_S2.vcf.gz";
        String result = instance.getKey();
        assertEquals(expResult, result);
    }

    /**
     * Test of isProduction method, of class S3NameResource.
     */
    @Test
    public void testIsProduction() {
        System.out.println("isProduction");
        boolean expResult = false;
        boolean result = instance.isProduction();
        assertEquals(expResult, result);
    }

    /**
     * Test of getAssay method, of class S3NameResource.
     */
    @Test
    public void testGetAssay() {
        System.out.println("getAssay");
        String expResult = "NBDxV1.1";
        String result = instance.getAssay();
        assertEquals(expResult, result);
    }

    /**
     * Test of getSampleId method, of class S3NameResource.
     */
    @Test
    public void testGetSampleId() {
        System.out.println("getSampleId");
        String expResult = "NA12878A";
        String result = instance.getSampleId();
        assertEquals(expResult, result);
    }

    /**
     * Test of getFilename method, of class S3NameResource.
     */
    @Test
    public void testGetFilename() {
        System.out.println("getFilename");
        String expResult = "NA12878A_S2.vcf.gz";
        String result = instance.getFilename();
        assertEquals(expResult, result);
    }


    /**
     * Test of getKeyPrefix method, of class S3NameResource.
     */
    @Test
    public void testGetKeyPrefix() {
        System.out.println("getKeyPrefix");
        String expResult = "NBDxV1.1/NA12878A/2016-07-25-15:21:42/160718_M03281_0050_000000000-ANMNN";
        String result = instance.getKeyPrefix();
        assertEquals(expResult, result);
    }
    
}
