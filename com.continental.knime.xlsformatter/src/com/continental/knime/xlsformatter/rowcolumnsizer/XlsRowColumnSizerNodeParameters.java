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
    
package com.continental.knime.xlsformatter.rowcolumnsizer;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.layout.Layout;
import org.knime.node.parameters.layout.Section;
import org.knime.node.parameters.migration.LoadDefaultsForAbsentFields;
import org.knime.node.parameters.persistence.NodeParametersPersistor;
import org.knime.node.parameters.persistence.Persist;
import org.knime.node.parameters.persistence.Persistor;
import org.knime.node.parameters.persistence.legacy.EnumBooleanPersistor;
import org.knime.node.parameters.updates.Effect;
import org.knime.node.parameters.updates.EffectPredicate;
import org.knime.node.parameters.updates.EffectPredicateProvider;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.StateProvider;
import org.knime.node.parameters.updates.ValueProvider;
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.widget.choices.Label;
import org.knime.node.parameters.widget.choices.RadioButtonsWidget;
import org.knime.node.parameters.widget.choices.ValueSwitchWidget;
import org.knime.node.parameters.widget.number.NumberInputWidget;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation.MinValidation.IsPositiveDoubleValidation;
import org.knime.node.parameters.widget.text.TextInputWidget;

import com.continental.knime.xlsformatter.commons.XlsFormatterControlTableAnalysisTools;
import com.continental.knime.xlsformatter.rowcolumnsizer.XlsRowColumnSizerNodeModel.ControlTableStyle;
import com.continental.knime.xlsformatter.rowcolumnsizer.XlsRowColumnSizerNodeModel.DimensionToSize;
import com.continental.knime.xlsformatter.util.PerformTagValidationParameter;
import com.continental.knime.xlsformatter.util.XlsFormatterNodeParameterUtil;

