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

package com.continental.knime.xlsformatter.xlscontroltablemerger;

import java.util.Optional;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ConfigurableNodeFactory;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeView;
import org.knime.core.node.context.NodeCreationConfiguration;
import org.knime.core.node.port.PortType;
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
import static org.knime.node.impl.description.PortDescription.dynamicPort;

@SuppressWarnings("restriction")
public class XlsControlTableMergerNodeFactory extends ConfigurableNodeFactory<XlsControlTableMergerNodeModel> 
	implements NodeDialogFactory, KaiNodeInterfaceFactory {

	@Override
	public XlsControlTableMergerNodeModel createNodeModel() {
		return new XlsControlTableMergerNodeModel();
	}

	@Override
	public int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<XlsControlTableMergerNodeModel> createNodeView(final int viewIndex,
			final XlsControlTableMergerNodeModel nodeModel) {
		return null;
	}

	@Override
	public boolean hasDialog() {
		return true;
	}

	@Override
	protected Optional<PortsConfigurationBuilder> createPortsConfigBuilder() {
		PortsConfigurationBuilder builder = new PortsConfigurationBuilder();
    builder.addExtendableInputPortGroup(
    		"input",
    		new PortType[]{ BufferedDataTable.TYPE, BufferedDataTable.TYPE},
    		BufferedDataTable.TYPE);
    builder.addFixedOutputPortGroup("Merged XLS Control Table", BufferedDataTable.TYPE);
    return Optional.of(builder);
	}

	@Override
	protected XlsControlTableMergerNodeModel createNodeModel(NodeCreationConfiguration creationConfig) {
    return new XlsControlTableMergerNodeModel(creationConfig.getPortConfig().get());
	}
	
    private static final String NODE_NAME = "XLS Control Table Merger";
    
    private static final String NODE_ICON = "./controltablemerger.png";
    
    private static final String SHORT_DESCRIPTION = """
            The XLS Control Table Merger combines two or more control tables. You can either append all tags, or
                overwrite the information from the first control table by that of the second one in case of conflict.
            """;
    
    private static final String FULL_DESCRIPTION = """
            The XLS Control Table Merger combines two or more control tables. You can either append all tags, or
                overwrite the information from the first control table by that of the second one in case of conflict.
            """;
    
    private static final List<PortDescription> INPUT_PORTS = List.of(
            fixedPort("Control Table", """
                XLS Control Table (holding tags used to select cells to be formatted by many XLS Formatting nodes).
                """),
            fixedPort("Control Table", """
                XLS Control Table (holding tags used to select cells to be formatted by many XLS Formatting nodes).
                """),
            dynamicPort("input", "Additional Control Table", """
                XLS Control Table (holding tags used to select cells to be formatted by many XLS Formatting nodes).
                """)
    );
    
    private static final List<PortDescription> OUTPUT_PORTS = List.of(
            fixedPort("Control Table", """
                XLS Control Table (holding tags used to select cells to be formatted by many XLS Formatting nodes).
                """)
    );

    /**
     * {@inheritDoc}
     * @since 1.7
     */
    @Override
    public NodeDialogPane createNodeDialogPane(NodeCreationConfiguration creationConfig) {
        return NodeDialogManager.createLegacyFlowVariableNodeDialog(createNodeDialog());
    }

    @Override
    public NodeDialog createNodeDialog() {
        return new DefaultNodeDialog(SettingsType.MODEL, XlsControlTableMergerNodeParameters.class);
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
            XlsControlTableMergerNodeParameters.class, //
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
        return new DefaultKaiNodeInterface(Map.of(SettingsType.MODEL, XlsControlTableMergerNodeParameters.class));
    }

}
