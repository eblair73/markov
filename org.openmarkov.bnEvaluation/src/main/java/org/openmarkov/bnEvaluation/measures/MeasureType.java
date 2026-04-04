/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.bnEvaluation.measures;

/**
 * Enumeration of the evaluation measure types supported by the BN evaluation module.
 */
public enum MeasureType {
    CONFUSIONMATRIX, LOGLIKEHOOD, BAYES, AIC, ENTROPY, BDE, K2, MDL
}
