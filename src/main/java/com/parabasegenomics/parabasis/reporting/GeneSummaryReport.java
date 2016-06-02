/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.reporting;

import static com.parabasegenomics.parabasis.decorators.AnnotationKeys.CAPTURE_KEY;
import static com.parabasegenomics.parabasis.decorators.AnnotationKeys.COVERAGE_KEY;
import static com.parabasegenomics.parabasis.decorators.AnnotationKeys.GC_KEY;
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
public class GeneSummaryReport extends Report {
    
    private final static String HEADER
        = "Gene\t%capture\t%coverage\t%gc\n";
     
    private final DecimalFormat decimalFormat;
    
    private AnnotationSummary annotationSummary;
    List<String> requiredKeys;
    List<String> orderedKeys;
    
    /**
     * Constructor in which an AnnotationSummary object is created and populated
     * with the appropriate decorators.
     * 
     * TargetReport requires the following decorator:
     *  GeneModelDecorator
     * 
     * @param file The report will be written to this file.
     * @throws IOException 
     */
    public GeneSummaryReport(File file) 
    throws IOException {
        super(file);  
        
        requiredKeys = new ArrayList<>();
        
        this.openForWriting();
        
        bufferedWriter.write(HEADER);     
        //requiredKeys.add(GC_KEY);
        annotationSummary = null;
        
        orderedKeys = new ArrayList<>();
        orderedKeys.add(CAPTURE_KEY);
        orderedKeys.add(COVERAGE_KEY);
        orderedKeys.add(GC_KEY);
        
        decimalFormat = new DecimalFormat(percentPattern);
        
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
     * @param intervals Intervals needing flair.
     * @param name The name of the gene being reported on.  Intervals
     * for a gene are best created by using 
     * the GeneModelCollection::createTargets method.
     * @throws IOException 
     */
    public void reportOn(List<Interval> intervals, String name) 
    throws IOException {     
        if (annotationSummary == null) {
            throw new IOException("AnnotationSummary is null.");
        }
        
        StringBuilder reportLine = new StringBuilder();
        reportLine.append(name);
     
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
            reportLine.append(TAB);
            reportLine.append(decimalFormat.format(pct));
        }

        reportLine.append(NEWLINE);
        bufferedWriter.write(reportLine.toString());
    }

    
    
}
