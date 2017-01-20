/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.aws.swf;

import com.parabasegenomics.parabasis.aws.S3TransferUtility;
import com.parabasegenomics.parabasis.omicia.OmiciaResource;
import com.parabasegenomics.parabasis.util.ReferenceGenomeTranslator;
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
 */
public class PushToOmiciaActivitiesImpl implements PushToOmiciaActivities {
    

    private final OmiciaResource omiciaResource = new OmiciaResource();
    private final EC2Resource ec2Resource = new EC2Resource();
    private final S3TransferUtility s3TransferUtility = new S3TransferUtility();
    
    private static final Logger logger 
        = Logger.getLogger(PushToOmiciaActivitiesImpl.class.getName());
    
    
    @Override
    public String downloadToLocalEC2(S3NameResource nameResource) {
        
        // last bit of the key is the filename we want to use upon download
        String localFile 
            = ec2Resource.getTmpDir()
            + "/"
            + nameResource.getFilename();
        try {
            s3TransferUtility
                .downloadFileFromS3Bucket(
                    nameResource.getBucket(),
                    nameResource.getKey(),
                    new File(localFile));
        } catch (InterruptedException ex) {
            String message = "Cannot download from: " 
                + nameResource.getBucket()
                + "/"
                + nameResource.getKey();
            logger.log(Level.SEVERE, message, ex);
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
     * @param nameResource
     */
    @Override
    public void push(
        String location, 
        S3NameResource nameResource) {
        // set the correct project id for Omicia given where the vcf file is going.
        String omiciaProjectId 
            = omiciaResource.getValidationId(nameResource.getAssay());
            
        List<String> command = new ArrayList<>();
        command.add(ec2Resource.getPythonPath());
        command.add(ec2Resource.getOmiciaScriptPath());
        command.add(omiciaProjectId);
        command.add(nameResource.getSampleId());
        command.add(omiciaResource.getDefaultSex());
        command.add(location);
        
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        processBuilder
            .redirectOutput(
                ProcessBuilder.Redirect.appendTo(
                    new File(PushToOmiciaActivitiesImpl.class.getName())));
        
        Process process = null;
        try {
            process = processBuilder.start();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "cannot start push process", ex);
        }
        try {
            logger.log(Level.INFO, "running:{0}", command.toString());
            int waitFor = process.waitFor();

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
    }
    
    
}
