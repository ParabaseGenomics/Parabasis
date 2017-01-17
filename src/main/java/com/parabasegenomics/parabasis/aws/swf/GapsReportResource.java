/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.aws.swf;

/**
 * The gaps reporting code (ReportOnGaps.jar) used in the production pipeline 
 * produces 3 output files.  This class coordinates the naming of those files.
 * @author evanmauceli
 */
public class GapsReportResource {
    
    private final String gapTargetReportSuffix = ".gap.report.txt";
    private final String gapGeneReportSuffix = ".gap.gene.report.txt";
    private final String resourceFileSuffix = ".resources.json";
    
    S3NameResource s3NameParser;
    
    public GapsReportResource(S3NameResource bamResource) {
        s3NameParser = bamResource;
    }
    
    /**
     *
     * @return  Returns the name (not the path) of the target gap report.
     */
    public String getTargetReportFilename() {
        return (s3NameParser.getSampleId() + gapTargetReportSuffix);
    }
    
    /**
     * 
     * @return Returns the name (not the path) of the gene summary gap report.
     */
    public String getGeneReportFilename() {
        return (s3NameParser.getSampleId() + gapGeneReportSuffix);
    }
    
    /**
     * 
     * @return Returns the name not the path) of the resource file used to 
     * produce the gene and target gap report files.
     */
    public String getResourceFilename() {
        return (s3NameParser.getSampleId() + resourceFileSuffix);
    }
    
}
