/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.menutoolbar.menu;

import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.gui.component.LastRecentFilesMenuItem;
import org.openmarkov.gui.configuration.LastOpenFiles;
import org.openmarkov.gui.configuration.LocalPreferences;
import org.openmarkov.gui.dialog.common.RequestDialogger;
import org.openmarkov.gui.loader.element.IconBind;
import org.openmarkov.gui.localize.LocalizedCheckBoxMenuItem;
import org.openmarkov.gui.localize.LocalizedMenuItem;
import org.openmarkov.gui.localize.MenuLocalizer;
import org.openmarkov.gui.menutoolbar.common.ActionCommands;
import org.openmarkov.gui.menutoolbar.common.MenuItemNames;
import org.openmarkov.gui.menutoolbar.common.MenuToolBarBasic;
import org.openmarkov.gui.menutoolbar.common.MenuToolBarBasicImpl;
import org.openmarkov.gui.toolplugin.ToolPlugin;
import org.openmarkov.gui.toolplugin.ToolPluginManager;
import org.openmarkov.gui.window.MainPanel;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Class that manages the main menubar. It configures the default main menubar
 * and chages it according to the state of the application.
 *
 * @author jmendoza
 * @version 1.3 gobispo Add Sensitiviy Analysis tools menu
 */
public class MainMenu extends JMenuBar implements MenuToolBarBasic {
    
    /**
     * Static field for serializable class.
     */
    private static final long serialVersionUID = 8267763502728836096L;
    /**
     * Set of menu items and their default texts.
     */
    final HashMap<JComponent, String> defaultText = new HashMap<JComponent, String>();
    /**
     * Object that represents the menu 'File'.
     */
    private JMenu fileMenu = null;
    /**
     * Object that represents the item 'File - New'.
     */
    private JMenuItem fileNewMenuItem = null;
    /**
     * Object that represents the item 'File - Open'.
     */
    private JMenuItem fileOpenMenuItem = null;
    /**
     * Object that represents the item 'File - Open from URL'.
     */
    private JMenuItem fileOpenURLMenuItem = null;
    /**
     * Object that represents the item 'File - Save'.
     */
    private JMenuItem fileSaveMenuItem = null;
    /**
     * Object that represents the item 'File - Save and Reopen'.
     */
    private JMenuItem fileSaveOpenMenuItem = null;
    /**
     * Object that represents the item 'File - Save as'.
     */
    private JMenuItem fileSaveAsMenuItem = null;
    /**
     * Object that represents the item 'File - Close'.
     */
    private JMenuItem fileCloseMenuItem = null;
    /**
     * Object that represents the item 'File - Load Evidence'.
     */
    private JMenuItem fileLoadEvidenceMenuItem = null;
    /**
     * Object that represents the item 'File - Save Evidence'.
     */
    private JMenuItem fileSaveEvidenceMenuItem = null;
    /**
     * Object that represents the item 'File - Network additionalProperties'.
     */
    private JMenuItem fileNetworkPropertiesMenuItem = null;
    /**
     * Object that represents the item 'File - Exit'.
     */
    private JMenuItem fileExitMenuItem = null;
    /**
     * Object that represents the menu 'Edit'.
     */
    private JMenu editMenu = null;
    /**
     * Object that represents the item 'Edit - Undo'.
     */
    private JMenuItem editUndoMenuItem = null;
    /**
     * Object that represents the item 'Edit - Redo'.
     */
    private JMenuItem editRedoMenuItem = null;
    /**
     * Object that represents the item 'Edit - Cut'.
     */
    private JMenuItem editCutMenuItem = null;
    /**
     * Object that represents the item 'Edit - Copy'.
     */
    private JMenuItem editCopyMenuItem = null;
    /**
     * Object that represents the item 'Edit - Paste'.
     */
    private JMenuItem editPasteMenuItem = null;
    /**
     * Object that represents the item 'Edit - Remove'.
     */
    private JMenuItem editRemoveMenuItem = null;
    /**
     * Object that represents the item 'Edit - Select all'.
     */
    private JMenuItem editSelectAllMenuItem = null;
    /**
     * Object that represents the item 'Edit - Object selection'.
     */
    private JCheckBoxMenuItem editObjectSelectionMenuItem = null;
    /**
     * Object that represents the item 'Edit - Chance nodes creation'.
     */
    private JCheckBoxMenuItem editChanceCreationMenuItem = null;
    /**
     * Object that represents the item 'Edit - Decision nodes creation'.
     */
    private JCheckBoxMenuItem editDecisionCreationMenuItem = null;
    /**
     * Object that represents the item 'Edit - Utility nodes creation'.
     */
    private JCheckBoxMenuItem editUtilityCreationMenuItem = null;
    /**
     * Object that represents the item 'Edit - Links creation'.
     */
    private JCheckBoxMenuItem editLinkCreationMenuItem = null;
    /**
     * Object that represents the item 'Edit - Instance creation'.
     */
    private JCheckBoxMenuItem editInstanceCreationMenuItem = null;
    /**
     * Object used to make autoexclusive the different select options.
     */
    private final ButtonGroup groupEditOptions = new ButtonGroup();
    /**
     * Object that represents the item 'Edit - Node additionalProperties'.
     */
    private JMenuItem editNodePropertiesMenuItem = null;
    /**
     * Object that represents the item 'Edit - Node Relation Table'.
     */
    private JMenuItem editRelationMenuItem = null;
    /**
     * Object that represents the item 'Edit - Link additionalProperties'.
     */
    private JMenuItem editLinkPropertiesMenuItem = null;
    /**
     * Object that represents the menu 'Inference'.
     */
    private JMenu inferenceMenu = null;
    /**
     * Object that represents the menu 'View'.
     */
    private JMenu viewMenu;
    /**
     * Object that represents the item 'Inference - Switch to Edition mode'.
     */
    private JMenuItem switchWorkingMode = null;
    /**
     * Object that represents the item 'Inference - Propagation Options'.
     */
    private JMenuItem propagationOptionsMenuItem = null;
    /**
     * Object that represents the item "Inference - Inference Options".
     */
    private JMenuItem inferenceOptionsItem = null;
    /**
     * Object that represents the item 'Inference - Create New Evidence Case'.
     */
    private JMenuItem inferenceCreateNewEvidenceCaseMenuItem = null;
    /**
     * Object that represents the item 'Inference - Go To First Evidence Case'.
     */
    private JMenuItem inferenceGoToFirstEvidenceCaseMenuItem = null;
    /**
     * Object that represents the item 'Inference - Go To Previous Evidence
     * Case'.
     */
    private JMenuItem inferenceGoToPreviousEvidenceCaseMenuItem = null;
    /**
     * Object that represents the item 'Inference - Go To Next Evidence Case'.
     */
    private JMenuItem inferenceGoToNextEvidenceCaseMenuItem = null;
    /**
     * Object that represents the item 'Inference - Go To Last Evidence Case'.
     */
    private JMenuItem inferenceGoToLastEvidenceCaseMenuItem = null;
    /**
     * Object that represents the item 'Inference - Clear Out All Evidence
     * Cases'.
     */
    private JMenuItem inferenceClearEvidenceCasesMenuItem = null;
    /**
     * Object that represents the item 'Inference - PropagateEvidence'.
     */
    private JMenuItem inferencePropagateEvidenceMenuItem = null;
    /**
     * Object that represents the item 'Inference - ExpandNode'.
     */
    private JMenuItem inferenceExpandNodeMenuItem = null;
    /**
     * Object that represents the item 'Inference - ContractNode'.
     */
    private JMenuItem inferenceContractNodeMenuItem = null;
    /**
     * Object that represents the item 'Inference - RemoveAllFindings'.
     */
    private JMenuItem inferenceRemoveAllFindingsMenuItem = null;
    
