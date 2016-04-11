/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.targets;

import htsjdk.samtools.util.Interval;

/**
 * Class to encapsulate what it means to be a genomic target. 
 * 
 * @author evanmauceli
 */
public class Target {
   
    private final String type;
    private final String name;
    private final boolean fullLengthFlag;
    
    private Interval interval;
    
    /**
     * Constructor.
     * @param targetType Target descriptor.  Valid types are "GEN" (gene), 
     * "VAR" (variant) and "REG" (region).
     * @param targetName The name for this target. Might be a gene name,
     * could be an rs ID, etc.
     * @param targetFullLengthFlag True if the target is to be full length. If 
     * True for gene targets, this sets the interval from transcript start 
     * to transcript end.  Region/variant targets are always full length.
     * 
     */
    public Target(
        String targetType, 
        String targetName, 
        boolean targetFullLengthFlag) {
        type=targetType;
        name=targetName;
        fullLengthFlag=targetFullLengthFlag;
    }
    
    
 
    
}
