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

/**
 *
 * @author evanmauceli
 */
public class SeqWorkflowTestMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    throws InterruptedException {

       ClientConfiguration config 
            = new ClientConfiguration().withSocketTimeout(70*1000);   
        
        AWSCredentials credentials 
            = new ProfileCredentialsProvider().getCredentials();
        
        AmazonSimpleWorkflow service
            = new AmazonSimpleWorkflowClient(credentials,config);
            
            
        service.setEndpoint("https://swf.us-east-1.amazonaws.com");
        String domain = "SeqWorkflowDomain";

        
        
        SeqWorkflowClientExternalFactory factory
            = new SeqWorkflowClientExternalFactoryImpl(service,domain);

        String id = "testPush";
        SeqWorkflowClientExternal pusher
            = factory.getClient(id);
        pusher.doWork(
            "parabase.genomics.sandbox",
            "NBDxV1.1/NA12878A/2016-07-25-15:21:42/160718_M03281_0050_000000000-ANMNN",
            "NBDxV1.1","NA12878A_S2");
        
        
    }
    
}
