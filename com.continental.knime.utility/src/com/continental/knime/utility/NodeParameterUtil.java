package com.continental.knime.utility;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.node.parameters.persistence.NodeParametersPersistor;

public class NodeParameterUtil {

    /**
     * Classic dialog had possibility to select RowID, but RowID could not be applied, so we changed to string and load 
     * null if the default is currently selected to have a nice state if no columns are available for selection 
     * (we have no values present instead of (MISSING) <m_columnNameDefault>)
     */
    public abstract static class LegacyColumnNamePersistor implements NodeParametersPersistor<String> {

    	private String m_columnNameDefault;
    	
    	protected LegacyColumnNamePersistor(final String columnNameDefault) {
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
