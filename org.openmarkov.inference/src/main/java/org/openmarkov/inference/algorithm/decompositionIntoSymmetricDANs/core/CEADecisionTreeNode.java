package org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.core;

import java.text.DecimalFormat;

import org.openmarkov.core.model.decisiontree.DecisionTreeBranch;
import org.openmarkov.core.model.decisiontree.DecisionTreeNode;
import org.openmarkov.core.model.network.CEP;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.GTablePotential;
import org.openmarkov.core.model.network.potential.Potential;

/**
 * This class represents a node of the decision tree used in the evaluation of a DAN with CEPs. It extends the class
 * DecisionTreeNode, and it is used to store the CEPs resulting from the evaluation of the branches of the decision tree.
 * 
 * @author Manuel Arias
 *
 */
public class CEADecisionTreeNode extends DecisionTreeNode<CEP> {

	// Constructors
	public CEADecisionTreeNode(Node node) {
		super(node);
		// TODO Auto-generated constructor stub
	}

	public CEADecisionTreeNode(Node node, ProbNet dan) {
		super(node, dan);
	}

	public CEADecisionTreeNode(Variable variable, ProbNet dan) {
		super(variable, dan);
	}

	@Override
	public boolean isBestDecision(DecisionTreeBranch treeBranch) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setOnlyValueForUtility(Potential tablePotential) {
		setUtility(DANOperations.getOnlyValuePotentialCEP((GTablePotential) tablePotential));
	}

	/**
	 * Returns a string with the cost and effectiveness of the CEP intervals.
	 * For a single interval, shows "Cost=X / Effectiveness=Y".
	 * For multiple intervals, shows each interval with its threshold range.
	 * @param df DecimalFormat to format the cost and effectiveness values.
	 * @param addSlashPrefixIfItAddsContent If true, a slash is added at
	 * the beginning of the string.
	 */
	@Override
	public String formatUtility(DecimalFormat df, boolean addSlashPrefixIfItAddsContent) {
		CEP cep = getUtility();
		if (!cep.hasStrategyTrees()) {
			return " ";
		}
		StringBuilder str = new StringBuilder();
		if (addSlashPrefixIfItAddsContent) {
			str.append(" /");
		}
		int numIntervals = cep.getNumIntervals();
		if (numIntervals == 1) {
			str.append(" Cost=").append(df.format(cep.getCost(0)));
			str.append(" / Effectiveness=").append(df.format(cep.getEffectiveness(0)));
		} else {
			double[] thresholds = cep.getThresholds();
			for (int i = 0; i < numIntervals; i++) {
				if (i > 0) {
					str.append(" |");
				}
				str.append(" λ∈[");
				if (i == 0) {
					str.append(df.format(cep.getMinThreshold()));
				} else {
					str.append(df.format(thresholds[i - 1]));
				}
				str.append(",");
				if (i == numIntervals - 1) {
					if (cep.getMaxThreshold() == Double.POSITIVE_INFINITY) {
						str.append("∞");
					} else {
						str.append(df.format(cep.getMaxThreshold()));
					}
				} else {
					str.append(df.format(thresholds[i]));
				}
				str.append("): C=").append(df.format(cep.getCost(i)));
				str.append("/E=").append(df.format(cep.getEffectiveness(i)));
			}
		}
		return str.toString();
	}

}
