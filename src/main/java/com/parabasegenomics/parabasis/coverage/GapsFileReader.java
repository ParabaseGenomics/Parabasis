/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.coverage;

import htsjdk.samtools.util.Interval;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to read a gaps.csv file produced by the MiSeq and listing
 * those regions with sequencing coverage less than the specified
 * threshold (20x is customary).
 * 
 * A gaps.csv file example:
 * 
 * #Chromosome,GapStart,GapStop,RegionID,MeanGapCoverage,RegionInterval,GapInterval
 * chr1,6484838,6485319,NBDx_v1_1-10bp-chr1-6484838-6485319,3.1,chr1:6484838-6485319,chr1:6484838-6485319
 * 
 * @author evanmauceli
 */
public class GapsFileReader {
   
    private final String comma = ",";
    private final BufferedReader reader;
   
    
    public GapsFileReader(String file) 
    throws FileNotFoundException {
       reader = new BufferedReader(new FileReader(file));  
    }
    
    /**
     * Read method.
     * @return Returns a list of intervals, one for each line in the
     * gaps.csv file.
     * @throws IOException 
     */
    public List<Interval> readFile() 
    throws IOException {
        
        List<Interval> gapsList = new ArrayList<>();
        String line;
        // read the header
        if (reader.ready()) {
            line = reader.readLine();
            if (line.charAt(0) != '#') {
                throw new IOException("first line of gaps file is not header");
            }
        }
        
        while (reader.ready()) {
            line = reader.readLine();
            
            // don't attempt to convert empty lines
            if (line.isEmpty()) {
                continue;
            }        
        
            String [] tokens = line.split(comma);
            Interval gapInterval;
            gapInterval = new Interval(
                tokens[0],
                Integer.parseInt(tokens[1]),
                Integer.parseInt(tokens[2]));
            gapsList.add(gapInterval);
            
        }
        return gapsList;
    }   
}
