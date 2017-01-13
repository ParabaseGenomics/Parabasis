/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.aws.swf;

import com.amazonaws.services.simpleworkflow.flow.core.Promise;


public class PushToOmiciaWorkflowImpl implements PushToOmiciaWorkflow {

    private final PushToOmiciaActivitiesClient pushToOmiciaActivitiesClient;
    
    public PushToOmiciaWorkflowImpl() {
        pushToOmiciaActivitiesClient = new PushToOmiciaActivitiesClientImpl();
        
    }
    @Override
    public Promise<Void> process(S3NameResource vcfParser) {
        pushToOmiciaActivitiesClient.initialize(vcfParser);
  
        Promise<String> localVcfFile 
            = pushToOmiciaActivitiesClient.downloadToLocalEC2();
        
        Promise<String> convertedVcfFile
            = pushToOmiciaActivitiesClient.convertCoordinates(localVcfFile);
        
        Promise<Void> pushedToOmicia
            = pushToOmiciaActivitiesClient.pushToOmicia(convertedVcfFile); 
        
        return pushedToOmicia;
    }
}