    /**
     * Object that represents the menu 'View - Nodes'.
     */
    private JMenu viewNodesMenu = null;
    /**
     * Object that represents the item 'View - Nodes - ByName'.
     */
    private JCheckBoxMenuItem viewNodesByNameMenuItem = null;
    /**
     * Object that represents the item 'View - Nodes - ByTitle'.
     */
    private JCheckBoxMenuItem viewNodesByTitleMenuItem = null;
    /**
     * Object used to make autoexclusive the options 'ByName' and 'ByTitle'.
     */
    private final ButtonGroup groupByNameByTitle = new ButtonGroup();
    
    /**
     * Object that represents the menu 'Tools'.
     */
    private @Nullable JMenu toolsMenu = null;
    /**
     * Object that represents the item 'Tools - Configuration'.
     */
    private JMenuItem toolsConfigurationMenuItem = null;
    /**
     * Object that represents the menu 'Help'.
     */
    private JMenu helpMenu = null;
    /**
     * Object that represents the item 'Help - Help'.
     */
    private JMenuItem helpOpenHelpMenuItem = null;
    /**
     * Object that represents the item 'Help - ChangeLanguage'.
     */
    private JMenuItem helpOpenChangeLanguageMenuItem = null;
    /**
     * Object that represents the item 'Help - About'.
     */
    private JMenuItem helpOpenAboutMenuItem = null;
    /**
     * Object that represents the item 'Help - Shortcuts'.
     */
    private JMenuItem helpOpenShortcutsMenuItem = null;
    
    private final MainPanel mainPanel;
    /**
     * Object that listen to the user's actions.
     */
    private final ActionListener listener;
    
    // private HashMap<JComponent, String> dynamicActions = new
    // HashMap<JComponent, String>();
    
    private static final double UI_SCALE_MAX = 5.0;
    private static final double UI_SCALE_MIN = 0.5;
    
    /**
     * Creates a new instance.
     *
     * @param mainPanel the main panel
     * @param newListener listener of the user's actions.
     */
    public MainMenu(MainPanel mainPanel, ActionListener newListener) {
        this.mainPanel = mainPanel;
        listener = newListener;
        reInitialize();
    }
    
