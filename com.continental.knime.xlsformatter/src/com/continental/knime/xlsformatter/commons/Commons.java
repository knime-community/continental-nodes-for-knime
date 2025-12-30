/*
 * Continental Nodes for KNIME
 * Copyright (C) 2019  Continental AG, Hanover, Germany
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.continental.knime.xlsformatter.commons;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.node.parameters.persistence.NodeParametersPersistor;
import org.knime.node.parameters.widget.choices.Label;

import com.continental.knime.xlsformatter.commons.Commons.Modes;
import com.continental.knime.xlsformatter.util.XlsFormatterNodeParameterUtil;
import com.continental.knime.xlsformatter.xlscontroltablegenerator.XlsControlTableGeneratorNodeModel.InconsistencyResolutionOptions;
import com.continental.knime.xlsformatter.xlscontroltablemerger.XlsControlTableMergerNodeModel;

public class Commons {

	/**
	 * Resolves a file path to a local path, esp. in regards to knime://knime.workflow/ syntax.
	 */
	@Deprecated
	public static String resolveKnimePath(String path) throws IOException, URISyntaxException {
		if (path.startsWith("knime:"))
			return org.knime.core.util.pathresolve.ResolverUtil.resolveURItoLocalFile(new URI("knime", path.substring(6), null)).getAbsolutePath();
		else
			return path;
	}
	
	/**
	 * Parses an integer and returns null on mismatch.
	 */
	public static Integer parseIntSilently(String value) {
		try {
			return Integer.valueOf(Integer.parseInt(value));
		}
		catch (NumberFormatException ne) {
			return null;
		}
	}
	
	/**
	 * Parses an integer and returns an IllegalArgumentException on mismatch.
	 */
	public static int parseInt(String value) throws IllegalArgumentException {
		try {
			return Integer.valueOf(Integer.parseInt(value));
		}
		catch (NumberFormatException ne) {
			throw new IllegalArgumentException("Expected an integer value (i.e. a whole number), but saw \"" + value + "\".");
		}
	}
	
	/**
	 * Helps to generate number/text combinations that have correct singular, plural form, e.g. "1 item", "2 items"
	 * @param number The numeric value that the returned String starts with.
	 * @param text The text at the end, containing (s) as the placeholder for plural that will be replaced depending on the number.
	 */
	public static String resolvePluralString(int number, String text) {
		if (!text.startsWith(" "))
			text = " " + text;
		String pluralReplacement = number == 1 ? "" : "s";
		return number + text.replace("(s)", pluralReplacement);
	}
	
	/**
	 * Modes of how to add two tables.
	 */
	public static enum Modes {
		
		@Label(value = "Append")
		APPEND, //
		@Label(value = "Overwrite")
		OVERWRITE;

		@Override
		public String toString() {
			switch (this) {
			case APPEND:
				return "append";
			case OVERWRITE:
				return "overwrite";
			default:
				throw new IllegalArgumentException();
			}
		}

		public static Modes getFromString(String value) {
			return XlsFormatterUiOptions.getEnumEntryFromString(Modes.values(), value);
		}

		/**
		 * Get enum entry from its String value.
		 * 
		 * @param value The String value.
		 * @return The enum entry.
		 * @throws InvalidSettingsException If no enum entry could be found for the given String value.
		 * @since 1.7
		 */
		public static Modes getFromValue(final String value) throws InvalidSettingsException {
			for (final Modes mode : values()) {
				if (mode.toString().equals(value)) {
					return mode;
				}
			}
			throw new InvalidSettingsException(XlsFormatterNodeParameterUtil.createInvalidEnumValueExceptionMessage(
					Modes.class, e -> e.toString(), value));
		}
		
	}
	
	/**
	 * Persistor for merge mode.
	 */
    public static abstract class ModePersistor implements NodeParametersPersistor<Modes> {
    	
    	private String m_configKey;
    	
    	private String m_defaultMode;
    	
    	protected ModePersistor(final String configKey, final String defaultMode) {
			m_configKey = configKey;
			m_defaultMode = defaultMode;
    	}

        @Override
        public Modes load(NodeSettingsRO settings) throws InvalidSettingsException {
            return Modes.getFromValue(settings.getString(m_configKey, m_defaultMode));
        }

        @Override
        public void save(Modes obj, NodeSettingsWO settings) {
            settings.addString(m_configKey, obj.toString());
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{m_configKey}};
        }
        
    }
	
}
