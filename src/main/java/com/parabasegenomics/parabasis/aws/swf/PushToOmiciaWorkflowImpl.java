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
    
    public PushToOmiciaWorkflowImpl() {    
    }
    
    @Override
    public void process(S3NameResource vcfParser) {
        Promise<Void> init = pushToOmiciaActivitiesClient.initialize(vcfParser);
        Promise<Void> processed = processAfterInitialized(init); 
    }
    
    @Asynchronous
    Promise<Void> processAfterInitialized(Promise<Void> init) {
         Promise<String> localVcfFile 
            = pushToOmiciaActivitiesClient.downloadToLocalEC2();
        
        Promise<String> convertedVcfFile
            = pushToOmiciaActivitiesClient.convertCoordinates(localVcfFile);
        
        Promise<Void> pushedToOmicia
            = pushToOmiciaActivitiesClient.pushToOmicia(convertedVcfFile); 
        
        return pushedToOmicia;
    }
}
