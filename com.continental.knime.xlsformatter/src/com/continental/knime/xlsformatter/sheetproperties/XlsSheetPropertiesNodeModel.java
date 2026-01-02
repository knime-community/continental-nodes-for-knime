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

package com.continental.knime.xlsformatter.sheetproperties;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.node.parameters.widget.choices.Label;

import com.continental.knime.xlsformatter.commons.TagBasedXlsCellFormatterNodeModel;
import com.continental.knime.xlsformatter.commons.UiValidation;
import com.continental.knime.xlsformatter.commons.WarningMessageContainer;
import com.continental.knime.xlsformatter.commons.XlsFormatterControlTableAnalysisTools;
import com.continental.knime.xlsformatter.commons.XlsFormatterControlTableValidator;
import com.continental.knime.xlsformatter.commons.XlsFormatterTagTools;
import com.continental.knime.xlsformatter.commons.XlsFormatterUiOptions;
import com.continental.knime.xlsformatter.porttype.XlsFormatterState;
import com.continental.knime.xlsformatter.porttype.XlsFormatterStateSpec;
import com.continental.knime.xlsformatter.util.XlsFormatterNodeParameterUtil;

public class XlsSheetPropertiesNodeModel extends TagBasedXlsCellFormatterNodeModel {

	// the logger instance
	private static final NodeLogger logger = NodeLogger
			.getLogger(XlsSheetPropertiesNodeModel.class);

	static final String CFGKEY_TAG = "Tag";
	static final String DEFAULT_TAG = "header";
	final SettingsModelString m_tag =
			new SettingsModelString(CFGKEY_TAG, DEFAULT_TAG);

	public enum FunctionOptions {
		@Label(value = "Freeze sheet at tagged cell", description = """
				Freeze your sheet at a specific cell (more precisely: at its top-left corner) identified via a tag in 
				the control table. Note that the tag may occur more than once in the control table. In this case, the 
				top-left-most occurrence is used.
				""")
		FREEZE("freeze sheet at tagged cell"),
		@Label(value = "Set auto-filter for tagged cell range", description = """
				Set an auto-filter for a range based on a single rectangular range of a matching tag in the control 
				table.
				""")
		AUTOFILTER("set auto-filter for tagged cell range"),
		@Label(value = "Hide columns of tagged cells", description = """
				Hide the columns from your sheet based on a matching tag in any of the columns' cells.
				""")
		HIDE_COLUMNS("hide columns of tagged cells"),
		@Label(value = "Hide rows of tagged cells", description = """
				Hide the rows from your sheet based on a matching tag in any of the rows' cells.
				""")
		HIDE_ROWS("hide rows of tagged cells");

		private String m_value;
		
		FunctionOptions(String value) {
			m_value = value;
		}
		
		@Override
		public String toString() {
			return m_value;
		}

		public static FunctionOptions getFromString(String value) {
			return XlsFormatterUiOptions.getEnumEntryFromString(FunctionOptions.values(), value);
		}
		
		/**
		 * Retrieves the enum option from its string value
		 * 
		 * @param value the string value
		 * @return the corresponding enum option
		 * @throws InvalidSettingsException if the value does not correspond to any option
		 * @since 1,7
		 */
		public static FunctionOptions getFromValue(final String value) throws InvalidSettingsException {
            for (final FunctionOptions option : values()) {
                if (option.toString().equals(value)) {
                    return option;
                }
            }
            throw new InvalidSettingsException(XlsFormatterNodeParameterUtil.createInvalidEnumValueExceptionMessage(
            		FunctionOptions.class, e -> e.toString(), value));
        }
	
	}

	static final String CFGKEY_FUNCTION = "Function";
	static final String DEFAULT_FUNCTION = FunctionOptions.FREEZE.toString();
	final SettingsModelString m_function =
			new SettingsModelString(CFGKEY_FUNCTION, DEFAULT_FUNCTION);


	/**
	 * Constructor for the node model.
	 */
	protected XlsSheetPropertiesNodeModel() {
		super(
				new PortType[] {BufferedDataTable.TYPE, XlsFormatterState.TYPE_OPTIONAL },
				new PortType[] { XlsFormatterState.TYPE });
	}

