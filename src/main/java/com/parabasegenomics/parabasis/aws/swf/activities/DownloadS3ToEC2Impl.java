/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.aws.swf.activities;

import static com.parabasegenomics.parabasis.aws.swf.SeqWorkflowActivities.downloader;
import com.parabasegenomics.parabasis.aws.swf.SeqWorkflowActivitiesImpl;
import java.io.File;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;

/**
 *
 * @author evanmauceli
 */
public class DownloadS3ToEC2Impl implements DownloadFromS3ToEC2 {
    
     private final static String localTmpdirFilePath
        = "/home/ec2-user/tmp/";
    
    
     // returns local path to vcf file
    @Override
    public String downloadToLocalEC2(Pair<String,String> location) {
        String localFile 
            = localTmpdirFilePath
            + "/"
            + UUID.randomUUID();
        try {
            downloader
                .downloadFileFromS3Bucket(
                    location.getKey(),
                    location.getValue(),
                    new File(localFile));
        } catch (InterruptedException ex) {
            Logger
                .getLogger(SeqWorkflowActivitiesImpl.class.getName())
                .log(Level.SEVERE, null, ex);
        }
        return localFile;
    }
    
    
}
