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
    
package com.continental.knime.xlsformatter.sheetselector;

import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.migration.LoadDefaultsForAbsentFields;
import org.knime.node.parameters.persistence.Persist;
import org.knime.node.parameters.persistence.Persistor;
import org.knime.node.parameters.persistence.legacy.EnumBooleanPersistor;
import org.knime.node.parameters.updates.Effect;
import org.knime.node.parameters.updates.Effect.EffectType;
import org.knime.node.parameters.updates.EffectPredicate;
import org.knime.node.parameters.updates.EffectPredicateProvider;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.widget.choices.Label;
import org.knime.node.parameters.widget.choices.ValueSwitchWidget;
import org.knime.node.parameters.widget.text.TextInputWidget;
import org.knime.node.parameters.widget.text.TextInputWidgetValidation.PatternValidation.IsNotBlankValidation;

/**
 * Node parameters for XLS Sheet Selector.
 * 
 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
@SuppressWarnings("restriction")
@LoadDefaultsForAbsentFields
final class XlsSheetSelectorNodeParameters implements NodeParameters {
    
    @Widget(title = "Sheet selection", description = """
    		Select either the first sheet (as if this node would be omitted) or specify the name of the sheet to apply 
    		further instructions on.
    		""")
    @Persistor(SheetSelectionModePersistor.class)
    @ValueReference(SheetSelectionModeRef.class)
    @ValueSwitchWidget
    SheetSelectionMode m_sheetSelectionMode = SheetSelectionMode.FIRST_SHEET;
    
    static final class SheetSelectionModeRef implements ParameterReference<SheetSelectionMode> {
    }
    
    @Widget(title = "Sheet name", 
            description = "The name of the XLS sheet that you want further instructions to be applied on.")
    @TextInputWidget(patternValidation = IsNotBlankValidation.class)
    @Effect(predicate = IsByNameSelectionMode.class, type = EffectType.ENABLE)
    @Persist(configKey = XlsSheetSelectorNodeModel.CFGKEY_SHEET_NAME)
    String m_sheetName = XlsSheetSelectorNodeModel.DEFAULT_SHEET_NAME;
    
    static final class IsByNameSelectionMode implements EffectPredicateProvider {

		@Override
		public EffectPredicate init(PredicateInitializer i) {
			return i.getEnum(SheetSelectionModeRef.class).isOneOf(SheetSelectionMode.BY_NAME);
		}
    	
    }
    
    static final class SheetSelectionModePersistor extends EnumBooleanPersistor<SheetSelectionMode> {

		protected SheetSelectionModePersistor() {
			super(XlsSheetSelectorNodeModel.CFGKEY_OPTION_NAMED, SheetSelectionMode.class, SheetSelectionMode.BY_NAME);
		}
    	
    }
    
    enum SheetSelectionMode {
    	
    	@Label("By name")
    	BY_NAME, //
    	@Label("First sheet")
		FIRST_SHEET;
    	
	}
    
}
