package org.openmarkov.integrationTests.gui_tests;

import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.DialogFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.gui.dialog.node.CommonNodePropertiesDialog;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "Your machine does not have a Graphic Environment")
public class TestCommonNodePropertiesDialog extends BaseWindowTest<DialogFixture, Dialog> {
    
    private ProbNet net;
    
    @Override protected DialogFixture setUpWindow() {
        this.net = new ProbNet();
        ProbNet net = this.net;
        net.addNode(new Variable("TestNode"), NodeType.CHANCE);
        Node nodeToTest = net.getNode("TestNode");
        CommonNodePropertiesDialog nodePropertiesDialog = new CommonNodePropertiesDialog(null, nodeToTest, false, false);
        return new DialogFixture(GuiActionRunner.execute(() -> nodePropertiesDialog));
    }
    
    @Test
    void testUI1() {
        assertNotNull(this.net.getNode("TestNode"));
        assertNull(this.net.getNode("ChangedNodeName"));
        this.window.textBox("jTextFieldNodeName").setText("ChangedNodeName");
        this.window.button("jButtonApply").click();
        assertNull(this.net.getNode("TestNode"));
        assertNotNull(this.net.getNode("ChangedNodeName"));
    }
    
}











