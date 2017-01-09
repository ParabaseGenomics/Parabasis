/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.aws.swf;

import com.amazonaws.AmazonClientException;
import com.parabasegenomics.parabasis.aws.ReferenceGenomeTranslator;
import com.parabasegenomics.parabasis.aws.S3TransferUtility;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author evanmauceli
 * 
 * 
 *         // but wait: need to support more than one Omicia project id
 * 
 * 
 * 
 */
public class SeqWorkflowActivitiesImpl implements SeqWorkflowActivities {

    // 4409 for ParabaseValidation/NBDxV1.1
    // 3923 for ParabaseProduction/NBDxV1.1
    // 35877 for ParabaseProduction/NBDxV2
    private final static String [] OMICIA_PROJECT_IDS = { "35877", "3923", "4409" }; 
    private final static String vcfFileSuffix = ".vcf.gz";
    private final static String SLASH = "/";
     
    private final static String PYTHON_PATH = "/usr/bin/python27";
    private final static String HOME_DIR = "/home/ec2-user";
    
    private final static String omiciaPythonUploadScript
            = HOME_DIR + "/omicia_api_examples/python/upload_genome.py";
    
    private final static String omiciaPythonUploadGender = "unspecified";
    private final static String omiciaPythonUploadFileFormat = "vcf";
    
    private final static String localTmpdirFilePath
        = HOME_DIR + "/tmp/";
    
    private final static String logFileName = "SeqWorkflow.log";
    
    private static final Logger logger 
        = Logger.getLogger(SeqWorkflowActivitiesImpl.class.getName());
    
    private S3TransferUtility s3TransferUtility;
    
    
    // returns local path to vcf file
    @Override
    public String downloadToLocalEC2(String bucket,String key) {
        
        // last bit of the key is the filename we want to use upon download
        String fileName = key.substring(key.lastIndexOf(SLASH));    
        String localFile 
            = localTmpdirFilePath
            + "/"
            + fileName;
        
        try {
            s3TransferUtility
                .downloadFileFromS3Bucket(
                    bucket,
                    key,
                    new File(localFile));
        } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, null, ex);
        }
        return localFile;
    }
    
    /**
     * Converts a vcf file in hg19 coordinates to one in b37 coordinates
     * for Omicia, since Omicia doesn't annotate hg19.  
     * @param location The local path to the hg19 vcf file.
     * @return Returns the local path to the b37 vcf file.
     */
    @Override
    public String convertCoordinates(String location) {
        String translatedFilename = null;
        try {
            ReferenceGenomeTranslator referenceGenomeTranslator
                = new ReferenceGenomeTranslator();
            
            File translatedFile
            = referenceGenomeTranslator
                .translate(new File(location));
            
            translatedFilename 
                = translatedFile.getAbsolutePath();
    
        } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
        }
       return translatedFilename;
    }
    
    /**
     * Push a vcf file to Omicia for annotation.
     * @param location Local path to the vcf file. 
     */
    @Override
    public String pushToOmicia(String location) {
        
        String label=location.substring(location.lastIndexOf(SLASH));   
        Integer id=1;
        File logFile = new File(label + ".pushToOmicia.log");
        
        // set the correct project id for Omicia given where the vcf file is going.
        String omiciaProjectId = OMICIA_PROJECT_IDS[0];
        if (location.contains("NBDxV1.1")) {
            omiciaProjectId = OMICIA_PROJECT_IDS[1];
        } else if (location.contains("Validation")) {
            omiciaProjectId = OMICIA_PROJECT_IDS[2];
        }
        
        List<String> command = new ArrayList<>();
        command.add(PYTHON_PATH);
        command.add(omiciaPythonUploadScript);
        command.add(omiciaProjectId);
        command.add(label); // this is the sample ID  - guess we need to keep it around
        command.add(omiciaPythonUploadGender);
        command.add(omiciaPythonUploadFileFormat);
        command.add(location);
        //command.add(id.toString());
        
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(logFile));
        
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
                "clean up, attempting to delete:{0}", location);
            File localFileToDelete 
                = new File(location);      
            try {
                Files.list(localFileToDelete.toPath());//Files.delete(localFileToDelete.toPath());
                logger.log(Level.INFO,"done cleanup");
            } catch (IOException ex) {
              logger.log(Level.SEVERE,null,ex);               
            }
            
        } catch (InterruptedException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return "Done";
    }
      
      // run the gaps report locally - return the local paths to the reports
    @Override
    public String runGapsReport(String localBamFile) {
        // create json
        // run main (target::reportOngaps.main)
        // return thelocal path to the report file - but wait, there are 3 files!!!
        //   but wait: all files have a common core!!!
        
        String filePath 
            = localBamFile.substring(0,localBamFile.indexOf(".bam"));
        String localResourceFile = filePath + ".resources.json";
        
        
        
        String reportBase = "";
        return reportBase;
    }
    
    // push the file at "location" to the provided S3 bucket and key.
    @Override
    public void pushToS3(String location, String bucket, String key) {
   
        File file = new File(location);
        try { 
            s3TransferUtility.uploadFileToS3Bucket(file, bucket, key);
        } catch (AmazonClientException | InterruptedException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }
    
    
}
