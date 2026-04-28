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

package com.continental.knime.xlsformatter.rowcolumnsizer;

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

public class XlsRowColumnSizerNodeFactory extends NodeFactory<XlsRowColumnSizerNodeModel> 
	implements NodeDialogFactory {

	@Override
	public XlsRowColumnSizerNodeModel createNodeModel() {
		return new XlsRowColumnSizerNodeModel();
	}

	@Override
	public int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<XlsRowColumnSizerNodeModel> createNodeView(final int viewIndex,
			final XlsRowColumnSizerNodeModel nodeModel) {
		return null;
	}

	@Override
	public boolean hasDialog() {
		return true;
	}

    private static final String NODE_NAME = "XLS Row and Column Sizer";
    
    private static final String NODE_ICON = "./sizer.png";
    
    private static final String SHORT_DESCRIPTION = """
            The XLS Row and Column Sizer node changes the row height or column width for a selected rows or columns.
            """;
    
    private static final String FULL_DESCRIPTION = """
            The XLS Row and Column Sizer node changes the row height or column width for a selected rows or
                columns.<p /> This node's standard mode of operation is to receive the selection of columns or rows to
                modify via tags in the provided XLS Control Table and the size instructions via the node dialog.
                Matching tags can appear anywhere on the sheet, so e.g. a matching tag in cell B2 would be able to
                address column B or row 2.<p /> This node alternatively provides a direct mode in which the XLS Control
                Table does not hold tags, but the desired size values directly (or missing values for rows/columns not
                intended to be sized). In this case, the incoming table must have all Double-typed columns but otherwise
                be in line with a XLS Control Table in terms of row and column names. In direct mode, row sizes are only
                allowed in column A and column widths in row 1.<p /> This node defines a formatting instruction only
                which needs to be written to an xlsx file via the <i>XLS Formatter (apply)</i> node subsequently.
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
        return new DefaultNodeDialog(SettingsType.MODEL, XlsRowColumnSizerNodeParameters.class);
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
            XlsRowColumnSizerNodeParameters.class, //
            null, //
            NodeType.Manipulator, //
            List.of(), //
            null //
        );
    }

}
