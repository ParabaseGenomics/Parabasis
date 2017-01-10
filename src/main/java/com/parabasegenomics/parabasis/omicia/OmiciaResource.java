/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.omicia;

/**
 * Class to hold the Omicia workspace and project we're working with.
 * @author evanmauceli
 */
public class OmiciaResource {
    
    private final String project;
    private final String workspace;
    
    public OmiciaResource(String theProject, String theWorkspace) {
        project = theProject;
        workspace = theWorkspace;
    }
    
    /**
     * Returns the current workspace.
     * @return 
     */
    public String getWorkspace() {
        return workspace;
    }
    
    /**
     * Returns the current project.
     * @return 
     */
    public String getProject() {
        return project;
    }
}
