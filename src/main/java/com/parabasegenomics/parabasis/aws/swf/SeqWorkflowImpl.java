/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.aws.swf;

import com.amazonaws.services.simpleworkflow.flow.core.Promise;

public class SeqWorkflowImpl implements SeqWorkflow {
    
    private final SeqWorkflowActivitiesClient ops
        = new SeqWorkflowActivitiesClientImpl();
    
    @Override
    public void doWork(
        String bucket,
        String keyPrefix,
        String assay,
        String sample) {
        

        String bamKey
            = bucket
            + "/" 
            + keyPrefix
            + "/" 
            + sample 
            + ".bam";

        String vcfKey 
            = bucket
            + "/" 
            + keyPrefix
            + "/"
            + sample
            + "vcf.gz";
         
        
        String gapReportSuffix = ".gap.report.txt";
        String gapGeneReportSuffix = "gap.gene.report.txt";
        String resourceFileSuffix = ".resources.json";
        
        String targetReportKey = keyPrefix + gapReportSuffix;
        String geneReportKey = keyPrefix + gapGeneReportSuffix;
        String resourceFileKey = keyPrefix + resourceFileSuffix;
        
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
        
        Promise<String> reportFileBase 
            = ops.runGapsReport(resourceFile);
        
        String localTargetReport 
            = reportFileBase + gapReportSuffix;
        String localGeneReport
            = reportFileBase + gapGeneReportSuffix;
        String localResourceFile 
            = reportFileBase + resourceFileSuffix;
 
        ops.pushToS3(localTargetReport, bucket, targetReportKey);
        ops.pushToS3(localGeneReport, bucket, geneReportKey);
        ops.pushToS3(localResourceFile, bucket, resourceFileKey);
        
   
    }
    
}
