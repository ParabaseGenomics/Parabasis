/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.aws;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.event.ProgressTracker;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3EncryptionClient;
import com.amazonaws.services.s3.model.CryptoConfiguration;
import com.amazonaws.services.s3.model.KMSEncryptionMaterialsProvider;
import com.amazonaws.services.s3.transfer.Download;
import com.amazonaws.services.s3.transfer.MultipleFileDownload;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Move KMS-encrypted files to and from S3.  
 * 
 * Needs the file: <HOME>/aws/kms_credentials to encrypt/decrypt.
 * 
 * @author evanmauceli
 */
public class S3TransferUtility {
    private static final String KEY_TAG = "[kms]";
    private static final String EQUALS = "=";

    private static final String DATE_FORMAT = "yyyy-MM-dd-HH:mm:ss";
    
    private static final File credentialsFile 
        = new File(System.getProperty("user.home")+"/.aws/kms_credentials");
    
    private final Logger logger 
        = Logger.getLogger(S3TransferUtility.class.getName());
    private static final String logFileName = "S3TransferUntility.log";
    
    private final TransferManager transferManager; 
    
    private static AmazonS3EncryptionClient encryptionClient;
    private  FileHandler fileHandler;
    private String kms_cmk_id;  
    
    private final ProgressTracker progressTracker;
    
    public S3TransferUtility()  {
        System.setProperty(SDKGlobalConfiguration.ENABLE_S3_SIGV4_SYSTEM_PROPERTY, "true");
                
        try {
            fileHandler = new FileHandler("%h/"+logFileName,true);
        } catch (IOException ex) {
        }

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
        
        progressTracker = new ProgressTracker();

    }
    
    /**
     * @return Returns the AWS S3 encryption client used for download.
     */
    public final AmazonS3EncryptionClient getEncryptionClient() {
        return encryptionClient;
    }
    
    /**
     * Returns the Progress associated with a transfer.
     * @return 
     */
    public Double getPctTransferred() {
        long requestedBytes = progressTracker.getProgress().getRequestContentLength();
        long transferredBytes = progressTracker.getProgress().getResponseBytesTransferred();
        
        return (double) transferredBytes/requestedBytes;
    }
    
    
 /**
     * Method to download and decrypt a single file from an S3 bucket. 
     * @param bucket AWS S3 bucket containing the file.
     * @param key AWS key to the file.
     * @param destinationFile Where to put the downloaded file.
     * @throws InterruptedException 
     */
    public void downloadFileFromS3Bucket(
            String bucket,
            String key,
            File destinationFile) 
    throws InterruptedException {   
        Download download
            = transferManager
                .download(bucket, key, destinationFile);
        String logString
            = "Downloading: "
            + bucket
            + "/"
            + key
            + " to "
            + destinationFile;
        logger.log(Level.INFO,logString);
        download.waitForCompletion();
    }    
    
    
    /**
     * Method to download and decrypt all contents from an S3 bucket to a local directory.
     * @param bucket AWS S3 bucket.
     * @param keyPrefix AWS S3 key to the "directory" containing the files.
     * @param destinationDirectory The downloaded files go into this directory.
     * @throws InterruptedException 
     */
    public void downloadS3BucketDirectory(
            String bucket,
            String keyPrefix,
            File destinationDirectory) 
    throws InterruptedException {        
        MultipleFileDownload download 
                = transferManager
                    .downloadDirectory(bucket, keyPrefix, destinationDirectory);
        
        String logString 
            = "Downloading: " 
            + bucket 
            + "/" 
            + keyPrefix 
            + " to " 
            + destinationDirectory;
        logger.log(Level.INFO,logString);
        download.waitForCompletion();
    }    
    
    /**
     * Encrypt and upload a single file to S3.
     * @param file The file to upload to S3.
     * @param bucket The destination S3 bucket.
     * @param key The destination key for the file.
     * @throws java.lang.InterruptedException
     */
    public void uploadFileToS3Bucket(
        File file,
        String bucket,
        String key) 
    throws AmazonClientException, AmazonServiceException, InterruptedException {
        
        Upload upload = transferManager.upload(bucket,key,file);   
        upload.addProgressListener(progressTracker);
        
        upload.waitForCompletion();
    }
    
    
    
    /**
     * Method to return the AWS KMS key ID from the credentials file.
     * @return A String with the AWS KMS key ID
     */
    private String readFromCredentialsFile() 
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
     * Method to check if the provided S3 location exists.
     * @param bucket The S3 bucket in question.
     * @param key "Path" to the file in question.
     * @return Returns the concatenated bucket and key as a String.
     * @throws IOException if the location is not valid.
     */
    private String isValidS3Location(
        String bucket,String key)
    throws IOException {
        String location = bucket+"/"+key;
        if (encryptionClient
            .doesObjectExist(
                bucket, key)) {
            return location;
        } else {
            throw( new IOException("not valid s3 object:" + location));
        }
    }
}
