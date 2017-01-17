/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.aws.swf;

import com.amazonaws.services.simpleworkflow.flow.annotations.Activities;
import com.amazonaws.services.simpleworkflow.flow.annotations.ActivityRegistrationOptions;

/**
 * Omicia workflow: 
 *  download vcf file from s3
 *  convert vcf file from hg19 to b37 (required by Omicia)
 *  push converted vcf file to Opal for processing
 * @author evanmauceli
 */

@ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = 300,
                             defaultTaskStartToCloseTimeoutSeconds = 300)
@Activities(version="1.4")

public interface PushToOmiciaActivities {
    
    public void initialize(S3NameResource vcfResource);
    
     // returns local path to vcf file
    public String downloadToLocalEC2();
    
    // returns local path to converted vcf file
    public String convertCoordinates(String location);
    
    // push file to Omicia for processing.
    public void pushToOmicia(String location);

}