	/**
	 * {@inheritDoc}
	 */
	protected PortObject[] execute(PortObject[] inObjects, final ExecutionContext exec) throws Exception { 

		if (!XlsFormatterControlTableValidator.isControlTable((BufferedDataTable)inObjects[0], exec, logger))
			throw new IllegalArgumentException("The provided input table is not a valid XLS control table. See log for details.");
		
		XlsFormatterState xlsf = XlsFormatterState.getDeepClone(inObjects[1]);
		WarningMessageContainer warningMessageContainer = new WarningMessageContainer();

		BufferedDataTable inputTable = (BufferedDataTable)inObjects[0];
		FunctionOptions function = FunctionOptions.getFromString(m_function.getStringValue());

		List<CellAddress> matchingCells = XlsFormatterControlTableAnalysisTools.getCellsMatchingTag(
				inputTable, m_tag.getStringValue(), exec, logger);
		if (matchingCells == null || matchingCells.size() == 0) {
			warnOnNoMatchingTags(matchingCells, m_tag.getStringValue().trim()); // not feeding the warningMessageContainer, because next line is return statement
			return new PortObject[] { xlsf };
		}

		switch (function) {
		case FREEZE:
			CellAddress currentCandidate = matchingCells.get(0);
			boolean isAmbiguousCandidate = false;
			for (CellAddress cell : matchingCells)
				if (cell.getColumn() + cell.getRow() < currentCandidate.getColumn() + currentCandidate.getRow()) {
					currentCandidate = cell;
					isAmbiguousCandidate = false;
				}
				else if (!cell.equals(currentCandidate) && cell.getColumn() + cell.getRow() == currentCandidate.getColumn() + currentCandidate.getRow()) {
					isAmbiguousCandidate = true;
					if (Math.abs(cell.getColumn() - cell.getRow()) < Math.abs(currentCandidate.getColumn() - currentCandidate.getRow()))
						currentCandidate = cell;
				}
			if (currentCandidate.getRow() == 0 && currentCandidate.getColumn() == 0) // warn at freeze at A1
				warningMessageContainer.addMessage("Freeze cell A1 means no visual freeze effect. Did you intend to freeze at the top-left corner of cell B2?");
			else if (isAmbiguousCandidate)
				warningMessageContainer.addMessage("Multiple cells matched that provided tag and a top-left one could not be identified unambiguously.");
			xlsf.getCurrentSheetStateForModification().freezeSheetAtTopLeftCornerOfCell = currentCandidate;
			break;

		case AUTOFILTER:
			List<CellRangeAddress> ranges = XlsFormatterControlTableAnalysisTools.getRangesFromTag(inputTable, m_tag.getStringValue(), false, true, warningMessageContainer, exec, logger);
			logger.debug("Detected auto-filter range as: " + ranges.stream().map(r -> r.formatAsString()).collect(Collectors.joining(";")));
			if (ranges.size() != 1)
				throw new IllegalArgumentException("For auto-filter, only one rectangular range may match the searched tag.");
			xlsf.getCurrentSheetStateForModification().autoFilterRange = ranges.get(0);
			break;

		case HIDE_COLUMNS:
			xlsf.getCurrentSheetStateForModification().hiddenColumns.addAll(matchingCells.stream().map(c -> c.getColumn()).collect(Collectors.toList()));
			break;

		case HIDE_ROWS:
			xlsf.getCurrentSheetStateForModification().hiddenRows.addAll(matchingCells.stream().map(c -> c.getRow()).collect(Collectors.toList()));
			break;

		default:
			throw new IllegalArgumentException();
		}

		if (warningMessageContainer.hasMessage())
			setWarningMessage(warningMessageContainer.getMessage());
		
		return new PortObject[] { xlsf };
	}

	@Override
	protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {   	

		if (m_tag.getStringValue().trim().equals(""))
			throw new IllegalArgumentException("Empty tag is not allowed, you need to enter something here that is also present in your control table (e.g., \"x\" or \"data\"");

		if (!XlsFormatterTagTools.isValidSingleTag(m_tag.getStringValue().trim()))
			throw new IllegalArgumentException("Only a single tag is allowed, no comma-separated list.");

		if (!XlsFormatterControlTableValidator.isControlTableSpec((DataTableSpec)inSpecs[0], logger))
			throw new InvalidSettingsException("The configured input table header is not that of a valid XLS Formatting control table. See log for details.");
		
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

		m_tag.saveSettingsTo(settings);
		m_function.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {

		m_tag.loadSettingsFrom(settings);
		m_function.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {

		m_tag.validateSettings(settings);
		m_function.validateSettings(settings);
		
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
