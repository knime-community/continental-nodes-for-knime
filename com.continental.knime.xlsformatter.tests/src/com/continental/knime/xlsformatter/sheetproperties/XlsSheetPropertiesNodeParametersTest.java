package com.continental.knime.xlsformatter.sheetproperties;

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
 * Test for {@link XlsSheetPropertiesNodeParameters}.
 */
@SuppressWarnings("restriction")
final class XlsSheetPropertiesNodeParametersTest extends DefaultNodeSettingsSnapshotTest {

    XlsSheetPropertiesNodeParametersTest() {
        super(getConfig());
    }

    private static SnapshotTestConfiguration getConfig() {
        return SnapshotTestConfiguration.builder() //
            .withInputPortObjectSpecs(createInputPortSpecs()) //
            .testJsonFormsForModel(XlsSheetPropertiesNodeParameters.class) //
            .testJsonFormsWithInstance(SettingsType.MODEL, () -> readSettings()) //
            .testNodeSettingsStructure(() -> readSettings()) //
            .build();
    }

    private static XlsSheetPropertiesNodeParameters readSettings() {
        try {
            var path = getSnapshotPath(XlsSheetPropertiesNodeParameters.class).getParent().resolve("node_settings")
                .resolve("XlsSheetPropertiesNodeParameters.xml");
            try (var fis = new FileInputStream(path.toFile())) {
                var nodeSettings = NodeSettings.loadFromXML(fis);
                return NodeParametersUtil.loadSettings(nodeSettings.getNodeSettings(SettingsType.MODEL.getConfigKey()),
                    XlsSheetPropertiesNodeParameters.class);
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
