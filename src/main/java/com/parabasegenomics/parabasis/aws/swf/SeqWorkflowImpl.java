/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.aws.swf;

import com.amazonaws.services.simpleworkflow.flow.core.Promise;

public class SeqWorkflowImpl implements SeqWorkflow {
    
    private final static String gapReportSuffix = ".gap.report.txt";
    private final static String gapGeneReportSuffix = ".gap.gene.report.txt";
    private final static String resourceFileSuffix = ".resources.json";
    
    private final SeqWorkflowActivitiesClient ops
        = new SeqWorkflowActivitiesClientImpl();
    
    @Override
    public void doWork(
        String bucket,
        String keyPrefix,
        String assay,
        String sample) {

        String bamKey =
            keyPrefix
            + "/" 
            + sample 
            + ".bam";

        String vcfKey 
            = keyPrefix
            + "/"
            + sample
            + ".vcf.gz";

        ops.setAssay(assay);
        ops.setThreshold("20");
        
        // push vcf to omicia
        Promise<String> localFile 
            = ops.downloadToLocalEC2(bucket,vcfKey);
        
        Promise<String> convertedFile
            = ops.convertCoordinates(localFile);
        
        Promise<String> pushedToOmicia
            = ops.pushToOmicia(convertedFile);

        // gaps report
        Promise<String> localBamFile
            = ops.downloadToLocalEC2(bucket, bamKey);

        Promise<String> resourceFile
            = ops.createResourceFile(localBamFile);
        
        //Promise<String> reportFileBase 
        //    = ops.runGapsReport(resourceFile);
        
        
        
        /**
        String gapFile = reportFileBase + gapReportSuffix;
        String gapFileKey = keyPrefix + "/" + sample + gapReportSuffix;
        ops.pushToS3(gapFile, bucket, gapFileKey);

        String gapGeneFile = reportFileBase + gapGeneReportSuffix;
        String gapGeneFileKey = keyPrefix + "/" + sample + gapGeneReportSuffix;
        ops.pushToS3(gapGeneFile, bucket, gapGeneFileKey);
        
        String gapResourceFile = reportFileBase + resourceFileSuffix;
        String resourceFileKey = keyPrefix + "/" + sample + resourceFileSuffix;
        ops.pushToS3(gapResourceFile, bucket, resourceFileKey);
        */
        
    }
    
}
