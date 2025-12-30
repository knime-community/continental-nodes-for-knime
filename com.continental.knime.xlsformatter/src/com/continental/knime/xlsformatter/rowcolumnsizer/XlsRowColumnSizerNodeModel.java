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

package com.continental.knime.xlsformatter.rowcolumnsizer;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellReference;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.node.parameters.widget.choices.Label;

import com.continental.knime.xlsformatter.commons.TagBasedXlsCellFormatterNodeModel;
import com.continental.knime.xlsformatter.commons.UiValidation;
import com.continental.knime.xlsformatter.commons.XlsFormatterControlTableAnalysisTools;
import com.continental.knime.xlsformatter.commons.XlsFormatterControlTableValidator;
import com.continental.knime.xlsformatter.commons.XlsFormatterControlTableValidator.ControlTableType;
import com.continental.knime.xlsformatter.commons.XlsFormatterUiOptions;
import com.continental.knime.xlsformatter.porttype.XlsFormatterState;
import com.continental.knime.xlsformatter.porttype.XlsFormatterStateSpec;
import com.continental.knime.xlsformatter.util.XlsFormatterNodeParameterUtil;

public class XlsRowColumnSizerNodeModel extends TagBasedXlsCellFormatterNodeModel {

	// the logger instance
	private static final NodeLogger logger = NodeLogger
			.getLogger(XlsRowColumnSizerNodeModel.class);


	public enum ControlTableStyle {
		@Label(value = "Standard tags", description = """
				Tag in your input table for which the formatting of this node should be applied to.
				""")
		STANDARD("standard tags"),
		@Label(value = "Size from control table", description = """
				Don't search for tags in the input table, but use the provided non-missing size values directly.
				""")
		DIRECT("size from control table");
		
		private String m_value;
		
		private ControlTableStyle(String value) {
			m_value = value;
		}
		
		@Override
		public String toString() {
			return m_value;
		}
		
		public static ControlTableStyle getFromString(String value) {
	    return XlsFormatterUiOptions.getEnumEntryFromString(ControlTableStyle.values(), value);
		}
		
		/**
		 * Retrieves the enum entry matching the provided value.
		 * 
		 * @param value the value to look for
		 * @return the matching enum entry
		 * @throws InvalidSettingsException if no matching entry could be found
		 * @since 1.7
		 */
		public static ControlTableStyle getFromValue(final String value) throws InvalidSettingsException {
            for (final ControlTableStyle tableStyle : values()) {
                if (tableStyle.toString().equals(value)) {
                    return tableStyle;
                }
            }
            throw new InvalidSettingsException(XlsFormatterNodeParameterUtil.createInvalidEnumValueExceptionMessage(
            		ControlTableStyle.class, e -> e.toString(), value));
        }
		
	}
	static final String CFGKEY_CONTROL_TABLE_STYLE = XlsFormatterUiOptions.UI_LABEL_CONTROL_TABLE_STYLE_KEY;
	static final String DEFAULT_CONTROL_TABLE_STYLE = ControlTableStyle.STANDARD.toString();
	final SettingsModelString m_controlTableStyle =
			new SettingsModelString(CFGKEY_CONTROL_TABLE_STYLE, DEFAULT_CONTROL_TABLE_STYLE);   	

	public enum DimensionToSize {
		@Label(value = "Column width")
		COLUMN("column width"), //
		@Label(value = "Row height")
		ROW("row height");
		
		private String m_value;
		
		private DimensionToSize(String value) {
			m_value = value;
		}
		
		@Override
		public String toString() {
			return m_value;
		}
		
		public static DimensionToSize getFromString(String value) {
	    return XlsFormatterUiOptions.getEnumEntryFromString(DimensionToSize.values(), value);
		}
		
		/**
		 * Retrieves the enum entry matching the provided value.
		 * 
		 * @param value the value to look for
		 * @return the matching enum entry
		 * @throws InvalidSettingsException if no matching entry could be found
		 * @since 1.7
		 */
		public static DimensionToSize getFromValue(final String value) throws InvalidSettingsException {
            for (final DimensionToSize dimensionToSize : values()) {
                if (dimensionToSize.toString().equals(value)) {
                    return dimensionToSize;
                }
            }
            throw new InvalidSettingsException(XlsFormatterNodeParameterUtil.createInvalidEnumValueExceptionMessage(
            		DimensionToSize.class, e -> e.toString(), value));
        }

	}
	
	static final String CFGKEY_ROW_COLUMN_SIZE = "RowColumnSize";
	static final String DEFAULT_ROW_COLUMN_SIZE = DimensionToSize.COLUMN.toString();
	final SettingsModelString m_rowColumnSize =
			new SettingsModelString(CFGKEY_ROW_COLUMN_SIZE, DEFAULT_ROW_COLUMN_SIZE);

	static final String CFGKEY_TAG = "Tag";
	static final String DEFAULT_TAG = "header";
	final SettingsModelString m_tag =
			new SettingsModelString(CFGKEY_TAG, DEFAULT_TAG);

	static final String CFGKEY_SIZE = "Size";
	static final double DEFAULT_SIZE = 14.0;
	final SettingsModelDouble m_size =
			new SettingsModelDouble(CFGKEY_SIZE, DEFAULT_SIZE);
	
