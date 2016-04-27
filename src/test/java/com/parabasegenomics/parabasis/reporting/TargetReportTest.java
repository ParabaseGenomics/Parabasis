/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.reporting;

import com.parabasegenomics.parabasis.coverage.AnnotatedInterval;
import htsjdk.samtools.util.Interval;
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
public class TargetReportTest {
    
    private final String reportFile;
    private final String targetIntervalFile;
    private final String origTargetIntervalFile;
    private final String humanReferenceFile;
    private final String refSeqGeneModelFile;
    private final String targetGenelistFile;

    
    public TargetReportTest() {
        reportFile = null;
        
        targetIntervalFile = "src/main/resources/targetIntervalFile.bed";
        origTargetIntervalFile = "src/main/resources/targetIntervalFileOrig.bed";
        humanReferenceFile = "src/main/resources/hg19sample.fasta";
        refSeqGeneModelFile = "src/main/resources/refseq_genemodel.txt";
        targetGenelistFile = "src/main/resources/targetGenelistFile";
        
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of main method, of class TargetReport.
     * @throws java.lang.Exception
     */
    @Test
    public void testMain() throws Exception {
        System.out.println("main");
        String[] args = new String [6];
        args[0] = targetIntervalFile;
        args[1] = humanReferenceFile;
        args[2] = refSeqGeneModelFile;
        args[3] = refSeqGeneModelFile;
        args[4] = null;
        args[5] = targetGenelistFile;
               
        TargetReport.main(args);
        
    }

    /**
     * Test of aggregateData method, of class TargetReport.
     */
    @Test
    public void testAggregateData() throws Exception {
        System.out.println("aggregateData");
        TargetReport targetReport = new TargetReport(reportFile);
        targetReport.loadGeneModelCollection(refSeqGeneModelFile,refSeqGeneModelFile);
        targetReport.loadTargetFile(targetIntervalFile);
        targetReport.loadReferenceSequence(humanReferenceFile);
        targetReport.aggregateData();
    
        List<AnnotatedInterval> annotatedTargets 
            = targetReport.getAnnotatedIntervals();
    
        assertEquals(1,annotatedTargets.size());
    }

    /**
     * Test of getGCPercentage method, of class TargetReport.
     */
    @Test
    public void testGetGCPercentage() {
        System.out.println("getGCPercentage");
        TargetReport targetReport = new TargetReport(reportFile);
        targetReport.loadReferenceSequence(humanReferenceFile);
        Interval interval = new Interval("chr1",0,472);
        double result = targetReport.getGCPercentage(interval);
        double expResult = 77.2;
        assertEquals(expResult, result, 0.1);
        
    }

    /**
     * Test of report method, of class TargetReport.
     */
    @Test
    public void testReport() 
    throws IOException {
        System.out.println("report");
        TargetReport targetReport = new TargetReport(reportFile);
        targetReport.loadGeneModelCollection(refSeqGeneModelFile,refSeqGeneModelFile);
        targetReport.loadTargetFile(origTargetIntervalFile);
        targetReport.aggregateData();
        
        targetReport.report();
        
    }
    
}
