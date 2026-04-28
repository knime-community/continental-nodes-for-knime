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
    
package com.continental.knime.xlsformatter.conditionalformatter;


import java.awt.Color;
import java.util.function.Supplier;

import org.knime.node.parameters.legacy.persistence.PersistWithin;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.layout.After;
import org.knime.node.parameters.layout.HorizontalLayout;
import org.knime.node.parameters.layout.Layout;
import org.knime.node.parameters.layout.Section;
import org.knime.node.parameters.migration.LoadDefaultsForAbsentFields;
import org.knime.node.parameters.persistence.Persist;
import org.knime.node.parameters.persistence.Persistor;
import org.knime.node.parameters.updates.Effect;
import org.knime.node.parameters.updates.Effect.EffectType;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.StateProvider;
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.updates.util.BooleanReference;
import org.knime.node.parameters.widget.number.NumberInputWidget;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation.MaxValidation;
import org.knime.node.parameters.widget.text.TextInputWidget;

import com.continental.knime.xlsformatter.util.PerformTagValidationParameter;
import com.continental.knime.xlsformatter.util.XlsFormatterNodeParameterUtil;
import com.continental.knime.xlsformatter.util.XlsFormatterNodeParameterUtil.LegacyColorPersistor;

/**
 * Node parameters for XLS Conditional Formatter.
 * 
 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
@LoadDefaultsForAbsentFields
final class XlsFormatterConditionalFormatterNodeParameters extends PerformTagValidationParameter {
    
    @Section(title = "Conditional Formatting Settings")
    interface ConditionalFormattingSettingsSection {
    	
    	interface MidPointActivationSection {
    	}
    	
    	@HorizontalLayout
    	@After(MidPointActivationSection.class)
    	interface MinFormattingSection {
    	}
    	
    	@HorizontalLayout
    	@After(MinFormattingSection.class)
    	interface MidFormattingSection {
    	}
    	
    	@HorizontalLayout
    	@After(MidFormattingSection.class)
    	interface MaxFormattingSection {
    	}
    	
    }

    @Widget(title = "Applies to tag (single tag only)", description = """
    		Tag in your input table for which the formatting of this node should be applied to.
    		""")
    @Persist(configKey = XlsFormatterConditionalFormatterNodeModel.CFGKEY_TAG)
    @TextInputWidget(patternValidationProvider = TagValidationProvider.class)
    @ValueReference(TagRef.class)
    String m_tag = XlsFormatterConditionalFormatterNodeModel.DEFAULT_TAG;

    static final class TagRef implements ParameterReference<String> {
    }

    @Layout(ConditionalFormattingSettingsSection.MidPointActivationSection.class)
    @Widget(title = "Enable mid scale point", description = """
    		Set this option if you want to create a three level coloring scheme.
    		""")
    @Persist(configKey = XlsFormatterConditionalFormatterNodeModel.CFGKEY_MIDSCALEPOINT_ACTIVE)
    @ValueReference(IsMidScalePointEnabled.class)
    boolean m_midScalePointActive = XlsFormatterConditionalFormatterNodeModel.DEFAULT_MIDSCALEPOINT_ACTIVE;

    static final class IsMidScalePointEnabled implements BooleanReference {
    }

    @Layout(ConditionalFormattingSettingsSection.MinFormattingSection.class)
    @Widget(title = "Min", description = """
    		Set the lower bound for the coloring in the conditional formatter. The corresponding color will 
    		be displayed for all values smaller or equal than this threshold.
    		""")
    @Persist(configKey = XlsFormatterConditionalFormatterNodeModel.CFGKEY_MIN_THRESHOLD)
    @NumberInputWidget(stepSize = 0.1, maxValidationProvider = MinIsSmallerThanMaxOrMidValidation.class)
    @ValueReference(MinThresholdRef.class)
    double m_minThreshold = XlsFormatterConditionalFormatterNodeModel.DEFAULT_MIN_THRESHOLD;

    static final class MinThresholdRef implements ParameterReference<Double> {
    }

    @Layout(ConditionalFormattingSettingsSection.MinFormattingSection.class)
    @Widget(title = "Min color", description = """
    		Select the color using the color pane.
    		""")
    @PersistWithin(XlsFormatterConditionalFormatterNodeModel.CFGKEY_MIN_COLOR)
    @Persistor(MinColorPersistor.class)
    Color m_minColor = Color.GREEN;
    
    @Layout(ConditionalFormattingSettingsSection.MidFormattingSection.class)
    @Widget(title = "Mid", description = """
    		Set the middle scaling point for the coloring in the conditional formatter.
    		""")
    @Persist(configKey = XlsFormatterConditionalFormatterNodeModel.CFGKEY_MID_THRESHOLD)
    @NumberInputWidget(stepSize = 0.1, maxValidationProvider = MidIsSmallerThanMaxValidation.class)
    @Effect(predicate = IsMidScalePointEnabled.class, type = EffectType.ENABLE)
    @ValueReference(MidThresholdRef.class)
    double m_midThreshold = XlsFormatterConditionalFormatterNodeModel.DEFAULT_MID_THRESHOLD;

    static final class MidThresholdRef implements ParameterReference<Double> {
    }

    @Layout(ConditionalFormattingSettingsSection.MidFormattingSection.class)
    @Widget(title = "Mid color", description = """
    		Select the color using the color pane.
    		""")
    @PersistWithin(XlsFormatterConditionalFormatterNodeModel.CFGKEY_MID_COLOR)
    @Persistor(MidColorPersistor.class)
    @Effect(predicate = IsMidScalePointEnabled.class, type = EffectType.ENABLE)
    Color m_midColor = Color.YELLOW;
    
    @Layout(ConditionalFormattingSettingsSection.MaxFormattingSection.class)
    @Widget(title = "Max", description = """
    		Set the upper bound for the coloring in the conditional formatter. The corresponding color will 
    		be displayed for all values greater or equal than this threshold.
    		""")
    @Persist(configKey = XlsFormatterConditionalFormatterNodeModel.CFGKEY_MAX_THRESHOLD)
    @NumberInputWidget(stepSize = 0.1)
    @ValueReference(MaxThresholdRef.class)
    double m_maxThreshold = XlsFormatterConditionalFormatterNodeModel.DEFAULT_MAX_THRESHOLD;

    static final class MaxThresholdRef implements ParameterReference<Double> {
    }

    @Layout(ConditionalFormattingSettingsSection.MaxFormattingSection.class)
    @Widget(title = "Max color", description = """
    		Select the color using the color pane.
    		""")
    @PersistWithin(XlsFormatterConditionalFormatterNodeModel.CFGKEY_MAX_COLOR)
    @Persistor(MaxColorPersistor.class)
    Color m_maxColor = Color.RED;
    
    static final class TagValidationProvider extends XlsFormatterNodeParameterUtil.XlsTagValidationProvider {

        protected TagValidationProvider() {
            super(TagRef.class);
        }
        
	}
    
    static final class MinIsSmallerThanMaxOrMidValidation implements StateProvider<MaxValidation> {

        private Supplier<Boolean> m_isMidEnabledSupplier;
        
        private Supplier<Double> m_midThresholdSupplier;
        
        private Supplier<Double> m_maxThresholdSupplier;

        @Override
        public void init(final StateProviderInitializer i) {
            i.computeAfterOpenDialog();
            m_isMidEnabledSupplier = i.computeFromValueSupplier(IsMidScalePointEnabled.class);
			m_midThresholdSupplier = i.computeFromValueSupplier(MidThresholdRef.class);
			m_maxThresholdSupplier = i.computeFromValueSupplier(MaxThresholdRef.class);
        }

        @Override
        public MaxValidation computeState(final NodeParametersInput context) {
			final boolean isMidEnabled = m_isMidEnabledSupplier.get();

			final double max = isMidEnabled ? m_midThresholdSupplier.get() : m_maxThresholdSupplier.get();
			final String errorMessage = String.format(
					"Min must be smaller than %s value.", isMidEnabled ? "mid" : "max");

			return new MaxValidation() {
			    @Override
			    protected double getMax() {
			        return max;
			    }

			    @Override
			    public String getErrorMessage() {
			        return errorMessage;
			    }
			};
        }

    }
    
    static final class MidIsSmallerThanMaxValidation implements StateProvider<MaxValidation> {

        private Supplier<Boolean> m_isMidEnabledSupplier;
        
        private Supplier<Double> m_maxThresholdSupplier;

        @Override
        public void init(final StateProviderInitializer i) {
            i.computeAfterOpenDialog();
            m_isMidEnabledSupplier = i.computeFromValueSupplier(IsMidScalePointEnabled.class);
			m_maxThresholdSupplier = i.computeFromValueSupplier(MaxThresholdRef.class);
        }

        @Override
        public MaxValidation computeState(final NodeParametersInput context) {
        	if (m_isMidEnabledSupplier.get()) {
        	    return new MaxValidation() {
        	        @Override
        	        protected double getMax() {
        	            return m_maxThresholdSupplier.get();
        	        }

        	        @Override
        	        public String getErrorMessage() {
        	            return "Mid must be smaller than max value.";
        	        }
        	    };
        	}
        	return null;
        }

    }
    
	static final class MinColorPersistor extends LegacyColorPersistor {

		MinColorPersistor() {
			super(Color.GREEN);
		}

	}

	static final class MidColorPersistor extends LegacyColorPersistor {

		MidColorPersistor() {
			super(Color.YELLOW);
		}

	}

	static final class MaxColorPersistor extends LegacyColorPersistor {

		MaxColorPersistor() {
			super(Color.RED);
		}

	}
    
}
