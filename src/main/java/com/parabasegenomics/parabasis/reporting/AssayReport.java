/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.reporting;

import static com.parabasegenomics.parabasis.decorators.AnnotationKeys.CAPTURE_KEY;
import static com.parabasegenomics.parabasis.decorators.AnnotationKeys.COVERAGE_KEY;
import static com.parabasegenomics.parabasis.decorators.AnnotationKeys.GAPS_KEY;
import static com.parabasegenomics.parabasis.decorators.FormatPatterns.percentPattern;
import static com.parabasegenomics.parabasis.reporting.Report.TAB;
import com.parabasegenomics.parabasis.target.AnnotatedInterval;
import com.parabasegenomics.parabasis.target.AnnotationSummary;
import htsjdk.samtools.util.Interval;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author evanmauceli
 */
public class AssayReport extends Report {
     private final static String HEADER 
        = "Assay\t%capture\tcoverage\t";
    private final DecimalFormat decimalFormat;
    
    private AnnotationSummary annotationSummary;
    List<String> requiredKeys;
    List<String> orderedKeys;
    String assayName;
    
    /**
     * Constructor in which an AnnotationSummary object is created and populated
     * with the appropriate decorators.
     * 
     * TargetReport requires the following decorator:
     *  GeneModelDecorator
     * 
     * @param file The report will be written to this file.
     * @param name the name of the assay.
     * @throws IOException 
     */
    public AssayReport(File file,String name) 
    throws IOException {
        super(file);  
        
        requiredKeys = new ArrayList<>();
        
        this.openForWriting();
          
        //equiredKeys.add(CAPTURE_KEY);
        annotationSummary = null;
        
        orderedKeys = new ArrayList<>();
        orderedKeys.add(CAPTURE_KEY);
        orderedKeys.add(COVERAGE_KEY);
        //orderedKeys.add(GAPS_KEY);
        
        decimalFormat = new DecimalFormat(percentPattern);
        assayName=name;
        
    }
    
    /**
     * Sets the annotation summary.  Checks to see if the summary fulfills
     * the minimum decorator requirements. If not, throws a IOException.
     * @param summary 
     * @throws java.io.IOException 
     */
    public void setAnnotationSummary(AnnotationSummary summary) 
    throws IOException {
        annotationSummary = summary;
        for (String key : requiredKeys) {
            if (!annotationSummary.hasDecorator(key)) {
                throw new IOException(
                    "The given AnnotationSummary is not sufficient, missing " 
                        + key);
            }      
        }
    }

    /**
     * Add flair to each of the given intervals and write each annotated interval
     * to the report.  Does not close the report file when done.
     * @param intervals
     * @throws IOException 
     */
    public void reportOn(List<Interval> intervals) 
    throws IOException {     
        if (annotationSummary == null) {
            throw new IOException("AnnotationSummary is null.");
        }
        
        // we may be using more than one threshold to calculate the gaps 
        // percentage, fix the header here
        String header = HEADER;
        List<String> decoratorKeylist = annotationSummary.heldKeys();
        for (String key : decoratorKeylist) {
            if (key.contains(GAPS_KEY)) {
                orderedKeys.add(key);
                header += (key + TAB);
            }
        }
        header+="\n";
        bufferedWriter.write(header);  
        
        StringBuilder reportLine = new StringBuilder();
        reportLine.append(assayName);
        
        List<AnnotatedInterval> annotatedIntervals
            = annotationSummary.annotate(intervals);

        for (String key : orderedKeys) {
            int annotatedLength=0;
            int length=0;
            
            if (!annotationSummary.hasDecorator(key)) {
                reportLine.append(TAB);
                reportLine.append("NA");
                continue;
            }
            for (AnnotatedInterval annotatedInterval : annotatedIntervals) {
              
                annotatedLength 
                    += Integer.valueOf(annotatedInterval.getAnnotation(key));
                length += annotatedInterval.length();
            }
            
            double pct 
                = 100.0*(double) annotatedLength/(double) length;
            if (key.equals(COVERAGE_KEY)) {
                pct = (double) annotatedLength/(double) length;
            }
            reportLine.append(TAB);
            reportLine.append(decimalFormat.format(pct));
        }

        reportLine.append(NEWLINE);
        bufferedWriter.write(reportLine.toString());
    }
  
}
