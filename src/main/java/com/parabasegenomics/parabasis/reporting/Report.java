/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.reporting;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author evanmauceli
 */
public abstract class Report {
    
    static final String TAB = "\t";
    static final String NEWLINE = "\n";
    
    
    BufferedWriter bufferedWriter;
    File reportFile;
    
    public Report(File file) 
    throws IOException {      
        reportFile=file;
        bufferedWriter = new BufferedWriter(new FileWriter(file));  
    }
    
    /**
     * Write method.
     * @param toWrite
     * @throws java.io.IOException
     */
    public void write(String toWrite) 
    throws IOException {
        bufferedWriter.write(toWrite);
    }
    
    /**
     * Returns the file being written to.
     * @return 
     */
    public File getFile() {
        return reportFile;
    }
    
    /**
     * Returns the writer for this Report.
     * @return 
     */
    public BufferedWriter getWriter() {
        return bufferedWriter;
    }
    
    /**
     * Closes the writer.
     * @throws java.io.IOException
     */
    public void close() 
    throws IOException {
        bufferedWriter.close();
    }

    
}
