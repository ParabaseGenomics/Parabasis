/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.decorators;

import static com.parabasegenomics.parabasis.decorators.AnnotationKeys.GENE_KEY;
import com.parabasegenomics.parabasis.target.AnnotatedInterval; 
import com.parabasegenomics.parabasis.gene.GeneModel;
import com.parabasegenomics.parabasis.gene.GeneModelCollection;
import htsjdk.samtools.util.Interval;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author evanmauceli
 */
public class GeneModelDecorator implements IntervalDecorator {
    
    private static final String KEY = GENE_KEY;
    
    private final GeneModelCollection geneModelCollection;
    private final Set<String> targetGenelist;
    private final List<GeneModel> genes;
    

    public GeneModelDecorator(
        GeneModelCollection geneModel,
        Set<String> targets) 
    throws IOException {    
            geneModelCollection = geneModel;
            targetGenelist = targets;
            
            if (targetGenelist != null) {
                genes = geneModelCollection.selectGeneModels(targetGenelist);
            } else {
                genes = geneModelCollection.getGeneModels();
            }
                
            for (GeneModel gene : genes) {
                if (!gene.hasCollapsedTranscript()) {
                    gene.Collapse();
                } 
            }     
    }
    
    @Override
    public void annotate(AnnotatedInterval annotatedInterval) {
        try {
            String overlap 
                = getGeneModelOverlap(annotatedInterval.getInterval());
            if (!overlap.isEmpty()) {
                annotatedInterval
                    .addAnnotation(
                        KEY,
                        overlap);
            }
        } catch (IOException ex) {
            Logger.getLogger(GeneModelDecorator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Returns a string of the gene-transcript-exon# for each overlap this interval
     * has with the gene models.  If no overlap, returns an empty string.
     * @param interval
     * @return
     * @throws IOException 
     */
    private String getGeneModelOverlap(Interval interval) 
    throws IOException {
        String overlaps = "";
        for (GeneModel gene : genes) {   
            String overlap = gene.overlap(interval);
            if (overlap != null) {
                overlaps += (overlap+";");
            }
        } 
        return overlaps;
    }

}
