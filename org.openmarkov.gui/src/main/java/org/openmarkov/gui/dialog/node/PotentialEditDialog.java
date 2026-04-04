/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.node;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.action.base.CloseEditStackOptions;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.core.SetPotentialEdit;
import org.openmarkov.core.action.core.SetPotentialVariablesEdit;
import org.openmarkov.core.developmentStaticAnalysis.ToCheck;
import org.openmarkov.core.exception.*;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.modelUncertainty.ProbDensFunctionManager;
import org.openmarkov.core.model.network.potential.*;
import org.openmarkov.core.model.network.potential.operation.LinkRestrictionPotentialOperations;
import org.openmarkov.core.model.network.potential.plugin.PotentialUtils;
import org.openmarkov.gui.action.AugmentedPotentialValueEdit;
import org.openmarkov.gui.commonComponents.JComboBoxFunctionRender;
import org.openmarkov.gui.dialog.common.*;
import org.openmarkov.gui.exception.BinomialPotentialWrongValueException;
import org.openmarkov.java.classUtils.ClassUtils;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Dialog box to edit all type of potentials ( TablePotential and TreeADDs ). If
 * the potential is a utility role or uniform type, then no Values panel is
 * displayed. If potential is TreeADDpotential, then graphic edition panel is
 * showed.
 *
 * @author mpalacios
 * @author jmendoza
 * @author ibermejo
 * @version 1.3 cmyago 19/06/2016 - adapted the class to the new utility treatment; minor changes
 */
