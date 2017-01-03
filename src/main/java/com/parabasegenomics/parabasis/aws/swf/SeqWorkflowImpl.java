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
    public void doWork(String file) {
        
        String bucket = file.substring(0,file.indexOf("/NBDx"));
        String vcfKey = file.substring(file.indexOf("NBDx"),file.length());
        
        String keyPath =  vcfKey.substring(0, vcfKey.indexOf(".vcf.gz"));
        String targetReportSuffix = ".target.report.bed";
        String geneReportSuffix = ".gene.report.txt";
        String resourceFileSuffix = ".resources.json";
        
        String bamKey = keyPath + ".bam";
        String targetReportKey = keyPath + targetReportSuffix;
        String geneReportKey = keyPath + geneReportSuffix;
        String resourceFileKey = keyPath + resourceFileSuffix;
        
        Promise<String> localFile 
            = ops.downloadToLocalEC2(bucket,vcfKey);
        
        Promise<String> convertedFile
            = ops.convertCoordinates(localFile);
        
        ops.pushToOmicia(convertedFile);
        
        Promise<String> localBamFile
            = ops.downloadToLocalEC2(bucket, bamKey);
        
        Promise<String> reportFileBase 
            = ops.runGapsReport(localBamFile);
        
        String localTargetReport 
            = reportFileBase + targetReportSuffix;
        String localGeneReport
            = reportFileBase + geneReportSuffix;
        String localResourceFile 
            = reportFileBase + resourceFileSuffix;
 
        ops.pushToS3(localTargetReport, bucket, targetReportKey);
        ops.pushToS3(localGeneReport, bucket, geneReportKey);
        ops.pushToS3(localResourceFile, bucket, resourceFileKey);
    }
    
}
