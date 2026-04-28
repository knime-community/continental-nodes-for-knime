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

package com.continental.knime.xlsformatter.cellmerger;

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

public class XlsFormatterCellMergerNodeFactory extends NodeFactory<XlsFormatterCellMergerNodeModel> 
	implements NodeDialogFactory {

	@Override
	public XlsFormatterCellMergerNodeModel createNodeModel() {
		return new XlsFormatterCellMergerNodeModel();
	}

	@Override
	public int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<XlsFormatterCellMergerNodeModel> createNodeView(final int viewIndex,
			final XlsFormatterCellMergerNodeModel nodeModel) {
		return null;
	}

	@Override
	public boolean hasDialog() {
		return true;
	}

    private static final String NODE_NAME = "XLS Cell Merger";
    
    private static final String NODE_ICON = "./cellmerger.png";
    
    private static final String SHORT_DESCRIPTION = """
            The XLS Cell Merger node merges the cells for given ranges of input tags.
            """;
    
    private static final String FULL_DESCRIPTION = """
            The XLS Cell Merger node merges the cells for given ranges of input tags.<p /> Tags in the provided XLS
                Control Table must appear in rectangular ranges. A single tag may re-appear, as long as two ranges of
                the same tag share at most a cell's corner, but not a cell's border. In this case, multiple ranges will
                be added based on the same tag.<p /> Optionally, all distinct tags can be used instead of a single one
                (see below).
            """;
    
    private static final List<PortDescription> INPUT_PORTS = List.of(
            fixedPort("Control Table", """
                XLS Control Table holding tags that define which parts of the sheet shall be merged.
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
        return new DefaultNodeDialog(SettingsType.MODEL, XlsFormatterCellMergerNodeParameters.class);
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
            XlsFormatterCellMergerNodeParameters.class, //
            null, //
            NodeType.Manipulator, //
            List.of(), //
            null //
        );
    }

    
}