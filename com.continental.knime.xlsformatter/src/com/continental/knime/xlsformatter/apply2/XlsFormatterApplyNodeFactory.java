/*
 * Continental Nodes for KNIME
 * Copyright (C) 2019-2021  Continental AG, Hanover, Germany
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
 
package com.continental.knime.xlsformatter.apply2;

import java.util.Optional;

import org.knime.core.node.ConfigurableNodeFactory;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeView;
import org.knime.core.node.context.NodeCreationConfiguration;
import org.knime.core.node.context.ports.PortsConfiguration;
import org.knime.filehandling.core.port.FileSystemPortObject;

import com.continental.knime.xlsformatter.porttype.XlsFormatterState;
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
public final class XlsFormatterApplyNodeFactory extends ConfigurableNodeFactory<XlsFormatterApplyNodeModel> 
	implements NodeDialogFactory, KaiNodeInterfaceFactory {

	/** The name of the optional source connection input port group. */
	private static final String CONNECTION_SOURCE_PORT_GRP_NAME = "Source File System Connection";

	/** The name of the optional destination connection input port group. */
	private static final String CONNECTION_DESTINATION_PORT_GRP_NAME = "Destination File System Connection";

	/** The name of the xls formatter input port group. */
	private static final String FORMATTER_STATE_GRP_NAME = "XLS Formatter";

	@Override
	protected Optional<PortsConfigurationBuilder> createPortsConfigBuilder() {
		final PortsConfigurationBuilder b = new PortsConfigurationBuilder();
		b.addOptionalInputPortGroup(CONNECTION_SOURCE_PORT_GRP_NAME, FileSystemPortObject.TYPE);
		b.addFixedInputPortGroup(FORMATTER_STATE_GRP_NAME, XlsFormatterState.TYPE);
		b.addOptionalInputPortGroup(CONNECTION_DESTINATION_PORT_GRP_NAME, FileSystemPortObject.TYPE);
		return Optional.of(b);
	}

	@Override
	protected XlsFormatterApplyNodeModel createNodeModel(NodeCreationConfiguration creationConfig) {
		final PortsConfiguration portsCfg = getPortsCfg(creationConfig);
		return new XlsFormatterApplyNodeModel(portsCfg, createSettings(portsCfg), getFormatterIdx(portsCfg));
	}


	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<XlsFormatterApplyNodeModel> createNodeView(int viewIndex, XlsFormatterApplyNodeModel nodeModel) {
		return null;
	}

	@Override
	protected boolean hasDialog() {
		return true;
	}

	private static PortsConfiguration getPortsCfg(final NodeCreationConfiguration creationCfg) {
		return creationCfg.getPortConfig().orElseThrow(IllegalStateException::new);
	}

	private static XlsFormatterApplySettings createSettings(final PortsConfiguration portsCfg) {
		return new XlsFormatterApplySettings(portsCfg, CONNECTION_SOURCE_PORT_GRP_NAME,
				CONNECTION_DESTINATION_PORT_GRP_NAME);
	}

	private static int getFormatterIdx(final PortsConfiguration portsCfg) {
		return portsCfg.getInputPortLocation().get(FORMATTER_STATE_GRP_NAME)[0];
	}
	
    private static final String NODE_NAME = "XLS Formatter (apply)";
    
    private static final String NODE_ICON = "../apply/apply.png";
    
    private static final String SHORT_DESCRIPTION = """
            The XLS Formatter (apply) node applies the chained commands from XLS Formatter nodes to an unformatted
                xlsx file.
            """;
    
    private static final String FULL_DESCRIPTION = """
            <p> The XLS Formatter (apply) node applies the chained commands from XLS Formatter nodes to an
                unformatted xlsx file. </p> <p> All previous XLS Formatter nodes do not modify any xlsx files yet but
                prepare the formatting steps to be taken here. The input file may not contain any formatting yet (except
                very few styles that are tolerated due to date/time values handling). </p> <p><i>This node supports the
                </i><a
                href="https://docs.knime.com/latest/analytics_platform_file_handling_guide/index.html#path"><i>path flow
                variable</i></a> <i> to specify the source and destination location. To convert the created path column
                to a string column which is required by some nodes that have not been migrated yet you can use the </i>
                <a href="https://kni.me/n/ZLCisQlHCzW4IiZG"><i>Path to String</i></a><i> node. For further information
                about file handling in general see the </i><a
                href="https://docs.knime.com/latest/analytics_platform_file_handling_guide/index.html"><i>File Handling
                Guide</i></a>.<br /></p>
            """;
    
    private static final List<PortDescription> INPUT_PORTS = List.of(
            dynamicPort("Source File System Connection", "Source file system connection", """
                The source file system connection.
                """),
            fixedPort("XLS Formatter", """
                The XLS Formatter input port holding the collected formatting instructions for your xlsx file.
                """),
            dynamicPort("Destination File System Connection", "Destination file system connection", """
                The destination file system connection.
                """)
    );
    
    private static final List<PortDescription> OUTPUT_PORTS = List.of();

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
        return new DefaultNodeDialog(SettingsType.MODEL, XlsFormatterApplyNodeParameters.class);
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
            XlsFormatterApplyNodeParameters.class, //
            null, //
            NodeType.Sink, //
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
        return new DefaultKaiNodeInterface(Map.of(SettingsType.MODEL, XlsFormatterApplyNodeParameters.class));
    }
    
}
