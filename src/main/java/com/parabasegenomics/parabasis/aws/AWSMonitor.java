/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.aws;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Class to monitor the Parabase production AWS S3 bucket and 
 * look for newly uploaded vcf files.  When found, vcf files are pushed
 * to Omicia for annotation using the Omicia API (python scripts).
 * 
 * Note that all communication with the production S3 bucket is through
 * an encrypted client with keys managed by AWS KMS.
 * 
 * @author evanmauceli
 */
public class AWSMonitor {
    
    private static ListObjectsRequest listObjectsRequest;
    
    private final ScheduledExecutorService scheduler =
        Executors.newScheduledThreadPool(5);
            
    private static final Logger logger 
        = Logger.getLogger(AWSMonitor.class.getName());

    private static FileHandler fileHandler;
    
    // 4409 for ParabaseValidation/NBDxV1.1
    // 3923 for ParabaseProduction/NBDxV1.1
    // 35877 for NBDxV2
    private static String OMICIA_PROJECT_ID; 
    
    private final static String vcfFileSuffix = ".vcf.gz";
    private final static String SLASH = "/";
    
    private final static String PYTHON_PATH = "/usr/bin/python27";
    
    final static String PRODUCTION_BUCKET = "parabase.genomics.production";
    
    private final static String omiciaPythonUploadScript
            = "/home/ec2-user/omicia_api_examples/python/upload_genome.py";
    
    private final static String omiciaPythonUploadGender = "unspecified";
    private final static String omiciaPythonUploadFileFormat = "vcf";
    
    private final static String localTmpdirFilePath
        = "/home/ec2-user/tmp/";
    
    private final static String homeDirectory = "/home/ec2-user";
    private final static String logFileName = "AWSMonitor.log";
    
    private boolean restart;
    private Set<String> vcfFileETags;
    AWSDownloader downloader; 
    String bucket;
    Integer id;
    ReferenceGenomeTranslator referenceGenomeTranslator;
    File logFile;
    
    /**
     * Constructor. 
     * @throws java.io.IOException
     */           
    public AWSMonitor() 
    throws IOException {
          
        vcfFileETags = new TreeSet<>();
        downloader = new AWSDownloader();
        bucket = PRODUCTION_BUCKET;
        
        listObjectsRequest = new ListObjectsRequest()
        .withBucketName(bucket)
        .withPrefix("NBDxV1.1")
        .withPrefix("NBDxV2");
        

        referenceGenomeTranslator = new ReferenceGenomeTranslator();
        
        logFile = new File(homeDirectory+"/"+logFileName);
        
        try {
            fileHandler = new FileHandler("%h/"+logFileName,true);
        } catch (IOException ex) {
        }
        fileHandler.setLevel(Level.ALL);
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);

        logger.addHandler(fileHandler);
        logger.setLevel(Level.ALL);
             
