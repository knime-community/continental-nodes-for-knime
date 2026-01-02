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
    
package com.continental.knime.xlsformatter.borderformatter;

import java.util.Arrays;
import java.util.List;

import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.layout.After;
import org.knime.node.parameters.layout.Layout;
import org.knime.node.parameters.layout.Section;
import org.knime.node.parameters.migration.LoadDefaultsForAbsentFields;
import org.knime.node.parameters.persistence.Persist;
import org.knime.node.parameters.persistence.Persistor;
import org.knime.node.parameters.updates.Effect;
import org.knime.node.parameters.updates.Effect.EffectType;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.updates.util.BooleanReference;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.EnumChoicesProvider;
import org.knime.node.parameters.widget.text.TextInputWidget;

import com.continental.knime.xlsformatter.porttype.XlsFormatterState.BorderStyle;
import com.continental.knime.xlsformatter.util.PerformTagValidationParameter;
import com.continental.knime.xlsformatter.util.XlsFormatterNodeParameterUtil;
import com.continental.knime.xlsformatter.util.XlsFormatterNodeParameterUtil.LegacyColorNodeParameters;
import com.continental.knime.xlsformatter.util.XlsFormatterNodeParameterUtil.UpperLowerCaseEnumFieldPersistor;

/**
 * Node parameters for XLS Border Formatter.
 * 
 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
@LoadDefaultsForAbsentFields
final class XlsFormatterBorderFormatterNodeParameters extends PerformTagValidationParameter {
    
    @Section(title = "Border Style and Color")
    interface BorderStyleAndColorSection {
    }

    @Section(title = "Outer Border Settings")
    @After(BorderStyleAndColorSection.class)
    interface OuterBorderSettingsSection {
    }
    
    @Section(title = "Inner Border Settings")
    @After(OuterBorderSettingsSection.class)
    interface InnerBorderSettingsSection {
    }
    
    @Widget(title = "Applies to all tags", description = """
    		Instead of specifying a single tag, the border formatting is applied repetitively to all distinct 
    		tags found in the control table. Note that in this case, all matching cells will be processed 
    		with the selected border formatting, which usually leads to overlapping cells to accumulate/overlap 
    		in their appearance.
    		""")
    @Persist(configKey = XlsFormatterBorderFormatterNodeModel.CFGKEY_ALL_TAGS)
    @ValueReference(AllTagsRef.class)
    boolean m_allTags = XlsFormatterBorderFormatterNodeModel.DEFAULT_ALL_TAGS;

    static final class AllTagsRef implements BooleanReference {
    }
    
    @Widget(title = "Applies to tag (single tag only)", description = """
    		Tag in your input table for which the formatting of this node should be applied to.
    		""")
    @Persist(configKey = XlsFormatterBorderFormatterNodeModel.CFGKEY_TAGSTRING)
    @TextInputWidget(patternValidationProvider = TagValidationProvider.class)
    @ValueReference(TagRef.class)
    @Effect(predicate = AllTagsRef.class, type = EffectType.DISABLE)
    String m_tag = XlsFormatterBorderFormatterNodeModel.DEFAULT_TAGSTRING;

    static final class TagRef implements ParameterReference<String> {
    }

    @Layout(BorderStyleAndColorSection.class)
    @Widget(title = "Border color style", description = """
    		Select the style your border should get (e.g. normal, thick, dashed, ...).
    		""")
    @Persistor(BorderStylePersistor.class)
    @ChoicesProvider(BorderStyleChoicesProvider.class)
    BorderStyle m_borderStyle = BorderStyle.NORMAL;
    
    @Layout(BorderStyleAndColorSection.class)
    @Widget(title = "Change border color?", description = """
    		If you want to change the color you can enable it here.
    		""")
    @Persist(configKey = XlsFormatterBorderFormatterNodeModel.CFGKEY_BORDER_CHANGECOLOR)
    @ValueReference(ChangeColorRef.class)
    boolean m_changeBorderColor = XlsFormatterBorderFormatterNodeModel.DEFAULT_BORDER_CHANGECOLOR;

    static final class ChangeColorRef implements BooleanReference {
    }
    
    @Layout(BorderStyleAndColorSection.class)
    @Widget(title = "Border color", description = "Select the border color.")
    @Persist(configKey = XlsFormatterBorderFormatterNodeModel.CFGKEY_BORDER_COLOR)
    @Effect(predicate = ChangeColorRef.class, type = EffectType.ENABLE)
    LegacyColorNodeParameters m_borderColor = new LegacyColorNodeParameters();
    
    @Layout(OuterBorderSettingsSection.class)
    @Widget(title = "Top", description = """
    		Set the top outer border of your tag range(s).
    		""")
    @Persist(configKey = XlsFormatterBorderFormatterNodeModel.CFGKEY_BORDER_TOP)
    boolean m_borderTop = XlsFormatterBorderFormatterNodeModel.DEFAULT_BORDER_TOP;

    @Layout(OuterBorderSettingsSection.class)
    @Widget(title = "Left", description = """
    		Set the left outer border of your tag range(s).
    		""")
    @Persist(configKey = XlsFormatterBorderFormatterNodeModel.CFGKEY_BORDER_LEFT)
    boolean m_borderLeft = XlsFormatterBorderFormatterNodeModel.DEFAULT_BORDER_LEFT;

    @Layout(OuterBorderSettingsSection.class)
    @Widget(title = "Right", description = """
    		Set the right outer border of your tag range(s).
    		""")
    @Persist(configKey = XlsFormatterBorderFormatterNodeModel.CFGKEY_BORDER_RIGHT)
    boolean m_borderRight = XlsFormatterBorderFormatterNodeModel.DEFAULT_BORDER_RIGHT;

    @Layout(OuterBorderSettingsSection.class)
    @Widget(title = "Bottom", description = """
    		Set the bottom outer border of your tag range(s).
    		""")
    @Persist(configKey = XlsFormatterBorderFormatterNodeModel.CFGKEY_BORDER_BOTTOM)
    boolean m_borderBottom = XlsFormatterBorderFormatterNodeModel.DEFAULT_BORDER_BOTTOM;

    @Layout(InnerBorderSettingsSection.class)
    @Widget(title = "Inner vertical", description = """
    		Set the inner vertical border of your tag range(s).
    		""")
    @Persist(configKey = XlsFormatterBorderFormatterNodeModel.CFGKEY_BORDER_INNER_VERTICAL)
    boolean m_borderInnerVertical = XlsFormatterBorderFormatterNodeModel.DEFAULT_BORDER_INNER_VERTICAL;

    @Layout(InnerBorderSettingsSection.class)
    @Widget(title = "Inner horizontal", description = """
    		Set the inner horizontal border of your tag range(s).
    		""")
    @Persist(configKey = XlsFormatterBorderFormatterNodeModel.CFGKEY_BORDER_INNER_HORIZONTAL)
    boolean m_borderInnerHorizontal = XlsFormatterBorderFormatterNodeModel.DEFAULT_BORDER_INNER_HORIZONTAL;

    static final class TagValidationProvider extends XlsFormatterNodeParameterUtil.XlsTagValidationProvider {

        protected TagValidationProvider() {
            super(TagRef.class);
        }
        
    }
    
    static final class BorderStyleChoicesProvider implements EnumChoicesProvider<BorderStyle> {

        @Override
        public List<BorderStyle> choices(NodeParametersInput context) {
            return Arrays.stream(BorderStyle.values())
                    .filter(style -> style != BorderStyle.UNMODIFIED && style != BorderStyle.NONE)
                    .toList();
        }
        
    }
    
    static final class BorderStylePersistor extends UpperLowerCaseEnumFieldPersistor<BorderStyle> {

		protected BorderStylePersistor() {
			super(XlsFormatterBorderFormatterNodeModel.CFGKEY_BORDER_STYLE, BorderStyle.class);
		}
    	
    }
    
}
