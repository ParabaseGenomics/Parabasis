/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.aws.swf.activities;

import java.io.IOException;
import javafx.util.Pair;

/**
 *
 * @author evanmauceli
 */
public interface IsValidS3Location {
    
    public Pair<String,String> isValidS3Location(Pair<String,String> location)
    throws IOException;
    
}