        id=0;
        restart=true;
    }
    
    /**
     * Method to screen a list of files in an S3 bucket for new vcf files
     * to push to Omicia
     * @param objectListing The list of files to screen. 
     * @param restart True if restarting the monitor and we don't want to 
     * reload all the previous files. False otherwise.
     * @throws java.lang.InterruptedException 
     */
    public void update(ObjectListing objectListing, boolean restart) 
    throws InterruptedException {
        for (S3ObjectSummary objectSummary : 
                    objectListing.getObjectSummaries()) { 
            if (objectSummary.getKey().contains("vcf.gz")) {
                // new vcf file to upload
                if (!vcfFileETags.contains(objectSummary.getETag())) {
                    vcfFileETags.add(objectSummary.getETag());
                    if (!restart) {
                        pushFileToOmicia(objectSummary);                        
                    }                         
                }       
            }
        }
    }
    
    /**
     * Method to check the AWS production bucket for new vcf files and
     * push those to Omicia.
     * @throws java.io.IOException
     * @throws java.lang.InterruptedException
     */
  
    public void pollAndPush() 
    throws IOException, InterruptedException {  
        try {
            ObjectListing objectListing = downloader
                    .getEncryptionClient()
                    .listObjects(listObjectsRequest);  
            update(objectListing,restart);
            while (objectListing.isTruncated()) {
                objectListing = downloader
                    .getEncryptionClient()
                    .listNextBatchOfObjects(objectListing);  
                update(objectListing,restart);
            }
         } catch (AmazonServiceException ase) {
            String logstring 
                = "Caught an AmazonServiceException\n"
                + "Error Message: " + ase.getMessage() + "\n"
                + "HTTP Status Code: " + ase.getStatusCode() + "\n"
                + "AWS Error Code:   " + ase.getErrorCode() + "\n"
                + "Error Type:       " + ase.getErrorType() + "\n"
                + "Request ID:       " + ase.getRequestId(); 
                
            LogRecord logRecord 
                = new LogRecord(Level.SEVERE,logstring);
            logger.log(logRecord);

        } catch (AmazonClientException ace) {
            String logstring
                = "Caught an AmazonClientException\n"
                + "Error Message: " + ace.getMessage();
            LogRecord logRecord = new LogRecord(Level.SEVERE,logstring);
            logger.log(logRecord);
        }
        if (restart) {
            restart=false;
        }
    }
            
    /**
     * Method to monitor the Parabase production AWS S3 bucket and 
     * look for newly uploaded vcf files.  When found, vcf files are pushed
     * to Omicia for annotation using the Omicia API (python scripts).
     */
    public void monitor() {       
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    pollAndPush();
                } catch (IOException | InterruptedException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            }
        };
       
        ScheduledFuture<?> scheduledFuture
            = scheduler
                .scheduleAtFixedRate(
                    runnable, 0, 5, TimeUnit.MINUTES);    
        try {
            scheduledFuture.get();
        } catch (InterruptedException | ExecutionException ex) {
            logger.log(Level.SEVERE, null, ex);
        }        
    }
      
    /**
     * Method to parse the sample ID from the AWS key of the vcf file.
     * @param key AWS key to the vcf file to download.
     * @return Parsed sample ID from the vcf file.
     */
    public String parseKeyForId(String key) {
        int suffixIndex = key.indexOf(vcfFileSuffix);
        int lastSlashIndex = key.lastIndexOf(SLASH);
        return (key.substring(lastSlashIndex+1, suffixIndex));
    }

    public void parseKeyForOmiciaProjectId(String key) {
        String [] tokens = key.split("\\/");
        for (int i=0; i<tokens.length;i++) {
            if (tokens[i].equals("NBDxV1.1")) {
                OMICIA_PROJECT_ID="4409";
                break;
            } else if (tokens[i].equals("NBDxV2")) {
                OMICIA_PROJECT_ID="35877";
                break;
            }
        }
    }
    
    /**
     * Method to download a vcf file from the AWS S3 production bucket
     * to the local tmp directory.  
     * @param key AWS key to the vcf file to download.
     * @return A String with the full path to the local copy of the vcf
     * file.
     * @throws java.lang.InterruptedException
     */
    public String downloadVcfFileLocally(String key) 
    throws InterruptedException {
        String localPathToDownloadedFile
            = localTmpdirFilePath
            + parseKeyForId(key)
            + vcfFileSuffix;
        File localCopy = new File(localPathToDownloadedFile);
        
        downloader.downloadFileFromS3Bucket(
            bucket,
            key,
            localCopy);           
        return (localPathToDownloadedFile);        
    }
  
   /**
     * Method to get the newly uploaded file from S3 and upload
     * to Omicia for annotation
     * @param objectSummary the ObjectSummary for the newly uploaded file
     *                      on S3
     * @throws java.lang.InterruptedException
     */
    public void pushFileToOmicia(S3ObjectSummary objectSummary) 
    throws InterruptedException {
        // label corresponds to sampleID from upload
        String label = parseKeyForId(objectSummary.getKey());
        parseKeyForOmiciaProjectId(objectSummary.getKey());
        
        String localFileName 
            = downloadVcfFileLocally(objectSummary.getKey());
        
        File localFile = new File(localFileName);

        File translatedVcfFile
            = referenceGenomeTranslator.translate(localFile);
        String translatedVcfFilename = translatedVcfFile.getAbsolutePath();
                
        List<String> command = new ArrayList<>();
        command.add(PYTHON_PATH);
        command.add(omiciaPythonUploadScript);
        command.add(OMICIA_PROJECT_ID);
        command.add(label);
        command.add(omiciaPythonUploadGender);
        command.add(omiciaPythonUploadFileFormat);
        command.add(translatedVcfFilename);
        //command.add(id.toString());
        
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        processBuilder.redirectOutput(Redirect.appendTo(logFile));
        
        Process process = null;
        try {
            process = processBuilder.start();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        try {
            logger.log(Level.INFO, "running:{0}", command.toString());
            int waitFor = process.waitFor();
            ++id;
            logger.log(Level.INFO, "done:{0}", waitFor);
           
            //delete the local copy of the file
            logger.log(Level.INFO, 
                "clean up, attempting to delete:{0}", localFileName);
            File localFileToDelete 
                = new File(localFileName);      
            try {
                Files.delete(localFileToDelete.toPath());
                logger.log(Level.INFO,"done cleanup");
            } catch (IOException ex) {
                logger.log(Level.SEVERE,null,ex);               
            }
            
        } catch (InterruptedException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Main class: create a new monitor with assay name and s3bucket and start
     * it up.
     * @param args: 
     *      [0] == Name of the assay/test to be monitored.
     *      [1] == S3 Bucket to monitor.
     * @throws IOException 
     */
    public static void main(String[] args) 
    throws IOException {
        AWSMonitor awsMonitor 
            = new AWSMonitor();
        awsMonitor.monitor();
    }
    
}


