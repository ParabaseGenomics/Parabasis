/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis;

import htsjdk.samtools.util.Interval;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author evanmauceli
 */
public class GeneModelFileReaderTest {
    
    String testFile = "src/main/resources/refseq_genemodel.txt";
    String badTestFile = "src/main/resources/fileNotExist.txt";
    
    String exampleLine
        = "1\tNM_018090\tchr1\t+\t16767166\t16786584\t16767256\t16785385\t8\t16767166,16770126,16774364,16774554,16775587,16778332,16782312,16785336,\t16767348,16770227,16774469,16774636,16775696,16778510,16782388,16786584,\t0\tNECAP2\tcmpl\tcmp\t0,2,1,1,2,0,1,2,";
    
    GeneModelFileReader reader;
    
    public GeneModelFileReaderTest() {
   
    }
    
    
    @Before
    public void setUp() 
    throws FileNotFoundException {
        reader = new GeneModelFileReader(testFile);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of readFile method, of class GeneModelFileReader.
     * @throws java.lang.Exception
     */
    @Test
    public void testReadFile() 
    throws Exception {
        System.out.println("readFile");
        reader.readFile();    
    }
    
    @Test (expected = FileNotFoundException.class)
    public void testBadReadFile() 
    throws FileNotFoundException, IOException {
        System.out.println("bad file read");
        GeneModelFileReader badReader 
            = new GeneModelFileReader(badTestFile);
    }
    
    @Test
    public void testConvertLineToTranscript() 
    throws IOException {
      
        reader.convertLineToTranscript(exampleLine);    
    }

    @Test
    public void testReadHeader() 
    throws FileNotFoundException, IOException {
        System.out.println("header read");
        GeneModelFileReader headerReader 
            = new GeneModelFileReader(testFile);
        headerReader.readHeader();
    }
    
    /**
     * Test exon parsing. Expect this to pass.
     * 
     * @throws IOException 
     */
    @Test
    public void testParseExons() 
    throws IOException {
        String exonStarts = "16767166,16770126";
        String exonEnds = "16767348,16770227";
        int exonCount = 2;
        String chr = "chr1";
        String strand = "-";
        
        List<Interval> exons 
            = reader.parseExons(exonCount, chr, exonStarts, exonEnds, strand);
        
    }
    
    /**
     * Test exon count.  Expect this one to fail as the expected count
     * is not the same as the number of starts and ends.
     * 
     * @throws IOException 
     */
    @Test (expected = IOException.class)
    public void testBadExonParse() 
    throws IOException {
        String exonStarts = "16767166";
        String exonEnds = "16767348";
        int exonCount = 2;
        String chr = "chr1";
        String strand = "-";
        
         List<Interval> exons 
            = reader.parseExons(exonCount, chr, exonStarts, exonEnds, strand);
    }

    
}

