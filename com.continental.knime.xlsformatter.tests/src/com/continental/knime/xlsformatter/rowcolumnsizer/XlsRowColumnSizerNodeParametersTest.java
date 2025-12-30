package com.continental.knime.xlsformatter.rowcolumnsizer;

import java.io.FileInputStream;
import java.io.IOException;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettings;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.webui.node.dialog.SettingsType;
import org.knime.core.webui.node.dialog.defaultdialog.NodeParametersUtil;
import org.knime.testing.node.dialog.DefaultNodeSettingsSnapshotTest;
import org.knime.testing.node.dialog.SnapshotTestConfiguration;

import com.continental.knime.xlsformatter.porttype.XlsFormatterStateSpec;

/**
 * Test for {@link XlsRowColumnSizerNodeParameters}.
 */
@SuppressWarnings("restriction")
final class XlsRowColumnSizerNodeParametersTest extends DefaultNodeSettingsSnapshotTest {

    XlsRowColumnSizerNodeParametersTest() {
        super(getConfig());
    }

    private static SnapshotTestConfiguration getConfig() {
        return SnapshotTestConfiguration.builder() //
            .withInputPortObjectSpecs(createInputPortSpecs()) //
            .testJsonFormsForModel(XlsRowColumnSizerNodeParameters.class) //
            .testJsonFormsWithInstance(SettingsType.MODEL, () -> readSettings()) //
            .testNodeSettingsStructure(() -> readSettings()) //
            .build();
    }

    private static XlsRowColumnSizerNodeParameters readSettings() {
        try {
            var path = getSnapshotPath(XlsRowColumnSizerNodeParameters.class).getParent().resolve("node_settings")
                .resolve("XlsRowColumnSizerNodeParameters.xml");
            try (var fis = new FileInputStream(path.toFile())) {
                var nodeSettings = NodeSettings.loadFromXML(fis);
                return NodeParametersUtil.loadSettings(nodeSettings.getNodeSettings(SettingsType.MODEL.getConfigKey()),
                    XlsRowColumnSizerNodeParameters.class);
            }
        } catch (IOException | InvalidSettingsException e) {
            throw new IllegalStateException(e);
        }
    }

    private static PortObjectSpec[] createInputPortSpecs() {
        DataTableSpec controlTableSpec = new DataTableSpec(
            new String[]{"Row ID", "Column ID", "Tag"},
            new DataType[]{
                DataType.getType(StringCell.class),
                DataType.getType(StringCell.class),
                DataType.getType(StringCell.class)
            }
        );
        XlsFormatterStateSpec xlsFormatterSpec = XlsFormatterStateSpec.getEmptySpec();
        return new PortObjectSpec[]{controlTableSpec, xlsFormatterSpec};
    }
}
