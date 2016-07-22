/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.reporting;

import com.parabasegenomics.parabasis.target.ReportOnAssay;
import java.io.IOException;
import org.junit.Test;

/**
 *
 * @author evanmauceli
 */
public class ReportOnAssayTest {
    
    private final String reportFile;
    private final String targetIntervalFile;
    private final String origTargetIntervalFile;
    private final String humanReferenceFile;
    private final String refSeqGeneModelFile;
    private final String targetGenelistFile;

    
    public ReportOnAssayTest() {
        reportFile = null;
        
        targetIntervalFile = "src/main/resources/targetIntervalFile.bed";
        origTargetIntervalFile = "src/main/resources/targetIntervalFileOrig.bed";
        humanReferenceFile = "src/main/resources/hg19sample.fasta";
        refSeqGeneModelFile = "src/main/resources/refseq_genemodel.txt";
        targetGenelistFile = "src/main/resources/targetGenelistFile";
        
    }
    
    /**
     * Test of main method, of class TargetReport.
     * @throws java.lang.Exception
     */
    //@Test
    public void testMain() throws Exception {
        System.out.println("main");
        String[] args = new String [6];
        args[0] = targetIntervalFile;
        args[1] = humanReferenceFile;
        args[2] = refSeqGeneModelFile;
        args[3] = refSeqGeneModelFile;
        args[4] = null;
        args[5] = targetGenelistFile;
        args[6] = "testAssay";
               
        ReportOnAssay.main(args);
        
    }

    /**
     * Test of report method, of class TargetReport.
     */
    @Test
    public void testReport() 
    throws IOException {
        System.out.println("report");
        ReportOnAssay targetReport = new ReportOnAssay(reportFile,"testAssay");
        targetReport.loadGeneModelCollection(refSeqGeneModelFile,refSeqGeneModelFile);
        targetReport.loadTargetFile(origTargetIntervalFile);
        targetReport.decorateTargets();
        
        targetReport.report();
        
    }
    
}
