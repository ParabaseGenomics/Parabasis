/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.aws.swf;

import com.amazonaws.services.simpleworkflow.flow.core.Promise;
import javafx.util.Pair;

/**
 *
 * @author evanmauceli
 */
public class PushToOmiciaWorkflowImpl implements PushToOmiciaWorkflow {
    
    private final PushToOmiciaActivitiesClient ops
        = new PushToOmiciaActivitiesClientImpl();
    
    @Override
    public void push(Pair<String, String> location) {
        
        Promise< Pair<String,String> > validLocation
            = ops.isValidS3Location(location);
    
        Promise<String> localFile 
            = ops.downloadToLocalEC2(validLocation);
        
        Promise<String> convertedFile
            = ops.convertCoordinates(localFile);
        
        ops.pushToOmicia(convertedFile);
    }
    
}
