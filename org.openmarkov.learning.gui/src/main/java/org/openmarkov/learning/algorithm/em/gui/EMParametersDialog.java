/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.learning.algorithm.em.gui;

import org.openmarkov.core.exception.InvalidArgumentException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.io.database.CaseDatabase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.learning.algorithm.em.EMAlgorithm;
import org.openmarkov.learning.core.algorithm.LearningAlgorithm;
import org.openmarkov.learning.gui.AlgorithmConfiguration;
import org.openmarkov.learning.gui.AlgorithmParametersDialog;

import javax.swing.*;
import java.util.ArrayList;

/**
 * Dialog showing the options and parameters of the EM algorithm.
 *
 * @author ibermejo
 */
@SuppressWarnings("serial")
@AlgorithmConfiguration(algorithm = EMAlgorithm.class)
public class EMParametersDialog extends AlgorithmParametersDialog {

    // Variables declaration
    private javax.swing.JButton AcceptButton;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;

    /**
     * Creates new form EMOptionsGUI
     */
    public EMParametersDialog(JFrame parent, boolean modal) {
        super(parent, modal);
        setLocationRelativeTo(parent);
        initComponents();
    }

    private void initComponents() {
        jPanel1 = new javax.swing.JPanel();
        alphaText = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        AcceptButton = new javax.swing.JButton();
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(stringDatabase.getString("Learning.EM.Title"));
        setResizable(false);
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(stringDatabase.getString("Learning.EM.Title")));
        alphaText.setText(alphaParameter);
        jLabel7.setText(stringDatabase.getString("Learning.Alpha") + ":");
        jLabel7.setToolTipText(stringDatabase.getString("Learning.Alpha.Tooltip"));
        AcceptButton.setText(stringDatabase.getString("Learning.Ok"));
        AcceptButton.addActionListener(evt -> {
            try {
                acceptButtonActionPerformed(evt);
            } catch (InvalidArgumentException e) {
                throw new UnrecoverableException(e);
            }
        });
        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                      .addGroup(jPanel1Layout.createSequentialGroup().addGroup(
                                                                                     jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                                                  .addGroup(
                                                                                                          jPanel1Layout.createSequentialGroup()
                                                                                                                       .addContainerGap()
                                                                                                                       .addComponent(jLabel7)
                                                                                                                       .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                                                       .addComponent(alphaText, javax.swing.GroupLayout.PREFERRED_SIZE, 40,
                                                                                                                                     javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                                                  .addGroup(
                                                                                                          jPanel1Layout.createSequentialGroup()
                                                                                                                       .addGap(102, 102, 102)
                                                                                                                       .addComponent(AcceptButton)))
                                                                             .addContainerGap(83, Short.MAX_VALUE)));
        jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                                                           .addGap(56, 56, 56)
                                                                           .addGroup(
                                                                                   jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                                .addComponent(alphaText, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                              javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                .addComponent(jLabel7))
                                                                           .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 39, Short.MAX_VALUE)
                                                                           .addComponent(AcceptButton)
                                                                           .addContainerGap()));
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                layout.createSequentialGroup().addContainerGap()
                      .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE,
                                    javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                      .addContainerGap(36, Short.MAX_VALUE)));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                layout.createSequentialGroup().addContainerGap()
                      .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE,
                                    javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                      .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        pack();
    }

    private void acceptButtonActionPerformed(java.awt.event.ActionEvent evt) {
        applyAlpha();
        this.setVisible(false);
    }

    @Override public String getDescription() {
        return stringDatabase.getString("Learning.Alpha") + ": " + alphaParameter;
    }

    @Override public LearningAlgorithm getInstance(ProbNet probNet, CaseDatabase database) {
        return new EMAlgorithm(probNet, database, Double.parseDouble(alphaParameter));
    }

    @Override public ArrayList<Object> getOptions() {
        ArrayList<Object> options = new ArrayList<>();
        options.add(Double.parseDouble(alphaParameter));
        return options;
    }
}
