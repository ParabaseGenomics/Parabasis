/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.reporting;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * A thin wrapper around the BufferedWriter class.
 * @author evanmauceli
 */
public abstract class Report {
    
    static final String TAB = "\t";
    static final String NEWLINE = "\n";
    
    protected BufferedWriter bufferedWriter;
    protected BufferedReader bufferedReader;
    
    private final File reportFile;
    
    public Report(File file) 
    throws IOException {      
        reportFile=file;
        bufferedReader = null;
        bufferedWriter = null;
    }
    
    /**
     * Opens the file for writing by creating a new BufferedWriter.  
     * @throws IOException 
     */
    public void openForWriting() 
    throws IOException {
        bufferedWriter = new BufferedWriter(new FileWriter(reportFile)); 
    }
    
    /**
     * Opens the file for reading by creating a new BufferedReader.
     * @throws FileNotFoundException 
     */
    public void openForReading() 
    throws FileNotFoundException {
        bufferedReader = new BufferedReader(new FileReader(reportFile));
    }
    
    /**
     * Wrapper around the BufferedWriter::write() method.
     * @param toWrite String to write to a file.
     * @throws java.io.IOException
     */
    public void write(String toWrite) 
    throws IOException {
        bufferedWriter.write(toWrite);
    }
        
    /**
     * Wrapper around the BufferedReader::readLine() method.
     * @return Returns the line of the file being read as a String. 
     * @throws IOException 
     */
    public String readLine() 
    throws IOException {
        if (!bufferedReader.ready()) {
            throw new IOException("Not ready to read file " + reportFile.getAbsolutePath());
        }
        return (bufferedReader.readLine());     
    }
    
    /**
     * Returns the file being written to.
     * @return 
     */
    public File getFile() {
        return reportFile;
    }
    
    /**
     * Returns the writer for this report.
     * @return 
     */
    public BufferedWriter getWriter() {
        return bufferedWriter;
    }
    
    /**
     * Returns the reader for this report.
     * @return 
     */
    public BufferedReader getReader() {
        return bufferedReader;
    }
    
    /**
     * Closes the writer.
     * @throws java.io.IOException
     */
    public void close() 
    throws IOException {
        if (bufferedWriter != null) {
            bufferedWriter.close();
        }
        if (bufferedReader != null) {
            bufferedReader.close();
        }
    }

    
}
