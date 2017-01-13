/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.aws.swf;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.simpleworkflow.flow.annotations.Asynchronous;
import com.parabasegenomics.parabasis.aws.S3TransferUtility;
import com.parabasegenomics.parabasis.util.CreateResourceJsonUtility;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author evanmauceli
 */
public class CreateGapsReportsActivitiesImpl implements  CreateGapsReportsActivities {
    
    private final EC2Resource ec2Resource = new EC2Resource();
    private final S3TransferUtility s3TransferUtility = new S3TransferUtility();
    private GapsReportResource gapsReportResource;
    
    private S3NameResource nameResource;
    private Integer coverageThreshold;
    
    private static final Logger logger 
        = Logger.getLogger(CreateGapsReportsActivitiesImpl.class.getName());
    

    
    @Override
    public void initialize(S3NameResource bamResource, Integer threshold) {
        nameResource = bamResource;
        gapsReportResource = new GapsReportResource(nameResource);
        coverageThreshold = threshold;  
    }
    
    
    @Override
    public String downloadToLocalEC2() {
        // the filename we want to use upon download
        String localFilepath
            = ec2Resource.getTmpDir()
            + "/"
            + nameResource.getFilename();
        
        File localFile = new File(localFilepath);
        if (localFile.exists()) {
            return localFilepath;
        }
        
        try {
            s3TransferUtility
                .downloadFileFromS3Bucket(
                    nameResource.getBucket(),
                    nameResource.getKey(),
                    localFile);
        } catch (InterruptedException ex) {
            String message = "Cannot download from: " 
                + nameResource.getBucket()
                + "/"
                + nameResource.getKey();
            logger.log(Level.SEVERE, message, ex);
        }
        return localFilepath;
    }
    
    /**
     * Creates the json file with resources required to create the gaps reports.
     * @param localBamFile
     * @return Returns the path to the resource file.
     */
    @Override
    public String createResourceFile(String localBamFile) {
        
        String targetFilepath 
            = ec2Resource.getResourceDir() + "/NBDx/targets.bed";
        String genelistFilepath
            = ec2Resource.getResourceDir() + "/NBDx/genelist";
        if (nameResource.getAssay().equals("NBDxV1.1")) {
            targetFilepath 
                = ec2Resource.getResourceDir() + "/NBDxHL/targets.bed";
            genelistFilepath
                = ec2Resource.getResourceDir() + "/NBDxHL/genelist";
        }
        
        String refseq = ec2Resource.getRefseqBuild();
        String gencode = ec2Resource.getGencodeBuild();
        
        String jsonFile = "";
        CreateResourceJsonUtility jsonResource = new CreateResourceJsonUtility();
        try {
            jsonFile = jsonResource.createResourceJson(
                localBamFile,
                targetFilepath,
                nameResource.getAssay(),
                genelistFilepath,
                refseq,
                gencode,
                coverageThreshold.toString());
        } catch (FileNotFoundException ex) {
            String message ="Cannot create resource json for:" + localBamFile;
            logger.log(Level.SEVERE, message, ex);
        }
        
        return jsonFile;          
    }
    
    /**
     * Run the gaps report.
     * @param resourceFilepath
     * @return 
     */
    @Override
    public void runGapsReport(String resourceFilepath) {
        // do nothing yet
    }
    
    /**
     * Push the local file to S3 for archiving.
     * @return 
     * @throws java.lang.InterruptedException 
     */
    @Override
    public void pushGapReportsToS3() 
    throws AmazonServiceException, InterruptedException {
       
        String bucket = nameResource.getBucket();
        String keyPrefix = nameResource.getKeyPrefix();
        
        String targetGapFilename = gapsReportResource.getTargetReportFilename();
        File targetGapFile = new File(ec2Resource.getTmpDir() + "/" + targetGapFilename);
        String targetGapFileKey = keyPrefix + "/" + targetGapFilename;
 
        String geneGapFilename = gapsReportResource.getGeneReportFilename();
        File geneGapFile = new File(ec2Resource.getTmpDir() + "/" + geneGapFilename);
        String geneGapFileKey = keyPrefix + "/" + geneGapFilename;
        
        String resourceFilename = gapsReportResource.getResourceFilename();
        File resourceFile = new File(ec2Resource.getTmpDir() + "/" + resourceFilename);
        String resourceFileKey = keyPrefix + "/" + resourceFilename;
        
        pushFileToS3(targetGapFile,bucket,targetGapFileKey);
        pushFileToS3(geneGapFile,bucket,geneGapFileKey);
        pushFileToS3(resourceFile,bucket,resourceFileKey);
        
    }
    
    /**
     *
     * @param file
     * @param bucket
     * @param key
     * @throws AmazonServiceException
     * @throws InterruptedException
     */
    @Asynchronous
    public void pushFileToS3(File file, String bucket, String key) 
    throws AmazonServiceException, InterruptedException {
        try { 
            s3TransferUtility.uploadFileToS3Bucket(file, bucket, key);
        } catch (AmazonClientException ex) {
            String message 
                = "Cannot upload:"+file.getAbsolutePath()+ " to " + nameResource.getBucket() + "/" + key;
            logger.log(Level.SEVERE, message, ex);
        }  
    }
    
}
