/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.aws;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * Class to handle the translation of our native hg19 variants to the 
 * b37 reference to keep alignment with Omicia's annotation pipeline.
 */
public class ReferenceGenomeTranslator {
    
    private static final Logger logger 
        = Logger.getLogger(ReferenceGenomeTranslator.class.getName());
    
    private static FileHandler fileHandler;
    
    private final static String VCF = "vcf";
    
    private final static String TRANSLATED_VCF_FILE_EXTENSION
        = ".b37.vcf";
    private final static String PYTHON_PATH = "/usr/bin/python27";
    
    private final static String homeDirectory 
        = "/home/ec2-user";
    
    private final static String crossmapHomeDirectory
        = homeDirectory + "/CrossMap-0.2.1";
    private final static String logFileName 
       = "ReferenceGenomeTranslator.log"; 
    
    String crossMapLiftoverScript
        = crossmapHomeDirectory + "/bin/CrossMap.py";
    String liftOverChainFile 
        = crossmapHomeDirectory + "/hg19ToGRCh37.over.chain";
    private final static String b37ReferenceFile
        =  crossmapHomeDirectory + "/human_g1k_v37.fasta";
    
    Integer id;
    File logFile;
   
    public ReferenceGenomeTranslator() 
    throws IOException { 
        id=0;
        logFile = new File(homeDirectory+"/"+logFileName);
        
        try {
            fileHandler = new FileHandler("%h/"+logFileName,true);
        } catch (IOException ex) {
        }
        fileHandler.setLevel(Level.ALL);
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);
        logger.addHandler(fileHandler);
        
    }
    
    /**
     * Method to create the filename for the translated vcf file.
     * @param vcfFile The untranslated vcf file.
     * @return The filename for the translated vcf file.
     */
    public String parseVcfFilename(String vcfFile) {
        
        // remove .gz
        String vcfFileWithoutExtensions 
            = FilenameUtils.removeExtension(vcfFile);
        
        // remove .vcf
        String vcfFileWithoutVcfExtension
            = FilenameUtils.removeExtension(vcfFileWithoutExtensions);
        
        String translatedVcfFilename 
            = vcfFileWithoutVcfExtension
            + TRANSLATED_VCF_FILE_EXTENSION;
        
        return translatedVcfFilename;
    }
    
    /**
     * Method to translate a vcf file called against the hg19 reference to a
     * vcf file called against the b37 reference.  Performs the liftOver using a
     * public software package called CrossMap.  
     * 
     * CrossMap provides a chain file to do the mapping from hg19 to b37, but 
     * the chain file they provide doesn't handle the mitochondrial chromosome 
     * properly.  The chain file used here is the one provided by CrossMap, with
     * the mito chain grabbed from the Broad Institute's liftOver files.
     * 
     * @param untranslatedVcfFile the vcf File (in hg19 coordinates)
     * @return the vcf file in b37 coordinates
     */
    public File translate(File untranslatedVcfFile) {
        
        String localFileName = untranslatedVcfFile.getAbsolutePath();
        String outputVcfFileName = parseVcfFilename(localFileName);
        
        List<String> command = new ArrayList<>();
        command.add(PYTHON_PATH); 
        command.add(crossMapLiftoverScript);
        command.add(VCF);
        command.add(liftOverChainFile);
        command.add(localFileName);
        command.add(b37ReferenceFile);
        command.add(outputVcfFileName);
        
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
            logger.log(Level.INFO, "done:{0}", waitFor);
           
        } catch (InterruptedException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
     
        return (new File(outputVcfFileName));
    }    
}
