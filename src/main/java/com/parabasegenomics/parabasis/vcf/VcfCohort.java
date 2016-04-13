/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.vcf;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to encapsulate a cohort, which is a collection of variants and samples
 * from a vcf file.
 * @author evanmauceli
 */
public class VcfCohort {
    
    private List<VcfCohortVariant> cohortVariants;
    private List<String> samples;
    
    
    public VcfCohort() {
        cohortVariants = new ArrayList<>();
        samples = new ArrayList<>();        
    }
    
    /**
     * Copy constructor.
     * @param toCopy 
     */
    public VcfCohort(VcfCohort toCopy) {
        this.cohortVariants = toCopy.getVariants();
        this.samples = toCopy.getSamples();
    }
    
    
    /**
     * Get methods.
     * @return 
     */
    
    public List<VcfCohortVariant> getVariants() {
        return cohortVariants;
    }
    public List<String> getSamples() {
        return samples;
    }
    public int getSampleCount() {
        return samples.size();
    }
    public int getVariantCount() {
        return cohortVariants.size();
    }
    
    /**
     * Add a line from a vcf file (VcfCohortVariant) to the cohort.
     * @param variant 
     */
    public void addCohortVariant(VcfCohortVariant variant) { 
        cohortVariants.add(variant);
    }
    
    /**
     * Add a sample name to the cohort.
     * @param sample 
     */
    public void addSample(String sample) {
        samples.add(sample);
    }
    
    /**
     * Method to return the sample name at the specified index.
     * @param index Index into the list of samples.
     * @return Returns the name of the sample.
     */
    public String getSampleName(int index) {
        return samples.get(index);
    }
    
    /**
     * Method to return the cohort variant at the specified index.
     * @param index Index into the list of cohort variants.
     * @return Returns the requested cohort variant.
     */
    public VcfCohortVariant getCohortVariant(int index) {
        return cohortVariants.get(index);
    }
    
    
    
}
