/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.aws.swf;

import com.amazonaws.services.s3.AmazonS3EncryptionClient;
import com.amazonaws.services.simpleworkflow.flow.annotations.Activities;
import com.amazonaws.services.simpleworkflow.flow.annotations.ActivityRegistrationOptions;
import com.parabasegenomics.parabasis.aws.AWSDownloader;
import java.io.IOException;
import javafx.util.Pair;

/**
 *
 * @author evanmauceli
 */
@ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = 300,
                             defaultTaskStartToCloseTimeoutSeconds = 300)
@Activities(version="1.0")


public interface PushToOmiciaActivities {
    
    public static final AWSDownloader downloader 
        = new AWSDownloader();
    
    public static final AmazonS3EncryptionClient s3encryptionClient
        = downloader.getEncryptionClient();
    
    // full S3 "path" to vcf file
    public Pair<String,String> isValidS3Location(Pair<String,String> location)
    throws IOException;
    
    // returns local path to vcf file
    public String downloadToLocalEC2(Pair<String,String> location);
    
    // returns local path to converted vcf file
    public String convertCoordinates(String location);
    
    // push file to Omicia for processing.
    public void pushToOmicia(String location);
    
}
