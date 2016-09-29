/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.reporting;

import com.parabasegenomics.parabasis.coverage.IntervalCoverage;
import static com.parabasegenomics.parabasis.decorators.AnnotationKeys.COVERAGE_KEY;
import static com.parabasegenomics.parabasis.decorators.AnnotationKeys.GAPS_KEY;
import static com.parabasegenomics.parabasis.decorators.AnnotationKeys.GENE_KEY;
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
public class GapsTableReport extends Report {

    private final static String HEADER 
        = "Chr\tStart\tEnd\tCoverage\t%Gaps\tGene\tLowCoverageRegions";
    
    private final DecimalFormat decimalFormat; 
    private AnnotationSummary annotationSummary;
    List<String> requiredKeys;
    List<String> orderedKeys;
    String assayName;
    Double lowCoverageThreshold;
    
    public GapsTableReport(File file,Double threshold) 
    throws IOException {
        super(file);
        
        this.openForWriting();
        
        orderedKeys = new ArrayList<>();
        
        lowCoverageThreshold=threshold;
        decimalFormat = new DecimalFormat(percentPattern);
        orderedKeys.add(COVERAGE_KEY);
    }
    
    public void setAnnotationSummary(AnnotationSummary summary) {
        annotationSummary=summary;
    }
    
    public void reportOn(List<IntervalCoverage> intervals) 
    throws IOException {     
        if (annotationSummary == null) {
            throw new IOException("AnnotationSummary is null.");
        }
        
        String header = HEADER;
        List<String> decoratorKeylist = annotationSummary.heldKeys();
        for (String key : decoratorKeylist) {
            if (key.contains(GAPS_KEY)) {
                orderedKeys.add(key);
                header += (key + TAB);
            }
        }
        header+=NEWLINE;
        bufferedWriter.write(header);  

        for (IntervalCoverage intervalCoverage : intervals) {          
            final Interval interval = intervalCoverage.getInterval();
            
            if (intervalCoverage
                .getLowCoverageCount(lowCoverageThreshold)==0) {
                continue;
            }
            
            StringBuilder reportLine = new StringBuilder();
            
            AnnotatedInterval annotatedInterval
                = annotationSummary.annotateOne(interval);
            
            Interval genomicInterval = annotatedInterval.getInterval();
            reportLine.append(genomicInterval.getContig());
            reportLine.append(TAB);
            reportLine.append(genomicInterval.getStart());
            reportLine.append(TAB);
            reportLine.append(genomicInterval.getEnd());
            
            for (String key : orderedKeys) {            
                if (!annotationSummary.hasDecorator(key)) {
                    reportLine.append(TAB);
                    reportLine.append("NA");
                    continue;
                }

                int length = annotatedInterval.length();
                int annotatedLength 
                    = Integer.valueOf(annotatedInterval.getAnnotation(key));
                double pct 
                    = 100.0*(double) annotatedLength/(double) length;
                if (key.equals(COVERAGE_KEY)) {
                    pct = (double) annotatedLength/(double) length;
                }
                reportLine.append(TAB);
                reportLine.append(decimalFormat.format(pct));
            }
            
            reportLine.append(TAB);
            reportLine.append(annotatedInterval.getAnnotation(GENE_KEY));
            reportLine.append(TAB);
            List<Interval> lowCoverageIntervals
                = intervalCoverage.getLowCoverageIntervals(lowCoverageThreshold);
            
            for (Interval lowCoverageInterval : lowCoverageIntervals) {
                reportLine.append(
                    lowCoverageInterval.getContig())
                    .append(":")
                    .append(lowCoverageInterval.getStart())
                    .append("-")
                    .append(lowCoverageInterval.getEnd());
                if (lowCoverageInterval 
                    != lowCoverageIntervals.get(lowCoverageIntervals.size()-1)) {
                    reportLine.append(";");
                }
            }
            reportLine.append(NEWLINE);
            bufferedWriter.write(reportLine.toString());
        }
    }
}
   
