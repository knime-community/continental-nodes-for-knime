package com.continental.knime.xlsformatter.conditionalformatter;

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
 * Test for {@link XlsFormatterConditionalFormatterNodeParameters}.
 */
@SuppressWarnings("restriction")
final class XlsFormatterConditionalFormatterNodeParametersTest extends DefaultNodeSettingsSnapshotTest {

    XlsFormatterConditionalFormatterNodeParametersTest() {
        super(getConfig());
    }

    private static SnapshotTestConfiguration getConfig() {
        return SnapshotTestConfiguration.builder() //
            .withInputPortObjectSpecs(createInputPortSpecs()) //
            .testJsonFormsForModel(XlsFormatterConditionalFormatterNodeParameters.class) //
            .testJsonFormsWithInstance(SettingsType.MODEL, () -> readSettings()) //
            .testNodeSettingsStructure(() -> readSettings()) //
            .build();
    }

    private static XlsFormatterConditionalFormatterNodeParameters readSettings() {
        try {
            var path = getSnapshotPath(XlsFormatterConditionalFormatterNodeParameters.class).getParent()
                    .resolve("node_settings").resolve("XlsFormatterConditionalFormatterNodeParameters.xml");
            try (var fis = new FileInputStream(path.toFile())) {
                var nodeSettings = NodeSettings.loadFromXML(fis);
                return NodeParametersUtil.loadSettings(nodeSettings.getNodeSettings(SettingsType.MODEL.getConfigKey()),
                    XlsFormatterConditionalFormatterNodeParameters.class);
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
