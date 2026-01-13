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
    
package com.continental.knime.utility.fiforesolver;


import java.util.List;
import java.util.stream.Collectors;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.StringValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.webui.node.dialog.defaultdialog.internal.widget.PersistWithin;
import org.knime.core.webui.node.dialog.defaultdialog.util.updates.StateComputationFailureException;
import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.migration.LoadDefaultsForAbsentFields;
import org.knime.node.parameters.persistence.NodeParametersPersistor;
import org.knime.node.parameters.persistence.Persist;
import org.knime.node.parameters.persistence.Persistor;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.ValueProvider;
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.updates.legacy.AutoGuessValueProvider;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.Label;
import org.knime.node.parameters.widget.choices.ValueSwitchWidget;
import org.knime.node.parameters.widget.choices.util.ColumnSelectionUtil;
import org.knime.node.parameters.widget.choices.util.CompatibleColumnsProvider.DoubleColumnsProvider;
import org.knime.node.parameters.widget.choices.util.CompatibleColumnsProvider.StringColumnsProvider;

import com.continental.knime.utility.NodeParameterUtil.LegacyColumnNamePersistor;

/**
 * Node parameters for FIFO / LIFO Resolver.
 * 
 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
@LoadDefaultsForAbsentFields
@SuppressWarnings("restriction")
class FifoResolverNodeParameters implements NodeParameters {
    
    @Widget(title = "Queue resolution mode", description = """
    		Choose one of the queue resolution modes 'first-in-first-out' (FIFO) or 'last-in-first-out' (LIFO).
    		""")
    @ValueSwitchWidget
    @Persistor(QueueModePersistor.class)
    QueueMode m_queueMode = QueueMode.FIFO;
    
    @Widget(title = "Grouping column", description = """
    		Name of the input table column used for grouping. Per unique value in this column, queues are analyzed 
    		independently. E.g. use a material number column here for a logistics use case.
            """)
    @ChoicesProvider(StringColumnsProvider.class)
    @ValueProvider(GroupingColumnAutoGuessProvider.class)
    @ValueReference(GroupingColumnRef.class)
    @PersistWithin(FifoResolverNodeModel.CFGKEY_COLUM_NAME_GROUP)
    @Persistor(GroupingColumnPersistor.class)
    String m_groupingColumn;
    
    static final class GroupingColumnRef implements ParameterReference<String> {
    }
    
    @Widget(title = "Quantity column", description = """
    		Name of the input table column that holds the quantity of each operation. E.g. use a 'number of stocks' 
    		column here for a finance/tax use case.
            """)
    @ChoicesProvider(DoubleColumnsProvider.class)
    @ValueProvider(QuantityColumnAutoGuessProvider.class)
    @ValueReference(QuantityColumnRef.class)
    @PersistWithin(FifoResolverNodeModel.CFGKEY_COLUMN_NAME_QTY)
    @Persistor(QuantityColumnPersistor.class)
    String m_quantityColumn;
    
    static final class QuantityColumnRef implements ParameterReference<String> {
	}
    
    @Widget(title = "Fail at queuing inconsistency", description = """
    		If checked, an inconsistent queue history will lead to an error in node execution. The only inconsistency 
    		analyzed is higher total outflow quantity than prior inflow quantity. E.g. a queue history of 2, 2, -5, 3 
    		would trigger this, because the outflowing 5 are exceeding the 4 pieces in the queue at that moment. If 
    		unchecked, the queue resolution will add a missing value in the RowId_IN output column.
            """)
    @Persist(configKey = FifoResolverNodeModel.CFGKEY_FAIL_AT_INCONSISTENCY)
    boolean m_failAtInconsistency = FifoResolverNodeModel.DEFAULT_FAIL_AT_INCONSISTENCY;
    
    static final class GroupingColumnAutoGuessProvider extends AutoGuessValueProvider<String> {

		protected GroupingColumnAutoGuessProvider() {
			super(GroupingColumnRef.class);
		}

		@Override
		protected boolean isEmpty(String value) {
			return value == null || value.isEmpty()
					|| value.equals(FifoResolverNodeModel.DEFAULT_COLUMN_NAME_GROUP);
		}

		@Override
		protected String autoGuessValue(NodeParametersInput parametersInput)
				throws StateComputationFailureException {
			return ColumnSelectionUtil.getFirstCompatibleColumnOfFirstPort(parametersInput, StringValue.class)
					.map(DataColumnSpec::getName)
					.orElse(null);
		}
    	
    }
    
    static final class QuantityColumnAutoGuessProvider extends AutoGuessValueProvider<String> {

		protected QuantityColumnAutoGuessProvider() {
			super(QuantityColumnRef.class);
		}

		@Override
		protected boolean isEmpty(String value) {
			return value == null || value.isEmpty()
					|| value.equals(FifoResolverNodeModel.DEFAULT_COLUMN_NAME_QTY);
		}

		@Override
		protected String autoGuessValue(NodeParametersInput parametersInput)
				throws StateComputationFailureException {
			return ColumnSelectionUtil.getFirstCompatibleColumnOfFirstPort(parametersInput, DoubleValue.class)
					.map(DataColumnSpec::getName)
					.orElse(null);
		}
    	
    }
    
    static final class QueueModePersistor implements NodeParametersPersistor<QueueMode> {
        
        @Override
        public QueueMode load(final NodeSettingsRO settings) throws InvalidSettingsException {
            return QueueMode.getFromValue(settings.getString(FifoResolverNodeModel.CFGKEY_MODE,
				FifoResolverNodeModel.DEFAULT_MODE));
        }
        
        @Override
        public void save(final QueueMode obj, final NodeSettingsWO settings) {
            settings.addString(FifoResolverNodeModel.CFGKEY_MODE, obj.getValue());
        }
        
        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{FifoResolverNodeModel.CFGKEY_MODE}};
        }

	}

	static final class GroupingColumnPersistor extends LegacyColumnNamePersistor {

		protected GroupingColumnPersistor() {
			super(FifoResolverNodeModel.CFGKEY_COLUM_NAME_GROUP);
		}
		
	}
    
	static final class QuantityColumnPersistor extends LegacyColumnNamePersistor {

		protected QuantityColumnPersistor() {
			super(FifoResolverNodeModel.CFGKEY_COLUMN_NAME_QTY);
		}
		
	}
    
    enum QueueMode {
    	
        @Label(value = "FIFO", description = """
        		The queue is analyzed as if the first-in-first-out principle was applied, e.g. a supermarket queue.
        		""")
        FIFO(FifoResolverNodeModel.OPTION_MODE_FIFO),
        
        @Label(value = "LIFO", description = """
        		The queue is analyzed as if the last-in-first-out principle was applied, e.g. a box that can only be 
        		filled and emptied from one side.
        		""")
        LIFO(FifoResolverNodeModel.OPTION_MODE_LIFO);
        
        private String m_value;
        
        QueueMode(final String value) {
			m_value = value;
		}
        
        String getValue() {
			return m_value;
		}
        
        static QueueMode getFromValue(final String value) throws InvalidSettingsException {
            for (final QueueMode mode : values()) {
                if (mode.getValue().equals(value)) {
                    return mode;
                }
            }
            throw new InvalidSettingsException(createInvalidSettingsExceptionMessage(value));
        }

        private static String createInvalidSettingsExceptionMessage(final String name) {
            var values = List.of(FifoResolverNodeModel.OPTION_MODE_FIFO, FifoResolverNodeModel.OPTION_MODE_LIFO)
            		.stream().collect(Collectors.joining(", "));
            return String.format("Invalid value '%s'. Possible values: %s", name, values);
        }
        
    }
    
}
