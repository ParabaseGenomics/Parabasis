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
public class SequencingWorkflowImpl implements SequencingWorkflow {

    private final PushToOmiciaWorkflowClientFactory pushToOmiciaWorkflowFactory
        = new PushToOmiciaWorkflowClientFactoryImpl();

    private final CreateGapsReportsWorkflowClientFactory gapsReports20xWorkflowFactory
        = new CreateGapsReportsWorkflowClientFactoryImpl();
    
    private final CreateGapsReportsWorkflowClientFactory gapsReports10xWorkflowFactory
        = new CreateGapsReportsWorkflowClientFactoryImpl();
   
    @Override
    public void process(S3NameResource vcfParser, S3NameResource bamParser) {

        PushToOmiciaWorkflowClient pushToOmiciaWorkflowClient 
            = pushToOmiciaWorkflowFactory.getClient();

        CreateGapsReportsWorkflowClient gapsReports20xWorkflowClient 
            = gapsReports20xWorkflowFactory.getClient();
                 
        pushToOmiciaWorkflowClient.pushToOmicia(vcfParser);
        gapsReports20xWorkflowClient.runGapsReport(bamParser,20);
        
        //Promise<Void> gaps20xDone = processFirstGapsReport(bamParser);
        //processNextGapsReport(gaps20xDone,bamParser);
    }
    
    @Asynchronous
    public Promise<Void> processFirstGapsReport(S3NameResource bamParser) {
        CreateGapsReportsWorkflowClient gapsReports20xWorkflowClient 
            = gapsReports20xWorkflowFactory.getClient();
         
        gapsReports20xWorkflowClient.runGapsReport(bamParser,20);
        return Promise.Void();
    }
    
    @Asynchronous
    public void processNextGapsReport(
        Promise<Void> donePrevious, 
        S3NameResource bamParser) {
        
         CreateGapsReportsWorkflowClient gapsReports10xWorkflowClient 
            = gapsReports10xWorkflowFactory.getClient();
        
        if (donePrevious.isReady()) {
            gapsReports10xWorkflowClient.runGapsReport(bamParser,10);
        }
    }
    
}
