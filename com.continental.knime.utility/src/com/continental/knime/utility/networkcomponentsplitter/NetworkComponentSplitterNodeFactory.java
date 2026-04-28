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

package com.continental.knime.utility.networkcomponentsplitter;

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

public class NetworkComponentSplitterNodeFactory 
extends NodeFactory<NetworkComponentSplitterNodeModel> implements NodeDialogFactory {

	@Override
	public NetworkComponentSplitterNodeModel createNodeModel() {
		return new NetworkComponentSplitterNodeModel();
	}

	@Override
	public int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<NetworkComponentSplitterNodeModel> createNodeView(final int viewIndex,
			final NetworkComponentSplitterNodeModel nodeModel) {
		return new NetworkComponentSplitterNodeView(nodeModel);
	}

	@Override
	public boolean hasDialog() {
		return true;
	}

    private static final String NODE_NAME = "Network Component Splitter";
    
    private static final String NODE_ICON = "./networkcomponentsplitter.png";
    
    private static final String SHORT_DESCRIPTION = """
            This node analyzes a list of relations between nodes for unconnected network components.
            """;
    
    private static final String FULL_DESCRIPTION = """
            This node analyzes a list of relations between nodes for unconnected network components. It expects an
                input table with two String columns containing (named) nodes of a network. A row represents a connection
                between the two nodes. The output is a two-column table stating each node and its cluster ID. All nodes
                with the same cluster ID are transitively connected to each other but to no other node of any other
                cluster. <p /> Cluster numbering starts at 1 and gaps are avoided, meaning that the maximum cluster ID
                represents the total number of unconnected network components. The output table is sorted according to
                ascending cluster IDs. The order of the clusters is unspecified. Note that cluster 1 needs not to be the
                biggest cluster. <p /> While this functionality can also be implemented via the <i>Network To Row</i>
                node and its 'Split-up unconnected components' option, our implementation is tuned for performance and
                large networks. Thus, it does not operate on KNIME's network data type but on an edge definition table
                with String-typed node columns directly. <p /> Examples for this node's applicability are:<br /> ● In
                production, new products can be assigned to facilities at minimal footprint complexity by keeping
                distinct material clusters in distinct entities.<br /> ● In logistics, hazardous goods can be analyzed
                for the ability to ship in one delivery.<br /> ● In human relations, an organizational chart analysis
                can reveal data quality issues with employees whose reporting lines do not end at the CEO.<br />
            """;
    
    private static final List<PortDescription> INPUT_PORTS = List.of(
            fixedPort("Network Definition Table", """
                Table which includes at least two String-typed columns. All unique values of the union of these two
                columns represent the node names of the network. Each row represents an (undirected) edge between the
                two respective nodes.
                """)
    );
    
    private static final List<PortDescription> OUTPUT_PORTS = List.of(
            fixedPort("Network Component List", """
                The output table is a unique list of nodes and their assigned cluster IDs.
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
        return new DefaultNodeDialog(SettingsType.MODEL, NetworkComponentSplitterNodeParameters.class);
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
            NetworkComponentSplitterNodeParameters.class, //
            null, //
            NodeType.Manipulator, //
            List.of(), //
            null //
        );
    }

    
}
