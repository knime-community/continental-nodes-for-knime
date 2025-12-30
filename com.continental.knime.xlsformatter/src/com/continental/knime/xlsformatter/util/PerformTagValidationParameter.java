package com.continental.knime.xlsformatter.util;

import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.migration.DefaultProvider;
import org.knime.node.parameters.migration.Migration;
import org.knime.node.parameters.updates.StateProvider;
import org.knime.node.parameters.updates.ValueProvider;

/**
 * Parameter to perform tag validation with default value true on dialog open.
 * 
 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
public class PerformTagValidationParameter implements NodeParameters {
	
    @Migration(LoadFalseIfAbsent.class)
    @ValueProvider(LoadTrueOnOpenDialog.class)
    boolean m_performTagValiation = true;

    static final class LoadFalseIfAbsent implements DefaultProvider<Boolean> {
        @Override
        public Boolean getDefault() {
            return false;
        }
    }

    static final class LoadTrueOnOpenDialog implements StateProvider<Boolean> {
        @Override
        public void init(StateProviderInitializer initializer) {
            initializer.computeBeforeOpenDialog();
        }

        @Override
        public Boolean computeState(NodeParametersInput parametersInput) {
            return true;
        }
    }
    
}
