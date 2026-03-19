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
    
package com.continental.knime.xlsformatter.fontformatter;

import java.awt.Color;

import org.knime.core.webui.node.dialog.defaultdialog.internal.widget.PersistWithin;
import org.knime.node.parameters.Widget;
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
import org.knime.node.parameters.widget.number.NumberInputWidget;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation.MinValidation.IsPositiveIntegerValidation;
import org.knime.node.parameters.widget.text.TextInputWidget;

import com.continental.knime.xlsformatter.util.PerformTagValidationParameter;
import com.continental.knime.xlsformatter.util.XlsFormatterNodeParameterUtil;
import com.continental.knime.xlsformatter.util.XlsFormatterNodeParameterUtil.LegacyColorPersistor;

/**
 * Node parameters for XLS Font Formatter.
 * 
 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
@LoadDefaultsForAbsentFields
final class XlsFormatterFontFormatterNodeParameters extends PerformTagValidationParameter {
    
    @Section(title = "Font Specification")
    interface FontSpecificationSection {
    }

    @Widget(title = "Applies to tag (single tag only)", description = """
            Tag in your input table for which the formatting of this node should be applied to.
            """)
    @Persist(configKey = XlsFormatterFontFormatterNodeModel.CFGKEY_TAG)
    @TextInputWidget(patternValidationProvider = TagValidationProvider.class)
    @ValueReference(TagRef.class)
    String m_tag = XlsFormatterFontFormatterNodeModel.DEFAULT_TAG;

    static final class TagRef implements ParameterReference<String> {
    }

    @Layout(FontSpecificationSection.class)
    @Widget(title = "Bold", description = "Set your font to bold.")
    @Persist(configKey = XlsFormatterFontFormatterNodeModel.CFGKEY_BOLD)
    boolean m_bold = XlsFormatterFontFormatterNodeModel.DEFAULT_BOLD;

    @Layout(FontSpecificationSection.class)
    @Widget(title = "Italic", description = "Set your font to italic.")
    @Persist(configKey = XlsFormatterFontFormatterNodeModel.CFGKEY_ITALIC)
    boolean m_italic = XlsFormatterFontFormatterNodeModel.DEFAULT_ITALIC;

    @Layout(FontSpecificationSection.class)
    @Widget(title = "Underline", description = "Set your font to underline.")
    @Persist(configKey = XlsFormatterFontFormatterNodeModel.CFGKEY_UNDERLINE)
    boolean m_underline = XlsFormatterFontFormatterNodeModel.DEFAULT_UNDERLINE;

    @Layout(FontSpecificationSection.class)
    @Widget(title = "Change font size?", description = """
            If you want to change the font size you can enable it here.
            """)
    @Persist(configKey = XlsFormatterFontFormatterNodeModel.CFGKEY_CHANGESIZE)
    @ValueReference(ChangeFontSizeRef.class)
    boolean m_changeFontSize = XlsFormatterFontFormatterNodeModel.DEFAULT_CHANGESIZE;

    static final class ChangeFontSizeRef implements BooleanReference {
    }

    @Layout(FontSpecificationSection.class)
    @Widget(title = "Font size", description = "Select the size of your font.")
    @Persist(configKey = XlsFormatterFontFormatterNodeModel.CFGKEY_SIZE)
    @NumberInputWidget(minValidation = IsPositiveIntegerValidation.class)
    @Effect(predicate = ChangeFontSizeRef.class, type = EffectType.ENABLE)
    int m_size = XlsFormatterFontFormatterNodeModel.DEFAULT_SIZE;

    @Layout(FontSpecificationSection.class)
    @Widget(title = "Change color?", description = """
            If you want to change the font color you can enable it here.
            """)
    @Persist(configKey = XlsFormatterFontFormatterNodeModel.CFGKEY_CHANGECOLOR)
    @ValueReference(ChangeColorRef.class)
    boolean m_changeColor = XlsFormatterFontFormatterNodeModel.DEFAULT_CHANGECOLOR;

    static final class ChangeColorRef implements BooleanReference {
    }

    @SuppressWarnings("restriction")
	@Layout(FontSpecificationSection.class)
	@Widget(title = "Color", description = "Define a color assigned to the font.")
    @PersistWithin(XlsFormatterFontFormatterNodeModel.CFGKEY_FONTCOLOR)
    @Persistor(LegacyColorPersistor.class)
    @Effect(predicate = ChangeColorRef.class, type = EffectType.ENABLE)
    Color m_fontColor = Color.BLACK;

    static final class TagValidationProvider extends XlsFormatterNodeParameterUtil.XlsTagValidationProvider {
    	
        protected TagValidationProvider() {
            super(TagRef.class);
        }
        
    }
    
}
