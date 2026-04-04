/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.probmodel.reader;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.jdom2.Element;
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.io.probmodel.exception.PGMXParserException;
import org.openmarkov.core.io.format.annotation.FormatType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.*;

import java.util.Map;
import org.openmarkov.core.model.network.modelUncertainty.ProbDensFunctionManager;
import org.openmarkov.io.probmodel.strings.XMLAttributes;
import org.openmarkov.io.probmodel.strings.XMLTags;

/**
 * @author Manuel Arias
 */
@FormatType(name = "PGMXReader", version = "1.0", extension = "pgmx", description = "OpenMarkov.1.0")
public class PGMXReader_1_0 extends PGMXReader_0_2 {
    
    public PGMXReader_1_0() {
        super();
    }
    
    /**
     * @param probNet      {@code ProbNet}
     * @param xmlPotential {@code Element}
     *
     * @return {@code Potential} read from the XML element
     *
     * @throws PGMXParserException if the potential type is not supported
     */
    @Override protected Potential getPotential(Element xmlPotential, ProbNet probNet) throws PGMXParserException {
        return getPotential(xmlPotential, probNet, PotentialRole.CONDITIONAL_PROBABILITY);
    }
    
    /**
     * @param probNet       {@code ProbNet}
     * @param eXMLPotential {@code Element}
     *
     * @return {@code Potential} read from the XML element
     *
     * @throws PGMXParserException if the potential type is not supported
     */
    @Override protected Potential getPotential(Element eXMLPotential, ProbNet probNet, PotentialRole potentialRole)
            throws PGMXParserException {
        // get type and role of potential
        String sXMLPotentialType = getStringXMLPotentialType(eXMLPotential);
        List<Variable> variables = getReferencedVariables(eXMLPotential, probNet);
        Potential potential = this.autoGetPotential(sXMLPotentialType, eXMLPotential, probNet, potentialRole, variables);
        Element xmlComment = eXMLPotential.getChild(XMLTags.COMMENT.toString());
        if (xmlComment != null) {
            potential.setComment(xmlComment.getText());
        }
        return potential;
    }
    
    
    /**
     * @param xmlPotential {@code Element}
     *
     * @return PotentialRole read from the XML element
     */
    @Override protected PotentialRole getPotentialRole(Element xmlPotential) {
        String xmlPotentialRole = xmlPotential.getAttributeValue(XMLAttributes.ROLE.toString());
        PotentialRole xmlRole;
        if (xmlPotentialRole.equalsIgnoreCase("utility")) {
            xmlRole = PotentialRole.UNSPECIFIED;
        } else {
            xmlRole = PGMXReader_0_2.getPotentialRolByLabel(xmlPotentialRole);
        }
        return xmlRole;
    }
    
    @Override
    protected Map<Class<? extends Potential>, PotentialParser> buildPotentialParsers() {
        Map<Class<? extends Potential>, PotentialParser> map = super.buildPotentialParsers();
        map.put(UnivariateDistrPotential.class,    PGMXReader_1_0::getUnivariateDistrPotential);
        map.put(AugmentedProbTablePotential.class, PGMXReader_1_0::getAugmentedProbTablePotential);
        return map;
    }

    protected static Potential getUnivariateDistrPotential(Element xmlPotential, ProbNet probNet, PotentialRole xmlRole,
                                                           List<Variable> variables) {
        String univariateName = xmlPotential.getAttributeValue(XMLAttributes.DISTRIBUTION.toString());
        String parametrization = xmlPotential.getAttributeValue(XMLAttributes.PARAMETRIZATION.toString());
        Element xmlRootTable = xmlPotential.getChild(XMLTags.PARAMETERS.toString());
        if (xmlRootTable == null) {
            xmlRootTable = xmlPotential.getChild(XMLTags.VALUES.toString());
        }
        double[] table = parseDoubles(xmlRootTable.getTextNormalize());
        
        UnivariateDistrPotential potential;
        if (parametrization != null) {
            potential = new UnivariateDistrPotential(variables, univariateName, parametrization, xmlRole);
        } else {
            var probDensFunctionClass = ProbDensFunctionManager.getUniqueInstance().getProbDensFunctionClass(univariateName);
            potential = new UnivariateDistrPotential(variables, probDensFunctionClass, xmlRole);
        }
        
        List<Variable> vDistributionTable = new ArrayList<>(potential.getFiniteStatesVariables());
        vDistributionTable.addFirst(potential.getPseudoVariableDistribution());
        potential.getAugmentedProbTable().setValues(table);
        if (xmlPotential.getChild(XMLTags.FUNCTIONS.toString()) != null) {
            potential.setDistributionTable(
                    getAugmentedProbTable(xmlPotential, xmlRole, vDistributionTable, variables));
        }
        
        return potential;
    }
    
    // TODO Remove?
    // Answer: Why? Are we getting rid of AugmentedProbTablePotentials?
    protected static Potential getAugmentedProbTablePotential(Element xmlPotential, ProbNet probNet, PotentialRole xmlRole,
                                                              List<Variable> variables) {
        
        AugmentedProbTablePotential potential = new AugmentedProbTablePotential(variables, xmlRole);
        List<Variable> parameterVariables = potential.getParameterVariables();
        List<Variable> finiteStatesVariables = potential.getFiniteStatesVariables();
        
        potential.setAugmentedProbTable(getAugmentedProbTable(xmlPotential, xmlRole, finiteStatesVariables, parameterVariables));
        return potential;
    }
    
    /**
     * @param xmlPotential                     {@code Element}
     * @param xmlRole                          {@code PotentialRole}
     * @param vDistributionTable {@code List} of {@code Variable} of the potential
     * @param variables the variables
     *
     * @return AugmentedProbTable
     */
    protected static AugmentedProbTable getAugmentedProbTable(Element xmlPotential, PotentialRole xmlRole,
                                                              List<Variable> vDistributionTable, List<Variable> variables) {
        String functionsValue = xmlPotential.getChild(XMLTags.FUNCTIONS.toString()).getValue();
        List<String> uncertainParametersList = Pattern.compile("\"(.*?)\"")
                                                      .matcher(functionsValue)
                                                      .results()
                                                      .map(m -> m.group(1))
                                                      .toList();
        VariableExpression[] functionValues = new VariableExpression[uncertainParametersList.size()];
        int i = 0;
        for (String uncertainParameter : uncertainParametersList) {
            functionValues[i++] = new VariableExpression(
                    Stream.concat(vDistributionTable.stream(), variables.stream()).distinct().toList(),
                    uncertainParameter);
        }
        return new AugmentedProbTable(vDistributionTable, xmlRole, functionValues);
    }
    
}
