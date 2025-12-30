/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ------------------------------------------------------------------------
 */
    
package com.continental.knime.xlsformatter.cellformatter;

import java.util.Arrays;
import java.util.List;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.layout.After;
import org.knime.node.parameters.layout.Layout;
import org.knime.node.parameters.layout.Section;
import org.knime.node.parameters.migration.LoadDefaultsForAbsentFields;
import org.knime.node.parameters.persistence.NodeParametersPersistor;
import org.knime.node.parameters.persistence.Persist;
import org.knime.node.parameters.persistence.Persistor;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.EnumChoicesProvider;
import org.knime.node.parameters.widget.choices.Label;
import org.knime.node.parameters.widget.choices.StringChoicesProvider;
import org.knime.node.parameters.widget.choices.SuggestionsProvider;
import org.knime.node.parameters.widget.text.TextInputWidget;

import com.continental.knime.xlsformatter.cellformatter.XlsFormatterCellFormatterNodeModel.TextPresets;
import com.continental.knime.xlsformatter.porttype.XlsFormatterState.CellAlignmentHorizontal;
import com.continental.knime.xlsformatter.porttype.XlsFormatterState.CellAlignmentVertical;
import com.continental.knime.xlsformatter.porttype.XlsFormatterState.CellDataType;
import com.continental.knime.xlsformatter.util.PerformTagValidationParameter;
import com.continental.knime.xlsformatter.util.XlsFormatterNodeParameterUtil;
import com.continental.knime.xlsformatter.util.XlsFormatterNodeParameterUtil.UpperLowerCaseEnumFieldPersistor;

