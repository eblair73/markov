/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.heuristic.minimalFillIn;

///** @author Manuel Arias */
//public class MinimalFillInTest extends InferenceAlgorithmBNTest {
//
//	MinimalFillIn heuristic;
//	
//	ProbNet probNet;
//	
//	@Before
//	public void setUp() throws Exception {
//		probNet = createProbNet("A", "B", "C", "D", "E", "F", "G");
//		addLink(probNet, "A", "B", false);
//		addLink(probNet, "B", "C", false);
//		addLink(probNet, "C", "D", false);
//		addLink(probNet, "D", "E", false);
//		addLink(probNet, "E", "F", false);
//		addLink(probNet, "E", "G", false);
//		addLink(probNet, "C", "G", false);
//		addLink(probNet, "E", "C", false);
//		addLink(probNet, "G", "B", false);
//		List<List<Variable>> listOfListsOfVariables = new ArrayList<>(1);
//		List<Variable> listOfVariables = probNet.getVariables();
//		listOfListsOfVariables.add(listOfVariables);
//		heuristic = new MinimalFillIn(probNet, listOfListsOfVariables);
//	}
//
//	@Test
//	public final void testGetVariableToDelete() throws NodeNotFoundException {
//		ArrayList<Variable> variables;
//		variables = new ArrayList<Variable>();
//		variables.add(removeNextVariable(heuristic));
//		variables.add(removeNextVariable(heuristic));
//		variables.add(removeNextVariable(heuristic));
//		variables.add(removeNextVariable(heuristic));
//		variables.add(removeNextVariable(heuristic));
//		variables.add(removeNextVariable(heuristic));
//		variables.add(removeNextVariable(heuristic));
//
//		assertEquals(variables.get(0),probNet.getVariable("A"));
//		assertEquals(variables.get(1),probNet.getVariable("B"));
//		assertEquals(variables.get(2),probNet.getVariable("D"));
//		assertEquals(variables.get(3),probNet.getVariable("C"));
//		assertEquals(variables.get(4),probNet.getVariable("F"));
//		assertEquals(variables.get(5),probNet.getVariable("E"));
//		assertEquals(variables.get(6),probNet.getVariable("G"));
//	}
//	
//	private Variable removeNextVariable(EliminationHeuristic heuristic) {
//		Variable variable = heuristic.getVariableToDelete();
//		RemoveNodeEdit removeEdit = new RemoveNodeEdit(probNet, probNet.getNode(variable));
//		heuristic.undoableEditHappened(new PNUndoableEditEvent(this, removeEdit, probNet));
//		return variable;
//	}
//
//    /** Create a <code>ProbNet</code> with the variables names received. All variables are binary.
//     * @param variablesNames. <code>String[]</code> */
//    private ProbNet createProbNet(String ... variablesNames) {
//    	int numVariables = variablesNames.length;
//    	ProbNet probNet = new ProbNet();
//    	Variable[] variables = new Variable[numVariables];
//    	for (int i = 0; i < numVariables; i++) {
//    		variables[i] = new Variable(variablesNames[i], "positive", "negative");
//    		Node node = probNet.addNode(variables[i], NodeType.CHANCE);
//    		ArrayList<Variable> tablePotentialVariables = new ArrayList<Variable>(1);
//    		tablePotentialVariables.add(variables[i]);
//    		double[] table = {0.5, 0.5};
//    		TablePotential potential = 
//    				new TablePotential(tablePotentialVariables, PotentialRole.CONDITIONAL_PROBABILITY, table);
//    		node.addPotential(potential);
//    	}
//    	return probNet;
//    }
//
//    /** Adds a link between two variables. The method assumes there are no problems.
//     * @param probNet
//     * @param variableName1
//     * @param variableName2
//     * @param directed
//     * @throws NodeNotFoundException
//     * @throws NodeNotFoundException
//     */
//    private void addLink(ProbNet probNet, String variableName1, String variableName2, boolean directed) 
//    		throws NodeNotFoundException, NodeNotFoundException {
//    	Variable variable1 = probNet.getVariable(variableName1);
//    	Variable variable2 = probNet.getVariable(variableName2);
//    	probNet.addLink(variable1, variable2, directed);
//    }
//
//	@Override
//	public InferenceAlgorithm buildInferenceAlgorithm(ProbNet probNet)
//			throws NotEvaluableNetworkException {
//		InferenceAlgorithm inferenceAlgorithm = new VariableElimination(probNet);
//		inferenceAlgorithm.setHeuristicFactory(new HeuristicFactory() {
//			
//			@Override
//			public EliminationHeuristic getHeuristic(ProbNet probNet, List<List<Variable>> variables) {
//				return new MinimalFillIn(probNet, variables);
//			}
//		});
//		return inferenceAlgorithm;
//	}
//
//
//}
