/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.vcf;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author evanmauceli
 */
public class VcfSampleVariant {
 
    private static final String comma = ",";
    private static final String colon = ":";
    private static final String GENOTYPE_KEY = "GT";
    private static final int GENOTYPE_INDEX = 0;
    private static final String GENOTYPE_SEPARATOR = "/";
    private static final String PHASED_GENOTYPE_SEPARATOR = "|";
    
    private final String sampleData;
    
    public VcfSampleVariant(String data) {
        sampleData = data;
    }
    
    /**
     * Copy constructor.
     * @param toCopy 
     */
    public VcfSampleVariant(VcfSampleVariant toCopy) {
        this.sampleData = toCopy.getData();
    }
    
    /**
     * Method that returns the data string for this sample.
     * @return 
     */
    public String getData() {
        return sampleData;
    }
    
    /**
     * Method to determine if the variant is homozygous reference (no-call).
     * @param referenceAllele The reference allele at this position.
     * @param alternateAllele The alternate allele for this sample at 
     * this position.
     * @return Returns true if the variant is homozygous reference, false 
     * otherwise.
     */
    public boolean isNoCall(String referenceAllele,String alternateAllele) {
        String genotype = this.getGenotype(referenceAllele,alternateAllele);
        
        String [] tokens = genotype.split(colon);
        return (
            tokens[0].equals(referenceAllele) 
            && tokens[1].equals(referenceAllele));
      
    }

    /**
     * Method to determine if the variant is homozygous.
     * @param referenceAllele The reference allele at this position.
     * @param alternateAllele The alternate allele for this sample at 
     * this position.
     * @return Returns true if the variant is homozygous, false otherwise.
     */
    public boolean isHomozygous(String referenceAllele,String alternateAllele) {
        String genotype = this.getGenotype(referenceAllele,alternateAllele);
        String [] tokens = genotype.split(colon);
        if (tokens.length<2) {
            throw new IllegalArgumentException("no colon in genotype "+ genotype);
        }
        return (tokens[0].equals(tokens[1]));       
    }

    /**
     * Method to determine if the variant is a SNP.
     * @param referenceAllele The reference allele at this position.
     * @param alternateAllele The alternate allele for this sample at 
     * this position.
     * @return Returns true if the variant is a SNP, false otherwise.
     */
    public boolean isSnp(String referenceAllele,String alternateAllele) {
        String genotype = this.getGenotype(referenceAllele,alternateAllele);
        String [] tokens = genotype.split(colon);
        
        String allele1 = tokens[0];
        String allele2 = tokens[1];
        if (allele1.length()==1 
            && allele2.length()==1 
            && referenceAllele.length()==1) {
            return true;
        }
        if (allele1.length()==referenceAllele.length()
            && allele2.length()==referenceAllele.length()) {
            return true;
        }
        return false;       
    }
    
    /**
     * Parse the sample data for a specified FORMAT field entry.
     * @param fieldName Name of the requested FORMAT field.
     * @param key A string containing the key for the FORMAT field. This 
     * string is held by the CohortVariant.
     * @return Returns the requested FORMAT field for this sample.
     */
    public String getField(String fieldName, String key) {
        String [] tokens = key.split(colon);
        int indexOfField = 0;
        for (; indexOfField < tokens.length; indexOfField++) {
            if (tokens[indexOfField].equals(fieldName)) {
                break;
            }
        }
        String [] dataTokens = sampleData.split(colon);
        return (dataTokens[indexOfField]);
    }
    
    public String getGenotype(String referenceAllele,String alternateAllele) {
        List<String> alleles = new ArrayList<>();
        
        alleles.add(referenceAllele);
        // parse the alternate allele in case there are multiples
        if (!alternateAllele.contains(comma)) {
            alleles.add(alternateAllele);
        } else {
            String [] tokens = alternateAllele.split(comma);
            for (String token : tokens) {
                alleles.add(token);
            }
        }
        
        String [] dataTokens = sampleData.split(colon);
        String genotypeString = dataTokens[GENOTYPE_INDEX];
        String genotypeSeparator = GENOTYPE_SEPARATOR;
        if (genotypeString.contains(PHASED_GENOTYPE_SEPARATOR)) {
            genotypeSeparator=PHASED_GENOTYPE_SEPARATOR;
        }
        
        String [] genotypeTokens = genotypeString.split(genotypeSeparator);
        int allele1Index 
            = Math.min(
                Integer.parseInt(genotypeTokens[0]),
                Integer.parseInt(genotypeTokens[1]));
            
        int allele2Index 
            = Math.max(
                Integer.parseInt(genotypeTokens[0]),
                Integer.parseInt(genotypeTokens[1]));
        
        String genotype 
            = alleles.get(allele1Index)
            + colon + alleles.get(allele2Index);
        
        return genotype;
        
    }
    
}

