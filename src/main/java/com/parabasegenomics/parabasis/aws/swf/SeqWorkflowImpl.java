/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.aws.swf;

import com.amazonaws.services.simpleworkflow.flow.core.Promise;

public class SeqWorkflowImpl implements SeqWorkflow {
    
    private final SeqWorkflowActivitiesClient ops
        = new SeqWorkflowActivitiesClientImpl();
    
    @Override
    public void push(String file) {
        
        String bucket = file.substring(0,file.indexOf("/NBDx"));
        String key = file.substring(file.indexOf("NBDx"),file.length());

        
        Promise<String> validLocation
            = ops.isValidS3Location(bucket,key);

        Promise<String> localFile 
            = ops.downloadToLocalEC2(bucket,key);
        
        Promise<String> convertedFile
            = ops.convertCoordinates(localFile);
        
        ops.pushToOmicia(convertedFile);

    }
    
}
