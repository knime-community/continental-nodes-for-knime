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
    
package com.continental.knime.xlsformatter.xlscontroltablegenerator;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.Widget;
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
import org.knime.node.parameters.updates.StateProvider;
import org.knime.node.parameters.updates.ValueProvider;
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.widget.choices.RadioButtonsWidget;

import com.continental.knime.xlsformatter.commons.XlsFormatterControlTableAnalysisTools;
import com.continental.knime.xlsformatter.xlscontroltablegenerator.XlsControlTableGeneratorNodeModel.InconsistencyResolutionOptions;
import com.continental.knime.xlsformatter.xlscontroltablegenerator.XlsControlTableGeneratorNodeModel.OperationType;

/**
 * Node parameters for XLS Control Table Generator.
 * 
 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
@LoadDefaultsForAbsentFields
final class XlsControlTableGeneratorNodeParameters implements NodeParameters {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(XlsControlTableGeneratorNodeParameters.class);
    
    @Section(title = "Result Table Structure Options")
    interface ResultTableStructureSection {
    }
    
    @Widget(title = "Operation Type (set automatically)", 
            description = """
                The operation type is automatically set based on the provided input table. 
                If no special input table is detected, the standard operation will exchange the input table's 
                column and row headers to XLS-like style. If the output of a previous unpivot operation 
                is detected, this mode pivots them back to a valid (wide) XLS Control Table.
                """)
    @Persistor(OperationTypePersistor.class)
    @RadioButtonsWidget
    @ValueProvider(OperationTypeProvider.class)
    @ValueReference(OperationTypeRef.class)
    @Effect(predicate = AlwaysTruePredicate.class, type = EffectType.DISABLE)
    OperationType operationType = OperationType.STANDARD;
    
    static final class OperationTypeRef implements ParameterReference<OperationType> {
    }
    
    @Widget(title = "Contradiction resolution strategy", 
            description = """
                In case the extended columns hold contradicting addressing information (e.g. A2 in 'Cell' and 3 in 
                'Column number'), which information shall be taken to locate the cell on the sheet? This setting 
                only applies when the operation type is 'long to wide'.
                """)
    @Persistor(InconsistencyResolutionPersistor.class)
    @Effect(predicate = IsExtendedControlTable.class, type = EffectType.ENABLE)
    InconsistencyResolutionOptions inconsistencyResolutionStrategy = InconsistencyResolutionOptions.FAIL;
    
    @Layout(ResultTableStructureSection.class)
    @Widget(title = "Use first row as column header", 
            description = """
                Copy the original table's column header to a new first row. This is typically used 
                in line with the corresponding setting in KNIME's Excel Writer node.
                """)
    @Persist(configKey = XlsControlTableGeneratorNodeModel.CFGKEY_ROW_SHIFT)
    @Effect(predicate = IsStandardOperationAndInputTablePresentPredicate.class, type = EffectType.SHOW)
    boolean shiftRows = XlsControlTableGeneratorNodeModel.DEFAULT_ROW_SHIFT;
    
    @Layout(ResultTableStructureSection.class)
    @Widget(title = "Unpivot result table (for easier post-processing and re-pivoting)", 
            description = """
                Unpivot your input table, meaning that each input table cell will become an own output table row. 
                On this basis, own rules can be defined, e.g. to define tags required for many of the XLS 
                Formatting nodes.
                """)
    @Persist(configKey = XlsControlTableGeneratorNodeModel.CFGKEY_UNPIVOT)
    @Effect(predicate = IsStandardOperationAndInputTablePresentPredicate.class, type = EffectType.ENABLE)
    @ValueReference(UnpivotRef.class)
    boolean unpivot = XlsControlTableGeneratorNodeModel.DEFAULT_UNPIVOT;
    
    static final class UnpivotRef implements ParameterReference<Boolean> {
    }
    
    @Layout(ResultTableStructureSection.class)
    @Widget(title = "Add additional header columns", 
            description = """
                Add column headers like the column number, original column names, original row ID, and padded 
                XLS column name. Padding is useful as wide tables might otherwise be difficult to correctly sort 
                (e.g. AA, Y, Z order instead of Y, Z, AA → solved via 00Y, 00Z, 0AA).
                """)
    @Persist(configKey = XlsControlTableGeneratorNodeModel.CFGKEY_EXTENDED_UNPIVOT_COLUMNS)
    @Effect(predicate = IsInputTablePresentAndStandardOperationAndUnpivotPredicate.class, type = EffectType.ENABLE)
    boolean extendedUnpivotColumns = XlsControlTableGeneratorNodeModel.DEFAULT_EXTENDED_UNPIVOT_COLUMNS;
    
    static final class AlwaysTruePredicate implements EffectPredicateProvider {

        @Override
        public EffectPredicate init(final PredicateInitializer i) {
            return i.getConstant(context -> true);
        }

    }
    
    static final class IsStandardOperationAndInputTablePresentPredicate implements EffectPredicateProvider {
        
        @Override
        public EffectPredicate init(PredicateInitializer i) {
            return i.getEnum(OperationTypeRef.class).isOneOf(OperationType.STANDARD)
            		.and(i.getConstant(pi -> {
            			final var specOpt = pi.getInTableSpec(0);
            			if (specOpt.isEmpty()) {
							return false;
						}
						final var spec = specOpt.get();
						return spec.getNumColumns() != 0;
            		}));
        }
    }
    
    static final class IsInputTablePresentAndStandardOperationAndUnpivotPredicate implements EffectPredicateProvider {
        
        @Override
        public EffectPredicate init(PredicateInitializer i) {
            return  i.getPredicate(IsStandardOperationAndInputTablePresentPredicate.class)
            		.and(i.getBoolean(UnpivotRef.class).isTrue());
        }
    }
    
    static final class IsExtendedControlTable implements EffectPredicateProvider {
        
        @Override
        public EffectPredicate init(PredicateInitializer i) {
            return i.getConstant(pi -> {
            	var specOpt = pi.getInTableSpec(0);
            	if (specOpt.isEmpty()) {
            		return false;
            	}
            	return XlsFormatterControlTableAnalysisTools.isLongControlTableSpec(specOpt.get(), null, LOGGER) == 8;
            });
        }
        
    }
    
    static final class OperationTypeProvider implements StateProvider<OperationType> {
        
        @Override
        public void init(StateProviderInitializer initializer) {
            initializer.computeAfterOpenDialog();
        }
        
        @Override
        public OperationType computeState(NodeParametersInput parametersInput) {
            var specOpt = parametersInput.getInTableSpec(0);
            if (specOpt.isEmpty()) {
				return OperationType.STANDARD;
			}
            final var spec = specOpt.get();
            
			int longUnpivotedInputTableColumnCount = XlsFormatterControlTableAnalysisTools
					.isLongControlTableSpec(spec, null, LOGGER);
			return longUnpivotedInputTableColumnCount == -1 ? OperationType.STANDARD : OperationType.PIVOT_BACK;
        }
    }
    
    static final class OperationTypePersistor implements NodeParametersPersistor<OperationType> {

        @Override
        public OperationType load(NodeSettingsRO settings) throws InvalidSettingsException {
            return OperationType.getFromValue(settings.getString(
            		XlsControlTableGeneratorNodeModel.CFGKEY_OPERATION_TYPE,
                    XlsControlTableGeneratorNodeModel.DEFAULT_OPERATION_TYPE));
        }

        @Override
        public void save(OperationType obj, NodeSettingsWO settings) {
            settings.addString(XlsControlTableGeneratorNodeModel.CFGKEY_OPERATION_TYPE, obj.toString());
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{XlsControlTableGeneratorNodeModel.CFGKEY_OPERATION_TYPE}};
        }
    }
    
    static final class InconsistencyResolutionPersistor 
    	implements NodeParametersPersistor<InconsistencyResolutionOptions> {

        @Override
        public InconsistencyResolutionOptions load(NodeSettingsRO settings) throws InvalidSettingsException {
            return InconsistencyResolutionOptions.getFromValue(settings.getString(
            		XlsControlTableGeneratorNodeModel.CFGKEY_INCONSISTENCY_RESOLUTION_STRATEGY,
                    XlsControlTableGeneratorNodeModel.DEFAULT_INCONSISTENCY_RESOLUTION_STRATEGY));
        }

        @Override
        public void save(InconsistencyResolutionOptions obj, NodeSettingsWO settings) {
            settings.addString(XlsControlTableGeneratorNodeModel.CFGKEY_INCONSISTENCY_RESOLUTION_STRATEGY, 
            		obj.toString());
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{XlsControlTableGeneratorNodeModel.CFGKEY_INCONSISTENCY_RESOLUTION_STRATEGY}};
        }
    }
    
}
