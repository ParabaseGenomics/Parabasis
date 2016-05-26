/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.coverage;

import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.util.SamLocusIterator;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;
import htsjdk.samtools.util.Interval;
import htsjdk.samtools.util.IntervalList;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author evanmauceli
 */
public class BamCoverage {

    File bamFile;
    SAMFileHeader samFileHeader;
    
    final SamReaderFactory samReaderFactory 
        = SamReaderFactory.makeDefault()
            .validationStringency(ValidationStringency.SILENT);
    
    final SamReader samReader;

  
    public BamCoverage(String bamFilePath) {
        bamFile = new File(bamFilePath);
        samFileHeader = samReaderFactory.open(bamFile).getFileHeader();
       
        samReader = samReaderFactory.open(bamFile);
        
    }
    
    /**
     * Calculate total read depth for a given interval.  Probably not very
     * efficient, but is conceptually clean in the expected workflow.
     * @param interval
     * @return Returns the count of reads in this interval.  Does not return 
     * the average coverage of the interval.
     */
    public Double getCoverage(Interval interval) {       
        Double coverage = 0.0;

        IntervalList intervalList = new IntervalList(samFileHeader);
        intervalList.add(interval);
        
        SamLocusIterator locusIterator 
            = new SamLocusIterator(samReader,intervalList);
         
        while (locusIterator.hasNext()) {
            SamLocusIterator.LocusInfo locusInfo 
                = locusIterator.next();

            List<SamLocusIterator.RecordAndOffset> list
                = locusInfo.getRecordAndPositions();
            coverage += list.size();
            
        }
        return coverage;
    }
    
    /**
     * Close the held SamReader.
     * @throws IOException 
     */
    public void closeBamFile() 
    throws IOException {
        this.samReader.close();
    }
    
}
