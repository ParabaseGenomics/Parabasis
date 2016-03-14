/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis;

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
public class GeneModelTest {
    
    public GeneModelTest() {
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
     * Test of addTranscript method, of class GeneModel.
     */
    @Test
    public void testAddTranscript() {
        System.out.println("addTranscript");
        Transcript transcript = null;
        GeneModel instance = new GeneModel();
        instance.addTranscript(transcript);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setGeneName method, of class GeneModel.
     */
    @Test
    public void testSetGeneName() {
        System.out.println("setGeneName");
        String name = "";
        GeneModel instance = new GeneModel();
        instance.setGeneName(name);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of Collapse method, of class GeneModel.
     */
    @Test
    public void testCollapse() {
        System.out.println("Collapse");
        GeneModel instance = new GeneModel();
        instance.Collapse();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
