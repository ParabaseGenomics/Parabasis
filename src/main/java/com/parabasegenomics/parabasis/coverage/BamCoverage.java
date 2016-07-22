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
import java.util.ArrayList;
import java.util.List;

/**
 * The BamCoverage class is a wrapper around the SamReader class to provide
 * coverage over intervals as well as low coverage regions within intervals.
 * @author evanmauceli
 */
public class BamCoverage {

    private final File bamFile;
    private final SAMFileHeader samFileHeader;
    
    private final SamReaderFactory samReaderFactory 
        = SamReaderFactory.makeDefault()
            .validationStringency(ValidationStringency.SILENT);
    
    private final SamReader samReader;
    
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
        Interval htsjdkInterval;
            htsjdkInterval = new Interval(
                interval.getContig(),
                interval.getStart()+1,
                interval.getEnd());
        intervalList.add(htsjdkInterval);
        
        //intervalList.add(interval);
        SamLocusIterator locusIterator 
            = new SamLocusIterator(samReader,intervalList);
             
        while (locusIterator.hasNext()) {
            SamLocusIterator.LocusInfo locusInfo 
                = locusIterator.next();

            List<SamLocusIterator.RecordAndOffset> list
                = locusInfo.getRecordAndPositions();

            coverage += list.size();
            
        }
        locusIterator.close();
        return coverage;
    }
    
    /**
     * Returns a list of sub-intervals of the provided interval with coverage 
     * less than the provided threshold.
     * @param interval
     * @param threshold
     * @return 
     */
    public List<Interval> getLowCoverage(Interval interval, Integer threshold) {
        List<Interval> lowCoverageIntervals = new ArrayList<>();
        
        IntervalList intervalList = new IntervalList(samFileHeader);
        Interval htsjdkInterval 
            = new Interval(
                interval.getContig(),
                interval.getStart()+1,
                interval.getEnd());       
        intervalList.add(htsjdkInterval);
        
        SamLocusIterator locusIterator 
            = new SamLocusIterator(samReader,intervalList);
             
        String chr = htsjdkInterval.getContig();
        int start = 0;
        int end = 0;
        boolean inGap = false;
        
        while (locusIterator.hasNext()) {
            SamLocusIterator.LocusInfo locusInfo 
                = locusIterator.next();

            List<SamLocusIterator.RecordAndOffset> list
                = locusInfo.getRecordAndPositions();

            if (list.size()<threshold) {
                //add to low coverage intervals
                if (!inGap) {
                    start = locusInfo.getPosition();
                    end = start;
                    inGap=true;
                } else {
                    ++end;
                }
            } else {
                if (inGap) {
                    Interval gapInterval = new Interval(chr,start-1,end);
                    lowCoverageIntervals.add(gapInterval);
                    start=0;
                    end=0;
                }
                inGap=false;
            }
            
        }
        if (inGap) {
            Interval gapInterval = new Interval(chr,start-1,end);           
            lowCoverageIntervals.add(gapInterval);  
        }
        locusIterator.close();
        
        return lowCoverageIntervals;
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