public class PotentialEditDialog extends OkCancelDialog
        implements ActionListener, PanelResizeEventListener {
    
    /**
     *
     */
    private static final long serialVersionUID = -7344555059488539825L;
    /**
     * The JComboBox object that shows all the potentials types
     */
    private JComboBox<Class<? extends Potential>> potentialTypeComboBox;
    /**
     * The node edited
     */
    private final Node node;
    /**
     * The panel that contains all the common option to potentials
     */
    private JPanel potentialTypePanel;
    
    private PolicyTypePanel pnlPolicyType;
    /**
     * Label for relation type
     */
    private JLabel lblPotentialType;
    /**
     * Panel of the graphic editor
     */
    private PotentialPanel potentialPanel;
    
    
    /**
     * If true, values inside the dialog will not be editable
     */
    private final boolean readOnly;
    
    private JButton reorderVariablesButton;
    
    //For Univariate
    /**
     * The JComboBox object that shows all the potentials types
     */
    
    private JComboBox<String> univariateDistrComboBox;
    /**
     * Label for distribution type
     */
    private JLabel lblUnivariateDistrComboBox;
    
    private JComboBox<String> univariateDistrParametrizationComboBox;
    
    private JLabel lblParametrizationComboBox;
    
    private String previouslySelectedDistributionName = "Exact";
    
    
    private CommentHTMLScrollPane commentPane;
    
    private final @Nullable Potential originalPotential;
    
    /**
     * Creates the dialog.
     */
    public PotentialEditDialog(Window owner, Node node, boolean readOnly, @Nullable Consumer<PotentialEditDialog> potentialInitializer) {
        super(owner);
        this.readOnly = readOnly;
        this.node = node;
        Potential lastPotential = this.node.getPotential();
        if (lastPotential != null) {
            this.originalPotential = this.node.getPotential().deepCopy(this.node.getProbNet());
        } else {
            this.originalPotential = null;
        }
        this.node.getProbNet().getPNESupport().setWithUndo(true);
        this.node.getProbNet().getPNESupport().openNewSubEditHistory();
        if (potentialInitializer != null) {
            potentialInitializer.accept(this);
        }
        List<Potential> potentials = this.node.getPotentials();
        if (!potentials.isEmpty() && potentials.getFirst().getComment() != null && !potentials.getFirst().getComment()
                                                                                              .isEmpty()) {
            this.getCommentPane().setCommentHTMLTextPaneText(potentials.getFirst().getComment());
        }
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        Rectangle bounds = owner.getBounds();
        int width = screenSize.width / 2;
        int height = screenSize.height / 2;
        // center point of the owner window
        int x = bounds.x / 2 - width / 2;
        int y = bounds.y / 2 - height / 2;
        this.setBounds(x, y, width, height);
        this.setLocationRelativeTo(null);
        this.setMinimumSize(new Dimension(width, height / 2));
        this.setResizable(true);
        // Set default title
        this.setTitle("NodePotentialDialog.Title");
        this.configureComponentsPanel();
        this.pack();
    }
    
    public PotentialEditDialog(Window owner, Node node, boolean readOnly) {
        this(owner, node, readOnly, null);
    }
    
    /**
     * Sets up the panel where all components, except the buttons of the buttons
     * panel, will be appear.
     */
    private void configureComponentsPanel() {
        this.getComponentsPanel().setLayout(new BorderLayout(5, 5));
        // getComponentsPanel().setSize(294, 29);
        this.getComponentsPanel().setMaximumSize(new Dimension(180, 40));
        this.getComponentsPanel().add(this.getPotentialTypePanel(), BorderLayout.PAGE_START);
        this.getComponentsPanel().add(this.getPotentialPanel(), BorderLayout.CENTER);
        
        // For univariate
        if (this.showUnivariateDistrComboBox()) {
            this.getUnivariateDistrJCombobox().setVisible(true);
            this.getUnivariateDistrJCombobox().setEnabled(true);
            this.getUnivariateDistrParametrizationJCombobox().setVisible(true);
            this.getUnivariateDistrParametrizationJCombobox().setEnabled(true);
        } else {
            this.getUnivariateDistrJCombobox().setVisible(false);
            this.getUnivariateDistrJCombobox().setEnabled(false);
            this.getUnivariateDistrParametrizationJCombobox().setVisible(false);
            this.getUnivariateDistrParametrizationJCombobox().setEnabled(false);
            
        }
        
        if (this.enableReorderVariableButton()) {
            this.getReorderVariablesButton().setVisible(true);
            this.getReorderVariablesButton().setEnabled(true);
        } else {
            this.getReorderVariablesButton().setVisible(false);
            this.getReorderVariablesButton().setEnabled(false);
        }
        this.getComponentsPanel().add(this.getCommentPane(), BorderLayout.PAGE_END);
        
    }
    
    /**
     * @return label for the type of relations or policy
     */
    private JLabel getPotentialTypeJLabel() {
        if (this.lblPotentialType == null) {
            this.lblPotentialType = new JLabel();
            this.lblPotentialType.setName("jLabelRelationType");
            this.lblPotentialType.setText("a Label");
            this.lblPotentialType.setText(this.stringDatabase.getString("NodeProbsValuesTablePanel.jLabelRelationType.Text"));
        }
        return this.lblPotentialType;
    }
    
    /**
     * @return ComboBox with the types of families of relation to be used
     */
    private JComboBox<Class<? extends Potential>> getPotentialTypeJCombobox() {
        if (this.potentialTypeComboBox == null) {
            Class<? extends Potential> potentialClass = this.node.getPotentials().getFirst().getClass();
            List<Class<? extends Potential>> filteredPotentialNames = new ArrayList<>(PotentialUtils.getFilteredPotentialClasses(this.node));
            filteredPotentialNames.removeIf(availablePotentialClass -> !ClassUtils.isConcrete(availablePotentialClass));
            if (!filteredPotentialNames.contains(potentialClass)) {
                filteredPotentialNames.add(potentialClass);
            }
            filteredPotentialNames.sort(Comparator.comparing(PotentialUtils::getPotentialName));
            this.potentialTypeComboBox = new JComboBox<Class<? extends Potential>>(filteredPotentialNames.toArray(new Class[0]));
            this.potentialTypeComboBox.setRenderer(new JComboBoxFunctionRender<Class<? extends Potential>>(PotentialUtils::getPotentialName));
            this.potentialTypeComboBox.setSelectedItem(potentialClass);
            this.potentialTypeComboBox.setBorder(new LineBorder(UIManager.getColor("List.dropLineColor"), 1, false));
            this.potentialTypeComboBox.setName("jComboBoxRelationType");
            this.potentialTypeComboBox.addActionListener(evt -> this.potentialTypeChanged());
            this.potentialTypeComboBox.setEnabled(!this.readOnly);
        }
        return this.potentialTypeComboBox;
    }
    
    /**
     * Enables or disables the potential type combo box
     *
     * @param enable To indicate if the Potential Type combobox should be enabled
     */
    public void setEnabledPotentialTypeCombobox(boolean enable) {
        this.getPotentialTypeJCombobox().setEnabled(enable);
    }
    
    /**
     * Gets the panel that matches the type of potential to be edited
     *
     * @return the potential panel matching the potential edited.
     */
    private PotentialPanel getPotentialPanel() {
        if (this.potentialPanel == null) {
            this.potentialPanel = PotentialPanelManager.getInstance().createPotentialPanel(this.node);
            this.potentialPanel.setReadOnly(this.readOnly);
            this.potentialPanel.suscribePanelResizeEventListener(this);
        }
        return this.potentialPanel;
    }
    
    @Override public void setTitle(String title) {
        String nodeName = (this.node == null) ? "" : this.node.getName();
        super.setTitle(this.stringDatabase.getString(title) + ": " + nodeName);
    }
    
    /**
     * @return An integer indicating the button clicked by the user when closing
     * this dialog
     */
    public ChosenOption requestValues() {
        // Shows the potentials' options table
        if (this.node.getNodeType() == NodeType.DECISION && this.node.getPolicyType() == PolicyType.OPTIMAL && this.readOnly) {
            this.setEnabledDecisionOptions();
        }
        this.setVisible(true);
        return this.getSelectedOption();
    }
    
    /**
     * @return The panel that indicates the type of the table (and perhaps the
     * type of policy (optimal or imposed))
     */
    private JPanel getPotentialTypePanel() {
        if (this.potentialTypePanel == null) {
            this.potentialTypePanel = new JPanel();
            this.potentialTypePanel.setLayout(new FlowLayout());
            this.potentialTypePanel.setSize(294, 29);
            this.potentialTypePanel.setName("potentialTypePanel");
            this.potentialTypePanel.add(this.getPotentialTypeJLabel());
            this.potentialTypePanel.add(this.getPotentialTypeJCombobox());
            //For Univariate
            this.potentialTypePanel.add(this.getUnivariateDistrTypeJLabel());
            this.potentialTypePanel.add(this.getUnivariateDistrJCombobox());
            this.potentialTypePanel.add(this.getParametrizationComboBoxJLabel());
            this.potentialTypePanel.add(this.getUnivariateDistrParametrizationJCombobox());
            this.potentialTypePanel.add(this.getReorderVariablesButton());
            // potentialTypePanel.add( getPoliticyTypePanel() );
            // /getPoliticyTypePanel().setVisible(false);
            // getPotentialPanel().setEnabled(false);
        }
        return this.potentialTypePanel;
    }
    
    //For Univariate
    
    /**
     * @return label for the type of relations or policy
     */
    private JLabel getUnivariateDistrTypeJLabel() {
        if (this.lblUnivariateDistrComboBox == null) {
            this.lblUnivariateDistrComboBox = new JLabel();
            this.lblUnivariateDistrComboBox.setName("jLabelDistrType");
            this.lblUnivariateDistrComboBox.setText("Distribution");
            //TODO
            //lblDistrType.setText (stringDatabase.getValuesInAString ("NodeProbsValuesTablePanel.jLabelRelationType.Text"));
        }
        return this.lblUnivariateDistrComboBox;
    }
    
    private boolean showUnivariateDistrComboBox() {
        if (!(this.getPotentialPanel() instanceof UnivariateDistrPotentialPanel)) {
            return false;
        }
        ProbDensFunctionManager probDensFunctionManager = ProbDensFunctionManager.getUniqueInstance();
        List<String> distributionUnivariateNames = probDensFunctionManager.getDistributions();
        Collections.sort(distributionUnivariateNames);
        this.univariateDistrComboBox
                .setModel(new DefaultComboBoxModel<String>(distributionUnivariateNames.toArray(new String[0])));
        String univariateName = ((UnivariateDistrPotential) (this.node.getPotentials().getFirst()))
                .getProbDensFunctionUnivariateName();
        this.univariateDistrComboBox.setSelectedItem(univariateName);
        return true;
    }
    
    /**
     * @return The univariate distribution JComboBox
     */
    private JComboBox<String> getUnivariateDistrJCombobox() {
        
        if (this.univariateDistrComboBox == null) {
            
            this.univariateDistrComboBox = new JComboBox<>();
            this.univariateDistrComboBox.setBorder(new LineBorder(UIManager.getColor("List.dropLineColor"), 1, false));
            this.univariateDistrComboBox.setName("jComboBoxDistr");
            this.univariateDistrComboBox.addActionListener(evt -> {
                String univariateName = (String) PotentialEditDialog.this.univariateDistrComboBox.getSelectedItem();
                PotentialEditDialog.this.showUnivariateDistrParametrizationComboBox(univariateName);
            });
            this.univariateDistrComboBox.setEnabled(!this.readOnly);
        }
        return this.univariateDistrComboBox;
        
    }
    
    /**
     * @return The univariate distribution parametrization JComboBox
     */
    private JComboBox<String> getUnivariateDistrParametrizationJCombobox() {
        
        if (this.univariateDistrParametrizationComboBox == null) {
            
            this.univariateDistrParametrizationComboBox = new JComboBox<>();
            this.univariateDistrParametrizationComboBox
                    .setBorder(new LineBorder(UIManager.getColor("List.dropLineColor"), 1, false));
            this.univariateDistrParametrizationComboBox.setName("jComboBoxParametrization");
            this.univariateDistrParametrizationComboBox.addActionListener(evt -> {
                try {
                    this.distributionChanged();
                } catch (DoEditException e) {
                    throw new UnrecoverableException(e);
                }
            });
            this.univariateDistrParametrizationComboBox.setEnabled(!this.readOnly);
        }
        return this.univariateDistrParametrizationComboBox;
        
    }
    
    private void distributionChanged() throws DoEditException {
        String distributionUnivariateName = (String) this.univariateDistrComboBox.getSelectedItem();
        String distributionParameters = (String) this.univariateDistrParametrizationComboBox.getSelectedItem();
        //When we are changing the distribution the first value should be selected
        
        String distributionName = ProbDensFunctionManager.getUniqueInstance()
                                                         .getDistributionName(distributionUnivariateName, distributionParameters);
        if (!this.previouslySelectedDistributionName.equals(distributionName)) {
            new AugmentedPotentialValueEdit(this.node, distributionName).executeEdit();
            this.updatePotentialPanel();
            this.previouslySelectedDistributionName = distributionName;
            
        }
    }
    
    /**
     * @return The parametrization ComboBox JLabel
     */
    private JLabel getParametrizationComboBoxJLabel() {
        if (this.lblParametrizationComboBox == null) {
            this.lblParametrizationComboBox = new JLabel();
            this.lblParametrizationComboBox.setName("jLabelDistrType");
            this.lblParametrizationComboBox.setText("Parametrization");
            //TODO
            //lblParametrizationComboBox.setText (stringDatabase.getValuesInAString ("NodeProbsValuesTablePanel.jLabelRelationType.Text"));
        }
        return this.lblParametrizationComboBox;
    }
    
    /**
     * @return True iff it is enabled
     */
    @ToCheck(reasonKind = ToCheck.ReasonKind.PROBABLE_BUG, reasonDescription = "This always returns true")
    private boolean showUnivariateDistrParametrizationComboBox(String univariateName) {
        // We retrieve the necessary data from the node
        //UNCLEAR this if is Necessary??
        ProbDensFunctionManager probDensFunctionManager = ProbDensFunctionManager.getUniqueInstance();
        List<String[]> parametrizationDataList = probDensFunctionManager.getParametrizations(univariateName);
        List<String> parametrizationNames = parametrizationDataList
                .stream().map(parametrizationData -> parametrizationData[0]).sorted().toList();
        this.univariateDistrParametrizationComboBox
                .setModel(new DefaultComboBoxModel<>(parametrizationNames.toArray(new String[0])));
        String parametrizationName;
        if (!univariateName
                .equals(((UnivariateDistrPotential) this.node.getPotentials()
                                                             .getFirst()).getProbDensFunctionUnivariateName())) {
            parametrizationName = parametrizationNames.getFirst();
        } else {
            parametrizationName = ((UnivariateDistrPotential) this.node.getPotentials().getFirst())
                    .getProbDensFunctionParametrizationName();
        }
        
        this.univariateDistrParametrizationComboBox.setSelectedItem(parametrizationName);
        return true;
    }
    
    
    /**
     * @return The panel that indicates the type of the table (and perhaps the
     * type of policy (optimal or imposed))
     */
    private JButton getReorderVariablesButton() {
        if (this.reorderVariablesButton == null) {
            this.reorderVariablesButton = new JButton(this.stringDatabase.getString("PotentialEditDialog.ReorderVariables.Text"));
            this.reorderVariablesButton.setName("reorderVariablesButton");
            // reorderVariablesButton.setVisible(false);
            this.reorderVariablesButton.addActionListener(this);
        }
        return this.reorderVariablesButton;
    }
    
    /**
     * This method initializes getCommentPane
     *
     * @return a new comment HTML scroll pane.
     */
    private CommentHTMLScrollPane getCommentPane() {
        
        if (this.commentPane == null) {
            this.commentPane = new CommentHTMLScrollPane();
            this.commentPane.setName("commentPane");
            this.commentPane.setPreferredSize(new Dimension(10, 30));
        }
        return this.commentPane;
    }
    
    /**
     * @return PolicyTypePanel with three radio buttons with the types of
     * policy: optimal, deterministic, or probabilistic
     */
    private PolicyTypePanel getPoliticyTypePanel() {
        if (this.pnlPolicyType == null) {
            this.pnlPolicyType = new PolicyTypePanel(this, this.node);
        }
        return this.pnlPolicyType;
    }
    
    private void potentialTypeChanged() {
        Class<? extends Potential> potentialType = (Class<? extends Potential>) this.potentialTypeComboBox.getSelectedItem();
        Potential newPotential = null;
        var currentPotential = this.node.getPotential();
        if (potentialType == TablePotential.class && (currentPotential instanceof TablePotential || currentPotential instanceof UniformPotential)) {
            var variables = this.node.getParents()
                                     .stream()
                                     .map(Node::getVariable)
                                     .collect(Collectors.toCollection(ArrayList::new));
            variables.addFirst(this.node.getVariable());
            newPotential = this.instanciatePotential(potentialType, variables);
        }
        if (newPotential == null && potentialType == TablePotential.class) {
            try {
                newPotential = this.node.getPotentials().getFirst().tableProject(null, null);
            } catch (RuntimeException e) {
            }
        }
        if (newPotential == null && potentialType == TablePotential.class) {
            try {
                newPotential = this.instanciatePotential(potentialType, currentPotential.getVariables())
                                   .tableProject(null, null);
            } catch (RuntimeException e) {
            }
        }
        if (newPotential == null) {
            newPotential = this.instanciatePotential(potentialType, currentPotential.getVariables());
        }
        this.setPotentialInNode(newPotential);
        
        this.updatePotentialPanel();
        this.getComponentsPanel().add(this.getPotentialPanel(), BorderLayout.CENTER);
        this.getComponentsPanel().updateUI();
        this.getComponentsPanel().repaint();
        this.repaint();
        this.pack();
    }
    
    
    /**
     * This method carries out the actions when the user presses the OK button
     * before hiding the dialog.
     *
     * @return true if all the fields are correct.
     */
    @Override
    protected boolean doOkClickBeforeHide() throws BinomialPotentialWrongValueException.ThetaValueIsWrong, BinomialPotentialWrongValueException.NValuesIsWrong, DoEditException {
        switch (this.getPotentialPanel()) {
            case ICIPotentialsTablePanel iciPotentialsTablePanel ->
                    iciPotentialsTablePanel.getICIValuesTable().stopCellEditing();
            case TablePotentialPanel tablePotentialPanel -> tablePotentialPanel.getValuesTable().stopCellEditing();
            default -> {
            }
        }
        this.getPotentialPanel().saveChanges();
        if (this.commentPane.isChanged()) {
            // check if the comment is empty
            String comment = this.commentPane.isEmpty() ? "" : this.commentPane.getCommentText();
            this.node.getPotentials().getFirst().setComment(comment);
        }
        Potential newPotential = this.node.getPotential();
        PNEdit setPotentialEdit = this.generateSetPotentialEdit(this.originalPotential, newPotential);
        this.node.getProbNet().getPNESupport().closeSubEditHistory();
        //Should cancel without triggering Undo
        setPotentialEdit.executeEdit();
        return true;
    }
    
    @Override protected void doCancelClickBeforeHide() {
        this.removePotentialOnClose(this.originalPotential);
        this.getPotentialPanel().close();
        this.node.getProbNet().getPNESupport().closeSubEditHistory(CloseEditStackOptions.FORGET);
    }
    
    /**
     * Update potential panel
     */
    private void updatePotentialPanel() {
        this.getComponentsPanel().remove(this.getPotentialPanel());
        this.potentialPanel.close();
        this.potentialPanel = null;
        // For Univariate
        if (this.showUnivariateDistrComboBox()) {
            this.getUnivariateDistrTypeJLabel().setVisible(true);
            this.getUnivariateDistrJCombobox().setVisible(true);
            this.getUnivariateDistrJCombobox().setEnabled(true);
            this.getParametrizationComboBoxJLabel().setVisible(true);
            this.getUnivariateDistrParametrizationJCombobox().setVisible(true);
            this.getUnivariateDistrParametrizationJCombobox().setEnabled(true);
        } else {
            this.getUnivariateDistrTypeJLabel().setVisible(false);
            this.getUnivariateDistrJCombobox().setVisible(false);
            this.getUnivariateDistrJCombobox().setEnabled(false);
            
            this.getParametrizationComboBoxJLabel().setVisible(false);
            this.getUnivariateDistrParametrizationJCombobox().setVisible(false);
            this.getUnivariateDistrParametrizationJCombobox().setEnabled(false);
        }
        if (this.enableReorderVariableButton()) {
            this.getReorderVariablesButton().setVisible(true);
            this.getReorderVariablesButton().setEnabled(true);
        } else {
            this.getReorderVariablesButton().setVisible(false);
            this.getReorderVariablesButton().setEnabled(false);
        }
        this.getComponentsPanel().add(this.getPotentialPanel(), BorderLayout.CENTER);
        this.getComponentsPanel().updateUI();
        this.getComponentsPanel().repaint();
        this.repaint();
        this.pack();
    }
    
    /**
     * Shows and activates the options related to decision policy
     */
    private void setEnabledDecisionOptions() {
        switch (this.node.getPolicyType()) {
            case OPTIMAL, DETERMINISTIC -> this.getPotentialTypeJCombobox().setEnabled(false);
            case PROBABILISTIC -> {
                Potential potential = this.node.getPotentials().getFirst();
                // TODO definir el comportamiento para los demás tipos de potenciales
                if (potential instanceof UniformPotential || potential instanceof TablePotential) {
                    this.getPotentialTypeJCombobox().setSelectedItem(potential.getClass());
                }
            }
        }
        this.getPoliticyTypePanel().setEnabledDecisionOptions(true);
    }
    
    @Override public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(this.reorderVariablesButton)) {
            try {
                this.actionPerformedReorderVariables();
            } catch (DoEditException ex) {
                throw new UnrecoverableException(ex);
            }
        }
    }
    
    private void actionPerformedReorderVariables() throws DoEditException {
        ReorderVariablesDialog reorderVariablesDialog = new ReorderVariablesDialog(this, this.node);
        if (reorderVariablesDialog.requestValues() != OkCancelDialog.ChosenOption.Ok) {
            return;
        }
        PotentialPanel potentialPanelForAction = this.getPotentialPanel();
        if (!(potentialPanelForAction instanceof ICIPotentialsTablePanel || potentialPanelForAction instanceof TablePotentialPanel)) {
            return;
        }
        List<Variable> newVariables = reorderVariablesDialog.getReorderVariablesPanel().getVariables();
        PNEdit edit = switch (potentialPanelForAction) {
            case TablePotentialPanel tp -> new SetPotentialVariablesEdit(this.node, newVariables);
            case ICIPotentialsTablePanel icip -> new SetPotentialVariablesEdit(this.node, newVariables);
            case null, default -> throw new IllegalStateException("Unexpected value: " + potentialPanelForAction);
        };
        edit.executeEdit();
        this.updatePotentialPanel();
    }
    
    @Override public void panelSizeChanged(PanelResizeEvent event) {
        this.pack();
        this.repaint();
    }
    
    /**
     * This method computes if reorderVariableButton should be enabled
     *
     * @return true if the ReorderVariableButton should be enabled
     */
    private boolean enableReorderVariableButton() {
        // We retrieve the necessary data from the node
        Potential potential = this.node.getPotentials().getFirst();
        int numPotentialVariables = potential.getNumVariables();
        
        return (numPotentialVariables > 2) && this.getPotentialPanel() instanceof ProbabilityTablePanel;
    }
    
    protected Node getNode() {
        return this.node;
    }
    
    private Potential instanciatePotential(Class<? extends Potential> potentialType, List<Variable> variables) {
        Potential currentPotential = this.node.getPotentials().getFirst();
        assert potentialType != null;
        if (potentialType == CycleLengthShift.class) {
            return PotentialUtils.instanciateSafely(potentialType, variables, currentPotential.getPotentialRole(),
                                                    this.node.getProbNet().getCycleLength());
        }
        return PotentialUtils.instanciateSafely(potentialType, variables, currentPotential.getPotentialRole());
    }
    
    /**
     * Specifies how the potential is set on the {@link Node}.
     */
    protected void setPotentialInNode(@NotNull Potential newPotential) {
        LinkRestrictionPotentialOperations.setPotentialWithRestrictions(this.node, newPotential);
    }
    
    /**
     * Specifies how to create the Edit that serves to go back to the previous potential, and forward to the current
     * one. In potential edition this is a {@link SetPotentialEdit} that has both the previous and the current
     * potentials, and in imposing policy this is done with {@link org.openmarkov.gui.action.ImposePolicyEdit}.
     */
    protected @NotNull PNEdit generateSetPotentialEdit(@Nullable Potential originalPotential, @NotNull Potential newPotential) {
        return new SetPotentialEdit(this.node, originalPotential, newPotential);
    }
    
    /**
     * Specifies how to remove the potential currently used while the dialog is in use. A chance node has potentials, so
     * it is removed by setting the original potential. But a decision node has policies, which might be none if no
     * policy was set, so in the case it had one, it uses
     * {@link org.openmarkov.gui.graphic.VisualDecisionNode#setPolicy(Potential)} to set it again, and if it had none,
     * it uses {@link org.openmarkov.gui.graphic.VisualDecisionNode#removePolicy()}.
     */
    protected void removePotentialOnClose(@Nullable Potential originalPotential) {
        if (originalPotential != null) {
            LinkRestrictionPotentialOperations.setPotentialWithRestrictions(this.node, originalPotential);
        }
    }
    
}