	static final String CFGKEY_AUTO_SIZE = "AutoSize";
	static final boolean DEFAULT_AUTO_SIZE = false;
	final SettingsModelBoolean m_autoSize =
			new SettingsModelBoolean(CFGKEY_AUTO_SIZE, DEFAULT_AUTO_SIZE);



	/**
	 * Constructor for the node model.
	 */
	protected XlsRowColumnSizerNodeModel() {

		super(
				new PortType[] { BufferedDataTable.TYPE, XlsFormatterState.TYPE_OPTIONAL },
				new PortType[] { XlsFormatterState.TYPE });
	}

	/**
	 * {@inheritDoc}
	 */
	protected PortObject[] execute(PortObject[] inObjects, final ExecutionContext exec) throws Exception { 
		
		XlsFormatterState xlsf = XlsFormatterState.getDeepClone(inObjects[1]);
		XlsFormatterState.SheetState xlsfs = xlsf.getCurrentSheetStateForModification();

		ControlTableStyle controlTableStyle = ControlTableStyle.getFromString(m_controlTableStyle.getStringValue());
		DimensionToSize dimension = DimensionToSize.getFromString(m_rowColumnSize.getStringValue());
		
		if (!XlsFormatterControlTableValidator.isControlTable((BufferedDataTable)inObjects[0],
				controlTableStyle == ControlTableStyle.STANDARD ? ControlTableType.STRING : ControlTableType.DOUBLE, exec, logger))
			throw new IllegalArgumentException("The provided input table is not a valid XLS control table. See log for details.");
		
		if (controlTableStyle == ControlTableStyle.DIRECT) {
			
			Map<CellAddress, Double> cellContentMap =
					XlsFormatterControlTableAnalysisTools.getCellsWithDoubleValues((BufferedDataTable)inObjects[0], exec, logger);
			
			Set<Integer> processedIndices = new HashSet<Integer>(); // row or columns already processed
			for (CellAddress cell : cellContentMap.keySet()) {
				int index = dimension == DimensionToSize.COLUMN ? cell.getColumn() : cell.getRow();
				if (processedIndices.contains(index))
					throw new IllegalArgumentException("Duplicate instruction found for " +
							(dimension == DimensionToSize.COLUMN ? "column" : "row") + " " +
							(dimension == DimensionToSize.COLUMN ? CellReference.convertNumToColString(index) : index + 1) + ". Only one entry allowed per column/row.");
				processedIndices.add(index);
				if (dimension == DimensionToSize.COLUMN)
					xlsfs.columnWidths.put(index, cellContentMap.get(cell));
				else
					xlsfs.rowHeights.put(index, cellContentMap.get(cell));
			}
		}
		else { // standard tags provided in UI
			
			List<CellAddress> targetCells =
					XlsFormatterControlTableAnalysisTools.getCellsMatchingTag((BufferedDataTable)inObjects[0], m_tag.getStringValue().trim(), exec, logger);
			warnOnNoMatchingTags(targetCells, m_tag.getStringValue().trim());
			
			for (CellAddress cell : targetCells) {
				if (dimension == DimensionToSize.COLUMN)
					xlsfs.columnWidths.put(cell.getColumn(), m_autoSize.getBooleanValue() ? null : m_size.getDoubleValue());
				else
					xlsfs.rowHeights.put(cell.getRow(), m_size.getDoubleValue());
			}
		}
		
		return new PortObject[] { xlsf };
	}

	@Override
	protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {   	
		
		if (!XlsFormatterControlTableValidator.isControlTableSpecDoubleOrString((DataTableSpec)inSpecs[0], logger))
			throw new InvalidSettingsException("The configured input table header is not that of a valid XLS Formatting control table. See log for details.");

		m_controlTableStyle.setStringValue(
				XlsFormatterControlTableAnalysisTools.isDoubleControlTableSpecCandidate((DataTableSpec)inSpecs[0]) ?
						ControlTableStyle.DIRECT.toString() : ControlTableStyle.STANDARD.toString());
		
		return new PortObjectSpec[] { inSpecs[1] == null ? XlsFormatterStateSpec.getEmptySpec() : ((XlsFormatterStateSpec)inSpecs[1]).getCopy() };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		
		m_autoSize.setEnabled(m_controlTableStyle.getStringValue().equals(XlsFormatterUiOptions.UI_LABEL_CONTROL_TABLE_STYLE_STANDARD)&&
						m_rowColumnSize.getStringValue().equals(DimensionToSize.COLUMN.toString()));
		
		m_controlTableStyle.saveSettingsTo(settings);
		m_rowColumnSize.saveSettingsTo(settings);
		m_tag.saveSettingsTo(settings);
		m_size.saveSettingsTo(settings);
		m_autoSize.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		
		m_controlTableStyle.loadSettingsFrom(settings);
		m_rowColumnSize.loadSettingsFrom(settings);
		m_tag.loadSettingsFrom(settings);
		m_size.loadSettingsFrom(settings);
		m_autoSize.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {

		m_controlTableStyle.validateSettings(settings);
		m_rowColumnSize.validateSettings(settings);
		m_tag.validateSettings(settings);
		m_size.validateSettings(settings);
		m_autoSize.validateSettings(settings);
		
		if (settings.getBoolean("performTagValiation", false)) {
			UiValidation.validateTagString(settings.getString(CFGKEY_TAG));
	    }
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException, CanceledExecutionException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException, CanceledExecutionException {
	}
}
