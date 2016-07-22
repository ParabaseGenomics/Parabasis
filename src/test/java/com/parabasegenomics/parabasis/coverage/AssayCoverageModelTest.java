/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.coverage;

import htsjdk.samtools.util.Interval;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author evanmauceli
 */
public class AssayCoverageModelTest {
    
    private final AssayCoverageModel instance;
    private final String jsonFile;
    
    public AssayCoverageModelTest() {
        instance = new AssayCoverageModel("Test");
        jsonFile = "src/main/resources/test.json";
        
    }

    @Before
    public void setUp() 
    throws IOException {
        instance.initialize(new File(jsonFile));
        instance.setThreshold(2.0);
        instance.setLowCoverageThreshold(20.0);
        
         IntervalCoverage addedCoverage
            = new IntervalCoverage(new Interval("chr1",6484847,6485319));
        addedCoverage.update(3600.0);
        instance.update(addedCoverage);  
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of isOutlier method, of class AssayCoverageModel.
     */
    @Test
    public void testIsOutlier() 
    throws IOException {
        System.out.println("isOutlier");
        IntervalCoverage intervalCoverage 
            = new IntervalCoverage(new Interval("chr1",6484847,6485319));
        intervalCoverage.update(20.0);

        AssayCoverageModel model = new AssayCoverageModel("Test");      
        model.initialize(new File(jsonFile));
        
        boolean expResult = true;
        model.setThreshold(2.0);
        boolean result = model.isOutlier(intervalCoverage);
        assertEquals(expResult, result);
    }

    /**
     * Test of getZscore method, of class AssayCoverageModel.
     */
    @Test
    public void testGetZscore() {
        System.out.println("getZscore");
        IntervalCoverage intervalCoverage 
            = new IntervalCoverage(new Interval("chr1",6484847,6485319));
        intervalCoverage.update(2500.0);
        
        Double expResult = -14.9;
        Double result = instance.getZscore(intervalCoverage);
        assertEquals(expResult, result, 0.1);
        
    }

    /**
     * Test of findOutliers method, of class AssayCoverageModel.
     */
    //@Test
    public void testFindOutliers() {
        System.out.println("findOutliers");
        List<IntervalCoverage> expResult = null;
        List<IntervalCoverage> result = instance.findOutliers();
        assert(result.isEmpty());
        
    }

    /**
     * Test of getAssayName method, of class AssayCoverageModel.
     */
    @Test
    public void testGetAssayName() {
        System.out.println("getAssayName");
        String expResult = "Test";
        String result = instance.getAssayName();
        assertEquals(expResult, result);
    }

    /**
     * Test of readFromFile method, of class AssayCoverageModel.
     */
    //@Test
    public void testReadFromFile() 
    throws IOException {
        System.out.println("readFromFile");
        File file = null;
        instance.readFromFile(file);
        
    }

    /**
     * Test of writeToFile method, of class AssayCoverageModel.
     */
    //@Test
    public void testWriteToFile() 
    throws IOException {
        System.out.println("writeToFile");
        File file = null;
        AssayCoverageModel instance = null;
        instance.writeToFile(file);
        
    }

    /**
     * Test of update method, of class AssayCoverageModel.
     */
    @Test
    public void testUpdate() {
        System.out.println("update");
        IntervalCoverage intervalCoverage 
            = new IntervalCoverage(new Interval("chr1",6484847,6485319));
        intervalCoverage.update(3000.0);

        instance.update(intervalCoverage);
       
    }

  
}
