/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.probmodel.strings;

import java.io.Serializable;

public enum XMLTags implements Serializable {
	ADDITIONAL_CONSTRAINTS("AdditionalConstraints"),
	ADDITIONAL_PROPERTIES("AdditionalProperties"),
	AGENTS("Agents"),
	AGENT("Agent"),
	ALWAYS_OBSERVED("AlwaysObserved"),
	ARGUMENT("Argument"),
	BRANCH("Branch"),
	BRANCHES("Branches"),
	CHOLESKY_DECOMPOSITION("CholeskyDecomposition"),
	COEFFICIENTS("Coefficients"),
	COVARIANCE_MATRIX("CovarianceMatrix"),
    COVARIATE("Covariate"),
	COVARIATES("Covariates"),
	COMMENT("Comment"),
	COORDINATES("Coordinates"),
	COORDINATES_SHIFT("CoordinatesShift"),
	CONSTRAINT("Constraint"),
	CONSTRAINTS("Constraints"),
    COST_EFFECTIVENESS("CostEffectiveness"),
	CE_CRITERIA("CE_Criteria"),
	CE_CRITERION("CE_Criterion"),
	CRITERION("Criterion"),
	CYCLE_LENGTH("CycleLength"),
	DECISION_CRITERIA("DecisionCriteria"),
	DECISION_CRITERION("DecisionCriterion"),
	DISCOUNT_RATE("DiscountRate"),
	DISCOUNT_RATES("DiscountRates"),
	EVIDENCE("Evidence"),
	EVIDENCE_CASE("EvidenceCase"),
	FINDING("Finding"),
    FUNCTION("Function"),
	HORIZON("Horizon"),
	INFERENCE_OPTIONS("InferenceOptions"),
	INTERVAL("Interval"),
	LABEL("Label"),
	LANGUAGE("Language"),
	LINK("Link"),
	LINKS("Links"),
	LOG("Log"),
	MODEL("Model"),
	MULTICRITERIA_OPTIONS("MulticriteriaOptions"),
	NUMBER_OF_CASES("NumberOfCases"),
    NUMERIC_VALUE("NumericValue"),
	NUMERIC_VARIABLES("NumericVariables"),
	OPEN_MARKOV_XML("OpenMarkov"),
	//For Univariate
	PARAMETERS("Parameters"),
	POLICIES("Policies"),
	POLICY("Policy"),
	POTENTIAL("Potential"),
	POTENTIALS("Potentials"),
	PRECISION("Precision"),
	PROB_NET("ProbNet"),
	PROPERTIES("Properties"),
	PROPERTY("Property"),
	PURPOSE("Purpose"),
	REFERENCE("Reference"),
	RELEVANCE("Relevance"),
	REVELATION_CONDITIONS("RevelationCondition"),
	SCALE("Scale"),
	SCALES("Scales"),
	SELECTED_ANALYSIS_TYPE("SelectedAnalysisType"),
	SLICES("Slices"),
	STATE("State"),
	STATES("States"),
    STATE_INDEX("StateIndex"),
	STRATEGY("Strategy"),
	SUBPOTENTIALS("Subpotentials"),
	TEMPORAL_OPTIONS("TemporalOptions"),
	THETA("Theta"),
	THRESHOLD("Threshold"),
	THRESHOLDS("Thresholds"),
	TITLE("Title"), // Obsolete. Included for compatibility with older networks
	TIME_UNIT("TimeUnit"),
	TIME_VARIABLE("TimeVariable"),
	TOP_VARIABLE("TopVariable"),
	TRANSITION("Transition"),
	UNCERTAIN_PARAMETERS("UncertainParameters"),
	PARAM("Param"),
	//temporal
	UNCERTAIN_VALUES("UncertainValues"),
	UNCERT_PARAM("UncertParam"),
	//
	UNICRITERION("Unicriterion"),
	UNIT("Unit"),
	UTILITY_VARIABLE("UtilityVariable"),
	VALUE("Value"),
	VALUES("Values"),
	VARIABLE("Variable"),
    VARIABLES("Variables"),
	FUNCTIONS("Functions")
	;
    
    private final String name;
	
	XMLTags(String name) {
		this.name = name;
    }
	
	public String toString() {
        return this.name;
	}
	
}
