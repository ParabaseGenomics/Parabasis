/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.reporting;

import com.parabasegenomics.parabasis.decorators.GeneModelDecorator;
import com.parabasegenomics.parabasis.decorators.IntervalDecorator;
import com.parabasegenomics.parabasis.gene.GeneModelCollection;
import com.parabasegenomics.parabasis.target.AnnotationSummary;
import com.parabasegenomics.parabasis.util.Reader;
import htsjdk.samtools.util.Interval;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author evanmauceli
 */
public class TargetReportTest {
    
    private static File intervalsFile;
    private static File reportFile;
    private final AnnotationSummary summary;
    List<Interval> intervals;
    IntervalDecorator decorator;
    Reader reader;
    private final String file;
    private final GeneModelCollection geneModelCollectionInstance;
    
    public TargetReportTest() 
    throws IOException {
        reader = new Reader();
        intervalsFile = new File("src/main/resources/targetIntervalFileOrig.bed");
        reportFile = new File("src/main/resources/tmp.txt");
        summary = new AnnotationSummary();
        intervals = reader.readBEDFile(intervalsFile.getAbsolutePath());
        file = "src/main/resources/refseq_genemodel.txt";
        geneModelCollectionInstance = new GeneModelCollection();
        geneModelCollectionInstance.readGeneModelCollection(file);
        geneModelCollectionInstance.aggregateTranscriptsByGenes();
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }
    
   
    /**
     * Test of setAnnotationSummary method, of class GeneSummaryReport.
     * @throws java.lang.Exception
     */
    @Test
    public void testSetAnnotationSummary() throws Exception {
        System.out.println("setAnnotationSummary");
        TargetReport instance = new TargetReport(reportFile);
        Set<String> genesToTarget = new HashSet<>();
        genesToTarget.add("ESPN");
        
        GeneModelDecorator decorator 
            = new GeneModelDecorator(geneModelCollectionInstance,genesToTarget);
        
        summary.addDecorator(decorator);
         
        instance.setAnnotationSummary(summary);
    }

    /**
     * Test of reportOn method, of class GeneSummaryReport.
     * @throws java.lang.Exception
     */
    @Test
    public void testReportOn() throws Exception {
        System.out.println("reportOn");
        String name = "ESPN";
        TargetReport instance = new TargetReport(reportFile);
        Set<String> genesToTarget = new HashSet<>();
        genesToTarget.add(name);
        
        GeneModelDecorator decorator 
            = new GeneModelDecorator(geneModelCollectionInstance,genesToTarget);
        
        summary.addDecorator(decorator);
         
        instance.setAnnotationSummary(summary);
        instance.reportOn(intervals);
        instance.close();
        reportFile.delete();
        
    }
    
}
