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

package com.continental.knime.xlsformatter.cellbackgroundcolorizer;

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

/**
 * This is the factory class for the "XLS Background Colorizer" Node.
 * 
 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
 */
@SuppressWarnings("restriction")
public class XlsFormatterCellBackgroundColorizerNodeFactory 
	extends NodeFactory<XlsFormatterCellBackgroundColorizerNodeModel> 
	implements NodeDialogFactory, KaiNodeInterfaceFactory {

	@Override
	public XlsFormatterCellBackgroundColorizerNodeModel createNodeModel() {
		return new XlsFormatterCellBackgroundColorizerNodeModel();
	}

	@Override
	public int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<XlsFormatterCellBackgroundColorizerNodeModel> createNodeView(final int viewIndex,
			final XlsFormatterCellBackgroundColorizerNodeModel nodeModel) {
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

    private static final String NODE_NAME = "XLS Background Colorizer";
    
    private static final String NODE_ICON = "./backgroundcolorizer.png";
    
    private static final String SHORT_DESCRIPTION = """
            The XLS Background Colorizer node changes the background color and/or pattern fill for selected cells.
                The color information is statically defined. Consider using the XLS Conditional Formatter node if you
                want your spreadsheet application to color the cell background according to its numeric value.
            """;
    
    private static final String FULL_DESCRIPTION = """
            The XLS Background Colorizer node changes the background color and/or pattern fill for selected cells.
                The color information is statically defined. Consider using the XLS Conditional Formatter node if you
                want your spreadsheet application to color the cell background according to its numeric value.<p /> This
                node's standard mode of operation is to receive the selection of cells to modify via tags in the
                provided XLS Control Table and the formatting instructions via the node dialog. However, it also
                provides a direct mode in which the XLS Control Table does not hold tags, but the desired color values
                directly (in RGB format, e.g. red either as #FF000 or 255/0/0) or missing values for cells not intended
                to be colorized. Note that the xlsx file format allows a maximum of 64,000 distinct styles per workbook
                only. Hence, even using a small fraction of the 16.8 million possible RGB colors can easily exhaust this
                quota.<p /> This node defines a formatting instruction only which needs to be written to an xlsx file
                via the <i>XLS Formatter (apply)</i> node subsequently.
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
        return new DefaultNodeDialog(SettingsType.MODEL, XlsFormatterCellBackgroundColorizerNodeParameters.class);
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
            XlsFormatterCellBackgroundColorizerNodeParameters.class, //
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
        return new DefaultKaiNodeInterface(Map.of(SettingsType.MODEL, XlsFormatterCellBackgroundColorizerNodeParameters.class));
    }
    
}