/**
 * Node parameters for XLS Row and Column Sizer.
 * 
 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
@SuppressWarnings("restriction")
@LoadDefaultsForAbsentFields
final class XlsRowColumnSizerNodeParameters extends PerformTagValidationParameter {
    
    @Section(title = "Row and Column Size")
    interface SizeModeSection {
    }

    @Widget(title = "Control table style (set automatically)", description = """
            The control table style is set automatically depending on the connected control table. 
            """)
    @Persistor(ControlTableStylePersistor.class)
    @RadioButtonsWidget
    @ValueReference(ControlTableStyleRef.class)
    @ValueProvider(ControlTableStyleProvider.class)
    @Effect(predicate = AlwaysTruePredicate.class, type = Effect.EffectType.DISABLE)
    ControlTableStyle controlTableStyle = ControlTableStyle.STANDARD;

    static final class ControlTableStyleRef implements ParameterReference<ControlTableStyle> {
    }

    @Widget(title = "Applies to tag (single tag only)", description = """
            Tag in your input table for which the formatting of this node should be applied to.
            """)
    @Persist(configKey = XlsRowColumnSizerNodeModel.CFGKEY_TAG)
    @TextInputWidget(patternValidationProvider = TagValidationProvider.class)
    @Effect(predicate = StandardTableStylePredicate.class, type = Effect.EffectType.ENABLE)
    @ValueReference(TagRef.class)
    String tag = XlsRowColumnSizerNodeModel.DEFAULT_TAG;

    static final class TagRef implements ParameterReference<String> {
    }
    
    @Layout(SizeModeSection.class)
    @Widget(title = "Resize dimension", description = "Selection of whether to change row heights or column widths.")
    @Persistor(DimensionToSizePersistor.class)
    @ValueReference(DimensionToSizeRef.class)
    @ValueSwitchWidget
    @Effect(predicate = StandardTableStylePredicate.class, type = Effect.EffectType.ENABLE)
    DimensionToSize dimensionToSize = DimensionToSize.COLUMN;

    static final class DimensionToSizeRef implements ParameterReference<DimensionToSize> {
    }
    
    @Layout(SizeModeSection.class)
    @Widget(title = "Size mode", description = """
    		Choose a custom row height or auto-size the column width depending on its cells' contents and formatting. 
    		Note that the order of XLS formatting nodes is arbitrary and that the column auto-size settings are written 
    		last to the XLS file by the XLS Formatter (apply) node. Auto-size functionality is only available for 
    		columns.
            """)
    @Persistor(SizeModePersistor.class)
    @Effect(predicate = AutoSizeEnabledPredicate.class, type = Effect.EffectType.ENABLE)
    @ValueReference(SizeModeRef.class)
    @ValueSwitchWidget
    SizeMode sizeMode = SizeMode.CUSTOM;

    static final class SizeModeRef implements ParameterReference<SizeMode> {
    }

    @Layout(SizeModeSection.class)
    @Widget(title = "Custom size", description = """
            Select the row height or the column width. Standard height is ~14 and standard width is ~8.
            """)
    @Persist(configKey = XlsRowColumnSizerNodeModel.CFGKEY_SIZE)
    @NumberInputWidget(minValidation = IsPositiveDoubleValidation.class)
    @Effect(predicate = SizeEnabledPredicate.class, type = Effect.EffectType.ENABLE)
    double size = XlsRowColumnSizerNodeModel.DEFAULT_SIZE;

    static final class AlwaysTruePredicate implements EffectPredicateProvider {

        @Override
        public EffectPredicate init(final PredicateInitializer i) {
            return i.getConstant(context -> true);
        }

    }
    
    static final class TagValidationProvider extends XlsFormatterNodeParameterUtil.XlsTagValidationProvider {

        protected TagValidationProvider() {
            super(TagRef.class);
        }

    }

    static final class StandardTableStylePredicate implements EffectPredicateProvider {

        @Override
        public EffectPredicate init(PredicateInitializer i) {
            return i.getEnum(ControlTableStyleRef.class).isOneOf(ControlTableStyle.STANDARD);
        }

    }

    static final class SizeEnabledPredicate implements EffectPredicateProvider {

        @Override
        public EffectPredicate init(PredicateInitializer i) {
            return i.getPredicate(StandardTableStylePredicate.class)
                .and(
                    i.getEnum(DimensionToSizeRef.class).isOneOf(DimensionToSize.ROW)
                    .or(i.getEnum(SizeModeRef.class).isOneOf(SizeMode.CUSTOM))
                );
        }

    }

    static final class AutoSizeEnabledPredicate implements EffectPredicateProvider {

        @Override
        public EffectPredicate init(PredicateInitializer i) {
            return i.getPredicate(StandardTableStylePredicate.class)
                .and(i.getEnum(DimensionToSizeRef.class).isOneOf(DimensionToSize.COLUMN));
        }

    }
    
    static final class ControlTableStyleProvider implements StateProvider<ControlTableStyle> {

        @Override
        public void init(StateProviderInitializer initializer) {
            initializer.computeAfterOpenDialog();
        }

        @Override
        public ControlTableStyle computeState(NodeParametersInput context) {
            final var firstInputSpec = context.getInPortSpec(0);
            if (firstInputSpec.isPresent() && firstInputSpec.get() instanceof DataTableSpec tableSpec) {
                if (XlsFormatterControlTableAnalysisTools.isDoubleControlTableSpecCandidate(tableSpec)) {
                    return ControlTableStyle.DIRECT;
                }
            }
            return ControlTableStyle.STANDARD;
        }

    }

    static final class ControlTableStylePersistor implements NodeParametersPersistor<ControlTableStyle> {

        @Override
        public ControlTableStyle load(final NodeSettingsRO settings) throws InvalidSettingsException {
            return ControlTableStyle.getFromValue(
            		settings.getString(XlsRowColumnSizerNodeModel.CFGKEY_CONTROL_TABLE_STYLE, 
            				ControlTableStyle.STANDARD.toString()));
        }

        @Override
        public void save(final ControlTableStyle obj, final NodeSettingsWO settings) {
            settings.addString(XlsRowColumnSizerNodeModel.CFGKEY_CONTROL_TABLE_STYLE, obj.toString());
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{XlsRowColumnSizerNodeModel.CFGKEY_CONTROL_TABLE_STYLE}};
        }

    }
    
    static final class SizeModePersistor extends EnumBooleanPersistor<SizeMode> {

		protected SizeModePersistor() {
			super(XlsRowColumnSizerNodeModel.CFGKEY_AUTO_SIZE, SizeMode.class, SizeMode.AUTO);
		}
    	
    }

    static final class DimensionToSizePersistor implements NodeParametersPersistor<DimensionToSize> {

        @Override
        public DimensionToSize load(final NodeSettingsRO settings) throws InvalidSettingsException {
            return DimensionToSize.getFromValue(
                    settings.getString(XlsRowColumnSizerNodeModel.CFGKEY_ROW_COLUMN_SIZE, 
							DimensionToSize.COLUMN.toString()));
        }

        @Override
        public void save(final DimensionToSize obj, final NodeSettingsWO settings) {
            settings.addString(XlsRowColumnSizerNodeModel.CFGKEY_ROW_COLUMN_SIZE, obj.toString());
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{XlsRowColumnSizerNodeModel.CFGKEY_ROW_COLUMN_SIZE}};
        }

    }
    
    enum SizeMode {
    	
    	@Label("Custom")
    	CUSTOM, //
    	@Label("Auto")
    	AUTO,
    	
    }

}
