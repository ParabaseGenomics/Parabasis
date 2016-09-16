/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.aws.swf.activities;

import javafx.util.Pair;

/**
 *
 * @author evanmauceli
 */
public interface DownloadFromS3ToEC2 {
    
    // returns local path to vcf file
    public String downloadToLocalEC2(Pair<String,String> location);
    
}
