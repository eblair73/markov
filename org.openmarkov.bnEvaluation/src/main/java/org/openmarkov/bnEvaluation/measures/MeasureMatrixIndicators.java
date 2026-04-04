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

import javax.swing.*;

/**
 * This class calculates and stores the indicators obtained from
 * the confusion matrix.
 *
 * @author evillar
 * @version 1.0
 */

public class MeasureMatrixIndicators {
    
    private static final String NUM_FORMAT = "%.3f";
    
    /**
     * confusion matrix indicators
     */
    private final int numStates;
    private final double[] tp;
    private final double[] fp;
    private final double[] precision;
    private final double[] fMeasure;
    private final double accuracy;
    
    /**
     * The builder gathers the necessary information
     * to calculate the indicators and calls the method
     * to calculate them.
     */
    public MeasureMatrixIndicators(int[][] matrix, int numCases) {
        this.numStates = matrix[0].length;
        
        //Calculate sums
        int[] sumCols = new int[this.numStates];
        int[] sumRows = new int[this.numStates];
        for (int i1 = 0; i1 < this.numStates; i1++) {
            sumRows[i1] = 0;
            sumCols[i1] = 0;
            for (int j = 0; j < this.numStates; j++) {
                sumRows[i1] = sumRows[i1] + matrix[i1][j];
                sumCols[i1] = sumCols[i1] + matrix[j][i1];
            }
        }
        
        //Calculate the indicators
        this.tp = new double[this.numStates + 1];
        this.fp = new double[this.numStates + 1];
        this.precision = new double[this.numStates + 1];
        this.fMeasure = new double[this.numStates + 1];
        // indicator for each state
        double tpAcum = 0.0;
        double fpAcum = 0.0;
        double precisionAcum = 0.0;
        double fMeasureAcum = 0.0;
        double accuracy = 0.0;
        // loop in each state
        for (int i = 0; i < this.numStates; i++) {
            this.tp[i] = ((double) matrix[i][i] / sumRows[i]);
            this.fp[i] = ((double) sumCols[i] - matrix[i][i]) / ((double) numCases - sumRows[i]);
            this.precision[i] = ((double) matrix[i][i] / sumCols[i]);
            this.fMeasure[i] = (2.0 * this.precision[i] * this.tp[i]) / (this.precision[i] + this.tp[i]);
            accuracy = accuracy + matrix[i][i];
            // sum of the indicators with weights=num cases of real states
            tpAcum = tpAcum + this.tp[i] * sumRows[i];
            fpAcum = fpAcum + this.fp[i] * sumRows[i];
            precisionAcum = precisionAcum + this.precision[i] * sumRows[i];
            fMeasureAcum = fMeasureAcum + this.fMeasure[i] * sumRows[i];
        }
        // average all states
        this.tp[this.numStates] = tpAcum / numCases;
        this.fp[this.numStates] = fpAcum / numCases;
        this.precision[this.numStates] = precisionAcum / numCases;
        this.fMeasure[this.numStates] = fMeasureAcum / numCases;
        this.accuracy = accuracy / numCases;
    }
    
    /**
     * This method returns a JTable with the indicators
     *
     * @return JTable
     */
    public JTable toTable(String varName, String[] statesNames) {
        // there are numStates+1 rows and 5 columns (indicators)
        String[][] indicatorsTable = new String[this.numStates + 2][6];
        for (int i = 0; i < this.numStates; i++) {
            indicatorsTable[i][0] = varName + " (" + statesNames[i] + ")";
            indicatorsTable[i][1] = String.format(MeasureMatrixIndicators.NUM_FORMAT, this.tp[i]);
            indicatorsTable[i][2] = String.format(MeasureMatrixIndicators.NUM_FORMAT, this.fp[i]);
            indicatorsTable[i][3] = String.format(MeasureMatrixIndicators.NUM_FORMAT, this.precision[i]);
            indicatorsTable[i][4] = String.format(MeasureMatrixIndicators.NUM_FORMAT, this.tp[i]);
            indicatorsTable[i][5] = String.format(MeasureMatrixIndicators.NUM_FORMAT, this.fMeasure[i]);
        }
        indicatorsTable[this.numStates][0] = varName + " mean";
        indicatorsTable[this.numStates][1] = String.format(MeasureMatrixIndicators.NUM_FORMAT, this.tp[this.numStates]);
        indicatorsTable[this.numStates][2] = String.format(MeasureMatrixIndicators.NUM_FORMAT, this.fp[this.numStates]);
        indicatorsTable[this.numStates][3] = String.format(MeasureMatrixIndicators.NUM_FORMAT, this.precision[this.numStates]);
        indicatorsTable[this.numStates][4] = String.format(MeasureMatrixIndicators.NUM_FORMAT, this.tp[this.numStates]);
        indicatorsTable[this.numStates][5] = String.format(MeasureMatrixIndicators.NUM_FORMAT, this.fMeasure[this.numStates]);
        // accuracity
        indicatorsTable[this.numStates + 1][0] = "Accuracy";
        indicatorsTable[this.numStates + 1][1] = String.format(MeasureMatrixIndicators.NUM_FORMAT, this.accuracy);
        JTable tabla = new JTable(indicatorsTable, new String[]{"State", "TP rate", "FP rate", "Precision", "Recall", "F Measure"});
        return tabla;
    }
    
