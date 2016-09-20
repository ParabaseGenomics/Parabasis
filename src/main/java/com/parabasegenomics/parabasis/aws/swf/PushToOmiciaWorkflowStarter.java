/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.aws.swf;

import javafx.util.Pair;

/**
 *
 * @author evanmauceli
 */
public class PushToOmiciaWorkflowStarter {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        PushToOmiciaWorkflow pushToOmiciaWorkflow
            = new PushToOmiciaWorkflowImpl();
        pushToOmiciaWorkflow.push(new Pair(args[0],args[1]));
    }
    
}
