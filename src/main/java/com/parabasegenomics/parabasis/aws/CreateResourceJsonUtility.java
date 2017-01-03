/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.aws;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;

/**
 * Create a json resources file from the provided list. 
 * 
 * Current implementation only provides a subset of options.
 * 
 * @author evanmauceli
 */
public class CreateResourceJsonUtility {

    public void createResourceJson(
        String file,
        String bamFile,
        String targetFile,
        String assay,
        String genelist,
        String reference,
        String refseq,
        String gencode,
        String threshold) 
    throws FileNotFoundException {
        
        JsonObjectBuilder resource = Json.createObjectBuilder();
        resource.add("BAM", Json.createArrayBuilder().add(bamFile));
        resource.add("TARGETS",targetFile);
        resource.add("ASSAY",assay);
        resource.add("GENELIST",genelist);
        resource.add("REFSEQ",refseq);
        resource.add("GENCODE",gencode);
        resource.add("THRESHOLD",threshold);
        resource.add("OUTPUT", file);
        
        File outputFile = new File(file);
        OutputStream outputStream = new FileOutputStream(outputFile);
        JsonWriter writer = Json.createWriter(outputStream);
        writer.writeObject(resource.build());
    }
    
    
}
