/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.reporting;


import static com.parabasegenomics.parabasis.decorators.FormatPatterns.percentPattern;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author evanmauceli
 */
public class Report {
    
    BufferedWriter bufferedWriter;
    File reportFile;
    
    public Report(File file) 
    throws IOException {      
        reportFile=file;
        bufferedWriter = new BufferedWriter(new FileWriter(file));       
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
     * Writes the given string to the output file.
     * @param toWrite The string to write to the file.
     * @throws java.io.IOException
     */
    public void write(String toWrite) 
    throws IOException {      
        bufferedWriter.write(toWrite);
        bufferedWriter.newLine();
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
