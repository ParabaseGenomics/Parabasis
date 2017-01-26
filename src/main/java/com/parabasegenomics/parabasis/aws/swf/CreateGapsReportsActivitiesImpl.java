/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.aws.swf;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.parabasegenomics.parabasis.aws.S3TransferUtility;
import com.parabasegenomics.parabasis.util.CreateResourceJsonUtility;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author evanmauceli
 */
public class CreateGapsReportsActivitiesImpl implements  CreateGapsReportsActivities {
    
    private final EC2Resource ec2Resource = new EC2Resource();
    private final S3TransferUtility s3TransferUtility = new S3TransferUtility();

    private static final Logger logger 
        = Logger.getLogger(CreateGapsReportsActivitiesImpl.class.getName());
    private final String logFileName = "CreateGapsReportActivitiesImpl.log";
    private FileHandler fileHandler;
    
    public CreateGapsReportsActivitiesImpl() {
        
        try {
            fileHandler = new FileHandler("%h/"+logFileName,true);
        } catch (IOException ex) {
        }

        fileHandler.setLevel(Level.ALL);
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);
        logger.addHandler(fileHandler);
    }
     
     
    @Override
    public String downloadToLocalEC2(S3NameResource nameResource) {
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
     * @param coverageThreshold
     * @param nameResource
     * @return Returns the path to the resource file.
     */
    @Override
    public String createResourceFile(
        String localBamFile,
        Integer coverageThreshold,
        S3NameResource nameResource) {
        
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
    public void generateGapsReport(String resourceFilepath) {
        // do nothing yet
    }
    
    /**
     * Push the local file to S3 for archiving.
     * @param nameResource
     * @return 
     * @throws java.lang.InterruptedException 
     */
    @Override
    public void pushGapReportsToS3(S3NameResource nameResource) 
    throws AmazonServiceException, InterruptedException {
       
        GapsReportResource gapsReportResource
            = new GapsReportResource(nameResource);
        
        String bucket = nameResource.getBucket();
        String keyPrefix = nameResource.getKeyPrefix();
        
        String targetGapFilename = gapsReportResource.getTargetReportFilename();
        File targetGapFile = new File(ec2Resource.getTmpDir() + "/" + targetGapFilename);
 
        String geneGapFilename = gapsReportResource.getGeneReportFilename();
        File geneGapFile = new File(ec2Resource.getTmpDir() + "/" + geneGapFilename);
        
        String resourceFilename = gapsReportResource.getResourceFilename();
        File resourceFile = new File(ec2Resource.getTmpDir() + "/" + resourceFilename);
        
        List<File> filesToUpload = new ArrayList<>();
        //filesToUpload.add(targetGapFile);
        //filesToUpload.add(geneGapFile);
        filesToUpload.add(new File(resourceFile.getAbsolutePath()));
        File localDir = new File(ec2Resource.getTmpDir());
        
        try {
            s3TransferUtility
                .uploadFileListToS3Bucket(
                bucket,
                keyPrefix,
                localDir,    
                filesToUpload);
        } catch (AmazonClientException ex) {
            String message = "Cannot upload to: " 
                + nameResource.getBucket()
                + "/"
                + nameResource.getKey();
            logger.log(Level.SEVERE, message, ex);
        }
        
    }
    
    
    @Override
    public void dummyUp() {
        
    }
    
}
