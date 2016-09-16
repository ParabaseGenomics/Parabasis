/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.aws.swf;

import com.parabasegenomics.parabasis.aws.AWSMonitor;
import com.parabasegenomics.parabasis.aws.ReferenceGenomeTranslator;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;

/**
 *
 * @author evanmauceli
 */
public class PushToOmiciaActivitiesImpl implements PushToOmiciaActivities {

    // 4409 for ParabaseValidation/NBDxV1.1
    // 3923 for ParabaseProduction/NBDxV1.1
    // NBDxV2 35877
    private final static String OMICIA_PROJECT_ID = "4409"; 
    private final static String vcfFileSuffix = ".vcf.gz";
    private final static String SLASH = "/";
    
    private final static String PYTHON_PATH = "/usr/bin/python27";
    
    private final static String omiciaPythonUploadScript
            = "/home/ec2-user/omicia_api_examples/python/upload_genome.py";
    
    private final static String omiciaPythonUploadGender = "unspecified";
    private final static String omiciaPythonUploadFileFormat = "vcf";
    
    private final static String localTmpdirFilePath
        = "/home/ec2-user/tmp/";
    
    private final static String homeDirectory = "/home/ec2-user";
    private final static String logFileName = "AWSMonitor.log";
    
    private static final Logger logger 
        = Logger.getLogger(AWSMonitor.class.getName());
    
    // full S3 "path" to vcf file
    @Override
    public Pair<String,String> isValidS3Location(
        Pair<String,String> location)
    throws IOException {
        if (s3encryptionClient
            .doesObjectExist(
                location.getKey(), location.getValue())) {
            return location;
        } else {
            throw( new IOException("not valid s3 object:" + location));
        }
    }
    
    // returns local path to vcf file
    @Override
    public String downloadToLocalEC2(Pair<String,String> location) {
        String localFile 
            = localTmpdirFilePath
            + "/"
            + UUID.randomUUID();
        try {
            downloader
                .downloadFileFromS3Bucket(
                    location.getKey(),
                    location.getValue(),
                    new File(localFile));
        } catch (InterruptedException ex) {
            Logger
                .getLogger(PushToOmiciaActivitiesImpl.class.getName())
                .log(Level.SEVERE, null, ex);
        }
        return localFile;
    }
    
    // returns local path to converted vcf file
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
            Logger.getLogger(
                PushToOmiciaActivitiesImpl.class.getName())
                .log(Level.SEVERE, null, ex);
        }
       return translatedFilename;
    }
    
    // push file to Omicia for processing.
    @Override
    public void pushToOmicia(String location) {
        
        String label="test";
        Integer id=1;
        File logFile = new File(logFileName);
        
        List<String> command = new ArrayList<>();
        command.add(PYTHON_PATH);
        command.add(omiciaPythonUploadScript);
        command.add(OMICIA_PROJECT_ID);
        command.add(label); // this is the sample ID  - guess we need to keep it around
        command.add(omiciaPythonUploadGender);
        command.add(omiciaPythonUploadFileFormat);
        command.add(location);
        command.add(id.toString());
        
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
