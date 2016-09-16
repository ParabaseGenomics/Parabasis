/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.aws.swf.activities;

import static com.parabasegenomics.parabasis.aws.swf.PushToOmiciaActivities.s3encryptionClient;
import java.io.IOException;
import javafx.util.Pair;

/**
 *
 * @author evanmauceli
 */
public class IsValidS3LocationImpl implements IsValidS3Location {

    /**
     * Check to see if the specified S3 location (bucket,key) exists.
     * @param location S3 bucket, key pair.
     * @return Returns bucket and key if the S3 location is good.
     * @throws IOException 
     */
    
    @Override
    public Pair<String,String> isValidS3Location(
        Pair<String,String> location)
    throws IOException {
        if (s3encryptionClient
            .doesObjectExist(
                location.getKey(), location.getValue())) {
            return location;
        } else {
            throw( new IOException("not valid s3 object:" + location));
        }
    }
    
}
