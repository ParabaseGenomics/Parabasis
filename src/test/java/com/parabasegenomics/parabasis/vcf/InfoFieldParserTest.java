/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.vcf;

import java.io.FileNotFoundException;
import java.io.IOException;
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
public class InfoFieldParserTest {
    
    String testFile = "src/main/resources/test.vcf";
    VcfLoader loader;
    String infoField;
    
    public InfoFieldParserTest() {
        infoField 
            = "AC=1;AF=0.50;AN=2;BaseQRankSum=-1.662;DP=28;FS=2.634;HRun=11;HaplotypeScore=25.4378;MQ=60.39;MQ0=0;MQRankSum=-1.137;QD=14.57;ReadPosRankSum=-1.487;SB=-44.28;TI=NM_005957;GI=MTHFR;FC=Noncoding;EXON";
    }
    
 
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of parse method, of class InfoFieldParser.
     */
    @Test
    public void testParse() {
        System.out.println("parse");
        InfoFieldParser instance = new InfoFieldParser();
        instance.parse(infoField);
       
    }

    /**
     * Test of find method, of class InfoFieldParser.
     */
    @Test
    public void testFind() {
        System.out.println("find");
        String key = "TI";
        InfoFieldParser instance = new InfoFieldParser();
        instance.parse(infoField);
        String expResult = "NM_005957";
        String result = instance.find(key);
        assertEquals(expResult, result);
       
    }
    
}
