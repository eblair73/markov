package org.openmarkov.bnEvaluation.measures;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openmarkov.bnEvaluation.FormatExcel;
import org.openmarkov.core.io.database.CaseDatabase;
import org.openmarkov.core.model.network.Variable;

import javax.swing.*;
import java.util.List;

/**
 * Stores individual posterior probabilities for each case in a dataset,
 * along with the most probable state per case. Supports export to JTable and Excel.
 */
public class MeasureMatrixIndProb {
    
    // information used when showing individual probabilities
    private final CaseDatabase caseDatabase;
    private final double[][] prob;
    private final String[] stateMaxProb;
    
    public MeasureMatrixIndProb(CaseDatabase cases, double[][] prob, String[] stateMaxProb) {
        this.caseDatabase = cases;
        this.prob = prob;
        this.stateMaxProb = stateMaxProb;
    }
    
    /**
     * Writes the individual probabilities and most probable states to an Excel sheet.
     *
     * @param workbook    the workbook to add the sheet to
     * @param statesNames the names of the classification variable states
     * @param varName     the name of the classification variable
     */
    public void probToExcel(Workbook workbook,
                            String[] statesNames,
                            String varName) {
        Sheet sheet = workbook.createSheet("Probabilities");
        FormatExcel format = new FormatExcel(workbook);
        List<Variable> variables = caseDatabase.getVariables();
        int[][] cases = caseDatabase.getCases();
        // initialize variables
        int numVariables = variables.size();
        // headers
        Row headers = sheet.createRow(0);
        
        for (int j = 0; j < numVariables; j++) {
            Cell nameVariable = headers.createCell(j);
            nameVariable.setCellValue(variables.get(j).getName());
            nameVariable.setCellStyle(format.getHeaderFormat());
        }
        for (int j = 0; j < statesNames.length; j++) {
            Cell nameState = headers.createCell(numVariables + j);
            nameState.setCellValue("P(" + varName + "=" + statesNames[j] + ")");
            nameState.setCellStyle(format.getHeaderFormat());
        }
        Cell nameState = headers.createCell(statesNames.length + numVariables);
        nameState.setCellValue("most probable state");
        nameState.setCellStyle(format.getHeaderFormat());
        // information
        for (int i = 0; i < caseDatabase.getNumCases(); i++) {
            Row rowProbs = sheet.createRow(i + 1);
            for (int j = 0; j < variables.size(); j++) {
                Cell case_ij = rowProbs.createCell(j);
                case_ij.setCellValue(variables.get(j).getStateName(cases[i][j]));
                case_ij.setCellStyle(format.getCellMatrixFormat());
            }
            for (int j = 0; j < statesNames.length; j++) {
                Cell prob_state_j = rowProbs.createCell(numVariables + j);
                prob_state_j.setCellValue(String.format("%.3f", prob[i][j]));
                prob_state_j.setCellStyle(format.getCellMatrixFormat());
            }
            Cell most_prob_state = rowProbs.createCell(numVariables + statesNames.length);
            most_prob_state.setCellValue(stateMaxProb[i]);
            most_prob_state.setCellStyle(format.getCellMatrixFormat());
        }
    }
    
    /**
     * This method returns a JTable with the probabilities
     *
     * @return JTable
     */
    public JTable probToTable(String[] statesNames,
                              String varName) {
        List<Variable> variables = caseDatabase.getVariables();
        int[][] cases = caseDatabase.getCases();
        // initialize variables
        int numVariables = variables.size();
        int ncol = numVariables + statesNames.length + 1;
        String[][] cases_table = new String[caseDatabase.getNumCases()][ncol];
        String[] headers = new String[ncol];
        // headers
        for (int j = 0; j < numVariables; j++) {
            headers[j] = variables.get(j).getName();
        }
        for (int j = 0; j < statesNames.length; j++) {
            headers[numVariables + j] = "P(" + varName + "=" + statesNames[j] + ")";
        }
        headers[numVariables + statesNames.length] = "most probable state";
        // information
        for (int i = 0; i < caseDatabase.getNumCases(); i++) {
            for (int j = 0; j < variables.size(); j++) {
                cases_table[i][j] = variables.get(j).getStateName(cases[i][j]);
            }
            for (int j = 0; j < statesNames.length; j++) {
                cases_table[i][numVariables + j] = String.format("%.3f", prob[i][j]);
            }
            cases_table[i][numVariables + statesNames.length] = stateMaxProb[i];
        }
        return new JTable(cases_table, headers);
    }
    
}
