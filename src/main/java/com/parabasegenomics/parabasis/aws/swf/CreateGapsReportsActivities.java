/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.aws.swf;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.simpleworkflow.flow.annotations.Activities;
import com.amazonaws.services.simpleworkflow.flow.annotations.ActivityRegistrationOptions;

/**
 *
 * @author evanmauceli
 */
@ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = 300,
                             defaultTaskStartToCloseTimeoutSeconds = 300)
@Activities(version="1.4")

public interface CreateGapsReportsActivities {

    public String downloadToLocalEC2(S3NameResource nameResource);
    
    public String createResourceFile(
        String bamFile, 
        Integer coverageThreshold,
        S3NameResource nameResource);
    
    // run the gaps report locally - return the local path to the reports
    public void runGapsReport(String resourceFilepath);
    
    // push the gap reports to S3 for archiving.
    public void pushGapReportsToS3(S3NameResource nameResource)
    throws AmazonServiceException, InterruptedException;
    
    
}