    /**
     * This method creates a sheet in the workbook with the confusion matrix
     * indicators
     */
    public void toExcel(Workbook workbook, String varName, String[] statesNames,
                        int numCases, int numIterations, boolean allVariablesAreUsed) {
        FormatExcel format = new FormatExcel(workbook);
        Sheet sheetI = workbook.createSheet("Indicators");
        
        // title indicators
        Row titleRowIndicators = sheetI.createRow(0);
        Cell titleCellIndicators = titleRowIndicators.createCell(0);
        titleCellIndicators.setCellValue("Confusion matrix indicators for " + varName.toUpperCase());
        titleCellIndicators.setCellStyle(format.getTitleFormat());
        // header
        Row headerRowIndicators = sheetI.createRow(1);
        Cell headerRowStates = headerRowIndicators.createCell(0);
        headerRowStates.setCellValue("States");
        headerRowStates.setCellStyle(format.getHeaderFormat());
        
        Cell headerRowTP = headerRowIndicators.createCell(1);
        headerRowTP.setCellValue("TP rate");
        headerRowTP.setCellStyle(format.getHeaderFormat());
        
        Cell headerRowFP = headerRowIndicators.createCell(2);
        headerRowFP.setCellValue("FP rate");
        headerRowFP.setCellStyle(format.getHeaderFormat());
        
        Cell headerRowPrecision = headerRowIndicators.createCell(3);
        headerRowPrecision.setCellValue("Precision");
        headerRowPrecision.setCellStyle(format.getHeaderFormat());
        
        Cell headerRowRecall = headerRowIndicators.createCell(4);
        headerRowRecall.setCellValue("Recall");
        headerRowRecall.setCellStyle(format.getHeaderFormat());
        
        Cell headerRowFmeasure = headerRowIndicators.createCell(5);
        headerRowFmeasure.setCellValue("F Measure");
        headerRowFmeasure.setCellStyle(format.getHeaderFormat());
        //loop in states
        for (int i = 0; i < this.numStates; i++) {
            Row rowIndicators_i = sheetI.createRow(2 + i);
            Cell headerRowStates_i = rowIndicators_i.createCell(0);
            headerRowStates_i.setCellStyle(format.getHeaderFormat());
            headerRowStates_i.setCellValue(statesNames[i]);
            
            Cell headerRowTP_i = rowIndicators_i.createCell(1);
            headerRowTP_i.setCellStyle(format.getCellMatrixFormat());
            headerRowTP_i.setCellValue(this.tp[i]);
            
            Cell headerRowFP_i = rowIndicators_i.createCell(2);
            headerRowFP_i.setCellStyle(format.getCellMatrixFormat());
            headerRowFP_i.setCellValue(this.fp[i]);
            
            Cell headerRowPrecision_i = rowIndicators_i.createCell(3);
            headerRowPrecision_i.setCellStyle(format.getCellMatrixFormat());
            headerRowPrecision_i.setCellValue(this.precision[i]);
            
            Cell headerRowRecall_i = rowIndicators_i.createCell(4);
            headerRowRecall_i.setCellStyle(format.getCellMatrixFormat());
            headerRowRecall_i.setCellValue(this.tp[i]);
            
            Cell headerRowFmeasure_i = rowIndicators_i.createCell(5);
            headerRowFmeasure_i.setCellStyle(format.getCellMatrixFormat());
            headerRowFmeasure_i.setCellValue(this.fMeasure[i]);
        }
        Row totalRowIndicators = sheetI.createRow(this.numStates + 2);
        Cell totalRowStatesInd = totalRowIndicators.createCell(0);
        totalRowStatesInd.setCellValue("States mean");
        totalRowStatesInd.setCellStyle(format.getHeaderFormat());
        
        Cell totalRowTP = totalRowIndicators.createCell(1);
        totalRowTP.setCellValue(this.tp[this.numStates]);
        totalRowTP.setCellStyle(format.getTotalFormat());
        
        Cell totalRowFP = totalRowIndicators.createCell(2);
        totalRowFP.setCellValue(this.fp[this.numStates]);
        totalRowFP.setCellStyle(format.getTotalFormat());
        
        Cell totalRowPrecision = totalRowIndicators.createCell(3);
        totalRowPrecision.setCellValue(this.precision[this.numStates]);
        totalRowPrecision.setCellStyle(format.getTotalFormat());
        
        Cell totalRowRecall = totalRowIndicators.createCell(4);
        totalRowRecall.setCellValue(this.tp[this.numStates]);
        totalRowRecall.setCellStyle(format.getTotalFormat());
        
        Cell totalRowFmeasure = totalRowIndicators.createCell(5);
        totalRowFmeasure.setCellValue(this.fMeasure[this.numStates]);
        totalRowFmeasure.setCellStyle(format.getTotalFormat());
        
        // accuracy
        
        Row accuracyRow = sheetI.createRow(this.numStates + 3);
        Cell accRowName = accuracyRow.createCell(0);
        accRowName.setCellValue("Accuracy");
        accRowName.setCellStyle(format.getHeaderFormat());
        
        Cell accRowValue = accuracyRow.createCell(1);
        accRowValue.setCellValue(this.accuracy);
        accRowValue.setCellStyle(format.getTotalFormat());
        
        // number of cases and iterations note
        Row rowNote = sheetI.createRow(this.numStates + 4);
        Cell note = rowNote.createCell(0);
        String noteMens = "Confusion matrix indicators calculated with " + numCases + " cases ";
        if (numIterations > 1) {
            noteMens = noteMens + " evaluated in " + numIterations + " networks";
        }
        note.setCellValue(noteMens);
        // if !allvariablesused note
        if (!allVariablesAreUsed) {
            Row rowNoteBis = sheetI.createRow(this.numStates + 5);
            Cell noteBis = rowNoteBis.createCell(0);
            noteBis.setCellValue("The probabilities were calculated without evidence in all the variables.");
        }
        
    }
    

}