/**
 * Node parameters for XLS Cell Formatter.
 * 
 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
@LoadDefaultsForAbsentFields
final class XlsFormatterCellFormatterNodeParameters extends PerformTagValidationParameter {
    
    @Section(title = "Tag Selection")
    interface TagSelectionSection {
    }
    
    @Section(title = "Text Position and Layout")
    @After(TagSelectionSection.class)
    interface TextPositionAndLayoutSection {
    }

    @Section(title = "Data Type and Format")
    @After(TextPositionAndLayoutSection.class)
    interface DataTypeAndFormatSection {
    }
        
    @Layout(TagSelectionSection.class)
	@Widget(title = "Applies to tag (single tag only)", description = """
			Tag in your input table for which the formatting of this node should be applied to.
			""")
	@Persist(configKey = XlsFormatterCellFormatterNodeModel.CFGKEY_TAGSTRING)
	@TextInputWidget(patternValidationProvider = TagValidationProvider.class)
	@ValueReference(TagRef.class)
	String tag = XlsFormatterCellFormatterNodeModel.DEFAULT_TAGSTRING;

	static final class TagRef implements ParameterReference<String> {
	}
	
	@Layout(TextPositionAndLayoutSection.class)
	@Widget(title = "Horizontal alignment", description = """
			Pick the horizontal alignment of your text in the respective cell (e.g. left, center, right).
			""")
	@Persistor(HorizontalAlignmentPersistor.class)
	CellAlignmentHorizontal horizontalAlignment = CellAlignmentHorizontal.UNMODIFIED;
	
	@Layout(TextPositionAndLayoutSection.class)
	@Widget(title = "Vertical alignment", description = """
			Pick the vertical alignment of your text in the respective cell (e.g. top, middle, bottom).
			""")
	@Persistor(VerticalAlignmentPersistor.class)
	CellAlignmentVertical verticalAlignment = CellAlignmentVertical.UNMODIFIED;

	@Layout(TextPositionAndLayoutSection.class)
	@Widget(title = "Text rotation angle", 
		description = "Set the angle by which your text in the cell should be rotated.")
	@Persistor(TextRotationPersistor.class)
	TextRotation textRotationAngle = TextRotation.UNMODIFIED;

	@Layout(TextPositionAndLayoutSection.class)
	@Widget(title = "Word wrap", description = """
			Select whether long text shall be wrapped to new lines within cells.
			""")
	@Persist(configKey = XlsFormatterCellFormatterNodeModel.CFGKEY_WORDWRAP)
	boolean wordWrap = XlsFormatterCellFormatterNodeModel.DEFAULT_WORDWRAP;

	@Layout(DataTypeAndFormatSection.class)
	@Widget(title = "Cell style conversion", description = "Define the data type conversion for the cell content.")
	@Persistor(CellDataTypePersistor.class)
	@ChoicesProvider(CellStyleChoicesProvider.class)
	CellDataType cellStyle = CellDataType.UNMODIFIED;
	
	@Layout(DataTypeAndFormatSection.class)
	@Widget(title = "Text format", description = """
			Define the text format of your cell (e.g. percent: 0.00% , whole number: #,##0, ...). Please use only 
			English locale values, irrespective of your target or local environment.
			""")
	@Persist(configKey = XlsFormatterCellFormatterNodeModel.CFGKEY_TEXT_FORMAT)
	@SuggestionsProvider(TextFormatSuggestionProvider.class)
	String textFormat = XlsFormatterCellFormatterNodeModel.DEFAULT_TEXT_FORMAT;
	
	@Persistor(TextPresetsPersistor.class)
	TextPresets textPresets = TextPresets.UNMODIFIED;
	
	static final class TagValidationProvider extends XlsFormatterNodeParameterUtil.XlsTagValidationProvider {

		protected TagValidationProvider() {
			super(TagRef.class);
		}

	}
		
	static final class CellStyleChoicesProvider implements EnumChoicesProvider<CellDataType> {

		@Override
		public List<CellDataType> choices(NodeParametersInput context) {
			return Arrays.stream(CellDataType.values()).filter(e -> e != CellDataType.FORMULA).toList();
		}
		
	}
	
	static final class TextFormatSuggestionProvider implements StringChoicesProvider {

		@Override
		public List<String> choices(NodeParametersInput context) {
			return Arrays.stream(TextPresets.values()).map(preset -> preset.getTextFormat()).toList();
		}

	}
	
	static final class HorizontalAlignmentPersistor extends UpperLowerCaseEnumFieldPersistor<CellAlignmentHorizontal> {

		protected HorizontalAlignmentPersistor() {
			super(XlsFormatterCellFormatterNodeModel.CFGKEY_HORIZONTALALIGNMENT, CellAlignmentHorizontal.class);
		}

	}
	
	static final class VerticalAlignmentPersistor extends UpperLowerCaseEnumFieldPersistor<CellAlignmentVertical> {

		protected VerticalAlignmentPersistor() {
			super(XlsFormatterCellFormatterNodeModel.CFGKEY_VERTICALALIGNMENT, CellAlignmentVertical.class);
		}

	}
	
	static final class TextRotationPersistor implements NodeParametersPersistor<TextRotation> {

	    @Override
	    public TextRotation load(final NodeSettingsRO settings) throws InvalidSettingsException {
	        return TextRotation.getFromValue(
	        		settings.getString(XlsFormatterCellFormatterNodeModel.CFGKEY_TEXTROTATION));
	    }

	    @Override
	    public void save(final TextRotation obj, final NodeSettingsWO settings) {
	        settings.addString(XlsFormatterCellFormatterNodeModel.CFGKEY_TEXTROTATION, obj.toString());
	    }

		@Override
		public String[][] getConfigPaths() {
			return new String[][]{{XlsFormatterCellFormatterNodeModel.CFGKEY_TEXTROTATION}};
		}

	}
	
	static final class CellDataTypePersistor implements NodeParametersPersistor<CellDataType> {

	    @Override
	    public CellDataType load(final NodeSettingsRO settings) throws InvalidSettingsException {
	        return CellDataType.getFromValue(settings.getString(XlsFormatterCellFormatterNodeModel.CFGKEY_CELL_STYLE));
	    }

	    @Override
	    public void save(final CellDataType obj, final NodeSettingsWO settings) {
	        settings.addString(XlsFormatterCellFormatterNodeModel.CFGKEY_CELL_STYLE, obj.toString());
	    }

		@Override
		public String[][] getConfigPaths() {
			return new String[][] {{XlsFormatterCellFormatterNodeModel.CFGKEY_CELL_STYLE}};
		}

	}
	
	static final class TextPresetsPersistor implements NodeParametersPersistor<TextPresets> {

	    @Override
	    public TextPresets load(final NodeSettingsRO settings) throws InvalidSettingsException {
	    	final var textPreset = settings.getString(XlsFormatterCellFormatterNodeModel.CFGKEY_TEXT_PRESETS);
	    	return TextPresets.getFromValue(textPreset == null || textPreset.isEmpty() ? 
	    			TextPresets.UNMODIFIED.toString() : textPreset);
	    }

	    @Override
	    public void save(final TextPresets obj, final NodeSettingsWO settings) {
	        settings.addString(XlsFormatterCellFormatterNodeModel.CFGKEY_TEXT_PRESETS, obj.toString());
	    }
	    
		@Override
		public String[][] getConfigPaths() {
			return new String[][]{};
		}

	}

	enum TextRotation {

		@Label("Unmodified")
		UNMODIFIED("unmodified"), 
		@Label("+90°")
		PLUS_90("+90°"), 
		@Label("+45°")
		PLUS_45("+45°"), 
		@Label("0°")
		ZERO("0°"), 
		@Label("-45°")
		MINUS_45("-45°"), 
		@Label("-90°")
		MINUS_90("-90°");

		private final String m_value;

		TextRotation(String value) {
			m_value = value;
		}

		@Override
		public String toString() {
			return m_value;
		}
		
		static TextRotation getFromValue(final String value) throws InvalidSettingsException {
            for (final TextRotation rotation : values()) {
                if (rotation.toString().equals(value)) {
                    return rotation;
                }
            }
            throw new InvalidSettingsException(XlsFormatterNodeParameterUtil.createInvalidEnumValueExceptionMessage(
            		TextRotation.class, e -> e.toString(), value));
        }
		
	}
    
}
