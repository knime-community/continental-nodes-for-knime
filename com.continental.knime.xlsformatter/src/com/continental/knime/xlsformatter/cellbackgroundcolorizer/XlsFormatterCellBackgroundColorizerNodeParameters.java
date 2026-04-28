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
    
package com.continental.knime.xlsformatter.cellbackgroundcolorizer;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.node.parameters.legacy.persistence.PersistWithin;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.layout.After;
import org.knime.node.parameters.layout.Layout;
import org.knime.node.parameters.layout.Section;
import org.knime.node.parameters.migration.LoadDefaultsForAbsentFields;
import org.knime.node.parameters.persistence.NodeParametersPersistor;
import org.knime.node.parameters.persistence.Persist;
import org.knime.node.parameters.persistence.Persistor;
import org.knime.node.parameters.updates.Effect;
import org.knime.node.parameters.updates.Effect.EffectType;
import org.knime.node.parameters.updates.EffectPredicate;
import org.knime.node.parameters.updates.EffectPredicateProvider;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.updates.util.BooleanReference;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.EnumChoicesProvider;
import org.knime.node.parameters.widget.choices.Label;
import org.knime.node.parameters.widget.choices.RadioButtonsWidget;
import org.knime.node.parameters.widget.text.TextInputWidget;

import com.continental.knime.xlsformatter.commons.XlsFormatterUiOptions;
import com.continental.knime.xlsformatter.porttype.XlsFormatterState.FillPattern;
import com.continental.knime.xlsformatter.util.PerformTagValidationParameter;
import com.continental.knime.xlsformatter.util.XlsFormatterNodeParameterUtil;
import com.continental.knime.xlsformatter.util.XlsFormatterNodeParameterUtil.LegacyColorPersistor;
import com.continental.knime.xlsformatter.util.XlsFormatterNodeParameterUtil.UpperLowerCaseEnumFieldPersistor;

