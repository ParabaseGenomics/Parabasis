/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.vcf;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author evanmauceli
 */
public class InfoFieldParser {
    
    private static final String EQUALS = "=";
    private static final String SEMICOLON = ";";
    
    private final Map<String,String> infoMap;
    
    public InfoFieldParser() {
        infoMap = new HashMap<>();
    }
    
    /**
     * Parse the INFO field into key-values pairs.
     * @param infoField The INFO field from a vcf file.
     */
    public void parse(String infoField) {
        String [] tokens = infoField.split(SEMICOLON);
        for (String token : tokens) {
            if (token.contains(EQUALS)) {
                String [] values = token.split(EQUALS);
                infoMap.put(values[0], values[1]);
            }
        }
        
    }
    
    /**
     * Find the value for a specified key.
     * @param key An INFO field parameter.
     * @return Returns the value associated with the specified key, or null
     * if not found.
     */
    public String find(String key) {
        if (infoMap.containsKey(key)) {
            return infoMap.get(key);
        } else {
            return null;
        }
    }
    
    
}
