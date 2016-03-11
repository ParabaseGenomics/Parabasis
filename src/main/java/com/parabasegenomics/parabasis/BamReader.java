/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis;

import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import java.io.File;

/**
 *
 * @author evanmauceli
 */
public class BamReader {
    
    String path="";
    File bamFile = new File(path);
    
    final SamReaderFactory samReaderFactory 
        = SamReaderFactory.makeDefault();
    
    final SamReader samReader 
        = samReaderFactory.open(bamFile);
    
}
