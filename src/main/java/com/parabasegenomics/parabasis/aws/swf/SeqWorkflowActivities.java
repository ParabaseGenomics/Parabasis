/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.aws.swf;

import com.amazonaws.services.simpleworkflow.flow.annotations.Activities;
import com.amazonaws.services.simpleworkflow.flow.annotations.ActivityRegistrationOptions;

/**
 *
 * @author evanmauceli
 */
@ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = 300,
                             defaultTaskStartToCloseTimeoutSeconds = 300)
@Activities(version="1.0")


public interface SeqWorkflowActivities {
    
    // returns local path to vcf file
    public String downloadToLocalEC2(String bucket, String key);
    
    // returns local path to converted vcf file
    public String convertCoordinates(String location);
    
    // push file to Omicia for processing.
    public String pushToOmicia(String location);
    
     // create a json resource file from the provided bam file.
    public String createResourceFile(String bamFile);
    
    // run the gaps report locally - return the local path to the reports
    public String runGapsReport(String resourceFilepath);
    
    // push the file at "location" to the provided S3 bucket and key.
    public void pushToS3(String location, String bucket, String key);
   
    public void setAssay(String name);
    
    public void setThreshold(String threshold);
    
    
}
