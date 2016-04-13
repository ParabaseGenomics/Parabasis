/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.vcf;

import com.parabasegenomics.parabasis.util.Reader;
import htsjdk.samtools.util.Interval;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
/**
 * Class to compare two vcf files, matching positions and genotypes.
 * 
 * @author evanmauceli
 */
public class VcfComparator {
    
    private VcfLoader truthLoader;
    private VcfLoader testLoader;
    
    private List<Interval> truthRegions;
    private List<Interval> testRegions;
    
    private final Reader utilityReader;
    
    
    public VcfComparator() {
        truthLoader = null;
        testLoader = null;
        utilityReader = new Reader();
        
        truthRegions = null;
        testRegions = null;
    }
    
    /**
     * Load the truth set consisting of a vcf file and a BED file of applicable
     * regions.
     * @param vcfFile The vcf file to use as the truth list of variants.
     * @param bedFile The BED file listing the genomic regions where the truth
     * vcf file is applicable.
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public void loadTruthSet(String vcfFile, String bedFile) 
    throws FileNotFoundException, IOException {
        truthLoader = new VcfLoader(vcfFile);
        truthLoader.loadFile();
        
        truthRegions = utilityReader.readBEDFile(bedFile);  
        
        
    }
    
     /**
     * Load the test set consisting of a vcf file and a BED file of applicable
     * regions.
     * @param vcfFile The vcf file to use as the test list of variants.
     * @param bedFile The BED file listing the genomic regions where the test
     * vcf file is applicable.
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public void loadTestSet(String vcfFile, String bedFile) 
    throws FileNotFoundException, IOException {
        testLoader = new VcfLoader(vcfFile);
        testLoader.loadFile();
        
        testRegions = utilityReader.readBEDFile(bedFile);        
        
    }
    
    
    public void compare() {
        
    }
    
}
