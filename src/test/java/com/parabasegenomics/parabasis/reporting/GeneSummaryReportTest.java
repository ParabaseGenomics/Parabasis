/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.reporting;

import com.parabasegenomics.parabasis.target.AnnotationSummary;
import com.parabasegenomics.parabasis.util.Reader;
import htsjdk.samtools.util.Interval;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.junit.Test;

/**
 *
 * @author evanmauceli
 */
public class GeneSummaryReportTest {
    
    private static File intervalsFile;
    private static File reportFile;
    private final AnnotationSummary summary;
    List<Interval> intervals;
    Reader reader;
    
    public GeneSummaryReportTest() 
    throws IOException {
        reader = new Reader();
        intervalsFile = new File("src/main/resources/targetIntervalFileOrig.bed");
        reportFile = new File("src/main/resources/tmpg.txt");
        summary = new AnnotationSummary();
        intervals = reader.readBEDFile(intervalsFile.getAbsolutePath());
 
    }
    
   
    /**
     * Test of setAnnotationSummary method, of class GeneSummaryReport.
     */
    @Test
    public void testSetAnnotationSummary() throws Exception {
        System.out.println("setAnnotationSummary");
        GeneSummaryReport instance = new GeneSummaryReport(reportFile);
        instance.setAnnotationSummary(summary);
    }

    /**
     * Test of reportOn method, of class GeneSummaryReport.
     */
    @Test
    public void testReportOn() throws Exception {
        System.out.println("reportOn");
        String name = "ESPN";
        GeneSummaryReport instance = new GeneSummaryReport(reportFile);
        instance.setAnnotationSummary(summary);
        instance.reportOn(intervals, name);
        instance.close();
        reportFile.delete();
    }
    
}
