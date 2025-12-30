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
    
package com.continental.knime.xlsformatter.xlscontroltablefromcellrange;

import org.knime.node.parameters.Widget;
import org.knime.node.parameters.migration.LoadDefaultsForAbsentFields;
import org.knime.node.parameters.persistence.Persist;
import org.knime.node.parameters.persistence.Persistor;
import org.knime.node.parameters.updates.Effect;
import org.knime.node.parameters.updates.Effect.EffectType;
import org.knime.node.parameters.updates.EffectPredicate;
import org.knime.node.parameters.updates.EffectPredicateProvider;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.widget.choices.ValueSwitchWidget;
import org.knime.node.parameters.widget.text.TextInputWidget;

import com.continental.knime.xlsformatter.commons.Commons.ModePersistor;
import com.continental.knime.xlsformatter.commons.Commons.Modes;
import com.continental.knime.xlsformatter.util.PerformTagValidationParameter;
import com.continental.knime.xlsformatter.util.XlsFormatterNodeParameterUtil;

/**
 * Node parameters for XLS Control Table from Cell Range.
 * 
 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
@LoadDefaultsForAbsentFields
final class XlsControlTableFromCellRangeNodeParameters extends PerformTagValidationParameter {
    
    @Widget(title = "Cell range", 
            description = """
                Cell range in XLS format (e.g. A2 or A2:B10) for which the tag should be created. Also supports 
                the numeric column addressing format of R1C1 or R1C1:R2C2 where 'R' is the row number and 'C' the 
                column number.
                """)
    @Persist(configKey = XlsControlTableFromCellRangeNodeModel.CFGKEY_CELLRANGE)
    @ValueReference(CellRangeRef.class)
    String cellRange = XlsControlTableFromCellRangeNodeModel.DEFAULT_CELLRANGE;
    
    static final class CellRangeRef implements ParameterReference<String> {
    }
    
    @Widget(title = "Tag to set in control table", 
            description = "Tag that shall be set for the cells of your range specified before.")
    @Persist(configKey = XlsControlTableFromCellRangeNodeModel.CFGKEY_TAG)
    @TextInputWidget(patternValidationProvider = TagValidationProvider.class)
    @ValueReference(TagRef.class)
    String tag = XlsControlTableFromCellRangeNodeModel.DEFAULT_TAG;
    
    static final class TagRef implements ParameterReference<String> {
    }
    
    @Widget(title = "Tag combination mode", 
            description = """
                If the optional control table input is used, you can either overwrite or append the tag for 
                overlapping cells.
                """)
    @Persistor(CombineModePersistor.class)
    @ValueSwitchWidget
    @Effect(predicate = IsOptionalTableConnected.class, type = EffectType.ENABLE)
    Modes combineMode = Modes.APPEND;
        
    static final class TagValidationProvider extends XlsFormatterNodeParameterUtil.XlsTagValidationProvider {
        
        protected TagValidationProvider() {
            super(TagRef.class);
        }
    }
    
    static final class IsOptionalTableConnected implements EffectPredicateProvider {

		@Override
		public EffectPredicate init(PredicateInitializer i) {
			return i.getConstant(pi -> pi.getInPortSpec(0).isPresent());		
		}
    	
    }
    
    static final class CombineModePersistor extends ModePersistor {

		protected CombineModePersistor() {
			super(XlsControlTableFromCellRangeNodeModel.CFGKEY_MODE, 
					XlsControlTableFromCellRangeNodeModel.DEFAULT_MODE);
		}
    	
    }
    
}
