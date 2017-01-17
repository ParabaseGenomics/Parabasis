/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.aws.swf;

import com.amazonaws.services.simpleworkflow.flow.annotations.Asynchronous;
import com.amazonaws.services.simpleworkflow.flow.core.Promise;

/**
 *
 * @author evanmauceli
 */
public class CreateGapsReportsWorkflowImpl implements CreateGapsReportsWorkflow {
    
    private final CreateGapsReportsActivitiesClient gapsReportsActivitiesClient
        = new CreateGapsReportsActivitiesClientImpl();
    
    public CreateGapsReportsWorkflowImpl() {
        
    }
    
    @Override
    public void process(S3NameResource bamResource, Integer threshold) {
        
        Promise<Void> init 
            = gapsReportsActivitiesClient.initialize(bamResource, threshold);
        Promise<Void> processed 
            = processAfterInitialized(init);
        
    }

    @Asynchronous
    Promise<Void> processAfterInitialized(Promise<Void> init) {
        
        Promise<String> localBamFile 
            = gapsReportsActivitiesClient.downloadToLocalEC2();
        
        Promise<String> resourceFilepath
            = gapsReportsActivitiesClient.createResourceFile(localBamFile);
        
        Promise<Void> ranReports 
            = gapsReportsActivitiesClient.runGapsReport(resourceFilepath);
        
        Promise<Void> pushedReports 
            = gapsReportsActivitiesClient.pushGapReportsToS3(ranReports);
        
        return pushedReports;
    }
    
}
