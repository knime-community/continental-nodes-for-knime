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

package com.continental.knime.utility.fiforesolver;

import java.util.List;
import java.util.Map;

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
import org.knime.node.impl.description.PortDescription;
import static org.knime.node.impl.description.PortDescription.fixedPort;

@SuppressWarnings("restriction")
public class FifoResolverNodeFactory 
extends NodeFactory<FifoResolverNodeModel> implements NodeDialogFactory, KaiNodeInterfaceFactory {

	@Override
	public FifoResolverNodeModel createNodeModel() {
		return new FifoResolverNodeModel();
	}

	@Override
	public int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<FifoResolverNodeModel> createNodeView(final int viewIndex,
			final FifoResolverNodeModel nodeModel) {
		return new FifoResolverNodeView(nodeModel);
	}

	@Override
	public boolean hasDialog() {
		return true;
	}

    private static final String NODE_NAME = "FIFO / LIFO Resolver";
    
    private static final String NODE_ICON = "./fiforesolver.png";
    
    private static final String SHORT_DESCRIPTION = """
            This node analyzes an existing queue of quantitative items flowing in and out.
            """;
    
    private static final String FULL_DESCRIPTION = """
            This node analyzes an existing queue of quantitative items flowing in and out. It resolves the history
                of this queue under the assumption that all outflow follows one of the first-in-first-out (FIFO) or
                last-in-first-out (LIFO) principles. Thereby the quantitative nature of each item can be split, meaning
                parts of items' values flowing out at different points in time. <p /> The node includes an inbuilt
                grouping mechanism as typical application scenarios will not desire to iterate over the entire input
                table. (In case the user does not need a grouping, a constant value column should be added prior to
                usage and be selected as group column.) <p /> The input table represents the history of the queue with
                positive values indicating inflow and negative values indicating outflow. Each output table row
                represents a match of two input table rows, namely the inflow and corresponding outflow rows of the
                represented quantity. Thereby, each in- and outflow can be split into several chunks, depending on the
                consistency of in- and outflow batches. If not all inflow is subsequently flowing out, the remaining
                queue inventory is displayed with missing outflow information but with its inflow origin. <p /> E.g. a
                queue history of 4, 3, -5 in input table rows Row1 to Row3 will lead to the result:<br /> ● RowId_IN
                'Row1', RowId_OUT 'Row3', quantity 4<br /> ● RowId_IN 'Row2', RowId_OUT 'Row3', quantity 1<br /> ●
                RowId_IN 'Row2', RowId_OUT missing, quantity 2<br /> <p /> Examples for this node's applicability
                are:<br /> ● In warehousing, maximal duration of storage can be calculated in order to avoid passing
                best-before-dates.<br /> ● In finance and tax, stock positions can be resolved in order to calculate a
                sell's counterpart in the buy-history and calculating corresponding position profit and holding
                duration.<br /> ● In service operations, backlogs can be analyzed for processing statistics.<br />
            """;
    
    private static final List<PortDescription> INPUT_PORTS = List.of(
            fixedPort("Queue History Table", """
                Table which includes the history of multiple groups' queues. It requires at least one String-typed
                column for the group and one number-typed column for the quantities. The sort order of this input table
                is important as it is inherently treated as time dimension in queue resolution (from top to bottom).
                """)
    );
    
    private static final List<PortDescription> OUTPUT_PORTS = List.of(
            fixedPort("FIFO/LIFO Resolved List", """
                The output table represents the split of quantitative chunks and their association from inflow to
                outflow. The input table rows are identified via their RowID. The RowId_IN and RowId_OUT columns will
                hence typically be used to join back in required meta information from the input table, e.g. event
                dates.
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
        return new DefaultNodeDialog(SettingsType.MODEL, FifoResolverNodeParameters.class);
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
            FifoResolverNodeParameters.class, //
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
        return new DefaultKaiNodeInterface(Map.of(SettingsType.MODEL, FifoResolverNodeParameters.class));
    }
    
}
