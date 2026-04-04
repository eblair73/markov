/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.bnEvaluation.measures;


import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openmarkov.bnEvaluation.FormatExcel;
import org.openmarkov.core.io.database.CaseDatabase;

import javax.swing.*;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * This class represents a confusion matrix measure.
 * Extends the abstract class Measure
 *
 * @author evillar
 * @version 1.0
 */
public class MeasureMatrix extends Measure {
    
    private int[][] matrix;
    private int numStates;
    private String[] statesNames;
    private String varName;
    private MeasureMatrixIndicators indicators;
    private MeasureMatrixIndProb individualProb;
    private boolean showIndividualProb;
    
    /**
     * This constructor is used when the instance
     * is created in BNEvaluationDialog or in LearningDialog
     *
     * @param type        the measure type (should be {@link MeasureType#CONFUSIONMATRIX})
     * @param statesNames the names of the classification variable states
     * @param varName     the name of the classification variable
     */
    public MeasureMatrix(MeasureType type, String[] statesNames,
                         String varName) {
        super(type);
        this.statesNames = statesNames;
        numStates = statesNames.length;
        matrix = new int[numStates][numStates];
        for (int i = 0; i < numStates; i++) {
            for (int j = 0; j < numStates; j++) {
                matrix[i][j] = 0;
            }
        }
        this.varName = varName;
        indicators = null;
        individualProb = null;
        showIndividualProb = false;
    }
    
    //setters and getters
    public int[][] getMatrix() {
        return matrix;
    }
    
    public int getNumStates() {
        return numStates;
    }
    
    public String[] getStatesNames() {
        return statesNames;
    }
    
    public String getVarName() {
        return varName;
    }
    
    public boolean getShowIndividualProb() {
        return showIndividualProb;
    }
    
    public void setShowIndividualProb() {
        showIndividualProb = true;
    }
    
    /**
     * Sets the individual posterior probabilities for each case and the estimated states.
     *
     * @param caseDatabase    the case database used for evaluation
     * @param prob            posterior probability matrix (cases x states)
     * @param estimatedStates index of the most probable state for each case
     */
    public void setIndividualProb(CaseDatabase caseDatabase,
                                  double[][] prob,
                                  int[] estimatedStates) {
        
        String[] stateMaxProb = new String[caseDatabase.getNumCases()];
        for (int i = 0; i < caseDatabase.getNumCases(); i++) {
            stateMaxProb[i] = statesNames[estimatedStates[i]];
        }
        individualProb = new MeasureMatrixIndProb(caseDatabase, prob, stateMaxProb);
        
    }
    
    /**
     * this method is called from evaluator
     *
     * @param matrix   the confusion matrix values
     * @param numCases the number of cases used to compute the matrix
     */
    public void setMatrix(int[][] matrix, int numCases) {
        this.matrix = matrix;
        super.setNumCases(numCases);
    }
    
    /** Computes and stores the confusion matrix indicators (TP, FP, precision, F-measure, accuracy). */
    public void setIndicators() {
        indicators = new MeasureMatrixIndicators(matrix, super.getNumCases());
    }
    
    
    /**
     * This method is called when evaluating an algorithm, to add two confusion matrices.
     *
     * @param measure the confusion matrix measure to add
     */
    @Override public void accumulate(Measure measure) {
        int[][] matrixToAdd = ((MeasureMatrix) measure).getMatrix();
        for (int i = 0; i < numStates; i++) {
            for (int j = 0; j < numStates; j++) {
                matrix[i][j] = matrix[i][j] + matrixToAdd[i][j];
            }
        }
        super.setNumCases(super.getNumCases() + measure.getNumCases());
    }
    
    /**
     * This method return a JTable with the confusion matrix
     *
     * @return JTable
     */
    public JTable matrixToTable() {
        
        NumberFormat format = NumberFormat.getInstance(Locale.getDefault());
        String[][] matrixTable = new String[numStates + 1][numStates + 2];
        String[] statesTable = new String[numStates + 2];
        statesTable[0] = "TRUE / PREDICTED->";
        statesTable[numStates + 1] = "Total";
        matrixTable[numStates][0] = "Total";
        int sum = 0;
        for (int i = 0; i < numStates; i++) {
            statesTable[i + 1] = varName + "(" + statesNames[i] + ")";
            matrixTable[i][0] = varName + "(" + statesNames[i] + ")";
            int rowTotal = 0;
            int colTotal = 0;
            for (int j = 1; j < (numStates + 1); j++) {
                matrixTable[i][j] = format.format(matrix[i][j - 1]);
                rowTotal = rowTotal + matrix[i][j - 1];
                colTotal = colTotal + matrix[j - 1][i];
            }
            matrixTable[i][numStates + 1] = format.format(rowTotal);
            matrixTable[numStates][i + 1] = format.format(colTotal);
            sum = sum + rowTotal;
        }
        matrixTable[numStates][numStates + 1] = format.format(sum);
        return new JTable(matrixTable, statesTable);
    }
    
