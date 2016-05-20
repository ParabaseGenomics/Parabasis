/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.coverage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.json.JsonObject;

/**
 *
 * @author evanmauceli
 */
public class AssayCoverageModel {
    
    private final String assayName;
    private final List<IntervalCoverage> intervalCoverages;
    
    public AssayCoverageModel(String name) {
        assayName=name;
        intervalCoverages=new ArrayList<>();
    }
    
    
    // TODO: we'll want a decorator for coverage - how does that 
    // work???
    
    
    /**
     * Is the provided coverage an outlier given the current state of the model?
     * @param intervalCoverage The coverage value to compare to the model.
     * @return Returns true if the provided coverage is outside of the 
     * current model distribution, false otherwise.
     * 
     * TODO: define "outside".
     * 
     */
    public boolean isOutlier(IntervalCoverage intervalCoverage) {
        return false;
    }
    
    /**
     * Does the current model show signs of wildly varying coverage for any
     * of the held intervals?
     * @return Returns a list of outliers.  If none, returns an empty list.
     * 
     * TODO: define what it means to be an outlier.
     * 
     */
    public List<IntervalCoverage> findOutliers() {
        return new ArrayList<>();
    }
    
    /**
     * 
     * @return Returns the name of the assay for this model.
     */
    public String getAssayName() {
        return assayName;
    }
    
    public void readFromFile(File file) {
        
    }
    
    public void writeToFile(File file) {
        
    }
    
    public void update(IntervalCoverage intervalCoverage) {
        
    }
    
    public void initialize(JsonObject jsonObject) {
        
        // parse json to get a list of BAM files 
        //  util:Reader:readJson - key "BAM"
        
        // parse json to get a list of targets
        //  util:Reader:readJson key "TARGETS"
    
        // create a new IntervalCoverage object for each target in the assay
        //  parse json for TARGETS
        //  util:Reader:readBEDFile
        //  loop over TARGETS
        //      new IC(target)
        //      add to member list
        //  done
  
        
        // loop over IC objects in member list
        //      loop over BAMs
        //          calculate average coverage over IC's interval
        //              BamReader:getMeanCoverage(target) ???
        //          update IC object with new coverage
        //      done
        // done
        

    }

}