    /**
     * This method initializes the instance.
     */
    public void reInitialize() {
        this.toolsMenu = null;
        removeAll();
        add(getFileMenu());
        add(getEditMenu());
        add(getInferenceMenu());
        add(getViewMenu());
        add(getToolsMenu());
        // add(getOptionsMenu()); //FOR FUTURE USE
        add(getHelpingMenu());
    }
    
    
    /**
     * This method initializes viewMenu.
     *
     * @return a new menu 'View'.
     */
    private JMenu getViewMenu() {
        
        if (viewMenu == null) {
            viewMenu = new JMenu();
            viewMenu.setName(MenuItemNames.VIEW_MENU);
            viewMenu.setText(MenuLocalizer.getLabel(MenuItemNames.VIEW_MENU));
            viewMenu.setMnemonic(MenuLocalizer.getMnemonic(MenuItemNames.VIEW_MENU).charAt(0));
            LocalizedMenuItem goNextTab = new LocalizedMenuItem(MenuItemNames.VIEW_GO_NEXT_TAB, null,
                                                                null, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
            viewMenu.add(goNextTab);
            goNextTab.addActionListener(e -> {
                var networksTabPanel = this.mainPanel.getNetworksTabPanel();
                if (networksTabPanel.getTabCount() <= 1) {
                    return;
                }
                int nextIndex = networksTabPanel.getSelectedIndex() + 1;
                if (nextIndex >= networksTabPanel.getTabCount()) {
                    nextIndex = 0;
                }
                networksTabPanel.setSelectedIndex(nextIndex);
            });
            
            LocalizedMenuItem goPreviousTab = new LocalizedMenuItem(MenuItemNames.VIEW_GO_PREVIOUS_TAB, null,
                                                                    null, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
            viewMenu.add(goPreviousTab);
            goPreviousTab.addActionListener(e -> {
                var networksTabPanel = this.mainPanel.getNetworksTabPanel();
                if (networksTabPanel.getTabCount() <= 1) {
                    return;
                }
                int previous = networksTabPanel.getSelectedIndex() - 1;
                if (previous == -1) {
                    previous = networksTabPanel.getTabCount() - 1;
                }
                networksTabPanel.setSelectedIndex(previous);
            });
            
            LocalizedMenuItem changeScale = new LocalizedMenuItem(MenuItemNames.VIEW_CHANGE_SCALE, null);
            viewMenu.add(changeScale);
            changeScale.addActionListener(e -> {
                RequestDialogger
                        .of(this.mainPanel.mainGUI, new JTextField(LocalPreferences.UI_SCALE.get().toString()))
                        .validating((textField, validator) -> {
                            if (!validator.addErrorWhen(!stringIsDouble(textField.getText()), "The value must be a number")) {
                                validator.addErrorWhen(Double.parseDouble(textField.getText()) > UI_SCALE_MAX, "Scale should not be greater than " + UI_SCALE_MAX);
                                validator.addErrorWhen(Double.parseDouble(textField.getText()) < UI_SCALE_MIN, "Scale should not be lower than " + UI_SCALE_MIN);
                            }
                        })
                        .mapInputAs(new TypeToken<>() {
                        }, textField -> Double.parseDouble(textField.getText()))
                        .withTitle("Change scale")
                        .onOk(num -> {
                            LocalPreferences.UI_SCALE.set(num);
                            JOptionPane.showMessageDialog(this.mainPanel.mainGUI, "Scale changed to " + num + "." + System.lineSeparator() + "Your changes will be applied in the next reset.",
                                                          "Changes accepted", JOptionPane.INFORMATION_MESSAGE, IconBind.OPENMARKOV_LOGO_16.icon());
                        })
                        .request();
            });
        }
        
        return viewMenu;
        
    }
    
    
    private static boolean stringIsDouble(String string) {
        try {
            Double.parseDouble(string);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }
    
    /**
     * This method initializes fileMenu.
     *
     * @return a new File menu.
     */
    private JMenu getFileMenu() {
        
        if (fileMenu == null) {
            fileMenu = new JMenu();
            fileMenu.setName(MenuItemNames.FILE_MENU);
            fileMenu.setText(MenuLocalizer.getLabel(MenuItemNames.FILE_MENU));
            fileMenu.setMnemonic(MenuLocalizer.getMnemonic(MenuItemNames.FILE_MENU).charAt(0));
        }
        rechargeFileMenu();
        
        return fileMenu;
        
    }
    
    /**
     * This method initializes fileNewMenuItem.
     *
     * @return a new item 'File - New'.
     */
    private JMenuItem getFileNewMenuItem() {
        
        if (fileNewMenuItem == null) {
            fileNewMenuItem = new LocalizedMenuItem(MenuItemNames.FILE_NEW_MENUITEM, ActionCommands.NEW_NETWORK.getCommandName(),
                                                    IconBind.NEW_ENABLED, KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
            fileNewMenuItem.addActionListener(listener);
        }
        
        return fileNewMenuItem;
        
    }
    
    /**
     * This method initializes fileOpenMenuItem.
     *
     * @return a new item 'File - Open'.
     */
    private JMenuItem getFileOpenMenuItem() {
        
        if (fileOpenMenuItem == null) {
            fileOpenMenuItem = new LocalizedMenuItem(MenuItemNames.FILE_OPEN_MENUITEM, ActionCommands.OPEN_NETWORK.getCommandName(),
                                                     IconBind.OPEN_ENABLED, KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
            fileOpenMenuItem.addActionListener(listener);
        }
        
        return fileOpenMenuItem;
        
    }
    
    /**
     * This method initializes fileOpenURLMenuItem.
     *
     * @return a new item 'File - Open'.
     */
    private JMenuItem getFileOpenURLMenuItem() {
        
        if (fileOpenURLMenuItem == null) {
            fileOpenURLMenuItem = new LocalizedMenuItem(MenuItemNames.FILE_OPEN_URL_MENUITEM,
                                                        ActionCommands.OPEN_NETWORK_URL.getCommandName(), IconBind.OPEN_URL_ENABLED,
                                                        KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK + InputEvent.ALT_DOWN_MASK));
            fileOpenURLMenuItem.addActionListener(listener);
        }
        
        return fileOpenURLMenuItem;
        
    }
    
    /**
     * This method initializes fileSaveMenuItem.
     *
     * @return a new item 'File - Save'.
     */
    private JMenuItem getFileSaveMenuItem() {
        
        if (fileSaveMenuItem == null) {
            fileSaveMenuItem = new LocalizedMenuItem(MenuItemNames.FILE_SAVE_MENUITEM, ActionCommands.SAVE_NETWORK.getCommandName(),
                                                     IconBind.SAVE_ENABLED, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
            fileSaveMenuItem.addActionListener(listener);
        }
        
        return fileSaveMenuItem;
        
    }
    
    /**
     * This method initializes fileSaveMenuItem.
     *
     * @return a new item 'File - Save'.
     */
    private JMenuItem getFileSaveOpenMenuItem() {
        
        if (fileSaveOpenMenuItem == null) {
            fileSaveOpenMenuItem = new LocalizedMenuItem(MenuItemNames.FILE_SAVE_OPEN_MENUITEM,
                                                         ActionCommands.SAVE_OPEN_NETWORK.getCommandName(), IconBind.SAVE_ENABLED,
                                                         KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK));
            fileSaveOpenMenuItem.addActionListener(listener);
        }
        
        return fileSaveOpenMenuItem;
        
    }
    
    /**
     * This method initializes fileSaveAsMenuItem.
     *
     * @return a new item 'File - Save as'.
     */
    private JMenuItem getFileSaveAsMenuItem() {
        
        if (fileSaveAsMenuItem == null) {
            fileSaveAsMenuItem = new LocalizedMenuItem(MenuItemNames.FILE_SAVEAS_MENUITEM,
                                                       ActionCommands.SAVEAS_NETWORK.getCommandName(), IconBind.SAVE_ENABLED,
                                                       KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
            fileSaveAsMenuItem.addActionListener(listener);
        }
        
        return fileSaveAsMenuItem;
        
    }
    
    /**
     * This method initializes fileCloseMenuItem.
     *
     * @return a new item 'File - Close'.
     */
    private JMenuItem getFileCloseMenuItem() {
        
        if (fileCloseMenuItem == null) {
            fileCloseMenuItem = new LocalizedMenuItem(MenuItemNames.FILE_CLOSE_MENUITEM, ActionCommands.CLOSE_TAB.getCommandName(),
                                                      null,
                                                      KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK));
            fileCloseMenuItem.addActionListener(listener);
        }
        
        return fileCloseMenuItem;
        
    }
    
    /**
     * This method initializes fileLoadEvidenceMenuItem.
     *
     * @return a new item 'File - Load Evidence'.
     */
    private JMenuItem getFileLoadEvidenceMenuItem() {
        
        if (fileLoadEvidenceMenuItem == null) {
            fileLoadEvidenceMenuItem = new LocalizedMenuItem(MenuItemNames.FILE_LOAD_EVIDENCE_MENUITEM,
                                                             ActionCommands.LOAD_EVIDENCE.getCommandName());
            fileLoadEvidenceMenuItem.addActionListener(listener);
        }
        
        return fileLoadEvidenceMenuItem;
        
    }
    
    /**
     * This method initializes fileSaveEvidenceMenuItem.
     *
     * @return a new item 'File - Save Evidence'.
     */
    private JMenuItem getFileSaveEvidenceMenuItem() {
        
        if (fileSaveEvidenceMenuItem == null) {
            fileSaveEvidenceMenuItem = new LocalizedMenuItem(MenuItemNames.FILE_SAVE_EVIDENCE_MENUITEM,
                                                             ActionCommands.SAVE_EVIDENCE.getCommandName());
            fileSaveEvidenceMenuItem.addActionListener(listener);
        }
        
        return fileSaveEvidenceMenuItem;
        
    }
    
    /**
     * This method initializes fileNetworkPropertiesMenuItem.
     *
     * @return a new item 'File - Network additionalProperties'.
     */
    private JMenuItem getFileNetworkPropertiesMenuItem() {
        if (fileNetworkPropertiesMenuItem == null) {
            fileNetworkPropertiesMenuItem = new LocalizedMenuItem(MenuItemNames.FILE_NETWORKPROPERTIES_MENUITEM,
                                                                  ActionCommands.NETWORK_PROPERTIES.getCommandName(), null,
                                                                  KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK)
            );
            fileNetworkPropertiesMenuItem.addActionListener(listener);
        }
        return fileNetworkPropertiesMenuItem;
        
    }
    
    /**
     * This method initializes fileExitMenuItem.
     *
     * @return a new item 'File - Exit'.
     */
    private JMenuItem getFileExitMenuItem() {
        
        if (fileExitMenuItem == null) {
            fileExitMenuItem = new LocalizedMenuItem(MenuItemNames.FILE_EXIT_MENUITEM, ActionCommands.EXIT_APPLICATION.getCommandName(), null,
                                                     KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
            fileExitMenuItem.addActionListener(listener);
        }
        
        return fileExitMenuItem;
        
    }
    
    /**
     * This method retrieves the LastOpenFiles and show them in the File Menu
     *
     * @return the result
     */
    private List<LastRecentFilesMenuItem> getLastOpenFiles() {
        var lastOpenFilesItems = new ArrayList<LastRecentFilesMenuItem>(LocalPreferences.LAST_OPEN_NETWORKS_FILES.get()
                                                                                                                 .size());
        int index = 0;
        for (String recentFile : LocalPreferences.LAST_OPEN_NETWORKS_FILES.get()) {
            LastRecentFilesMenuItem item = new LastRecentFilesMenuItem();
            item.setName("lastRecentFilesMenuItem" + index);
            item.setText(recentFile);
            ActionCommands command = ActionCommands.openLastFileCommandAt(index);
            if (command != null) {
                item.setActionCommand(command.getCommandName());
            }
            item.addActionListener(listener);
            lastOpenFilesItems.add(item);
            index += 1;
        }
        return lastOpenFilesItems;
    }
    
    /**
     * This method resets the LastOpenFiles set of items in the File Menu
     */
    public void rechargeFileMenu() {
        fileMenu.removeAll();
        fileMenu.add(getFileNewMenuItem());
        fileMenu.add(getFileOpenMenuItem());
        fileMenu.add(getFileOpenURLMenuItem());
        fileMenu.addSeparator();
        fileMenu.add(getFileSaveMenuItem());
        fileMenu.add(getFileSaveOpenMenuItem());
        fileMenu.add(getFileSaveAsMenuItem());
        fileMenu.add(getFileCloseMenuItem());
        fileMenu.addSeparator();
        fileMenu.add(getFileNetworkPropertiesMenuItem());
        fileMenu.add(getFileLoadEvidenceMenuItem());
        //fileMenu.add(getFileSaveEvidenceMenuItem ());
        if (LastOpenFiles.existLastOpenFiles()) {
            var lastOpenFilesItems = getLastOpenFiles();
            fileMenu.addSeparator();
            lastOpenFilesItems.forEach(fileMenu::add);
        }
        fileMenu.addSeparator();
        fileMenu.add(getFileExitMenuItem());
        
        fileMenu.repaint();
    }
    
    /**
     * This method initializes editMenu.
     *
     * @return a new Edit menu.
     */
    private JMenu getEditMenu() {
        
        if (editMenu == null) {
            editMenu = new JMenu();
            editMenu.setName(MenuItemNames.EDIT_MENU);
            editMenu.setText(MenuLocalizer.getLabel(MenuItemNames.EDIT_MENU));
            editMenu.setMnemonic(MenuLocalizer.getMnemonic(MenuItemNames.EDIT_MENU).charAt(0));
            editMenu.add(getEditCutMenuItem());
            editMenu.add(getEditCopyMenuItem());
            editMenu.add(getEditPasteMenuItem());
            editMenu.add(getEditRemoveMenuItem());
            editMenu.addSeparator();
            editMenu.add(getEditUndoMenuItem());
            editMenu.add(getEditRedoMenuItem());
            editMenu.addSeparator();
            editMenu.add(getEditSelectAllMenuItem());
            editMenu.addSeparator();
            editMenu.add(getEditObjectSelectionMenuItem());
            editMenu.add(getEditChanceCreationMenuItem());
            editMenu.add(getEditDecisionCreationMenuItem());
            editMenu.add(getEditUtilityCreationMenuItem());
            editMenu.add(getEditLinkCreationMenuItem());
            editMenu.addSeparator();
            editMenu.add(getEditNodePropertiesMenuItem());
            editMenu.add(getEditRelationMenuItem());
            /*
             * This item must be added to the menu when is active the
             * possibility of editing the additionalProperties of a link in
             * future versions.
             */
            // editMenu.add(getEditLinkPropertiesMenuItem());
            getEditLinkPropertiesMenuItem();
        }
        
        return editMenu;
        
    }
    
    /**
     * This method initializes editCutMenuItem.
     *
     * @return a new item 'Edit - Cut'.
     */
    private JMenuItem getEditCutMenuItem() {
        
        if (editCutMenuItem == null) {
            editCutMenuItem = new LocalizedMenuItem(MenuItemNames.EDIT_CUT_MENUITEM, ActionCommands.CLIPBOARD_CUT.getCommandName(),
                                                    IconBind.CUT_ENABLED, KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));
            editCutMenuItem.addActionListener(listener);
        }
        
        return editCutMenuItem;
        
    }
    
    /**
     * This method initializes editCopyMenuItem.
     *
     * @return a new item 'Edit - Copy'.
     */
    private JMenuItem getEditCopyMenuItem() {
        
        if (editCopyMenuItem == null) {
            editCopyMenuItem = new LocalizedMenuItem(MenuItemNames.EDIT_COPY_MENUITEM, ActionCommands.CLIPBOARD_COPY.getCommandName(),
                                                     IconBind.COPY_ENABLED, KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
            editCopyMenuItem.addActionListener(listener);
        }
        
        return editCopyMenuItem;
        
    }
    
    /**
     * This method initializes editPasteMenuItem.
     *
     * @return a new item 'Edit - Paste'.
     */
    private JMenuItem getEditPasteMenuItem() {
        
        if (editPasteMenuItem == null) {
            editPasteMenuItem = new LocalizedMenuItem(MenuItemNames.EDIT_PASTE_MENUITEM, ActionCommands.CLIPBOARD_PASTE.getCommandName(),
                                                      IconBind.PASTE_ENABLED, KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
            editPasteMenuItem.addActionListener(listener);
        }
        
        return editPasteMenuItem;
        
    }
    
    /**
     * This method initializes editRemoveMenuItem.
     *
     * @return a new item 'Edit - Remove'.
     */
    private JMenuItem getEditRemoveMenuItem() {
        
        if (editRemoveMenuItem == null) {
            editRemoveMenuItem = new LocalizedMenuItem(MenuItemNames.EDIT_REMOVE_MENUITEM,
                                                       ActionCommands.OBJECT_REMOVAL.getCommandName(), IconBind.REMOVE_ENABLED,
                                                       KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
            editRemoveMenuItem.addActionListener(listener);
        }
        
        return editRemoveMenuItem;
        
    }
    
    /**
     * This method initializes editUndoMenuItem.
     *
     * @return a new item 'Edit - Undo'.
     */
    private JMenuItem getEditUndoMenuItem() {
        
        if (editUndoMenuItem == null) {
            editUndoMenuItem = new LocalizedMenuItem(MenuItemNames.EDIT_UNDO_MENUITEM, ActionCommands.UNDO.getCommandName(),
                                                     IconBind.UNDO_ENABLED, KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
            editUndoMenuItem.addActionListener(listener);
        }
        
        return editUndoMenuItem;
        
    }
    
    /**
     * This method initializes editRedoMenuItem.
     *
     * @return a new item 'Edit - Redo'.
     */
    private JMenuItem getEditRedoMenuItem() {
        
        if (editRedoMenuItem == null) {
            editRedoMenuItem = new LocalizedMenuItem(MenuItemNames.EDIT_REDO_MENUITEM, ActionCommands.REDO.getCommandName(),
                                                     IconBind.REDO_ENABLED, KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK));
            editRedoMenuItem.addActionListener(listener);
        }
        
        return editRedoMenuItem;
        
    }
    
    /**
     * This method initializes editSelectAllMenuItem.
     *
     * @return a new item 'Edit - Select all'.
     */
    private JMenuItem getEditSelectAllMenuItem() {
        
        if (editSelectAllMenuItem == null) {
            editSelectAllMenuItem = new LocalizedMenuItem(MenuItemNames.EDIT_SELECTALL_MENUITEM,
                                                          ActionCommands.SELECT_ALL.getCommandName());
            editSelectAllMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK));
            editSelectAllMenuItem.addActionListener(listener);
        }
        
        return editSelectAllMenuItem;
        
    }
    
    /**
     * This method initializes editObjectSelectionMenuItem.
     *
     * @return a new item 'Edit - Object selection'.
     */
    private JCheckBoxMenuItem getEditObjectSelectionMenuItem() {
        
        if (editObjectSelectionMenuItem == null) {
            editObjectSelectionMenuItem = new LocalizedCheckBoxMenuItem(MenuItemNames.EDIT_MODE_SELECTION_MENUITEM,
                                                                        ActionCommands.OBJECT_SELECTION.getCommandName(), IconBind.SELECTION_ENABLED);
            editObjectSelectionMenuItem.addActionListener(listener);
            groupEditOptions.add(editObjectSelectionMenuItem);
        }
        
        return editObjectSelectionMenuItem;
        
    }
    
    /**
     * This method initializes editChanceCreationMenuItem.
     *
     * @return a new item 'Edit - Chance nodes creation'.
     */
    private JCheckBoxMenuItem getEditChanceCreationMenuItem() {
        
        if (editChanceCreationMenuItem == null) {
            editChanceCreationMenuItem = new LocalizedCheckBoxMenuItem(MenuItemNames.EDIT_MODE_CHANCE_MENUITEM,
                                                                       ActionCommands.CHANCE_CREATION.getCommandName(), IconBind.CHANCE_ENABLED);
            editChanceCreationMenuItem.addActionListener(listener);
            groupEditOptions.add(editChanceCreationMenuItem);
        }
        
        return editChanceCreationMenuItem;
        
    }
    
    /**
     * This method initializes editDecisionCreationMenuItem.
     *
     * @return a new item 'Edit - Decision nodes creation'.
     */
    private JCheckBoxMenuItem getEditDecisionCreationMenuItem() {
        
        if (editDecisionCreationMenuItem == null) {
            editDecisionCreationMenuItem = new LocalizedCheckBoxMenuItem(MenuItemNames.EDIT_MODE_DECISION_MENUITEM,
                                                                         ActionCommands.DECISION_CREATION.getCommandName(), IconBind.DECISION_ENABLED);
            editDecisionCreationMenuItem.addActionListener(listener);
            groupEditOptions.add(editDecisionCreationMenuItem);
        }
        
        return editDecisionCreationMenuItem;
        
    }
    
    /**
     * This method initializes editUtilityCreationMenuItem.
     *
     * @return a new item 'Edit - Utility nodes creation'.
     */
    private JCheckBoxMenuItem getEditUtilityCreationMenuItem() {
        
        if (editUtilityCreationMenuItem == null) {
            editUtilityCreationMenuItem = new LocalizedCheckBoxMenuItem(MenuItemNames.EDIT_MODE_UTILITY_MENUITEM,
                                                                        ActionCommands.UTILITY_CREATION.getCommandName(), IconBind.UTILITY_ENABLED);
            editUtilityCreationMenuItem.addActionListener(listener);
            groupEditOptions.add(editUtilityCreationMenuItem);
        }
        
        return editUtilityCreationMenuItem;
        
    }
    
    /**
     * This method initializes editLinkCreationMenuItem.
     *
     * @return a new item 'Edit - Links creation'.
     */
    private JCheckBoxMenuItem getEditLinkCreationMenuItem() {
        
        if (editLinkCreationMenuItem == null) {
            editLinkCreationMenuItem = new LocalizedCheckBoxMenuItem(MenuItemNames.EDIT_MODE_LINK_MENUITEM,
                                                                     ActionCommands.LINK_CREATION.getCommandName(), IconBind.LINK_ENABLED);
            editLinkCreationMenuItem.addActionListener(listener);
            groupEditOptions.add(editLinkCreationMenuItem);
        }
        
        return editLinkCreationMenuItem;
        
    }
    
    /**
     * This method initializes editNodePropertiesMenuItem.
     *
     * @return a new item 'Edit - Node additionalProperties'.
     */
    private JMenuItem getEditNodePropertiesMenuItem() {
        
        if (editNodePropertiesMenuItem == null) {
            editNodePropertiesMenuItem = new LocalizedMenuItem(MenuItemNames.EDIT_NODEPROPERTIES_MENUITEM,
                                                               ActionCommands.NODE_PROPERTIES.getCommandName());
            editNodePropertiesMenuItem.addActionListener(listener);
        }
        
        return editNodePropertiesMenuItem;
        
    }
    
    /**
     * This method initializes editNodeRelationMenuItem.
     *
     * @return a new item 'Edit - Node Relation Table'.
     */
    private JMenuItem getEditRelationMenuItem() {
        
        if (editRelationMenuItem == null) {
            editRelationMenuItem = new LocalizedMenuItem(MenuItemNames.EDIT_NODERELATION_MENUITEM,
                                                         ActionCommands.EDIT_POTENTIAL.getCommandName());
            editRelationMenuItem.addActionListener(listener);
        }
        
        return editRelationMenuItem;
        
    }
    
    /**
     * This method initializes editLinkPropertiesMenuItem.
     *
     * @return a new item 'Edit - Link additionalProperties'.
     */
    private JMenuItem getEditLinkPropertiesMenuItem() {
        
        if (editLinkPropertiesMenuItem == null) {
            editLinkPropertiesMenuItem = new LocalizedMenuItem(MenuItemNames.EDIT_LINKPROPERTIES_MENUITEM,
                                                               ActionCommands.LINK_PROPERTIES.getCommandName());
            editLinkPropertiesMenuItem.addActionListener(listener);
        }
        return editLinkPropertiesMenuItem;
    }
    
    /**
     * This method initializes inferenceMenu.
     *
     * @return a new Inference menu.
     */
    private JMenu getInferenceMenu() {
        if (inferenceMenu == null) {
            inferenceMenu = new JMenu();
            inferenceMenu.setName(MenuItemNames.INFERENCE_MENU);
            inferenceMenu.setText(MenuLocalizer.getLabel(MenuItemNames.INFERENCE_MENU));
            inferenceMenu.setMnemonic(MenuLocalizer.getMnemonic(MenuItemNames.INFERENCE_MENU).charAt(0));
            inferenceMenu.add(getSwitchWorkingMode());
            inferenceMenu.addSeparator();
            inferenceMenu.add(getPropagationOptionsMenuItem());
            
            inferenceMenu.add(getInferenceOptionsItem());
            
            inferenceMenu.addSeparator();
            inferenceMenu.add(getInferenceCreateNewEvidenceCaseMenuItem());
            inferenceMenu.add(getInferenceClearOutAllEvidenceCasesMenuItem());
            inferenceMenu.addSeparator();
            inferenceMenu.add(getInferenceGoToFirstEvidenceCaseMenuItem());
            inferenceMenu.add(getInferenceGoToPreviousEvidenceCaseMenuItem());
            inferenceMenu.add(getInferenceGoToNextEvidenceCaseMenuItem());
            inferenceMenu.add(getInferenceGoToLastEvidenceCaseMenuItem());
            inferenceMenu.addSeparator();
            inferenceMenu.add(getInferenceExpandNodeMenuItem());
            inferenceMenu.add(getInferenceContractNodeMenuItem());
            inferenceMenu.addSeparator();
            inferenceMenu.add(getInferenceRemoveAllFindingsMenuItem());
        }
        return inferenceMenu;
    }
    
    private JMenuItem getInferenceOptionsItem() {
        if (inferenceOptionsItem == null) {
            inferenceOptionsItem = new LocalizedMenuItem(MenuItemNames.INFERENCE_OPTIONS_MENUITEM,
                                                         ActionCommands.INFERENCE_OPTIONS.getCommandName());
            inferenceOptionsItem.addActionListener(listener);
        }
        return inferenceOptionsItem;
    }
    
    /**
     * This method adds the item 'Propagate Now' on the inference menu.
     */
    public void addPropagateNowItem() {
        if (inferenceMenu != null) {
            inferenceMenu.removeAll();
            inferenceMenu.add(getSwitchWorkingMode());
            inferenceMenu.addSeparator();
            inferenceMenu.add(getPropagationOptionsMenuItem());
            inferenceMenu.add(getInferenceOptionsItem());
            
            inferenceMenu.addSeparator();
            inferenceMenu.add(getInferenceCreateNewEvidenceCaseMenuItem());
            inferenceMenu.add(getInferenceClearOutAllEvidenceCasesMenuItem());
            inferenceMenu.addSeparator();
            inferenceMenu.add(getInferenceGoToFirstEvidenceCaseMenuItem());
            inferenceMenu.add(getInferenceGoToPreviousEvidenceCaseMenuItem());
            inferenceMenu.add(getInferenceGoToNextEvidenceCaseMenuItem());
            inferenceMenu.add(getInferenceGoToLastEvidenceCaseMenuItem());
            inferenceMenu.addSeparator();
            inferenceMenu.add(getInferencePropagateEvidenceMenuItem());
            inferenceMenu.addSeparator();
            inferenceMenu.add(getInferenceExpandNodeMenuItem());
            inferenceMenu.add(getInferenceContractNodeMenuItem());
            inferenceMenu.addSeparator();
            inferenceMenu.add(getInferenceRemoveAllFindingsMenuItem());
        }
    }
    
    /**
     * This method removes the item 'Propagate Now' from the inference menu.
     */
    public void removePropagateNowItem() {
        if (inferenceMenu != null) {
            inferenceMenu.removeAll();
            inferenceMenu.add(getSwitchWorkingMode());
            inferenceMenu.addSeparator();
            inferenceMenu.add(getPropagationOptionsMenuItem());
            inferenceMenu.add(getInferenceOptionsItem());
            
            inferenceMenu.addSeparator();
            inferenceMenu.add(getInferenceCreateNewEvidenceCaseMenuItem());
            inferenceMenu.add(getInferenceClearOutAllEvidenceCasesMenuItem());
            inferenceMenu.addSeparator();
            inferenceMenu.add(getInferenceGoToFirstEvidenceCaseMenuItem());
            inferenceMenu.add(getInferenceGoToPreviousEvidenceCaseMenuItem());
            inferenceMenu.add(getInferenceGoToNextEvidenceCaseMenuItem());
            inferenceMenu.add(getInferenceGoToLastEvidenceCaseMenuItem());
            inferenceMenu.addSeparator();
            inferenceMenu.add(getInferenceExpandNodeMenuItem());
            inferenceMenu.add(getInferenceContractNodeMenuItem());
            inferenceMenu.addSeparator();
            inferenceMenu.add(getInferenceRemoveAllFindingsMenuItem());
        }
    }
    
    /**
     * This method initializes inferenceSwitchToEditionModeMenuItem.
     *
     * @return a new item 'Inference - Switch to Edition mode'.
     */
    public JMenuItem getSwitchWorkingMode() {
        if (switchWorkingMode == null) {
            switchWorkingMode = new LocalizedMenuItem(
                    MenuItemNames.INFERENCE_SWITCH_TO_EDITION_MODE_MENUITEM, ActionCommands.CHANGE_TO_EDITION_MODE.getCommandName(),
                    null,
                    KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK));
            switchWorkingMode.addActionListener(listener);
        }
        return switchWorkingMode;
    }
    
    /**
     * This method initializes propagationOptionsMenuItem.
     *
     * @return a new item 'Inference - Inference Options'.
     */
    private JMenuItem getPropagationOptionsMenuItem() {
        if (propagationOptionsMenuItem == null) {
            propagationOptionsMenuItem = new LocalizedMenuItem(MenuItemNames.PROPAGATION_OPTIONS_MENUITEM,
                                                               ActionCommands.PROPAGATION_OPTIONS.getCommandName());
            propagationOptionsMenuItem.addActionListener(listener);
        }
        return propagationOptionsMenuItem;
    }
    
    /**
     * This method initializes inferenceCreateNewEvidenceCaseMenuItem.
     *
     * @return a new item 'Inference - Create New Evidence Case'.
     */
    private JMenuItem getInferenceCreateNewEvidenceCaseMenuItem() {
        if (inferenceCreateNewEvidenceCaseMenuItem == null) {
            inferenceCreateNewEvidenceCaseMenuItem = new LocalizedMenuItem(
                    MenuItemNames.INFERENCE_CREATE_NEW_EVIDENCE_CASE_MENUITEM, ActionCommands.CREATE_NEW_EVIDENCE_CASE.getCommandName(),
                    IconBind.CREATE_NEW_EVIDENCE_CASE_ENABLED, null);
            inferenceCreateNewEvidenceCaseMenuItem.addActionListener(listener);
        }
        return inferenceCreateNewEvidenceCaseMenuItem;
    }
    
    /**
     * This method initializes inferenceGoToFirstEvidenceCaseMenuItem.
     *
     * @return a new item 'Inference - Go To First Evidence Case'.
     */
    private JMenuItem getInferenceGoToFirstEvidenceCaseMenuItem() {
        if (inferenceGoToFirstEvidenceCaseMenuItem == null) {
            inferenceGoToFirstEvidenceCaseMenuItem = new LocalizedMenuItem(
                    MenuItemNames.INFERENCE_GO_TO_FIRST_EVIDENCE_CASE_MENUITEM,
                    ActionCommands.GO_TO_FIRST_EVIDENCE_CASE.getCommandName(), IconBind.GO_TO_FIRST_EVIDENCE_CASE_ENABLED, null);
            inferenceGoToFirstEvidenceCaseMenuItem.addActionListener(listener);
        }
        return inferenceGoToFirstEvidenceCaseMenuItem;
    }
    
    /**
     * This method initializes inferenceGoToPreviousEvidenceCaseMenuItem.
     *
     * @return a new item 'Inference - Go To Previous Evidence Case'.
     */
    private JMenuItem getInferenceGoToPreviousEvidenceCaseMenuItem() {
        if (inferenceGoToPreviousEvidenceCaseMenuItem == null) {
            inferenceGoToPreviousEvidenceCaseMenuItem = new LocalizedMenuItem(
                    MenuItemNames.INFERENCE_GO_TO_PREVIOUS_EVIDENCE_CASE_MENUITEM,
                    ActionCommands.GO_TO_PREVIOUS_EVIDENCE_CASE.getCommandName(), IconBind.GO_TO_PREVIOUS_EVIDENCE_CASE_ENABLED, null);
            inferenceGoToPreviousEvidenceCaseMenuItem.addActionListener(listener);
        }
        return inferenceGoToPreviousEvidenceCaseMenuItem;
    }
    
    /**
     * This method initializes inferenceGoToNextEvidenceCaseMenuItem.
     *
     * @return a new item 'Inference - Go To Next Evidence Case'.
     */
    private JMenuItem getInferenceGoToNextEvidenceCaseMenuItem() {
        if (inferenceGoToNextEvidenceCaseMenuItem == null) {
            inferenceGoToNextEvidenceCaseMenuItem = new LocalizedMenuItem(
                    MenuItemNames.INFERENCE_GO_TO_NEXT_EVIDENCE_CASE_MENUITEM, ActionCommands.GO_TO_NEXT_EVIDENCE_CASE.getCommandName(),
                    IconBind.GO_TO_NEXT_EVIDENCE_CASE_ENABLED, null);
            inferenceGoToNextEvidenceCaseMenuItem.addActionListener(listener);
        }
        return inferenceGoToNextEvidenceCaseMenuItem;
    }
    
    /**
     * This method initializes inferenceGoToLastEvidenceCaseMenuItem.
     *
     * @return a new item 'Inference - Go To Last Evidence Case'.
     */
    private JMenuItem getInferenceGoToLastEvidenceCaseMenuItem() {
        if (inferenceGoToLastEvidenceCaseMenuItem == null) {
            inferenceGoToLastEvidenceCaseMenuItem = new LocalizedMenuItem(
                    MenuItemNames.INFERENCE_GO_TO_LAST_EVIDENCE_CASE_MENUITEM, ActionCommands.GO_TO_LAST_EVIDENCE_CASE.getCommandName(),
                    IconBind.GO_TO_LAST_EVIDENCE_CASE_ENABLED, null);
            inferenceGoToLastEvidenceCaseMenuItem.addActionListener(listener);
        }
        return inferenceGoToLastEvidenceCaseMenuItem;
    }
    
    /**
     * This method initializes inferenceClearOutAllEvidenceCasesMenuItem.
     *
     * @return a new item 'Inference - Clear Out All Evidence Cases'.
     */
    private JMenuItem getInferenceClearOutAllEvidenceCasesMenuItem() {
        if (inferenceClearEvidenceCasesMenuItem == null) {
            inferenceClearEvidenceCasesMenuItem = new LocalizedMenuItem(
                    MenuItemNames.INFERENCE_CLEAR_OUT_ALL_EVIDENCE_CASES_MENUITEM,
                    ActionCommands.CLEAR_OUT_ALL_EVIDENCE_CASES.getCommandName(), IconBind.CLEAR_OUT_ALL_EVIDENCE_CASES_ENABLED, null);
            inferenceClearEvidenceCasesMenuItem.addActionListener(listener);
        }
        return inferenceClearEvidenceCasesMenuItem;
    }
    
    /**
     * This method initializes inferencePropagateEvidenceMenuItem.
     *
     * @return a new item 'Inference - Switch to Edition mode'.
     */
    private JMenuItem getInferencePropagateEvidenceMenuItem() {
        if (inferencePropagateEvidenceMenuItem == null) {
            inferencePropagateEvidenceMenuItem = new LocalizedMenuItem(
                    MenuItemNames.INFERENCE_PROPAGATE_EVIDENCE_MENUITEM, ActionCommands.PROPAGATE_EVIDENCE.getCommandName(),
                    IconBind.PROPAGATE_EVIDENCE_ENABLED,
                    KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK));
            inferencePropagateEvidenceMenuItem.addActionListener(listener);
        }
        return inferencePropagateEvidenceMenuItem;
    }
    
    /**
     * This method initializes inferenceExpandNodeMenuItem.
     *
     * @return a new item 'Inference - ExpandNode'.
     */
    private JMenuItem getInferenceExpandNodeMenuItem() {
        if (inferenceExpandNodeMenuItem == null) {
            inferenceExpandNodeMenuItem = new LocalizedMenuItem(MenuItemNames.INFERENCE_EXPAND_NODE_MENUITEM,
                                                                ActionCommands.NODE_EXPANSION.getCommandName());
            inferenceExpandNodeMenuItem.addActionListener(listener);
        }
        return inferenceExpandNodeMenuItem;
    }
    
    /**
     * This method initializes inferenceContractNodeMenuItem.
     *
     * @return a new item 'Inference - ContractNode'.
     */
    private JMenuItem getInferenceContractNodeMenuItem() {
        if (inferenceContractNodeMenuItem == null) {
            inferenceContractNodeMenuItem = new LocalizedMenuItem(MenuItemNames.INFERENCE_CONTRACT_NODE_MENUITEM,
                                                                  ActionCommands.NODE_CONTRACTION.getCommandName());
            inferenceContractNodeMenuItem.addActionListener(listener);
        }
        return inferenceContractNodeMenuItem;
    }
    
    /**
     * This method initializes inferenceRemoveAllFindingsMenuItem.
     *
     * @return a new item 'Inference - RemoveAllFindings'.
     */
    private JMenuItem getInferenceRemoveAllFindingsMenuItem() {
        if (inferenceRemoveAllFindingsMenuItem == null) {
            inferenceRemoveAllFindingsMenuItem = new LocalizedMenuItem(
                    MenuItemNames.INFERENCE_REMOVE_ALL_FINDINGS_MENUITEM, ActionCommands.NODE_REMOVE_ALL_FINDINGS.getCommandName());
            inferenceRemoveAllFindingsMenuItem.addActionListener(listener);
        }
        return inferenceRemoveAllFindingsMenuItem;
    }
    
    /**
     * This method initializes viewNodesMenu.
     *
     * @return a new menu 'View - Nodes'.
     */
    private JMenu getViewNodesMenu() {
        
        if (viewNodesMenu == null) {
            viewNodesMenu = new JMenu();
            viewNodesMenu.setName(MenuItemNames.VIEW_NODES_MENU);
            viewNodesMenu.setText(MenuLocalizer.getLabel(MenuItemNames.VIEW_NODES_MENU));
            viewNodesMenu.setMnemonic(MenuLocalizer.getMnemonic(MenuItemNames.VIEW_NODES_MENU).charAt(0));
            viewNodesMenu.add(getViewNodesByNameMenuItem());
            viewNodesMenu.add(getViewNodesByTitleMenuItem());
        }
        
        return viewNodesMenu;
        
    }
    
    /**
     * This method initializes viewNodesByNameMenuItem.
     *
     * @return a new item 'View - Nodes - ByName'.
     */
    private JCheckBoxMenuItem getViewNodesByNameMenuItem() {
        
        if (viewNodesByNameMenuItem == null) {
            viewNodesByNameMenuItem = new LocalizedCheckBoxMenuItem(MenuItemNames.VIEW_NODES_BYNAME_MENUITEM,
                                                                    ActionCommands.BYNAME_NODES.getCommandName());
            viewNodesByNameMenuItem.addActionListener(listener);
            groupByNameByTitle.add(viewNodesByNameMenuItem);
        }
        
        return viewNodesByNameMenuItem;
        
    }
    
    /**
     * This method initializes viewNodesByTitleMenuItem.
     *
     * @return a new item 'View - Nodes - ByTitle'.
     */
    private JCheckBoxMenuItem getViewNodesByTitleMenuItem() {
        
        if (viewNodesByTitleMenuItem == null) {
            viewNodesByTitleMenuItem = new LocalizedCheckBoxMenuItem(MenuItemNames.VIEW_NODES_BYTITLE_MENUITEM,
                                                                     ActionCommands.BYTITLE_NODES.getCommandName());
            viewNodesByTitleMenuItem.addActionListener(listener);
            groupByNameByTitle.add(viewNodesByTitleMenuItem);
        }
        
        return viewNodesByTitleMenuItem;
        
    }
    
    /**
     * This method initializes toolsMenu.
     *
     * @return a new File menu.
     */
    private JMenu getToolsMenu() {
        if (toolsMenu == null) {
            toolsMenu = new JMenu();
            toolsMenu.setName(MenuItemNames.TOOLS_MENU);
            toolsMenu.setText(MenuLocalizer.getLabel(MenuItemNames.TOOLS_MENU));
            toolsMenu.setMnemonic(MenuLocalizer.getMnemonic(MenuItemNames.TOOLS_MENU).charAt(0));
            ToolPluginManager toolsMenuManager = ToolPluginManager.getInstance();
            //Get all tool plugins grouped by pluging group
            var pluginsByGroupIterator
                    = new TreeMap<>(toolsMenuManager.getAllToolPlugins().stream()
                                                    .collect(Collectors.groupingBy(ToolPlugin::pluginGroup)))
                    .entrySet()
                    .iterator();
            while (pluginsByGroupIterator.hasNext()) {
                var plugins = pluginsByGroupIterator.next().getValue();
                //Sort all plugins in this group by priority and then by name.
                plugins.sort(Comparator.comparing(ToolPlugin::priorityInGroup));
                //Add all of the sorted plugins of the group to the menu.
                for (ToolPlugin plugin : plugins) {
                    toolsMenu.add(plugin.toMenuItem());
                }
                //Add separator only if this group isn't the last (To avoid having a separator that is empty).
                if (pluginsByGroupIterator.hasNext()) {
                    toolsMenu.addSeparator();
                }
            }
            addToolsConfigurationToMenu(toolsMenu);
        }
        return toolsMenu;
        
    }
    
    /**
     * This method initializes toolsConfigurationMenuItem.
     *
     * @return a new item 'Tools - Configuration'.
     */
    private void addToolsConfigurationToMenu(JMenu toolsMenu) {
        toolsMenu.addSeparator();
        toolsConfigurationMenuItem = new LocalizedMenuItem(MenuItemNames.CONFIGURATION_MENUITEM,
                                                           ActionCommands.CONFIGURATION.getCommandName());
        toolsConfigurationMenuItem.addActionListener(listener);
        toolsMenu.add(toolsConfigurationMenuItem);
    }
    
    /**
     * This method initializes helpMenu
     *
     * @return a new Help menu.
     */
    private JMenu getHelpingMenu() {
        
        if (helpMenu == null) {
            helpMenu = new JMenu();
            helpMenu.setName(MenuItemNames.HELP_MENU);
            helpMenu.setText(MenuLocalizer.getLabel(MenuItemNames.HELP_MENU));
            helpMenu.setMnemonic(MenuLocalizer.getMnemonic(MenuItemNames.HELP_MENU).charAt(0));
            helpMenu.add(getHelpOpenShortcutsItem());
            helpMenu.add(getHelpOpenAboutItem());
        }
        
        return helpMenu;
        
    }
    
    /**
     * This methods initializes openChangeLanguageMenuItem
     *
     * @return a new item 'Help - ChangeLanguage'
     */
    private JMenuItem getHelpOpenChangeLanguageItem() {
        
        if (helpOpenChangeLanguageMenuItem == null) {
            helpOpenChangeLanguageMenuItem = new LocalizedMenuItem(MenuItemNames.HELP_CHANGELANGUAGE_MENUITEM,
                                                                   ActionCommands.HELP_CHANGE_LANGUAGE.getCommandName());
            helpOpenChangeLanguageMenuItem.addActionListener(listener);
        }
        
        return helpOpenChangeLanguageMenuItem;
        
    }
    
    /**
     * This methods initializes openShortcutMenuItem
     *
     * @return a new item 'Help - Shortcuts'
     */
    private JMenuItem getHelpOpenShortcutsItem() {
        
        if (helpOpenShortcutsMenuItem == null) {
            helpOpenShortcutsMenuItem = new LocalizedMenuItem(MenuItemNames.HELP_SHORTCUTS_MENUITEM, ActionCommands.HELP_SHORTCUTS.getCommandName());
            helpOpenShortcutsMenuItem.addActionListener(listener);
        }
        
        return helpOpenShortcutsMenuItem;
        
    }
    
    /**
     * This methods initializes openAboutMenuItem
     *
     * @return a new item 'Help - About'
     */
    private JMenuItem getHelpOpenAboutItem() {
        
        if (helpOpenAboutMenuItem == null) {
            helpOpenAboutMenuItem = new LocalizedMenuItem(MenuItemNames.HELP_ABOUT_MENUITEM, ActionCommands.HELP_ABOUT.getCommandName());
            helpOpenAboutMenuItem.addActionListener(listener);
        }
        
        return helpOpenAboutMenuItem;
        
    }
    
    /**
     * Returns the component that correspond to an action command.
     *
     * @param actionCommand action command that identifies the component.
     *
     * @return a components identified by the action command.
     */
    private JComponent getJComponentActionCommand(String actionCommand) {
        return switch (ActionCommands.of(actionCommand)) {
            case ActionCommands.NEW_NETWORK -> fileNewMenuItem;
            case ActionCommands.OPEN_NETWORK -> fileOpenMenuItem;
            case ActionCommands.OPEN_NETWORK_URL -> fileOpenURLMenuItem;
            case ActionCommands.SAVE_NETWORK -> fileSaveMenuItem;
            case ActionCommands.SAVEAS_NETWORK -> fileSaveAsMenuItem;
            case ActionCommands.SAVE_OPEN_NETWORK -> fileSaveOpenMenuItem;
            case ActionCommands.CLOSE_TAB -> fileCloseMenuItem;
            case ActionCommands.LOAD_EVIDENCE -> fileLoadEvidenceMenuItem;
            case ActionCommands.SAVE_EVIDENCE -> fileSaveEvidenceMenuItem;
            case ActionCommands.NETWORK_PROPERTIES -> fileNetworkPropertiesMenuItem;
            case ActionCommands.EXIT_APPLICATION -> fileExitMenuItem;
            case ActionCommands.CLIPBOARD_CUT -> editCutMenuItem;
            case ActionCommands.CLIPBOARD_COPY -> editCopyMenuItem;
            case ActionCommands.CLIPBOARD_PASTE -> editPasteMenuItem;
            case ActionCommands.OBJECT_REMOVAL -> editRemoveMenuItem;
            case ActionCommands.UNDO -> editUndoMenuItem;
            case ActionCommands.REDO -> editRedoMenuItem;
            case ActionCommands.SELECT_ALL -> editSelectAllMenuItem;
            case ActionCommands.OBJECT_SELECTION -> editObjectSelectionMenuItem;
            case ActionCommands.CHANCE_CREATION -> editChanceCreationMenuItem;
            case ActionCommands.DECISION_CREATION -> editDecisionCreationMenuItem;
            case ActionCommands.UTILITY_CREATION -> editUtilityCreationMenuItem;
            case ActionCommands.LINK_CREATION -> editLinkCreationMenuItem;
            case ActionCommands.NODE_PROPERTIES -> editNodePropertiesMenuItem;
            case ActionCommands.EDIT_POTENTIAL -> editRelationMenuItem;
            case ActionCommands.LINK_PROPERTIES -> editLinkPropertiesMenuItem;
            case ActionCommands.CHANGE_WORKING_MODE, ActionCommands.CHANGE_TO_INFERENCE_MODE,
                 ActionCommands.CHANGE_TO_EDITION_MODE -> switchWorkingMode;
            // TODO - MultiCriteria Action Command
            case ActionCommands.INFERENCE_OPTIONS -> inferenceOptionsItem;
            case ActionCommands.PROPAGATION_OPTIONS -> propagationOptionsMenuItem;
            case ActionCommands.CREATE_NEW_EVIDENCE_CASE -> inferenceCreateNewEvidenceCaseMenuItem;
            case ActionCommands.GO_TO_FIRST_EVIDENCE_CASE -> inferenceGoToFirstEvidenceCaseMenuItem;
            case ActionCommands.GO_TO_PREVIOUS_EVIDENCE_CASE -> inferenceGoToPreviousEvidenceCaseMenuItem;
            case ActionCommands.GO_TO_NEXT_EVIDENCE_CASE -> inferenceGoToNextEvidenceCaseMenuItem;
            case ActionCommands.GO_TO_LAST_EVIDENCE_CASE -> inferenceGoToLastEvidenceCaseMenuItem;
            case ActionCommands.CLEAR_OUT_ALL_EVIDENCE_CASES -> inferenceClearEvidenceCasesMenuItem;
            case ActionCommands.PROPAGATE_EVIDENCE -> inferencePropagateEvidenceMenuItem;
            case ActionCommands.NODE_EXPANSION -> inferenceExpandNodeMenuItem;
            case ActionCommands.NODE_CONTRACTION -> inferenceContractNodeMenuItem;
            case ActionCommands.NODE_REMOVE_ALL_FINDINGS -> inferenceRemoveAllFindingsMenuItem;
            case ActionCommands.BYTITLE_NODES -> viewNodesByTitleMenuItem;
            case ActionCommands.BYNAME_NODES -> viewNodesByNameMenuItem;
            case ActionCommands.NODES -> viewNodesMenu;
            case null, default -> null;
        };
        
    }
    
    /**
     * Enables or disabled an option identified by an action command.
     *
     * @param actionCommand action command that identifies the option.
     * @param b             true to enable the option, false to disable.
     */
    @Override public void setOptionEnabled(String actionCommand, boolean b) {
        
        MenuToolBarBasicImpl.setOptionEnabled(getJComponentActionCommand(actionCommand), b);
        
    }
    
    /**
     * Selects or unselects an option identified by an action command. Only
     * selects or unselects the components that are AbstractButton.
     *
     * @param actionCommand action command that identifies the option.
     * @param b             true to select the option, false to unselect.
     */
    @Override public void setOptionSelected(String actionCommand, boolean b) {
        
        MenuToolBarBasicImpl.setOptionSelected(getJComponentActionCommand(actionCommand), b);
        
    }
    
    /**
     * Adds a text to the label of an option identified by an action command.
     * Only adds a text to the components that are AbstractButton.
     *
     * @param actionCommand action command that identifies the option.
     * @param text          text to add to the label of the options. If null, nothing is
     *                      added.
     */
    @Override public void addOptionText(String actionCommand, String text) {
        
        JComponent component = getJComponentActionCommand(actionCommand);
        MenuToolBarBasicImpl.addOptionText(component, defaultText.get(component), text);
        
    }
    
    /**
     * Changes the text of menu item
     *
     * @param actionCommand action command that identifies the option.
     * @param text          text to set to the label.
     */
    @Override public void setText(String actionCommand, String text) {
        
        JComponent component = getJComponentActionCommand(actionCommand);
        MenuToolBarBasicImpl.setText(component, text);
        
    }
    
    
}
