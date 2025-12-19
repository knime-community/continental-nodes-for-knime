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

package com.continental.knime.xlsformatter.sheetselector;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.webui.node.dialog.NodeDialog;
import org.knime.core.webui.node.dialog.NodeDialogFactory;
import org.knime.core.webui.node.dialog.NodeDialogManager;
import org.knime.core.webui.node.dialog.SettingsType;
import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeDialog;
import org.knime.core.webui.node.dialog.defaultdialog.DefaultKaiNodeInterface;
import org.knime.core.webui.node.dialog.kai.KaiNodeInterface;
import org.knime.core.webui.node.dialog.kai.KaiNodeInterfaceFactory;
import org.knime.core.node.NodeDescription;
import org.knime.node.impl.description.DefaultNodeDescriptionUtil;
import java.util.Map;
import org.knime.node.impl.description.PortDescription;
import java.util.List;
import static org.knime.node.impl.description.PortDescription.fixedPort;

@SuppressWarnings("restriction")
public class XlsSheetSelectorNodeFactory extends NodeFactory<XlsSheetSelectorNodeModel> 
	implements NodeDialogFactory, KaiNodeInterfaceFactory {

	@Override
	public XlsSheetSelectorNodeModel createNodeModel() {
		return new XlsSheetSelectorNodeModel();
	}

	@Override
	public int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<XlsSheetSelectorNodeModel> createNodeView(final int viewIndex,
			final XlsSheetSelectorNodeModel nodeModel) {
		return null;
	}

	@Override
	public boolean hasDialog() {
		return true;
	}

    private static final String NODE_NAME = "XLS Sheet Selector";
    
    private static final String NODE_ICON = "./sheetselector.png";
    
    private static final String SHORT_DESCRIPTION = """
            The XLS Sheet Selector node specifies the target XLS sheet for further XLS formatting instructions.
            """;
    
    private static final String FULL_DESCRIPTION = """
            The XLS Sheet Selector node specifies the target XLS sheet for further XLS formatting instructions.<p />
                Further downstream XLS Formatting nodes then define the formatting instructions for this sheet. These
                can finally be written to a xlsx file for all sheets at once via the <i>XLS Formatter (apply)</i> node.
            """;
    
    private static final List<PortDescription> INPUT_PORTS = List.of();
    
    private static final List<PortDescription> OUTPUT_PORTS = List.of(
            fixedPort("XLS Formatter", """
                The XLS Formatter output port as collector for downstream formatting instructions for this sheet.
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
        return new DefaultNodeDialog(SettingsType.MODEL, XlsSheetSelectorNodeParameters.class);
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
            XlsSheetSelectorNodeParameters.class, //
            null, //
            NodeType.Manipulator, //
            List.of(), //
            null //
        );
    }

    /**
     * {@inheritDoc}
     * @since 1.7
     */
    @Override
    public KaiNodeInterface createKaiNodeInterface() {
        return new DefaultKaiNodeInterface(Map.of(SettingsType.MODEL, XlsSheetSelectorNodeParameters.class));
    }
}
