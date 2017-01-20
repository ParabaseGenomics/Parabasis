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

    @Override
    public void runGapsReport(S3NameResource bamResource, Integer threshold) {
        
        Promise<String> localBamFile 
            = gapsReportsActivitiesClient.downloadToLocalEC2(bamResource);
        
        Promise<String> resourceFilepath
            = createResourceFile(localBamFile,threshold,bamResource);
        
        Promise<Void> ranReports 
            = gapsReportsActivitiesClient.generateGapsReport(resourceFilepath);
        
        push(bamResource,ranReports);
 
    }

    @Asynchronous
    Promise<String> createResourceFile(
        Promise<String> bamFile, 
        Integer threshold,
        S3NameResource bamResource) {
     
        return (gapsReportsActivitiesClient
                .createResourceFile(bamFile.get(),threshold,bamResource));
       
    }
    
    @Asynchronous
    void push(S3NameResource nameResource, Promise<Void> ranReports) {
        if (ranReports.isReady()) {
            gapsReportsActivitiesClient.pushGapReportsToS3(nameResource);
        } //else {
          //  throw new IllegalStateException("reports not created");
        //}
    }
        
}
