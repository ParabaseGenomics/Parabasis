/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.aws.swf;

/**
 * Class to parse useful information from the production s3 bucket and key
 * conventions.
 * 
 * S3 Key format: assay/sample ID/date/run ID/filename
 * 
 * @author evanmauceli
 */
public class S3NameResource {
    
    private final String SLASH = "/";
    private final String PRODUCTION = "production";
    
    private  String s3Bucket;
    private  String s3Key;

    
    public S3NameResource(String bucket, String key) {
        s3Bucket=bucket;
        s3Key=key;   
    }
    
    public S3NameResource() {
        s3Bucket=null;
        s3Key=null;
    }
    
    public void setBucket(String bucket) {
        s3Bucket=bucket;
    }
    public void setKey(String key) {
        s3Key=key;
    }
       
    public String getBucket() {
        return s3Bucket;
    }
    public String getKey() {
        return s3Key;
    }
    
    /**
     * Returns true if the held bucket is the production bucket.
     * @return 
     */
    public boolean isProduction() {
        return (s3Bucket.contains(PRODUCTION));
    }
    
    
    /**
     * Returns the assay ID from the key.
     * @return 
     */
    public String getAssay() {
        String [] tokens = s3Key.split(SLASH);
        return tokens[0];
    }
    
    /**
     * Returns the sample ID from the key.
     * @return 
     */
    public String getSampleId() {
        String [] tokens = s3Key.split(SLASH);
        return tokens[1];
    }
    
    /**
     * Returns the file name of the s3 key.
     * @return 
     */
    public String getFilename() {
        String [] tokens = s3Key.split(SLASH);
        return tokens[4];
    }
    
    /**
     * Returns the "path" portion of the s3 key, so the key without the filename.
     * @return 
     */
    public String getKeyPrefix() {
        return s3Key.substring(0,s3Key.lastIndexOf(SLASH));
    }
    
}
