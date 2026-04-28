/*
 * Continental Nodes for KNIME
 * Copyright (C) 2019  Continental AG, Hanover, Germany
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.continental.knime.xlsformatter.fontformatter;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.webui.node.dialog.NodeDialog;
import org.knime.core.webui.node.dialog.NodeDialogFactory;
import org.knime.core.webui.node.dialog.NodeDialogManager;
import org.knime.core.webui.node.dialog.SettingsType;
import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeDialog;
import org.knime.core.node.NodeDescription;
import org.knime.node.impl.description.DefaultNodeDescriptionUtil;
import org.knime.node.impl.description.PortDescription;
import java.util.List;
import static org.knime.node.impl.description.PortDescription.fixedPort;

public class XlsFormatterFontFormatterNodeFactory extends NodeFactory<XlsFormatterFontFormatterNodeModel> 
	implements NodeDialogFactory {

	@Override
	public XlsFormatterFontFormatterNodeModel createNodeModel() {
		return new XlsFormatterFontFormatterNodeModel();
	}

	@Override
	public int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<XlsFormatterFontFormatterNodeModel> createNodeView(final int viewIndex,
			final XlsFormatterFontFormatterNodeModel nodeModel) {
		return null;
	}

	@Override
	public boolean hasDialog() {
		return true;
	}
	
	@Override
	public boolean hasNodeDialog() {
		return true;
	}

    private static final String NODE_NAME = "XLS Font Formatter";
    
    private static final String NODE_ICON = "./fontformatter.png";
    
    private static final String SHORT_DESCRIPTION = """
            The XLS Font Formatter node changes font properties of the text for selected cells, such as bold,
                italic, underline, size, and color.
            """;
    
    private static final String FULL_DESCRIPTION = """
            The XLS Font Formatter node changes font properties of the text for selected cells, such as bold,
                italic, underline, size, and color.<p /> This node defines a formatting instruction only which needs to
                be written to an xlsx file via the <i>XLS Formatter (apply)</i> node subsequently.
            """;
    
    private static final List<PortDescription> INPUT_PORTS = List.of(
            fixedPort("Control Table", """
                XLS Control Table holding tags that define which cells of the sheet to format.
                """),
            fixedPort("Optional XLS Formatter", """
                The XLS Formatter input port potentially holding previous formatting instructions that the instructions
                of this node shall be added to.
                """)
    );
    
    private static final List<PortDescription> OUTPUT_PORTS = List.of(
            fixedPort("XLS Formatter", """
                The XLS Formatter output port holding the collected formatting instructions including the added
                formatting information from this node.
                """)
    );

    /**
     * {@inheritDoc}
     * @since 1.7
     */
	@Override
	public NodeDialogPane createNodeDialogPane() {
		return NodeDialogManager.createLegacyFlowVariableNodeDialog(createNodeDialog());
	}

    @Override
    public NodeDialog createNodeDialog() {
        return new DefaultNodeDialog(SettingsType.MODEL, XlsFormatterFontFormatterNodeParameters.class);
    }

    @Override
    public NodeDescription createNodeDescription() {
        return DefaultNodeDescriptionUtil.createNodeDescription( //
            NODE_NAME, //
            NODE_ICON, //
            INPUT_PORTS, //
            OUTPUT_PORTS, //
            SHORT_DESCRIPTION, //
            FULL_DESCRIPTION, //
            List.of(), //
            XlsFormatterFontFormatterNodeParameters.class, //
            null, //
            NodeType.Manipulator, //
            List.of(), //
            null //
        );
    }

    
}
