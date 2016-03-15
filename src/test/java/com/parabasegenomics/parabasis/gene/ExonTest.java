/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.gene;

import com.parabasegenomics.parabasis.gene.Exon;
import htsjdk.samtools.util.Interval;
import static java.lang.Boolean.FALSE;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author evanmauceli
 */
public class ExonTest {
    
    private final String name = "NM_032291";
    private final String gene = "SGIP1";
    private final String strand = "+";
    private final int exonCount = 2;
    private final Interval codingIntervalShort = new Interval("chr1",67000101,67210768);
    private final Interval codingInterval = new Interval("chr1",67000041,67208778);
    
    private List<Exon> exons;
    
    public ExonTest() {
    }
    
    @Before
    public void setUp() {
        exons = new ArrayList<>();
        exons.add(new Exon(new Interval("chr1",66999824,67000051,FALSE,"1")));
        exons.add(new Exon(new Interval("chr1",67091529,67091593,FALSE,"2")));
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getStart method, of class Exon.
     */
    @Test
    public void testGetStart() {
        System.out.println("getStart");
        int expResult = 66999824;
        int result = exons.get(0).getStart();
        assertEquals(expResult, result);
    }

    /**
     * Test of getEnd method, of class Exon.
     */
    @Test
    public void testGetEnd() {
        System.out.println("getEnd");
        int expResult = 67000051;
        int result = exons.get(0).getEnd();
        assertEquals(expResult, result);
    }

    /**
     * Test of getName method, of class Exon.
     */
    @Test
    public void testGetName() {
        System.out.println("getName");
        String expResult = "1";
        String result = exons.get(0).getName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getCodingExon method, of class Exon.
     */
    @Test
    public void testGetCodingExonPartial() {
        System.out.println("getCodingExon (partial)");

        Exon expResult = new Exon(new Interval(
            codingInterval.getContig(),
            67000041,
            67000051));
        
        Exon result = exons.get(0).getCodingExon(codingInterval);
        assertEquals(expResult.getChromosome(), result.getChromosome());
        assertEquals(expResult.getStart(),result.getStart());
        assertEquals(expResult.getEnd(),result.getEnd());
    }
    
   /**
     * Test of getCodingExon method, of class Exon.
     */
    @Test
    public void testGetCodingExonFull() {
        System.out.println("getCodingExon (full)");

        Exon expResult = exons.get(1);
        
        Exon result = exons.get(1).getCodingExon(codingInterval);
        assertEquals(expResult.getChromosome(), result.getChromosome());
        assertEquals(expResult.getStart(),result.getStart());
        assertEquals(expResult.getEnd(),result.getEnd());
    }

       /**
     * Test of getCodingExon method, of class Exon.
     */
    @Test
    public void testGetCodingExonNull() {
        System.out.println("getCodingExon (null)");
    
        Exon result = exons.get(0).getCodingExon(codingIntervalShort);
        assertEquals(result,null);
    }
    
}

