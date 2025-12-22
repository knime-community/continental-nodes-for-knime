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
    
package com.continental.knime.utility.networkcomponentsplitter;

import org.knime.core.data.StringValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.webui.node.dialog.defaultdialog.internal.widget.PersistWithin;
import org.knime.core.webui.node.dialog.defaultdialog.util.updates.StateComputationFailureException;
import org.knime.node.parameters.NodeParameters;
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
import org.knime.node.parameters.updates.StateProvider;
import org.knime.node.parameters.updates.ValueProvider;
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.util.CompatibleColumnsProvider.StringColumnsProvider;
import org.knime.node.parameters.widget.text.TextInputWidget;
import org.knime.node.parameters.widget.text.TextInputWidgetValidation.PatternValidation.IsNotBlankValidation;

import static com.continental.knime.utility.networkcomponentsplitter.NetworkComponentSplitterNodeModel.CFGKEY_COLUM_NAME1;
import static com.continental.knime.utility.networkcomponentsplitter.NetworkComponentSplitterNodeModel.CFGKEY_COLUMN_NAME2;
import static com.continental.knime.utility.networkcomponentsplitter.NetworkComponentSplitterNodeModel.CFGKEY_MISSING;
import static com.continental.knime.utility.networkcomponentsplitter.NetworkComponentSplitterNodeModel.CFGKEY_OUTPUT_COLUMN_NAME_NODE;
import static com.continental.knime.utility.networkcomponentsplitter.NetworkComponentSplitterNodeModel.CFGKEY_OUTPUT_COLUMN_NAME_CLUSTER;
import static com.continental.knime.utility.networkcomponentsplitter.NetworkComponentSplitterNodeModel.DEFAULT_OUTPUT_COLUMN_NAME_NODE;

import java.util.function.Supplier;

import static com.continental.knime.utility.networkcomponentsplitter.NetworkComponentSplitterNodeModel.DEFAULT_OUTPUT_COLUMN_NAME_CLUSTER;
import static com.continental.knime.utility.networkcomponentsplitter.NetworkComponentSplitterNodeModel.DEFAULT_MISSING;

