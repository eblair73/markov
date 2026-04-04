/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.bnEvaluation;

import org.openmarkov.bnEvaluation.measures.MeasureMatrix;
import org.openmarkov.bnEvaluation.measures.MeasureType;
import org.openmarkov.bnEvaluation.measures.MeasureValue;
import org.openmarkov.bnEvaluation.measures.MeasuresSet;
import org.openmarkov.core.exception.*;
import org.openmarkov.core.inference.tasks.Propagation;
import org.openmarkov.core.io.database.CaseDatabase;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEEvaluation;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEPropagation;
import org.openmarkov.learning.metric.Metric;
import org.openmarkov.learning.metric.aic.AICMetric;
import org.openmarkov.learning.metric.bayesian.BayesianMetric;
import org.openmarkov.learning.metric.bde.BDeMetric;
import org.openmarkov.learning.metric.entropy.EntropyMetric;
import org.openmarkov.learning.metric.k2.K2Metric;
import org.openmarkov.learning.metric.mdlm.MDLMetric;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;


/**
 * This class obtains a measure of the goodness of fit of a dataset to
 * a network
 *
 * @author evillar
 * @version 1.0 evillar
 */
public class NetEvaluator {
    private ProbNet probNet;
    private CaseDatabase caseDatabase = null;
    private MeasuresSet measuresSet;
    
    /**
     * A goodness-of-fit evaluator is created with a network, a dataset and a measuresSet
     */
    public NetEvaluator(ProbNet probNet, CaseDatabase caseDatabase, MeasuresSet measuresSet) {
        this.probNet = probNet;
        this.caseDatabase = caseDatabase;
        this.measuresSet = measuresSet;
    }
    
    /**
     * this method activates the evaluator:
     * obtains the data needed for the measurement of the goodness of fit
     *
     * @return measuresSet with the calculated measurements or null
     */
    public MeasuresSet runEvaluator() throws IncompatibleEvidenceException, ConstraintViolatedException {
        MeasureMatrix measureMatrix = measuresSet.getMeasureMatrix();
        if (measureMatrix != null) {
            double[][] probStates = getProbabilitiesVarClas(measureMatrix.getVarName());
            int[] realStates = realStates(measureMatrix.getVarName());
            int[] estimatedStates = estimatedStates(probStates, caseDatabase.getNumCases());
            int[][] matrix = calculateConfusionMatrix(realStates, estimatedStates, measureMatrix.getNumStates());
            measureMatrix.setMatrix(matrix, caseDatabase.getNumCases());
            measureMatrix.setIndicators();
            if (measureMatrix.getShowIndividualProb()) {
                measureMatrix.setIndividualProb(caseDatabase, probStates, estimatedStates);
            }
        }// loop in measures
        for (MeasureValue measure : measuresSet.getMeasures()) {
            switch (measure.getMeasureType()) {
                case CONFUSIONMATRIX -> {
                }
                case LOGLIKEHOOD -> measure.setValue(calculateLoglikehood(), caseDatabase.getNumCases());
                case BAYES, AIC, ENTROPY, BDE, K2, MDL ->
                        measure.setValue(calculateScore(measure.getMeasureType()), caseDatabase.getNumCases());
            }
        }
        return measuresSet;
        
    }
    
    /**
     * This method returns an array with the states of the classification variable
     *
     * @param varName name of classification variable
     *
     * @return int[] index of state of the classification variable
     */
    private int[] realStates(String varName) {
        for (Variable variable : caseDatabase.getVariables()) {
            if (variable.getName().equals(varName)) {
                return caseDatabase.getCases(variable);
            }
        }
        return null;
    }
    
    /**
     * this method returns an array with the most probable state of
     * the classification variable
     *
     * @param prob     [i][j] probability of state j in the i-th case
     * @param numCases number of cases
     *
     * @return int [i] index of the most probable state of i-th case
     */
    private int[] estimatedStates(double[][] prob, int numCases) {
        int[] estimatedStates = new int[numCases];
        int numStates = prob[0].length;
        for (int i = 0; i < numCases; i++) {
            int j_max = 0;
            double prob_max = prob[i][0];
            for (int j = 1; j < numStates; j++) {
                if (prob[i][j] > prob_max) {
                    j_max = j;
                    prob_max = prob[i][j];
                }
            }
            estimatedStates[i] = j_max;
        }
        return estimatedStates;
    }
    
    /**
     * This method calculates the sum of the logarithm of the likelihoods.
     *
     * @return Double or null
     *
     * @throws IncompatibleEvidenceException
     */
    private Double calculateLoglikehood()
            throws IncompatibleEvidenceException, ConstraintViolatedException {
        double[] prob = getProbabilitiesGeneralized();
        return IntStream.range(0, caseDatabase.getNumCases()).mapToDouble(i -> (Math.log(prob[i]))).sum();
    }
    
    /**
     * This method calculates the scores according to the metric
     *
     * @param type
     *
     * @return double
     */
    private double calculateScore(MeasureType type) {
        Metric metrica = null;
        switch (type) {
            case BAYES:
                metrica = new BayesianMetric(0.5);
                break;
            case AIC:
                metrica = new AICMetric();
                break;
            case ENTROPY:
                metrica = new EntropyMetric();
                break;
            case BDE:
                metrica = new BDeMetric(0.5);
                break;
            case K2:
                metrica = new K2Metric();
                break;
            case MDL:
                metrica = new MDLMetric();
                break;
        }
        metrica.init(probNet, caseDatabase);
        double medida = metrica.getScore();
        return medida;
    }
    
