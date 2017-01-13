/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.omicia;


/**
 * Omicia handler.
 * 
 * 4409 for ParabaseValidation/NBDxV1.1
   35893 for ParabaseValidation/NBDxV2
   3923 for ParabaseProduction/NBDxV1.1
   35877 for ParabaseProduction/NBDxV2
 * @author evanmauceli
 */
public class OmiciaResource {
    
    public static final String V1 = "NBDxV1.1";
    public static final String V2 = "NBDxV2";
    
    public OmiciaResource() {
        
    }
   
    /**
     * Returns the validation id for the provided assay.
     * @param assay
     * @return 
     */
    public String getValidationId(String assay) {
        switch (assay) {
            case V1:
                return "4409";
            case V2:
                return "35893";
            default:
                throw new IllegalArgumentException("no validation ID for " + assay);
        }
    }
    
    
    /**
     * Returns the validation id for the provided assay.
     * @param assay
     * @return 
     */
    public String getProductionId(String assay) {
        switch (assay) {
            case V1:
                return "3923";
            case V2:
                return "35877";
            default:
                throw new IllegalArgumentException("no production ID for " + assay);
        }
    }
    
}