/**
 * Node parameters for Network Component Splitter.
 * 
 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
@LoadDefaultsForAbsentFields
@SuppressWarnings("restriction")
final class NetworkComponentSplitterNodeParameters implements NodeParameters {
    
	@Section(title = "Missing Value Handling")
	interface MissingValueHandling {
	}

	@Section(title = "Output Column Names")
	@After(MissingValueHandling.class)
	interface OutputColumnNames {
	}

    @Widget(title = "Select node1 column", description = """
    		The input table's String-typed column holding the name of the first node that represents an edge in the 
    		network.
    		""")
    @PersistWithin({CFGKEY_COLUM_NAME1})
    @Persistor(InputColumn1Persistor.class)
    @ChoicesProvider(StringColumnsProvider.class)
	@ValueReference(InputColumn1Ref.class)
	@ValueProvider(InputColumn1Provider.class)
    @Effect(predicate = HasInputTableSpec.class, type = EffectType.ENABLE)
	String m_inputColumn1;
    
	static final class InputColumn1Ref implements ParameterReference<String> {
	}
	
    @Widget(title = "Select node2 column", description = """
    		The input table's String-typed column holding the name of the second node that represents an edge in the 
    		network.
    		""")
    @PersistWithin({CFGKEY_COLUMN_NAME2})
    @Persistor(InputColumn2Persistor.class)
    @ChoicesProvider(StringColumnsProvider.class)
    @ValueReference(InputColumn2Ref.class)
    @ValueProvider(InputColumn2Provider.class)
    @Effect(predicate = HasInputTableSpec.class, type = EffectType.ENABLE)
    String m_inputColumn2;
    
    static final class InputColumn2Ref implements ParameterReference<String> {
	}
    
    @Layout(MissingValueHandling.class)
    @Widget(title = "Handle missing value as node", description = """
    		 If checked, the missing value is treated as a valid node name and will appear as a node in the output 
    		 table. If unchecked, missing values are resolved by (1) ignoring edges of both missing values and (2) 
    		 treating edges between a valid String and a missing value as self-relation of the valid String node. Hence,
    		  if unchecked, missing value will not appear in the output table.
    		 """)
    @Persist(configKey = CFGKEY_MISSING)
    boolean m_missingValueAllowedAsOwnNode = DEFAULT_MISSING;
    
    @Layout(OutputColumnNames.class)
    @Widget(title = "Nodelist column name", description = """
    		Name of the output table's first column, containing node names originating from both columns of the input 
    		table.
    		""")
    @Persist(configKey = CFGKEY_OUTPUT_COLUMN_NAME_NODE)
    @TextInputWidget(patternValidation = IsNotBlankValidation.class)
    String m_outputColumnNameNode = DEFAULT_OUTPUT_COLUMN_NAME_NODE;
    
    @Layout(OutputColumnNames.class)
    @Widget(title = "ClusterID column name", 
            description = "Name of the output table's second column, assigning a cluster ID to every node name.")
    @Persist(configKey = CFGKEY_OUTPUT_COLUMN_NAME_CLUSTER)
    @TextInputWidget(patternValidation = IsNotBlankValidation.class)
    String m_outputColumnNameCluster = DEFAULT_OUTPUT_COLUMN_NAME_CLUSTER;
    
    static final class HasInputTableSpec implements EffectPredicateProvider {

		@Override
		public EffectPredicate init(PredicateInitializer i) {
			return i.getConstant(pi -> pi.getInTableSpec(0).isPresent());
		}
    	
    }
    
    static final class InputColumn1Provider implements StateProvider<String> {

    	private Supplier<String> m_inputColumn1Supplier;
    	
		@Override
		public void init(StateProviderInitializer initializer) {
			initializer.computeBeforeOpenDialog();
			m_inputColumn1Supplier = initializer.getValueSupplier(InputColumn1Ref.class);
		}

		@Override
		public String computeState(NodeParametersInput parametersInput) throws StateComputationFailureException {
            final var currentValue = m_inputColumn1Supplier.get();
			if (currentValue != null && !currentValue.isEmpty()) {
				if (currentValue.equals(NetworkComponentSplitterNodeModel.DEFAULT_COLUMN_NAME1)) {
					return null;
				}
				throw new StateComputationFailureException();
			}
			
			var tableSpecOpt = parametersInput.getInTableSpec(0);
            if (tableSpecOpt.isEmpty()) {
                throw new StateComputationFailureException();
            }
            return tableSpecOpt.get().stream() //
                .filter(colSpec -> colSpec.getType().isCompatible(StringValue.class)) //
                .findFirst() //
                .map(colSpec -> colSpec.getName()) //
                .orElseThrow(StateComputationFailureException::new);
		}
        
    }
    
    static final class InputColumn2Provider implements StateProvider<String> {

    	private Supplier<String> m_inputColumn2Supplier;
    	
		@Override
		public void init(StateProviderInitializer initializer) {
			initializer.computeBeforeOpenDialog();
			m_inputColumn2Supplier = initializer.getValueSupplier(InputColumn2Ref.class);
		}

		@Override
		public String computeState(NodeParametersInput parametersInput) throws StateComputationFailureException {
            final var currentValue = m_inputColumn2Supplier.get();
			if (currentValue != null && !currentValue.isEmpty()) {
				if (currentValue.equals(NetworkComponentSplitterNodeModel.DEFAULT_COLUMN_NAME2)) {
					return null;
				}
				throw new StateComputationFailureException();
			}
			
            var tableSpecOpt = parametersInput.getInTableSpec(0);
            if (tableSpecOpt.isEmpty()) {
                throw new StateComputationFailureException();
            }
            final var compatibleChoices = tableSpecOpt.get().stream() //
                .filter(colSpec -> colSpec.getType().isCompatible(StringValue.class)) //
                .map(colSpec -> colSpec.getName()) //
                .toList();
            if (compatibleChoices.size() == 0) {
				throw new StateComputationFailureException();
			} else if (compatibleChoices.size() == 1) {
				return compatibleChoices.get(0);
			} else {
				return compatibleChoices.get(1);
			}
        }
        
    }
    
    static final class InputColumn1Persistor extends InputColumnPersistor {

		protected InputColumn1Persistor() {
			super(NetworkComponentSplitterNodeModel.DEFAULT_COLUMN_NAME1);
		}
    	
    }
    
    static final class InputColumn2Persistor extends InputColumnPersistor {

		protected InputColumn2Persistor() {
			super(NetworkComponentSplitterNodeModel.DEFAULT_COLUMN_NAME2);
		}
    	
    }
    
    /**
     * Classic dialog had possibility to select RowID, but RowID could not be applied, so we changed to string and load 
     * null if the default is currently selected to have a nice state if no columns are available for selection 
     * (we have no values present instead of (MISSING) <m_columnNameDefault>)
     */
    abstract static class InputColumnPersistor implements NodeParametersPersistor<String> {

    	private String m_columnNameDefault;
    	
    	protected InputColumnPersistor(final String columnNameDefault) {
			m_columnNameDefault = columnNameDefault;
    	}
    	
        @Override
        public String load(final NodeSettingsRO settings) throws InvalidSettingsException {
        	final var columnName = settings.getString("columnName");
        	return columnName == m_columnNameDefault ? null : columnName;
        }

		@Override
		public void save(final String param, final NodeSettingsWO settings) {
			settings.addBoolean("useRowID", false);
			settings.addString("columnName", param == null ? m_columnNameDefault : param);
		}

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{"columnName"}};
        }
        
    }
    
}
