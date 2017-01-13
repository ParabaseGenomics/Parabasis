/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.aws.swf;

import com.amazonaws.AmazonClientException;
import com.parabasegenomics.parabasis.util.CreateResourceJsonUtility;
import com.parabasegenomics.parabasis.util.ReferenceGenomeTranslator;
import com.parabasegenomics.parabasis.aws.S3TransferUtility;
import com.parabasegenomics.parabasis.omicia.OmiciaResource;
import com.parabasegenomics.parabasis.target.ReportOnGaps;
import java.io.File;
import java.io.FileNotFoundException;
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
public class SequencingWorkflowActivitiesImpl implements SequencingWorkflowActivities {

    private final static String vcfFileSuffix = ".vcf.gz";
    private final static String SLASH = "/";
     
    private final static String [] TEST_IDS = {"NBDx","NBDx_HL"};
        
    private final static String PYTHON_PATH = "/usr/bin/python27";
    private final static String HOME_DIR = "/home/ec2-user";
    
    private final static String omiciaPythonUploadGender = "unspecified";
    private final static String omiciaPythonUploadFileFormat = "vcf";
    
    private final static String localTmpdirFilePath
        = HOME_DIR + "/tmp";
    
    private final static String localResourcesdirFilepath
       = HOME_DIR + "/Resources";
       
    private final static String omiciaPythonUploadScript
            = localResourcesdirFilepath 
            + "/omicia_api_examples/python/GenomeWorkflows/upload_genome.py";
    
    private final static OmiciaResource omiciaResource
        = new OmiciaResource();
    
    private final static String logFileName = "SeqWorkflow.log";
    
    private static final Logger logger 
        = Logger.getLogger(SequencingWorkflowActivitiesImpl.class.getName());
    
    private final S3TransferUtility s3TransferUtility 
        = new S3TransferUtility();
    private String assay;
    private String threshold;

    
    // returns local path to vcf file
    @Override
    public String downloadToLocalEC2(String bucket,String key) {
        
        // last bit of the key is the filename we want to use upon download
        String fileName = key.substring(key.lastIndexOf(SLASH)+1);    
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
                logger.log(Level.SEVERE, bucket+"/"+key, ex);
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
     * @param location Local path to the vcf file (b37 reference). 
     * @return Returns "Done" on successful completion, or throws an exception
     * trying.
     */
    @Override
    public String pushToOmicia(String location) {
        
        String label
            =location
                .substring(
                    location
                        .lastIndexOf(SLASH)+1,
                        location.indexOf(".b37.vcf"));   
        Integer id=1;
        File logFile = new File(HOME_DIR+ "/" + label + ".pushToOmicia.log");
        
        // set the correct project id for Omicia given where the vcf file is going.
        String omiciaProjectId = omiciaResource.getValidationId(assay);
        
        
        List<String> command = new ArrayList<>();
        command.add(PYTHON_PATH);
        command.add(omiciaPythonUploadScript);
        command.add(omiciaProjectId);
        command.add(label); // this is the sample ID  - guess we need to keep it around
        command.add(omiciaPythonUploadGender);
        command.add(location);
        //command.add(id.toString());
        
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(logFile));
        
        Process process = null;
        try {
            process = processBuilder.start();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "cannot start push process", ex);
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
                Files.delete(localFileToDelete.toPath());
                logger.log(Level.INFO,"done cleanup");
            } catch (IOException ex) {
              logger.log(Level.SEVERE,null,ex);               
            }
            
        } catch (InterruptedException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return "Done";
    }
      
    // run the gaps report locally - return the local prefix of the path 
    // to the reports
    @Override
    public String runGapsReport(String resourceFile) {

        String [] args = new String [1];
        args[0]=resourceFile;
        try {
            ReportOnGaps.main(args);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, resourceFile, ex);
        }
        
        String reportFilePrefix 
            = resourceFile.substring(0,resourceFile.indexOf(".resource.json"));
        return reportFilePrefix;
    }
    
    // push the file at "location" to the provided S3 bucket and key.
    @Override
    public String pushToS3(String location, String bucket, String key) {
   
        File file = new File(location);
        try { 
            s3TransferUtility.uploadFileToS3Bucket(file, bucket, key);
        } catch (AmazonClientException | InterruptedException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return "done";
    }
    
    
    /**
     * Creates the json resource file used to create a gaps report.
     * @param bamFile
     * @return 
     */
    @Override
    public String createResourceFile(String bamFile) {
        
        String targetFilepath = localResourcesdirFilepath + "/NBDx/targets.bed";
        String genelistFilepath = localResourcesdirFilepath + "/NBDx/genelist";
        if (assay.equals("NBDxV1.1")) {
            targetFilepath =  localResourcesdirFilepath + "/NBDxHL/targets.bed";
            genelistFilepath = localResourcesdirFilepath + "/NBDxHL/genelist";
        }
        
        String refseq =  localResourcesdirFilepath + "/RefSeq_4_11_2016";
        String gencode = localResourcesdirFilepath + "/gencode_v19_02162015";
        
        String jsonFile = "";
        CreateResourceJsonUtility jsonResource = new CreateResourceJsonUtility();
        try {
            jsonFile = jsonResource.createResourceJson(
                bamFile,
                targetFilepath,
                assay,
                genelistFilepath,
                refseq,
                gencode,
                threshold);
        } catch (FileNotFoundException ex) {
            logger.log(Level.SEVERE, "cannot create resource for "+bamFile, ex);
        }
        
        return jsonFile;      
    }
 
    @Override
    public String setAssay(String name) {
        assay=name;
        return "done";
    }
    
    @Override
    public String setThreshold(String T) {
        threshold=T;
        return "done";
    }
   
}

