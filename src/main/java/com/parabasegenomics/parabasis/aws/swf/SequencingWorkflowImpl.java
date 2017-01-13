/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.aws.swf;

import com.amazonaws.services.simpleworkflow.flow.core.Promise;

public class SequencingWorkflowImpl implements SequencingWorkflow {
    
    private final static String gapReportSuffix = ".gap.report.txt";
    private final static String gapGeneReportSuffix = ".gap.gene.report.txt";
    private final static String resourceFileSuffix = ".resources.json";
    
    private final SequencingWorkflowActivitiesClient client
        = new SequencingWorkflowActivitiesClientImpl();
    
    @Override
    public void processSample(
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

        client.setAssay(assay);
        client.setThreshold("20");
        
        // push vcf to omicia
        Promise<String> localFile 
            = client.downloadToLocalEC2(bucket,vcfKey);
        
        Promise<String> convertedFile
            = client.convertCoordinates(localFile);
        
        Promise<String> pushedToOmicia
            = client.pushToOmicia(convertedFile);

        // gaps report
        Promise<String> localBamFile
            = client.downloadToLocalEC2(bucket, bamKey);

        Promise<String> resourceFile
            = client.createResourceFile(localBamFile);
        
        //Promise<String> reportFileBase 
        //    = ops.runGapsReport(resourceFile);
        
        
        
        /**
        String gapFile = reportFileBase + gapReportSuffix;
        String gapFileKey = keyPrefix + "/" + sample + gapReportSuffix;
        ops.pushToS3(gapFile, bucket, gapFileKey);

        String gapGeneFile = reportFileBase + gapGdeneReportSuffix;
        String gapGeneFileKey = keyPrefix + "/" + sample + gapGeneReportSuffix;
        ops.pushToS3(gapGeneFile, bucket, gapGeneFileKey);
        
       
        String resourceFileKey = keyPrefix + "/" + sample + resourceFileSuffix;
        client.pushToS3(resourceFile, bucket, resourceFileKey);
       */
        
    }
    
    
}