    /**
     * This method return a JTable with the indicators
     *
     * @return JTable
     */
    public JTable indicatorsToTable() {
        return indicators.toTable(varName, statesNames);
    }
    
    /**
     * This method return a JTable with the individual probabilities
     *
     * @return JTable
     */
    
    public JTable probToTable() {
        return individualProb.probToTable(statesNames, varName);
    }
    
    /**
     * This method create a Sheet with the confusion matrix values
     *
     */
    public void matrixToExcel(Workbook workbook, int numIterations, boolean allVariablesAreUsed) {
        FormatExcel format = new FormatExcel(workbook);
        int numCases = super.getNumCases();
        Sheet sheetCM = workbook.createSheet("Confusion matrix");
        //title confusion matrix
        Row titleRowCM = sheetCM.createRow(0);
        Cell titleCellCM = titleRowCM.createCell(0);
        titleCellCM.setCellStyle(format.getTitleFormat());
        titleCellCM.setCellValue("Confusion matrix for " + varName.toUpperCase());
        // header
        Row headerPredictedRow = sheetCM.createRow(1);
        Cell headerPredictedCell = headerPredictedRow.createCell(1);
        headerPredictedCell.setCellValue("Predicted states");
        headerPredictedCell.setCellStyle(format.getHeaderFormat());
        Row headerRowCM = sheetCM.createRow(2);
        Cell headerRowCMReal = headerRowCM.createCell(0);
        headerRowCMReal.setCellValue("True states");
        headerRowCMReal.setCellStyle(format.getHeaderFormat());
        int numStates = statesNames.length;
        for (int i = 0; i < numStates; i++) {
            Cell headerState = headerRowCM.createCell(1 + i);
            headerState.setCellValue(statesNames[i]);
            headerState.setCellStyle(format.getHeaderFormat());
            Cell upHeaderSate = headerPredictedRow.createCell(2 + i);
            upHeaderSate.setCellStyle(format.getHeaderFormat());
        }
        // total (rows total)
        Cell headerRowCMRealTotal = headerRowCM.createCell(1 + numStates);
        headerRowCMRealTotal.setCellValue("Total");
        headerRowCMRealTotal.setCellStyle(format.getHeaderFormat());
        
        // total (cols total)
        Row rowTotalCols = sheetCM.createRow(numStates + 3);
        Cell total = rowTotalCols.createCell(0);
        total.setCellValue("Total");
        total.setCellStyle(format.getHeaderFormat());
        
        // loop confusion matrix
        int rowTotal = 0;
        int colTotal = 0;
        int sum = 0;
        for (int i = 0; i < numStates; i++) {
            rowTotal = 0;
            colTotal = 0;
            Row rowCM = sheetCM.createRow(i + 3);
            Cell nameState = rowCM.createCell(0);
            nameState.setCellValue(statesNames[i]);
            nameState.setCellStyle(format.getHeaderFormat());
            for (int j = 0; j < numStates; j++) {
                Cell cellCM = rowCM.createCell(1 + j);
                cellCM.setCellValue(matrix[i][j]);
                cellCM.setCellStyle(format.getCellMatrixFormat());
                rowTotal = rowTotal + matrix[i][j];
                colTotal = colTotal + matrix[j][i];
            }
            sum = sum + rowTotal;
            Cell totalRow = rowCM.createCell(numStates + 1);
            totalRow.setCellValue(rowTotal);
            totalRow.setCellStyle(format.getTotalFormat());
            Cell totalCol = rowTotalCols.createCell(i + 1);
            totalCol.setCellValue(colTotal);
            totalCol.setCellStyle(format.getTotalFormat());
        }
        Cell totalTotal = rowTotalCols.createCell(numStates + 1);
        totalTotal.setCellValue(sum);
        totalTotal.setCellStyle(format.getTotalFormat());
        // note with the number of cases
        Row rowNote = sheetCM.createRow(numStates + 4);
        Cell note = rowNote.createCell(0);
        String noteMens = "Confusion matrix calculated with " + numCases + " cases ";
        if (numIterations > 1) {
            noteMens = noteMens + " evaluated in " + numIterations + " networks";
        }
        note.setCellValue(noteMens);
        if (!allVariablesAreUsed) {
            Row rowNoteBis = sheetCM.createRow(numStates + 5);
            Cell noteBis = rowNoteBis.createCell(0);
            noteBis.setCellValue("The probabilities were calculated without evidence in all the variables.");
        }
    }
    
    /**
     * This method create a Sheet with the confusion matrix-indicators calling
     * to the method toExcel of indicators
     */
    public void indicatorsToExcel(Workbook workbook, int numIterations, boolean allVariablesAreUsed) {
        indicators.toExcel(workbook, varName, statesNames, super.getNumCases(), numIterations, allVariablesAreUsed);
    }
    
    /**
     * This method create a Sheet with the casedatabase and the
     * posterior probabilities of the classification variable
     */
    public void probToExcel(Workbook workbook) {
        individualProb.probToExcel(workbook, statesNames, varName);
    }
    
}
