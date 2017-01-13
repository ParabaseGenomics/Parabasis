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
@Activities(version="1.0")

public interface CreateGapsReportsActivities {
    

    public void initialize(S3NameResource bamResource, Integer threshold);
    
    public String downloadToLocalEC2();
    
    public String createResourceFile(String bamFile);
    
    // run the gaps report locally - return the local path to the reports
    public void runGapsReport(String resourceFilepath);
    
    // push the gap reports to S3 for archiving.
    public void pushGapReportsToS3()
    throws AmazonServiceException, InterruptedException;
    
    
}
