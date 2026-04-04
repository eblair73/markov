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
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openmarkov.bnEvaluation.FormatExcel;

import javax.swing.*;
import java.util.ArrayList;

import static org.openmarkov.bnEvaluation.measures.MeasureType.LOGLIKEHOOD;

/**
 * This class stores the set of measures. It can be relative to
 * - a network evaluation (numIterations=1)
 * - an algorithm evaluation (numIterations>1).
 *
 * @author evillar
 * @version 1.0
 */
public class MeasuresSet {
    
    private MeasureMatrix matrix;
    private ArrayList<MeasureValue> measures;
    
    private int numIterations;
    //private int numCases;
    private boolean allVariablesAreUsed;
    private String measureTitle;
    
    
    /**
     * constructor to create an empty object (numIterations=1)
     * Its used in BNEvaluationDialog and CrossValidationDialog
     */
    public MeasuresSet(String measureTitle) {
        matrix = null;
        measures = new ArrayList<MeasureValue>();
        numIterations = 1;
        allVariablesAreUsed = true;
        this.measureTitle = measureTitle;
    }
    
    /**
     * copy constructor to create a measuresSet with the measures empty
     * and numIterations=0 !!!
     *
     * @param measuresSet the template to copy structure from
     */
    public MeasuresSet(MeasuresSet measuresSet) {
        MeasureMatrix matrixToCopy = measuresSet.getMeasureMatrix();
        if (matrixToCopy != null) {
            matrix = new MeasureMatrix(MeasureType.CONFUSIONMATRIX,
                                       matrixToCopy.getStatesNames(),
                                       matrixToCopy.getVarName());
        } else {
            matrix = null;
        }
        measures = new ArrayList<MeasureValue>();
        for (MeasureValue measure : measuresSet.getMeasures()) {
            measures.add(new MeasureValue(measure.getMeasureType()));
        }
        numIterations = 0;
        allVariablesAreUsed = true;
        measureTitle = measuresSet.getMeasureTitle();
    }
    
    // getters
    public MeasureMatrix getMeasureMatrix() {
        return matrix;
    }
    
    public ArrayList<MeasureValue> getMeasures() {
        return measures;
    }
    
    public String getMeasureTitle() {
        return measureTitle;
    }
    
    public int getNumMeasuresValue() {
        return measures.size();
    }
    
    public void setNotAllVariablesAreUsed() {
        allVariablesAreUsed = false;
    }
    
    /**
     * This method adds a new measure to the Array measures
     *
     * @param measure the scalar measure to add
     */
    public void addMeasureValue(MeasureValue measure) {
        measures.add(measure);
    }
    
    /**
     * This method adds a measure to MeasureMAtrix variable
     *
     * @param measure the confusion matrix measure to set
     */
    public void addMeasureMatrix(MeasureMatrix measure) {
        matrix = measure;
    }
    
    /**
     * This method adds a new iteration, add value in each measures
     * and sum numCases
     *
     * @param measureSetToAdd the measure set from one iteration to accumulate
     */
    public void accumulateMeasureSet(MeasuresSet measureSetToAdd) {
        // the sets to accumulate are similar, if matrix is not null, then getMeasureMatrix is also not null
        if (matrix != null) {
            matrix.accumulate(measureSetToAdd.getMeasureMatrix());
        }
        ArrayList<MeasureValue> measuresValue = measureSetToAdd.getMeasures();
        for (int i = 0; i < measures.size(); i++) {
            measures.get(i).accumulate(measuresValue.get(i));
        }
        numIterations = numIterations + 1;
    }
    
    /** Computes averages by dividing accumulated values by the number of iterations. */
    public void setAveraged() {
        // the indicators are calculated with the total of cases
        if (matrix != null) {
            matrix.setIndicators();
        }
        // the value-measures are divided by the number of iterations
        for (MeasureValue measure : measures) {
            measure.averageValue(numIterations);
        }
    }
    
    /**
     * This methods manages the note to use when not all the variables are used
     * to calculate the measures
     *
     * @return a human-readable information string about the measures
     */
    public String getMeasureInformation() {
        String information = measureTitle + "\n";
        if (matrix != null) {
            information = information +
                    "Confusion matrix are calculated with " + matrix.getNumCases() + " cases.\n";
        }
        if (measures.size() > 0) {
            int numCasesScores = measures.get(0).getNumCases();
            information = information + "Scores are calculated with " + numCasesScores +
                    " cases.\n ";
        }
        if (!allVariablesAreUsed) {
            information = information +
                    "The probabilities were calculated without evidence in all the variables.\n";
        }
        return information;
    }
    
