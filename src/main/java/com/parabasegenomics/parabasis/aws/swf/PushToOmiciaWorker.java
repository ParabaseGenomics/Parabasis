/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.aws.swf;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflowClient;
import com.amazonaws.services.simpleworkflow.flow.ActivityWorker;
import com.amazonaws.services.simpleworkflow.flow.WorkflowWorker;

/**
 *
 * @author evanmauceli
 */
public class PushToOmiciaWorker {

    
    /**
     * @param args the command line arguments
     * @throws java.lang.InstantiationException
     * @throws java.lang.IllegalAccessException
     * @throws java.lang.NoSuchMethodException
     */
    public static void main(String[] args) 
    throws 
        InstantiationException, 
        IllegalAccessException, 
        SecurityException, 
        NoSuchMethodException {
        ClientConfiguration config 
            = new ClientConfiguration().withSocketTimeout(70*1000);   
        
        AWSCredentials credentials 
        = new ProfileCredentialsProvider().getCredentials();
        
        AmazonSimpleWorkflow service
            = new AmazonSimpleWorkflowClient(credentials,config);
            
            
        service.setEndpoint("https://swf.us-east-1.amazonaws.com");
        String domain = "PushToOmiciaDomain";
        String taskList = "PushToOmiciaTasklist";
        
        ActivityWorker activityWorker 
            = new ActivityWorker(service,domain,taskList);
        activityWorker.addActivitiesImplementation(new PushToOmiciaActivitiesImpl());
        activityWorker.start();
        
        WorkflowWorker workflowWorker
            = new WorkflowWorker(service,domain,taskList);
        workflowWorker.addWorkflowImplementationType(PushToOmiciaWorkflowImpl.class);
        workflowWorker.start();
    }
    
}
