/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.aws.swf;

/**
 *
 * @author evanmauceli
 */

import com.amazonaws.services.simpleworkflow.flow.annotations.Execute;
import com.amazonaws.services.simpleworkflow.flow.annotations.Workflow;
import com.amazonaws.services.simpleworkflow.flow.annotations.WorkflowRegistrationOptions;
import com.amazonaws.services.simpleworkflow.model.ChildPolicy;

@Workflow
@WorkflowRegistrationOptions(
    defaultExecutionStartToCloseTimeoutSeconds = 3600, 
    defaultChildPolicy = ChildPolicy.TERMINATE)

public interface SequencingWorkflow {   
    
    @Execute(version = "1.4")
    public void process(S3NameResource vcfParser, S3NameResource bamParser);
}