    /**
     * This method returns a JTable with the scores
     *
     * @return JTable
     */
    public JTable scoresToTable() {
        int numMeasures = measures.size();
        boolean logIncluded = (measures.getFirst().getMeasureType() == LOGLIKEHOOD);
        int numRows = logIncluded ? (numMeasures + 3) : (numMeasures + 1);
        int indexRow = 0;
        String[][] tableScrString = new String[numRows][2];
        if (logIncluded) {
            tableScrString[indexRow][0] = "Goodness of fit Log-likelihood measures";
        } else {
            tableScrString[indexRow][0] = "Score measures";
        }
        indexRow = indexRow + 1;
        for (int i = 0; i < numMeasures; i++) {
            MeasureValue measureS = measures.get(i);
            tableScrString[indexRow][0] = measureS.getMeasureType().toString() + " score";
            tableScrString[indexRow][1] = String.format("%.3f", measureS.getValue());
            indexRow = indexRow + 1;
            if (measureS.getMeasureType() == LOGLIKEHOOD) {
                tableScrString[indexRow][0] = measureS.getMeasureType().toString() + " Loss";
                tableScrString[indexRow][1] = String.format("%.3f", -measureS.getValue() / measureS.getNumCases());
                indexRow = indexRow + 1;
                if (numMeasures > 1) {
                    tableScrString[indexRow][0] = "Score measures";
                    indexRow = indexRow + 1;
                }
            }
        }
        return new JTable(tableScrString, new String[]{"", ""});
    }
    
    /**
     * This method returns a Workbook that can be exported from the results GUI
     *
     * @return Workbook
     */
    public Workbook toExcel() {
        Workbook workbook = new XSSFWorkbook();
        FormatExcel format = new FormatExcel(workbook);
        if (matrix != null) {
            matrix.matrixToExcel(workbook, numIterations, allVariablesAreUsed);
            matrix.indicatorsToExcel(workbook, numIterations, allVariablesAreUsed);
        }
        // scores sheet
        Sheet sheetM = null;
        int numMeasures = measures.size();
        if (numMeasures > 0) {
            boolean logIncluded = (measures.getFirst().getMeasureType() == LOGLIKEHOOD);
            int numCasesScores = measures.get(0).getNumCases();
            sheetM = workbook.createSheet("Scores");
            Row titleRow = sheetM.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            if (logIncluded) {
                titleCell.setCellValue("Goodness of fit Log-likelihood measures");
                titleCell.setCellStyle(format.getTitleFormat());
            } else {
                titleCell.setCellValue("Score measures");
                titleCell.setCellStyle(format.getTitleFormat());
            }
            int nRowSheetM = 1;
            for (MeasureValue measure : measures) {
                Row measureRow = sheetM.createRow(nRowSheetM);
                Cell measureNameCell = measureRow.createCell(0);
                Cell measureValueCell = measureRow.createCell(1);
                
                measureNameCell.setCellValue(measure.getMeasureType().toString() + " score");
                measureNameCell.setCellStyle(format.getTotalFormat());
                
                measureValueCell.setCellValue(measure.getValue());
                measureValueCell.setCellStyle(format.getCellMatrixFormat());
                
                nRowSheetM = nRowSheetM + 1;
                
                if (measure.getMeasureType() == LOGLIKEHOOD) {
                    //Loglikehood loss
                    Row measureLossRow = sheetM.createRow(nRowSheetM);
                    Cell measureLossNameCell = measureLossRow.createCell(0);
                    Cell measureLossValueCell = measureLossRow.createCell(1);
                    measureLossNameCell.setCellValue(measure.getMeasureType().toString() + " Loss");
                    measureLossNameCell.setCellStyle(format.getTotalFormat());
                    double loss = -measure.getValue() / measure.getNumCases();
                    measureLossValueCell.setCellValue(loss);
                    measureLossValueCell.setCellStyle(format.getCellMatrixFormat());
                    nRowSheetM = nRowSheetM + 1;
                    if (numMeasures > 1) {
                        // title Score measures
                        Row titleRow_bis = sheetM.createRow(nRowSheetM);
                        Cell titleCell_bis = titleRow_bis.createCell(0);
                        titleCell_bis.setCellValue("Score measures");
                        titleCell_bis.setCellStyle(format.getTitleFormat());
                        nRowSheetM = nRowSheetM + 1;
                    }
                }
            }
            // number of cases and iterations note
            Row rowNote = sheetM.createRow(nRowSheetM);
            Cell note = rowNote.createCell(0);
            note.setCellValue("Scores are calculated with " + numCasesScores +
                                      " cases");
            nRowSheetM = nRowSheetM + 1;
            if (numIterations > 1) {
                rowNote = sheetM.createRow(nRowSheetM);
                note = rowNote.createCell(0);
                note.setCellValue("Measures calculated as an average of " + numIterations + " iterations");
                nRowSheetM = nRowSheetM + 1;
            }
        }
        if (matrix != null) {
            if (matrix.getShowIndividualProb()) {
                matrix.probToExcel(workbook);
            }
        }
        return workbook;
    }
    
}
