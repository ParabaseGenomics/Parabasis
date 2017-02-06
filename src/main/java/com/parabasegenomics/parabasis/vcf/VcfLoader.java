/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.vcf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Class to load a standard v4+ vcf file.  The implementation is to first
 * parse the header (only the #CHROM line), then parse the variant lines.
 * 
 * @author evanmauceli
 */
public class VcfLoader {

    private static final String TAB = "\t";
    private static final String COLON = ":";
    private static final String GENOTYPE_SEPARATOR = "/";
    private static final String PHASED_GENOTYPE_SEPARATOR = "|";
    private static final String HASH = "#";
    private static final String SAMPLE_LINE = "#CHROM";
    private static final String HG19CHR = "chr";
    private static final String PASS = "PASS";
    private static final String PERIOD = ".";
    
    private final File vcfFile;
    private String header;
    private boolean passingVariantsOnly;
    private int variantCount;
    
    private final BufferedReader reader;
    private final VcfCohort cohort;
    
    public VcfLoader(String filePath) 
    throws FileNotFoundException {
        vcfFile = new File(filePath);
        passingVariantsOnly = false;
        reader = new BufferedReader(new FileReader(vcfFile)); 
        variantCount = 0;
        cohort = new VcfCohort();
    }
    
    /**
     * Only load variants with "." or "PASS" in the FILTER field.  Set to "false"
     * by default.
     */
    public void loadPassingVariantsOnly() {
        passingVariantsOnly = true;
    }

    public int getVariantCount() {
        return variantCount;
    }
    
    /**
     * Returns the cohort of variants from the vcf file.
     * @return 
     */
    public VcfCohort getCohort() {
        return cohort;
    }
    
    /**
     * Load a vcf file.
     * @throws IOException 
     */
    public void loadFile() 
    throws IOException {
        parseHeader();
        parseLines();
        variantCount = cohort.getVariantCount();
    }
    
    /**
     * Parse the "#CHROM" line from the vcf file to get the sample names. It 
     * is ok for there to be no sample names - GIAB and ClinVar are two 
     * examples.
     * @throws IOException 
     */
    public void parseHeader() 
    throws IOException {
        while (reader.ready()) {
            String line = reader.readLine();
            if (!line.startsWith(HASH)) {
                throw new IOException("File does not have a header.");    
            }
            
            if (line.startsWith(SAMPLE_LINE)) {
                String [] tokens = line.split(TAB);
                for (int index=9; index<tokens.length; index++) {
                    cohort.addSample(tokens[index]);
                }
                // leaves the reader at the first variant line in the vcf file
                break;
            }
        }
    }
    
    /**
     * Parse a variant line in a vcf file into a cohort.
     * @throws IOException 
     */
    public void parseLines() 
    throws IOException {
        while (reader.ready()) {
            String line = reader.readLine();
            String [] tokens = line.split(TAB);
            String chromosome = tokens[0];
            
            if (passingVariantsOnly
                && (!tokens[6].equals(PASS)
                    && !tokens[6].equals(PERIOD))) {
                continue;
            }
            
            // this means there is sample-level info in the vcf file
            String format = null;
            if (tokens.length >= 9) {
                format = tokens[8];
            }
            
            String refAllele = tokens[3];
            String altAllele = tokens[4];
            String firstRefAllele = refAllele.substring(0,1);
           
            
            String firstAltAllele = altAllele.substring(0,1);
            
            
            // problematic MNPs. The MiSeq reports these as two SNPs, NIST as a
            // single MNP.
            if (refAllele.length()==2 && altAllele.length()==2) {
                VcfCohortVariant cohortVariant 
                    = new VcfCohortVariant(
                        chromosome,
                        Integer.parseInt(tokens[1]),
                        tokens[2],
                        firstRefAllele,
                        firstAltAllele,
                        tokens[5],
                        tokens[6],
                        tokens[7],
                        format);
                for (int index=9; index<tokens.length; index++) {
                    VcfSampleVariant sampleVariant
                        = new VcfSampleVariant(tokens[index]);
                    cohortVariant.addSampleVariant(sampleVariant);
                }
                
                cohort.addCohortVariant(cohortVariant);
                
                String secondRefAllele = refAllele.substring(1,2);
                String secondAltAllele = altAllele.substring(1,2);
                VcfCohortVariant cohortVariant2 
                    = new VcfCohortVariant(
                        chromosome,
                        Integer.parseInt(tokens[1])+1,
                        tokens[2],
                        secondRefAllele,
                        secondAltAllele,
                        tokens[5],
                        tokens[6],
                        tokens[7],
                        format);
                for (int index=9; index<tokens.length; index++) {
                    VcfSampleVariant sampleVariant
                        = new VcfSampleVariant(tokens[index]);
                    cohortVariant2.addSampleVariant(sampleVariant);
                }
                
                cohort.addCohortVariant(cohortVariant2);
                
                
            } else {
                VcfCohortVariant cohortVariant 
                    = new VcfCohortVariant(
                        chromosome,
                        Integer.parseInt(tokens[1]),
                        tokens[2],
                        refAllele,
                        altAllele,
                        tokens[5],
                        tokens[6],
                        tokens[7],
                        format);
                for (int index=9; index<tokens.length; index++) {
                    VcfSampleVariant sampleVariant
                        = new VcfSampleVariant(tokens[index]);
                    cohortVariant.addSampleVariant(sampleVariant);
                }
                
                cohort.addCohortVariant(cohortVariant);
            }
            
        }
    }
    
    
    
}


