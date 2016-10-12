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
import com.amazonaws.services.s3.transfer.MultipleFileDownload;
import com.amazonaws.services.s3.transfer.TransferManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author evanmauceli
 */
public class AWSDownloader {   
    private static final String KEY_TAG = "[kms]";
    private static final String EQUALS = "=";
    
    //final static String PRODUCTION_BUCKET = "parabase.genomics.production";
    private static final String DATE_FORMAT = "yyyy-MM-dd-HH:mm:ss";
    private static final String PROFILE_NAME = "ParabasisAWSDownloader";
    
    private static final File credentialsFile 
        = new File(System.getProperty("user.home")+"/.aws/kms_credentials");
    
    private static final Logger logger 
        = Logger.getLogger(AWSDownloader.class.getName());
    private static final String logFileName = "AWSDownloader.log";
    
    private final TransferManager transferManager; 
    
    private static AmazonS3EncryptionClient encryptionClient;
    private static FileHandler fileHandler;
    private String kms_cmk_id;      
          
    public AWSDownloader() {
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
     *  
     * @return Returns the AWS S3 encryption client used for download.
     */
    public AmazonS3EncryptionClient getEncryptionClient() {
        return encryptionClient;
    }
    
    /**
     * Method to download all contents from an S3 bucket to a local directory.
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
     * Method to download a single file from an S3 bucket
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
        try {
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
        } catch (InterruptedException exception) {
            logger
                .log(Level.SEVERE,
                "Exception downloading:"
                + destinationFile.getAbsolutePath(),
                exception);
        }
    }
    
    /**
     * Main method lets you download a file from S3 or a full directory.
     * @param args
     *      [0] == S3 bucket
     *      [1] == key to file or directory
     *      [2] == local file or directory to download to
     *      [3] == file/directory switch: "S" for file, "D" for directory.
     * @throws InterruptedException 
     */
    public static void main(String[] args) 
    throws InterruptedException {
        AWSDownloader awsDownloader = new AWSDownloader();
        File file = new File(args[2]);
        
        System.out.println(args[0]);
        System.out.println(args[1]);
        System.out.println(args[2]);
        System.out.println(args[3]);
        if (args[3].equals("S")) {     
            awsDownloader.downloadFileFromS3Bucket(args[0], args[1], file);
        } else {
            awsDownloader.downloadS3BucketDirectory(args[0],args[1],file);
        }
        Handler [] handlers = logger.getHandlers();
        handlers[0].close();
        System.exit(0);
    }

    
    
}
