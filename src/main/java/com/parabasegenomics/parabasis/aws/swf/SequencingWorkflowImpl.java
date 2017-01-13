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
public class SequencingWorkflowImpl implements SequencingWorkflow {

    private final PushToOmiciaWorkflow pushToOmiciaWorkflow;
    private final CreateGapsReportsWorkflow gapsReports20xWorkflow;
    private final CreateGapsReportsWorkflow gapsReports10xWorkflow;
    
    public SequencingWorkflowImpl() {
        pushToOmiciaWorkflow = new PushToOmiciaWorkflowImpl();
        gapsReports20xWorkflow = new CreateGapsReportsWorkflowImpl();
        gapsReports10xWorkflow = new CreateGapsReportsWorkflowImpl();
    }
    
    public void process(S3NameResource vcfParser, S3NameResource bamParser) {
        Promise<Void> pushedToOmicia = pushToOmiciaWorkflow.process(vcfParser);
        Promise<Void> gaps20xDone = gapsReports20xWorkflow.process(bamParser,20);
        processNextGapsReport(gaps20xDone,bamParser);
    }
    
    public void processNextGapsReport(
        Promise<Void> donePrevious, 
        S3NameResource bamParser) {
        if (donePrevious.isReady()) {
            gapsReports10xWorkflow.process(bamParser,10);
        }
    }
    
}
