/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.vcf;

import htsjdk.samtools.util.Interval;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author evanmauceli
 */
public class VcfCohortVariant implements Comparable<VcfCohortVariant>{
    
    private final static String comma = ",";
    
    private final String chromosome;
    private final int position;
    private final String variantId;
    private final String referenceAllele;
    private final String alternateAllele;
    private final String qualityField;
    private final String filterField;
    private final String infoField;
    private final String formatKey;
    
    List<VcfSampleVariant> sampleVariants;
    
    public VcfCohortVariant(
        String chr,
        int pos,
        String id,
        String ref,
        String alt,
        String qual,
        String filter,
        String info,
        String format) {
            chromosome=chr;
            position=pos;
            variantId=id;
            referenceAllele=ref;
            alternateAllele=alt;
            qualityField=qual;
            filterField=filter;
            infoField=info;
            formatKey=format;
            
            sampleVariants=new ArrayList<>();
               
    }
    
    /**
     * Copy constructor.
     * @param toCopy VcfCohortVariant to copy.
     */
    public VcfCohortVariant(VcfCohortVariant toCopy) {
        chromosome=toCopy.getChromosome();
        position=toCopy.getPosition();
        variantId=toCopy.getVariantId();
        referenceAllele=toCopy.getReferenceAllele();
        alternateAllele=toCopy.getAlternateAllele();
        qualityField=toCopy.getQualityField();
        filterField=toCopy.getFilterField();
        infoField=toCopy.getInfoField();
        formatKey=toCopy.getFormatKey();
        
        sampleVariants=toCopy.sampleVariants;
    }
    
    // Getter methods.
    public String getChromosome() {
        return chromosome;
    }
    public int getPosition() {
        return position;
    }
    public String getVariantId() {
        return variantId;
    }
    public String getReferenceAllele() {
        return referenceAllele;
    }
    public String getAlternateAllele() {
        return alternateAllele;
    }
    public String getQualityField() {
        return qualityField;
    }
    public String getFilterField() {
        return filterField;
    }
    public String getInfoField() {
        return infoField;
    }
    public String getFormatKey() {
        return formatKey;
    }
    
    /**
     * Add a sample to the cohort.  Note that in multi-sample vcf files, a
     * sample does not necessarily have a non-reference allele.
     * @param sampleVariant Sample to add to the cohort.
     */
    public void addSampleVariant(VcfSampleVariant sampleVariant) {
        sampleVariants.add(sampleVariant);
    }
    
    /**
     * Method to get the value associated with a key from the INFO field.
     * @param key Sub-field to get from the INFO field.
     * @param infoFieldParser Parser class for the INFO field.
     * @return Returns the value from the INFO field for the specified key.
     */
    public String getInfoForKey(String key, InfoFieldParser infoFieldParser) {
        infoFieldParser.parse(infoField);
        return (infoFieldParser.find(key));
    }
    
    /**
     * Returns the end position of the variant as an open interval.
     * @return 
     */
    public int getEndPosition() {
        if (isSNP()) {
            return position+1;
        } else if (isMNP()) {
            return position+alternateAllele.length();
        } else {
            return position+referenceAllele.length();
        }
    }
    
    /**
     * Method to determine if the variant is a SNP.
     * @return Returns true if the variant is a SNP, false otherwise.
     */
    public boolean isSNP() {
        int altLength=alternateAllele.length();
        int refLength=referenceAllele.length();
        if (altLength==1 && refLength==1) {
            return true;
        } else if (refLength>1) {
            return false;
        } else {
            String [] tokens = alternateAllele.split(comma);
            for (String token : tokens) {
                if (token.length() != refLength) {
                    return false;
                }
            }
            return true;
        } 
    }
    
    /**
     * Method to determine if the variant is a MNP (i.e. a change of more than
     * one base pair that is not an insertion or deletion.
     * @return Returns true if more than one bp is changed (but not inserted or
     * deleted), false otherwise.
     */
    public boolean isMNP() {
        int altLength=alternateAllele.length();
        int refLength=referenceAllele.length(); 
        
        if (altLength==1 && refLength==1) {
            return false;
        } else {
            String [] tokens = alternateAllele.split(comma);
            for (String token : tokens) {
                if (token.length() == refLength) {
                    return true;
                }
            }
            return false;
        }
    }
    
    /**
     * Method to determine if the variant is an insertion or deletion (indel).
     * @return Returns true if the variant gains or loses bps.
     */
    public boolean isIndel() {
        return ( (!isSNP() && !isMNP()) );
    }
    
    /**
     * Method to return the variant as an Interval object (0-based, open).
     * @return 
     */
    public Interval asInterval() {
        int start = position-1;
        int end = getEndPosition()-1;
        return (new Interval(chromosome,start,end));
    }
    
    
    /**
     * Method provides access to the samples.
     * @return Returns the list of sample variants for this cohort variant.
     */
    public List<VcfSampleVariant> getSampleVariants() {
        return sampleVariants;
    }
    
  
    public int getSampleVariantCount() {
        return sampleVariants.size();
    }
  
    /**
     * 
     * @param index Index of the sample in the vcf file.
     * @return Returns the VcfSampleVariant for the requested sample at the
     * specified index.
     */
    public VcfSampleVariant getSampleVariant(int index) {
        return sampleVariants.get(index);
    }
    
    /**
     * Method to check if the positions of two variants overlap (i.e. are on the
     * same chromosome and at the same position.
     * @param toCompare Variant to compare this to.
     * @return Returns true if the chromosomes and positions match, 
     * false otherwise.
     */
    public boolean overlaps(VcfCohortVariant toCompare) {
        if (!this.chromosome.equals(toCompare.getChromosome())) {
            return false;
        }
        return (this.position == toCompare.getPosition());
        
    }
 
    /**
     * Sort based on chromosome, start position, null objects come last.
     * @param toCompare
     * @return 
     */
    @Override
    public int compareTo(VcfCohortVariant toCompare) {
        return (this.asInterval().compareTo(toCompare.asInterval()));        
    }
    
    
}
