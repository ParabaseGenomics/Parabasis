/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.coverage;

import htsjdk.samtools.util.SamLocusIterator;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;
import htsjdk.samtools.util.IntervalList;
import java.io.File;
import java.util.List;

/**
 *
 * @author evanmauceli
 */
public class BamReader {
    
    String path="";
    File bamFile = new File(path);
    
    final SamReaderFactory samReaderFactory 
        = SamReaderFactory.makeDefault()
            .validationStringency(ValidationStringency.SILENT);
    
    final SamReader samReader;

  
    public BamReader() {
        this.samReader = samReaderFactory.open(bamFile);
        
        IntervalList intervalList = null;
        SamLocusIterator locusIterator 
            = new SamLocusIterator(samReader,intervalList);
        
   
        while (locusIterator.hasNext()) {
            SamLocusIterator.LocusInfo locusInfo 
                = locusIterator.next();
            
            int pos = locusInfo.getPosition();
            List<SamLocusIterator.RecordAndOffset> list
                = locusInfo.getRecordAndPositions();
            int coverage = list.size();
            
        }
    }
}
