/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.aws;

import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3EncryptionClient;
import com.amazonaws.services.s3.model.CryptoConfiguration;
import com.amazonaws.services.s3.model.KMSEncryptionMaterialsProvider;
import com.amazonaws.services.s3.transfer.Download;
import com.amazonaws.services.s3.transfer.TransferManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author evanmauceli
 */
public class AWSDownloader {
      private static AmazonS3EncryptionClient encryptionClient;
    
    private static final File credentialsFile 
        = new File(System.getProperty("user.home")+"/.aws/kms_credentials");
    
    private static final String KEY_TAG = "[kms]";
    private final static String EQUALS = "=";
    
    final static String PRODUCTION_BUCKET = "parabase.genomics.production";
    final static String DATE_FORMAT = "yyyy-MM-dd-HH:mm:ss";

    final static String PROFILE_NAME = "ParabaseProdutionDownloader";
    
    TransferManager transferManager; 
    
    private static FileHandler fileHandler;
    private String kms_cmk_id;  
          
    private static Logger logger 
        = Logger.getLogger(AWSDownloader.class.getName());

    private final static String logFileName = "AWSDownloader.log";
    
        
    public AWSDownloader() {
        System.setProperty(SDKGlobalConfiguration.ENABLE_S3_SIGV4_SYSTEM_PROPERTY, "true");
                
        try {
            fileHandler = new FileHandler("%h/"+logFileName,true);
        } catch (IOException ex) {
          ex.printStackTrace();
        }

        logger.setLevel(Level.ALL);
        fileHandler.setLevel(Level.ALL);
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);
        logger.addHandler(fileHandler);

        try {
            kms_cmk_id = readFromCredentialsFile();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
                
        KMSEncryptionMaterialsProvider materialProvider 
                = new KMSEncryptionMaterialsProvider(kms_cmk_id);
       
        encryptionClient = new AmazonS3EncryptionClient(
                new ProfileCredentialsProvider().getCredentials(), 
                materialProvider,
                new CryptoConfiguration().withKmsRegion(Regions.US_EAST_1))
            .withRegion(Region.getRegion(Regions.US_EAST_1));
        
        transferManager 
                = new TransferManager(encryptionClient);
                
    }

            /**
     * Method to return the AWS KMS key ID from the credentials file.
     * @return A String with the AWS KMS key ID
     */
    public String readFromCredentialsFile() 
    throws FileNotFoundException, IOException {
        String key;
        BufferedReader reader 
            = new BufferedReader(new FileReader(credentialsFile));
        while (reader.ready()) {
            key = reader.readLine();
            if (key.equals(KEY_TAG)) {
                String keyLine = reader.readLine();
                key = keyLine.substring(keyLine.indexOf(EQUALS));
                return key;
            }
        }
        throw new IOException("Cannot find KMS key in credentials file.");
    }
    
    
    /**
     * 
     * @return Returns the bucket we're downloading from
     */
    public String getBucket() {
        return PRODUCTION_BUCKET;
    }
    
    /**
     *  
     * @return Returns the AWS S3 encryption client used for download.
     */
    public AmazonS3EncryptionClient getEncryptionClient() {
        return encryptionClient;
    }
    
    /**
     * Method to download a file from an S3 bucket
     * @param bucket AWS S3 bucket containing the file.
     * @param key AWS S3 key to file.
     * @param file The downloaded file.
     * @throws InterruptedException 
     */
    public void downloadFromAWS(
            String bucket,
            String key,
            File file) 
    throws InterruptedException {   
        try {
            Download download 
                = transferManager.download(bucket, key, file);   
            logger.log(Level.INFO, "Downloading: "  + bucket + "/" + key + " to " + file);
            download.waitForCompletion();
        } catch (InterruptedException exception) {
            logger.log(Level.SEVERE,
            "Exception downloading:" + file.getAbsolutePath(),
            exception);
        }
    }
}
