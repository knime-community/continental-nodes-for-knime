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

package com.continental.knime.xlsformatter.xlscontroltablegenerator;

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

public class XlsControlTableGeneratorNodeFactory extends NodeFactory<XlsControlTableGeneratorNodeModel> 
	implements NodeDialogFactory {

	@Override
	public XlsControlTableGeneratorNodeModel createNodeModel() {
		return new XlsControlTableGeneratorNodeModel();
	}

	@Override
	public int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<XlsControlTableGeneratorNodeModel> createNodeView(final int viewIndex,
			final XlsControlTableGeneratorNodeModel nodeModel) {
		return null;
	}

	@Override
	public boolean hasDialog() {
		return true;
	}

    private static final String NODE_NAME = "XLS Control Table Generator";
    
    private static final String NODE_ICON = "./xlstablegenerator.png";
    
    private static final String SHORT_DESCRIPTION = """
            The XLS Control Table Generator node takes an input data table and transforms it to an XLS Control
                Table, meaning it exchanges the column names to A, B, C, ... and the row IDs to 1, 2, 3, ... just like
                they would appear when opening the xlsx file in a spreadsheet application.
            """;
    
    private static final String FULL_DESCRIPTION = """
            The XLS Control Table Generator node takes an input data table and transforms it to an XLS Control
                Table, meaning it exchanges the column names to A, B, C, ... and the row IDs to 1, 2, 3, ... just like
                they would appear when opening the xlsx file in a spreadsheet application.<p /> Optionally you can
                unpivot the table as well to make use of Rule Engine nodes later for flexible and dynamic XLS formatting
                workflows. A second operation type allows to pivot this special table layout back to the final desired
                XLS Control Table structure.
            """;
    
    private static final List<PortDescription> INPUT_PORTS = List.of(
            fixedPort("Data Table", """
                Data table, typically holding the raw data of your xlsx sheet. Alternatively, the output of a previous
                instance of this node (in 'unpivot' mode) with the purpose to be pivoted back to wide table layout.
                """)
    );
    
    private static final List<PortDescription> OUTPUT_PORTS = List.of(
            fixedPort("Control Table", """
                The generated XLS Control Table (or its unpivoted version).
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
        return new DefaultNodeDialog(SettingsType.MODEL, XlsControlTableGeneratorNodeParameters.class);
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
            XlsControlTableGeneratorNodeParameters.class, //
            null, //
            NodeType.Manipulator, //
            List.of(), //
            null //
        );
    }

}