/**
 * Node parameters for XLS Background Colorizer.
 * 
 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
@LoadDefaultsForAbsentFields
final class XlsFormatterCellBackgroundColorizerNodeParameters extends PerformTagValidationParameter {
    
    @Section(title = "Background Color")
    interface BackgroundColorSection {
    }

    @Section(title = "Pattern Fill")
    @After(BackgroundColorSection.class)
    interface PatternFillSection {
    }

    @Widget(title = "Control table style", description = """
            Choose the control table style: Either use a tag to define your background color (standard tags) 
            or use RGB color codes in your control table (e.g. 255/0/255 or #FF00FF).
            """)
    @Persistor(ControlTableStylePersistor.class)
    @RadioButtonsWidget
    @ValueReference(ControlTableStyleRef.class)
    ControlTableStyle m_controlTableStyle = ControlTableStyle.STANDARD_TAGS;

    static final class ControlTableStyleRef implements ParameterReference<ControlTableStyle> {
    }
    
    @Widget(title = "Applies to tag (single tag only)", description = """
            Tag in your input table for which the formatting of this node should be applied to.
            """)
    @Persist(configKey = XlsFormatterCellBackgroundColorizerNodeModel.CFGKEY_TAGSTRING)
    @TextInputWidget(patternValidationProvider = TagValidationProvider.class)
    @ValueReference(TagRef.class)
    @Effect(predicate = IsStandardTagsMode.class, type = EffectType.ENABLE)
    String m_tag = XlsFormatterCellBackgroundColorizerNodeModel.DEFAULT_TAGSTRING;

    static final class TagRef implements ParameterReference<String> {
    }

    @Layout(BackgroundColorSection.class)
    @Widget(title = "Change background color", description = """
            If you want to change the background color you can enable it here.
            """)
    @Persist(configKey = XlsFormatterCellBackgroundColorizerNodeModel.CFGKEY_BACKGROUND_COLOR_SELECTION)
    @ValueReference(ChangeBackgroundColorRef.class)
    @Effect(predicate = IsStandardTagsMode.class, type = EffectType.ENABLE)
    boolean m_changeBackgroundColor = XlsFormatterCellBackgroundColorizerNodeModel.DEFAULT_BACKGROUND_COLOR_SELECTION;

    static final class ChangeBackgroundColorRef implements BooleanReference {
    }

    @Layout(BackgroundColorSection.class)
    @Widget(title = "Background color", description = "Select the background color.")
    @PersistWithin(XlsFormatterCellBackgroundColorizerNodeModel.CFGKEY_BACKGROUNDCOLOR)
    @Persistor(LegacyColorPersistor.class)
    @Effect(predicate = IsStandardTagsModeAndHasBackgroundColor.class, type = EffectType.ENABLE)
    Color m_backgroundColor = Color.BLACK;
    
    @Layout(PatternFillSection.class)
    @Widget(title = "Pattern fill", description = """
            You can change the pattern fill for your selected tag here or leave it as is 
            (e.g. unmodified, horizontal, diagonal, ...).
            """)
    @Persistor(PatternFillPersistor.class)
    @ChoicesProvider(PatternFillChoicesProvider.class)
    @ValueReference(PatternFillRef.class)
    @Effect(predicate = IsStandardTagsMode.class, type = EffectType.ENABLE)
    FillPattern m_backgroundPattern = FillPattern.UNMODIFIED;

    static final class PatternFillRef implements ParameterReference<FillPattern> {
    }

    @Layout(PatternFillSection.class)
    @Widget(title = "Change pattern color", description = """
            If you want to change the pattern fill color you can enable it here.
            """)
    @Persist(configKey = XlsFormatterCellBackgroundColorizerNodeModel.CFGKEY_BACKGROUND_PATTERN_COLOR_SELECTION)
    @ValueReference(ChangePatternColorRef.class)
    @Effect(predicate = IsStandardTagsModeAndIsModifiedBackgroundPattern.class, type = EffectType.ENABLE)
    boolean m_changeBackgroundPatternColor = 
    	XlsFormatterCellBackgroundColorizerNodeModel.DEFAULT_BACKGROUND_PATTERN_COLOR_SELECTION;

    static final class ChangePatternColorRef implements BooleanReference {
    }

    @Layout(PatternFillSection.class)
    @Widget(title = "Pattern color", description = "Select the pattern color.")
    @PersistWithin(XlsFormatterCellBackgroundColorizerNodeModel.CFGKEY_BACKGROUND_PATTERN_COLOR)
    @Persistor(BackgroundPatternColorPersistor.class)
    @Effect(predicate = IsStandardTagsModeAndIsModifiedBackgroundPatternAndHasBackgroundColor.class, 
    	type = EffectType.ENABLE)
    Color m_backgroundPatternColor = Color.YELLOW;
    
    static final class TagValidationProvider extends XlsFormatterNodeParameterUtil.XlsTagValidationProvider {
    	
        protected TagValidationProvider() {
            super(TagRef.class);
        }
        
    }

    static final class IsStandardTagsMode implements EffectPredicateProvider {

		@Override
		public EffectPredicate init(PredicateInitializer i) {
			return i.getEnum(ControlTableStyleRef.class).isOneOf(ControlTableStyle.STANDARD_TAGS);
		}
    	
    }
    
    static final class IsStandardTagsModeAndHasBackgroundColor implements EffectPredicateProvider {

		@Override
		public EffectPredicate init(PredicateInitializer i) {
			return i.getPredicate(IsStandardTagsMode.class).and(i.getBoolean(ChangeBackgroundColorRef.class).isTrue());
		}
    	
    }
    
    static final class IsStandardTagsModeAndIsModifiedBackgroundPattern implements EffectPredicateProvider {

		@Override
		public EffectPredicate init(PredicateInitializer i) {
			return i.getEnum(PatternFillRef.class).isOneOf(FillPattern.UNMODIFIED).negate()
					.and(i.getPredicate(IsStandardTagsMode.class));
		}
    	
    }
    
    static final class IsStandardTagsModeAndIsModifiedBackgroundPatternAndHasBackgroundColor 
    	implements EffectPredicateProvider {

		@Override
		public EffectPredicate init(PredicateInitializer i) {
			return i.getPredicate(IsStandardTagsModeAndIsModifiedBackgroundPattern.class)
					.and(i.getBoolean(ChangePatternColorRef.class).isTrue());
		}
    	
    }

    static final class PatternFillChoicesProvider implements EnumChoicesProvider<FillPattern> {
    	
        @Override
        public List<FillPattern> choices(NodeParametersInput context) {
            return Arrays.stream(FillPattern.values())
                    .filter(pattern -> pattern != FillPattern.NONE && pattern != FillPattern.SOLID_BACKGROUND_COLOR)
                    .toList();
        }
        
    }

    static final class BackgroundPatternColorPersistor extends LegacyColorPersistor {
    	
    	BackgroundPatternColorPersistor() {
			super(Color.YELLOW);
		}
    	
    }
    
    static final class PatternFillPersistor extends UpperLowerCaseEnumFieldPersistor<FillPattern> {
    	
        protected PatternFillPersistor() {
            super(XlsFormatterCellBackgroundColorizerNodeModel.CFGKEY_BACKGROUND_PATTERN_SELECTION, FillPattern.class);
        }
        
    }
 
	static final class ControlTableStylePersistor implements NodeParametersPersistor<ControlTableStyle> {

	    @Override
	    public ControlTableStyle load(final NodeSettingsRO settings) throws InvalidSettingsException {
	        return ControlTableStyle.getFromValue(settings.getString(
	        		XlsFormatterCellBackgroundColorizerNodeModel.CFGKEY_CONTROLTABLESTYLE));
	    }

	    @Override
	    public void save(final ControlTableStyle obj, final NodeSettingsWO settings) {
	        settings.addString(XlsFormatterCellBackgroundColorizerNodeModel.CFGKEY_CONTROLTABLESTYLE, obj.toString());
	    }

		@Override
		public String[][] getConfigPaths() {
			return new String[][] {{XlsFormatterCellBackgroundColorizerNodeModel.CFGKEY_CONTROLTABLESTYLE}};
		}

	}
	
	enum ControlTableStyle {
    	
        @Label("Standard tags")
        STANDARD_TAGS(XlsFormatterUiOptions.UI_LABEL_CONTROL_TABLE_STYLE_STANDARD), //
        
        @Label("Direct color codes in RGB format")
        DIRECT_RGB(XlsFormatterCellBackgroundColorizerNodeModel.OPTION_CONTROLTABLESTYLE_DIRECT);
        
        private final String m_value;
        
        ControlTableStyle(final String value) {
			m_value = value;
		}
        
		@Override
		public String toString() {
			return m_value;
		}

		/**
		 * Get enum entry from its String value.
		 * 
		 * @param value The String value.
		 * @return The enum entry.
		 * @throws InvalidSettingsException If no enum entry could be found for the given String value.
		 */
		static ControlTableStyle getFromValue(final String value) throws InvalidSettingsException {
            for (final ControlTableStyle controlTableStyle : values()) {
                if (controlTableStyle.toString().equals(value)) {
                    return controlTableStyle;
                }
            }
            throw new InvalidSettingsException(XlsFormatterNodeParameterUtil.createInvalidEnumValueExceptionMessage(
            		ControlTableStyle.class, e -> e.toString(), value));
        }
        
    }
    
}
