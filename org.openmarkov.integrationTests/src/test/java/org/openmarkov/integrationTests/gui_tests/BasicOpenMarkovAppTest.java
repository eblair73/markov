package org.openmarkov.integrationTests.gui_tests;

import org.assertj.swing.core.MouseButton;
import org.assertj.swing.fixture.JPanelFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.integrationTests.IntegrationTest;
import org.openmarkov.java.classUtils.ClassUtils;

import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * This test verifies basic functionalities of the interface.
 * <p>
 * If they were to fail, OpenMarkov would have no meaning.
 *
 * @author jrico
 */
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "Your machine does not have a Graphic Environment")
public class BasicOpenMarkovAppTest extends BaseOpenMarkovAppTest {
    
    /**
     * Opens a network and then changes the name of a node, verifying the node's name change was successfully changed.
     */
    @Test
    void testChangeNodeName() {
        OpenNetworkResult result = this.openNetwork(ClassUtils.getResourceAsFile(IntegrationTest.class, "/networks/bn/BN-catarnet.pgmx"));
        ProbNet probNet = result.probNet();
        JPanelFixture editorPanelFixture = result.editorPanel();
        
        var nodeClosestToLeftUpCorner = probNet
                .getNodes().stream().min(
                        Comparator.comparingDouble(node -> Math.sqrt(Math.pow(node.getCoordinateX(), 2) + Math.pow(node.getCoordinateY(), 2))))
                .get();
        // Double-click on the node to open the Node Properties dialog directly (EditorInputHandler
        // opens CommonNodePropertiesDialog on double-click in edition mode).
        editorPanelFixture.robot().click(
                editorPanelFixture.target(),
                new java.awt.Point((int) nodeClosestToLeftUpCorner.getCoordinateX(), (int) nodeClosestToLeftUpCorner.getCoordinateY()),
                MouseButton.LEFT_BUTTON, 2);
        var nodePropertiesDialog = this.window.dialog("CommonNodePropertiesDialog");
        
        String originalNodeName = nodeClosestToLeftUpCorner.getName();
        String newNodeName = originalNodeName + "_Test";
        assertNotNull(probNet.getNode(originalNodeName));
        assertNull(probNet.getNode(newNodeName));
        nodePropertiesDialog.textBox("jTextFieldNodeName").setText(newNodeName);
        nodePropertiesDialog.button("jButtonApply").click();
        assertNull(probNet.getNode(originalNodeName));
        assertNotNull(probNet.getNode(newNodeName));
    }
    

    
}











