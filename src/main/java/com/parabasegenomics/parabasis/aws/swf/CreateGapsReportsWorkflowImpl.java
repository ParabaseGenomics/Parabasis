/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.aws.swf;

import com.amazonaws.services.simpleworkflow.flow.core.Promise;

/**
 *
 * @author evanmauceli
 */
public class CreateGapsReportsWorkflowImpl implements CreateGapsReportsWorkflow {
    
    private final CreateGapsReportsActivitiesClient gapsReportsActivitiesClient;
    
    public CreateGapsReportsWorkflowImpl() {
        gapsReportsActivitiesClient = new CreateGapsReportsActivitiesClientImpl();
    }
    
    @Override
    public Promise<Void> process(S3NameResource bamResource, Integer threshold) {
        
        gapsReportsActivitiesClient.initialize(bamResource, threshold);
        
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
