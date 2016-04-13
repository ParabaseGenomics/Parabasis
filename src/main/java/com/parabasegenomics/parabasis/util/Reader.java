/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.util;

import htsjdk.samtools.util.Interval;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Reads from various file types.
 * @author evanmauceli
 */
public class Reader {
    
    private static final String TAB = "\t";
    
    private  BufferedReader reader;   
    private  File fileToRead;
    
    public Reader() {
        
    }
    
    public List<Interval> readBEDFile(String file) 
    throws FileNotFoundException, IOException {
        List<Interval> entries = new ArrayList<>();
        fileToRead = new File(file);
        reader = new BufferedReader(new FileReader(fileToRead));    
        while (reader.ready()) {
           String line = reader.readLine();
           String [] tokens = line.split(TAB);
           if (tokens[0].equals("track")) {
               continue;
           }
           String name = tokens[0];
           int start = Integer.parseInt(tokens[1])+1;
           int end = Integer.parseInt(tokens[2]);
           entries.add(new Interval(name,start,end));
        }
       return entries;        
    }
    
    /**
     * Reads a file into an ArrayList.
     * 
     * @param file File to read. One string per line.
     * @return Returns an ArrayList of file entries.
     * @throws java.io.FileNotFoundException
     */
    public List<String> readArrayList(String file) 
    throws FileNotFoundException, IOException {
        
        List<String> entries = new ArrayList<>();
        fileToRead = new File(file);
        reader = new BufferedReader(new FileReader(fileToRead));    
        while (reader.ready()) {
            entries.add(reader.readLine());
        }
        
        return entries;
    }
    
    /**
     * Reads a file into a HashSet.
     * 
     * @param file File to read. One string per line.
     * @return Returns a HashSet of file entries.
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public Set<String> readHashSet(String file) 
    throws FileNotFoundException, IOException {
        Set<String> entries = new HashSet<>();
        fileToRead = new File(file);
        reader = new BufferedReader(new FileReader(fileToRead));  
        while (reader.ready()) {
            entries.add(reader.readLine());
        }
        return entries;
    }
    
}
