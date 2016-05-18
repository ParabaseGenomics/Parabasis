/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.target;

import com.parabasegenomics.parabasis.decorators.IntervalDecorator;
import htsjdk.samtools.util.Interval;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author evanmauceli
 */
public class AnnotationSummary {

    private final List<IntervalDecorator> decorators;
    private final List<AnnotatedInterval> annotatedIntervals;
    
    /**
     * Constructor assigns the intervals to be annotated and summarized. 
     */
    public AnnotationSummary() {
        decorators = new ArrayList<>();
        annotatedIntervals = new ArrayList<>();
    }
    
    /**
     * 
     * @return Returns a list of decorator keys that have been added.
     */
    public List<String> heldKeys() {
        List<String> heldKeys = new ArrayList<>();
        for (IntervalDecorator decorator : decorators) {
            heldKeys.add(decorator.getKey());
        }
        return heldKeys;
    }
    /**
     * Check if the class has the correct decorator.
     * @param key
     * @return Returns true if the decorator specified by the given key if found,
     * false otherwise.
     */
    public boolean hasDecorator(String key) {
        for (IntervalDecorator decorator : decorators) {
            if (decorator.getKey().equals(key)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     *   
     * @param key
     * @return Returns the decorator corresponding to the given key,
     * or null if a decorator is not found.
     */
    public IntervalDecorator getDecorator(String key) {
        if (!hasDecorator(key)) {
            return null;
        }
        for (IntervalDecorator decorator : decorators) {
            if (decorator.getKey().equals(key)) {
                return decorator;
            }
        }
        return null;
    }
    
    /**
     * Get ready to add some flair.
     * @param decorator 
     */
    public void addDecorator(IntervalDecorator decorator) {
        if (decorator!=null) {
            decorators.add(decorator);
        }
    }

    /**
     * Add some flair to the list of intervals with the given decorators and
     * show it off.
     * @param intervals
     * @return Returns the list of annotated intervals. Will return the original 
     * intervals if there are no decorators.
     */
    public List<AnnotatedInterval> annotate(List<Interval> intervals) {
        annotatedIntervals.clear();
        for (Interval interval : intervals) {
            AnnotatedInterval annotatedTarget 
                = new AnnotatedInterval(interval);
            for (IntervalDecorator decorator : decorators) {
                decorator.annotate(annotatedTarget);
            }
            annotatedIntervals.add(annotatedTarget);
        }   
        return annotatedIntervals;
    }
   
    /**
     * Add some flair to a single interval.  
     * @param interval
     * @return Returns the annotated interval with appropriate flair.
     * Will return the original interval if there are no decorators.
     */
    public AnnotatedInterval annotateOne(Interval interval) {
        AnnotatedInterval annotatedTarget = new AnnotatedInterval(interval);
        for (IntervalDecorator decorator : decorators) {
            decorator.annotate(annotatedTarget);
        }
        return annotatedTarget;
        
    }
}
