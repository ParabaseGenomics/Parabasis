/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.vcf;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Estimate the sex of a sample by counting homozygous variants on the X
 * chromosome.
 * @author evanmauceli
 */
public class VcfChrXCounter {
    
    /**
     * expect >= THRESHOLD fraction of variants on the X chromosome 
     * to be homozygous for male samples
     */
    private static final Double THRESHOLD = .9;
    private final VcfLoader variantLoader;
    private final InfoFieldParser infoFieldParser;
    
    public VcfChrXCounter(String vcfFile) 
    throws FileNotFoundException, IOException {
        variantLoader = new VcfLoader(vcfFile);
        variantLoader.loadFile();
        infoFieldParser = new InfoFieldParser();
        infoFieldParser.parse(vcfFile);
    }
    
    public boolean isMale() {
        Integer homozygousXCount=0;
        Integer totalXCount=0;
        
        VcfCohort vcfCohort = variantLoader.getCohort();
        int variantCount = vcfCohort.getVariantCount();
        for (int index=0; index<variantCount; ++index) {
            final VcfCohortVariant vcfCohortVariant
                = vcfCohort.getCohortVariant(index);
            
            // only accept SNPs
            if (!vcfCohortVariant.isSNP()) {
                continue;
            }
            
            // only accept SNPs on the X chromosome
            if (!vcfCohortVariant.getChromosome().equals("chrX")) {
                continue;
            }
            
            // onlyaccept SNPs with good coverage so we don't get fooled
            String variantCoverage 
                = vcfCohortVariant.getInfoForKey("DP", infoFieldParser);
            if (variantCoverage == null) {
                System.out.println("No DP field found for " 
                    + vcfCohortVariant.getChromosome()
                +":"
                + vcfCohortVariant.getPosition()
                +"\t"
                + vcfCohortVariant.getInfoField());
            }
            if ( Integer.parseInt(variantCoverage)< 40) {
                continue;
            }
            
            // only one sample per vcf file, so use the index 0
            VcfSampleVariant vcfSampleVariant
                = vcfCohortVariant.getSampleVariant(0);
            
            if (vcfSampleVariant
                .isHomozygous(
                    vcfCohortVariant.getReferenceAllele(), 
                    vcfCohortVariant.getAlternateAllele())) {
                ++homozygousXCount;
            }
            
            ++totalXCount;
            
        }

        return ( homozygousXCount.doubleValue()/totalXCount.doubleValue() 
            >= THRESHOLD );        
    } 
}
