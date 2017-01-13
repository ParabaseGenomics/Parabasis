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
    
    private static final String SLASH = "/";
    
    private final String s3Bucket;
    private final String s3Key;
    
    private String [] tokens;
    
    public S3NameResource(String bucket, String key) {
        s3Bucket=bucket;
        s3Key=key;      
        
        tokens = new String [5];
        parseKey();
        
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
        return (s3Bucket.contains("produciton"));
    }
    
    
    /**
     * Returns the assay ID from the key.
     * @return 
     */
    public String getAssay() {
        return tokens[0];
    }
    
    /**
     * Returns the sample ID from the key.
     * @return 
     */
    public String getSampleId() {
        return tokens[1];
    }
    
    /**
     * Returns the file name of the s3 key.
     * @return 
     */
    public String getFilename() {
        return tokens[4];
    }
    
    /**
     * Returns the run ID from the key.
     * @return 
     */
    public String getRunId() {
        return tokens[3];
    }
    
    /**
     * Returns the date from the key.
     * @return 
     */     
    public String getDate() {
        return tokens[2];
    }
    
    /**
     * Returns the "path" portion of the s3 key, so the key without the filename.
     * @return 
     */
    public String getKeyPrefix() {
        return s3Key.substring(0,s3Key.lastIndexOf(SLASH));
    }
    
    
    /**
     * Fill the tokens array by parsing the key. 
     * @return 
     */
    private void parseKey() {
        tokens = s3Key.split(SLASH);
    }
}
