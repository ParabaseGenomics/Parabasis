/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.aws.swf;

/**
 * A central repository of information about the production EC2 instance deployment.
 * @author evanmauceli
 */
public class EC2Resource {
    
    private final String PYTHON_PATH = "/usr/bin/python27";
    private final String HOME_DIR = "/home/ec2-user"; 
    
    private final String localTmpdirPath
        = HOME_DIR + "/tmp";
    
    private final String localResourcesdirPath
       = HOME_DIR + "/Resources";
       
    private final String omiciaPythonUploadScript
            = localResourcesdirPath 
            + "/omicia_api_examples/python/GenomeWorkflows/upload_genome.py";
    
    private final String refseqBuild
        = localResourcesdirPath + "/RefSeq_4_11_2016";
    private final String gencodeBuild
        = localResourcesdirPath + "/gencode_v19_02162015";
    
    public EC2Resource() {       
    }
    
    public String getPythonPath() {
        return PYTHON_PATH;
    }
    public String getTmpDir() {
        return localTmpdirPath;
    }
    public String getResourceDir() {
        return localResourcesdirPath;
    }
    public String getOmiciaScriptPath() {
        return omiciaPythonUploadScript;
    }
    public String getRefseqBuild() {
        return refseqBuild;
    }
    public String getGencodeBuild() {
        return gencodeBuild;
    }
}
