package org.openmarkov.learning.metric.cmi.util;

import org.openmarkov.core.io.database.CaseDatabase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;



public class MetricUtils {

    private ProbNet probNet;
    private String classVariable;

    public MetricUtils(ProbNet probNet, String name){
        this.probNet=probNet;
        this.classVariable=name;

    }

    public List<Variable> getNonRootVariables(){
        return getNonRootNodes().stream().map(Node::getVariable).collect(Collectors.toList());
    }

    public List<Node> getNonRootNodes(){
        return probNet.getNodes().stream()
                .filter(n-> !n.getName().equalsIgnoreCase(classVariable))
                .collect(Collectors.toList());
    }

    public Node getRootNode(){
        return probNet.getNodes().stream().filter(n-> n.getName().equalsIgnoreCase(classVariable)).findFirst().get();
    }

    public static double sumArray(double[] freq, int b, int e, int rate){
        double result = 0;
        while (b < e) {
            result+= freq[b];
            b+=rate;
        }
        return result;
    }


    public static int getIndexOfMaxValue(double[] probs){
        int hProbClass=0;
        double max=0.0;

        for(int i=0; i< probs.length; i++){
            if(probs[i]>max){
                max=probs[i];
                hProbClass=i;
            }
        }

        return hProbClass;
    }


    public static TablePotential getAbsoluteFrequencies(ProbNet probNet, CaseDatabase caseDatabase, Node node,
                                                 List<Variable> variables, int[][] cases) {
        int parentsConfigurations = 1;
        int numValues = node.getVariable().getNumStates();
        // We miss the first one as it is the node itself, not one of its parents
        int[] indexesOfParents = new int[variables.size() - 1];
        for (int i = 0; i < indexesOfParents.length; ++i) {
            indexesOfParents[i] = caseDatabase.getVariables().indexOf(variables.get(i + 1));
            parentsConfigurations *= variables.get(i + 1).getNumStates();
        }

        TablePotential absoluteFreqPotential = new TablePotential(variables, PotentialRole.CONDITIONAL_PROBABILITY);
        double[] absoluteFreqs = absoluteFreqPotential.getValues();
        // Initialize the table
        for (int i = 0; i < parentsConfigurations * numValues; i++) {
            absoluteFreqs[i] = 0;
        }
        variables.remove(0);
        // Compute the absolute frequencies
        int iCPT;
        int iParent, iNode = caseDatabase.getVariables().indexOf(node.getVariable());
        List<Node> nodes = probNet.getNodes(variables);
        for (int i = 0; i < cases.length; i++) {
            iCPT = 0;
            for (int j = 0; j < nodes.size(); ++j) {
                iParent = indexesOfParents[j];
                iCPT = iCPT * nodes.get(j).getVariable().getNumStates() + cases[i][iParent];
            }
            if (numValues * iCPT + cases[i][iNode] >= absoluteFreqs.length)
                System.out.println("fdx");

            absoluteFreqs[numValues * iCPT + cases[i][iNode]]++;
        }
        return absoluteFreqPotential;
    }

    public static TablePotential buildAbsoluteFreqCrossTab(ProbNet probNet, CaseDatabase caseDatabase, Node nodeX,
                                                     Node nodeY, Node root, int[][] cases) {
        int parentsConfigurations = 1;
        int numValues = nodeX.getVariable().getNumStates();
        // We miss the first one as it is the node itself, not one of its parents
        int[] indexesOfParents = new int[2];
        List<Variable> variables = new ArrayList<>(Arrays.asList(nodeX.getVariable(), root.getVariable(), nodeY.getVariable()));
        TablePotential potential = new TablePotential(variables, PotentialRole.CONDITIONAL_PROBABILITY);

        for (int i = 0; i < indexesOfParents.length; ++i) {
            indexesOfParents[i] = caseDatabase.getVariables().indexOf(variables.get(i + 1));
            parentsConfigurations *= variables.get(i + 1).getNumStates();
        }

        double[] absoluteFreqs = potential.getValues();
        // Initialize the table
        for (int i = 0; i < parentsConfigurations * numValues; i++) {
            absoluteFreqs[i] = 0;
        }
        variables.remove(0);
        // Compute the absolute frequencies
        int iCPT;
        int iParent, iNode = caseDatabase.getVariables().indexOf(nodeX.getVariable());
        if (iNode == -1)
            System.out.println("fdx");
        List<Node> nodes = probNet.getNodes(variables);
        for (int i = 0; i < cases.length; i++) {
            iCPT = 0;
            for (int j = 0; j < nodes.size(); ++j) {
                iParent = indexesOfParents[j];
                iCPT = iCPT * nodes.get(j).getVariable().getNumStates() + cases[i][iParent];
            }
            absoluteFreqs[numValues * iCPT + cases[i][iNode]]++;
        }
        return potential;
    }

}
