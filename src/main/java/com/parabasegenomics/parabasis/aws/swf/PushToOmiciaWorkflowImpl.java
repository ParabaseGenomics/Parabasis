/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.aws.swf;

import com.amazonaws.services.simpleworkflow.flow.annotations.Asynchronous;
import com.amazonaws.services.simpleworkflow.flow.core.Promise;


public class PushToOmiciaWorkflowImpl implements PushToOmiciaWorkflow {

    private final PushToOmiciaActivitiesClient pushToOmiciaActivitiesClient
        = new PushToOmiciaActivitiesClientImpl();
    
    @Override
    public void pushToOmicia(S3NameResource nameResource) {
        Promise<String> localVcfFile 
            = pushToOmiciaActivitiesClient.downloadToLocalEC2(nameResource);
        
        Promise<String> convertedVcfFile
            = pushToOmiciaActivitiesClient.convertCoordinates(localVcfFile);
       
        uploadToOmicia(convertedVcfFile,nameResource);
    }
    
    /**
     *
     * @param convertedFile
     * @param nameResource
     */
    @Asynchronous
    private void uploadToOmicia(
        Promise<String> convertedFile,
        S3NameResource nameResource) {

        pushToOmiciaActivitiesClient.push(convertedFile.get(), nameResource);
        
    }
}