    /**
     * This method calculate the confusion matrix
     *
     * @param realStates      true states of the classification variable
     * @param estimatedStates estimated state for the classification variable,
     *                        given the other variables
     * @param numStates       number of states of classification variable
     */
    private int[][] calculateConfusionMatrix(int[] realStates, int[] estimatedStates, int numStates) {
        int[][] matrix = new int[numStates][numStates];
        for (int i = 0; i < numStates; i++) {
            for (int j = 0; j < numStates; j++) {
                matrix[i][j] = 0;
            }
        }
        for (int icase = 0; icase < realStates.length; icase++) {
            matrix[realStates[icase]][estimatedStates[icase]]++;
        }
        return matrix;
    }
    
    /**
     * This method calculates the probabilities of the states of
     * classification variable given the remaining variables.
     *
     * @return double [i][j] probability of j-states given i-case
     *
     * @throws IncompatibleEvidenceException
     */
    private double[][] getProbabilitiesVarClas(String varName)
            throws IncompatibleEvidenceException, ConstraintViolatedException {
        int[][] cases = caseDatabase.getCases();
        int numCases = caseDatabase.getNumCases();
        Variable varClas = null;
        // list of variables in database
        List<Variable> caseVariables = caseDatabase.getVariables();
        int numVariables = caseVariables.size();
        int numStatesVarClas = 0;
        // It is necessary to know the name of the variable in each case
        // to search in probNet
        String[] var_name = new String[numVariables];
        for (int i = 0; i < numVariables; i++) {
            var_name[i] = caseVariables.get(i).getName();
            if (var_name[i].equals(varName)) {
                varClas = caseVariables.get(i);
                numStatesVarClas = varClas.getNumStates();
            }
        }
        double[][] probStates = new double[numCases][numStatesVarClas];
        // loop in num_cases
        for (int i_case = 0; i_case < numCases; i_case++) {
            // For each case create a list of findings without varclas
            ArrayList<Finding> listFindings = new ArrayList<Finding>();
            for (int j = 0; j < numVariables; j++) {
                if (!var_name[j].equals(varName)) {
                    // search the j-th variable of cases in probNet; which name=var_name[j]
                    Variable variable = probNet.getVariable(var_name[j]);
                    // search the state case[i_case][j] in the probNet variable
                    State state = variable.getState(variable.getStateName(cases[i_case][j]));
                    Finding finding = new Finding(variable, state);
                    listFindings.add(finding);
                }
            }
            EvidenceCase evidencia = new EvidenceCase(listFindings);
            double[] prob_case_i = doPropagationVarClas(evidencia, varClas);
            if (numStatesVarClas >= 0)
                System.arraycopy(prob_case_i, 0, probStates[i_case], 0, numStatesVarClas);
        } //end loop en num_cases (i_case)
        return probStates;
    }
    
    /**
     * This method does the propagation of the evidence in the network.
     *
     * @param evidenceCase the evidence case with which the propagation must be done
     * @param varClas      classification variable
     *
     * @return prob        array with classification variable probabilities
     *
     * @throws IncompatibleEvidenceException
     */
    private double[] doPropagationVarClas(EvidenceCase evidenceCase, Variable varClas)
            throws IncompatibleEvidenceException, ConstraintViolatedException {
        ArrayList<Variable> listVarClass = new ArrayList<>();
        listVarClass.add(varClas);
        Propagation vePosteriorValues = new VEPropagation(probNet);
        vePosteriorValues.setVariablesOfInterest(listVarClass);
        vePosteriorValues.setPreResolutionEvidence(evidenceCase);
        TablePotential prob_varClas = vePosteriorValues.getPosteriorValues().get(varClas);
        return prob_varClas.getValues();
        
    }
    
    /**
     * This method calculates the probability of the evidence
     *
     * @param evidenceCase the evidence case with which the propagation must be done
     *
     * @return prob         TablePotential with the probabilities
     *
     * @throws IncompatibleEvidenceException
     */
    private TablePotential doEvaluation(EvidenceCase evidenceCase)
            throws IncompatibleEvidenceException, ConstraintViolatedException {
        VEEvaluation veEvaluation = new VEEvaluation(probNet);
        veEvaluation.setPreResolutionEvidence(evidenceCase);
        TablePotential tableprob = veEvaluation.getProbability();
        return tableprob;
        
    }
    
    
    /**
     * This method calculates the probability of each case, even
     * when not all net-variables are in the database set
     *
     * @return: double[] prob
     */
    private double[] getProbabilitiesGeneralized()
            throws IncompatibleEvidenceException, ConstraintViolatedException {
        int[][] cases = caseDatabase.getCases();
        int numCases = caseDatabase.getNumCases();
        
        // list of variables in database
        List<Variable> caseVariables = caseDatabase.getVariables();
        int numVariables = caseVariables.size();
        // It is necessary to know the name of the variable in each case
        // to search in probNet
        String[] var_name = new String[numVariables];
        for (int i = 0; i < numVariables; i++) {
            var_name[i] = caseVariables.get(i).getName();
        }
        // loop in num_cases
        double[] probCases = new double[numCases];
        for (int i_case = 0; i_case < numCases; i_case++) {
            // For each case create a list of findings without varclas
            List<Finding> listFindings = new ArrayList<Finding>();
            for (int j = 0; j < numVariables; j++) {
                // search the j-th variable of cases in probNet; which name=var_name[j]
                Variable variable = probNet.getVariable(var_name[j]);
                // search the state case[i_case][j] in the probNet variable
                State state = variable.getState(variable.getStateName(cases[i_case][j]));
                Finding finding = new Finding(variable, state);
                listFindings.add(finding);
            }
            EvidenceCase evidencia = new EvidenceCase(listFindings);
            TablePotential tablePotential = doEvaluation(evidencia);
            probCases[i_case] = tablePotential.getValues()[0];
        }
        //end loop en num_cases (i_case)
        return probCases;
    }
}