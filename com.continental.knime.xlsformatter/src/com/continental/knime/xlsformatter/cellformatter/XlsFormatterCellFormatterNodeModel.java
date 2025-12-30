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

package com.continental.knime.xlsformatter.cellformatter;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.util.CellAddress;
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
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.node.parameters.widget.choices.Label;

import com.continental.knime.xlsformatter.commons.AddressingTools;
import com.continental.knime.xlsformatter.commons.TagBasedXlsCellFormatterNodeModel;
import com.continental.knime.xlsformatter.commons.UiValidation;
import com.continental.knime.xlsformatter.commons.WarningMessageContainer;
import com.continental.knime.xlsformatter.commons.XlsFormatterControlTableAnalysisTools;
import com.continental.knime.xlsformatter.commons.XlsFormatterControlTableValidator;
import com.continental.knime.xlsformatter.commons.XlsFormatterUiOptions;
import com.continental.knime.xlsformatter.commons.XlsFormattingStateValidator;
import com.continental.knime.xlsformatter.commons.XlsFormattingStateValidator.ValidationModes;
import com.continental.knime.xlsformatter.porttype.XlsFormatterState;
import com.continental.knime.xlsformatter.porttype.XlsFormatterState.FormattingFlag;
import com.continental.knime.xlsformatter.porttype.XlsFormatterStateSpec;
import com.continental.knime.xlsformatter.util.XlsFormatterNodeParameterUtil;

public class XlsFormatterCellFormatterNodeModel extends TagBasedXlsCellFormatterNodeModel {

	// the logger instance
	private static final NodeLogger logger = NodeLogger
			.getLogger(XlsFormatterCellFormatterNodeModel.class);

	//Input tag string
	static final String CFGKEY_TAGSTRING = "Tag";
	static final String DEFAULT_TAGSTRING = "header";
	final SettingsModelString m_tag =
			new SettingsModelString(CFGKEY_TAGSTRING, DEFAULT_TAGSTRING); 

	static final String CFGKEY_HORIZONTALALIGNMENT = "HorizontalAlignment";
	static final String DEFAULT_HORIZONTALALIGNMENT = XlsFormatterState.FormattingFlag.UNMODIFIED.toString().toLowerCase();
	final SettingsModelString m_horizontalAlignment =
			new SettingsModelString(CFGKEY_HORIZONTALALIGNMENT, DEFAULT_HORIZONTALALIGNMENT);

	static final String CFGKEY_VERTICALALIGNMENT = "VerticalAlignment";
	static final String DEFAULT_VERTICALALIGNMENT = XlsFormatterState.FormattingFlag.UNMODIFIED.toString().toLowerCase();
	final SettingsModelString m_verticalAlignment =
			new SettingsModelString(CFGKEY_VERTICALALIGNMENT, DEFAULT_VERTICALALIGNMENT);

	static final String CFGKEY_WORDWRAP = "WordWrap";
	static final boolean DEFAULT_WORDWRAP = false;
	final SettingsModelBoolean m_wordWrap =
			new SettingsModelBoolean(CFGKEY_WORDWRAP, DEFAULT_WORDWRAP); 

	static final String CFGKEY_TEXTROTATION = "TextRotation";
	static final String DEFAULT_TEXTROTATION = XlsFormatterState.FormattingFlag.UNMODIFIED.toString().toLowerCase();
	final SettingsModelString m_textRotation =
			new XlsFormatterCellFormatterSettingsModelString(CFGKEY_TEXTROTATION, DEFAULT_TEXTROTATION);

	static final String CFGKEY_CELL_STYLE = "CellStyle";
	static final String DEFAULT_CELL_STYLE = XlsFormatterState.CellDataType.UNMODIFIED.toString();
	final SettingsModelString m_cellStyle =
			new SettingsModelString(CFGKEY_CELL_STYLE, DEFAULT_CELL_STYLE);

	static final String CFGKEY_TEXT_FORMAT = "TextFormat";
	static final String DEFAULT_TEXT_FORMAT = "";
	final SettingsModelString m_textFormat =
			new SettingsModelString(CFGKEY_TEXT_FORMAT, DEFAULT_TEXT_FORMAT);
	
	/**
	 * Enum of pre-defined text formats. It's safe to add new entries here (in both methods), they are not explicitly
	 * referenced elsewhere (except unmodified), e.g. in the dialog implementation.
	 */
	public enum TextPresets {
		@Label("[Select preset text format]")
		UNMODIFIED("[select preset text format]"),
		@Label("Percent: 0.00 %")
		PERCENT("percent: 0.00 %"),
		@Label("Integer: 0")
		INTEGER("integer: 0"),
		@Label("Thousand separated: #,##0")
		THOUSANDSEPARATED("thousand separated: #,##0"),
		@Label("Financial: #,##0.00;[Red](#,##0.00)")
		FINANCIAL("financial: #,##0.00;[Red](#,##0.00)"),
		@Label("Date: yyyy-MM-dd")
		DATE("date: yyyy-MM-dd"),
		@Label("Date/time: yyyy-MM-dd hh:mm:ss")
		DATETIME("date/time: yyyy-MM-dd hh:mm:ss");
		
		private String m_value;
		
		TextPresets(String value) {
			m_value = value;
		}
		
		@Override
		public String toString() {
			return m_value;
		}
		
		public String getTextFormat() {
			switch (this) {
			case PERCENT:
				return "0.00 %";
			case INTEGER:
				return "0";
			case THOUSANDSEPARATED:
				return "#,##0";
			case FINANCIAL:
				return "#,##0.00;[Red](#,##0.00)";
			case DATE:
				return "yyyy-MM-dd";
			case DATETIME:
				return "yyyy-MM-dd hh:mm:ss";
			case UNMODIFIED:
			default:
				return "";
			}
		}
		
		/**
		 * Get enum entry from its String value.
		 * 
		 * @param value The String value.
		 * @return The enum entry.
		 * @throws InvalidSettingsException If no enum entry could be found for the given String value.
		 * @since 1.7
		 */
		public static TextPresets getFromValue(final String value) throws InvalidSettingsException {
            for (final TextPresets rotation : values()) {
                if (rotation.toString().equals(value)) {
                    return rotation;
                }
            }
            throw new InvalidSettingsException(XlsFormatterNodeParameterUtil.createInvalidEnumValueExceptionMessage(
            		TextPresets.class, e -> e.toString(), value));
        }
		
	}
	
	static final String CFGKEY_TEXT_PRESETS = "TextPresets";
	static final String DEFAULT_TEXT_PRESETS = "";
	final SettingsModelString m_textPresets =
			new SettingsModelString(CFGKEY_TEXT_PRESETS, DEFAULT_TEXT_PRESETS);
	
	/**
	 * Constructor for the node model.
	 */
	protected XlsFormatterCellFormatterNodeModel() {

		super(
				new PortType[] { BufferedDataTable.TYPE, XlsFormatterState.TYPE_OPTIONAL },
				new PortType[] { XlsFormatterState.TYPE });
	}



	/**
	 * {@inheritDoc}
	 */
	protected PortObject[] execute(PortObject[] inObjects,
			final ExecutionContext exec) throws Exception {

		if (!XlsFormatterControlTableValidator.isControlTable((BufferedDataTable)inObjects[0], exec, logger))
			throw new IllegalArgumentException("The provided input table is not a valid XLS control table. See log for details.");

		XlsFormatterState xlsf = XlsFormatterState.getDeepClone(inObjects[1]);
		XlsFormatterState.SheetState xlsfs = xlsf.getCurrentSheetStateForModification();
		WarningMessageContainer warningMessageContainer = new WarningMessageContainer();

		List<CellAddress> targetCells =
				XlsFormatterControlTableAnalysisTools.getCellsMatchingTag((BufferedDataTable)inObjects[0], m_tag.getStringValue().trim(), exec, logger);
		warnOnNoMatchingTags(targetCells, m_tag.getStringValue().trim(), warningMessageContainer);

		// check for a partly overlap of these target cells with a previously merged range and warn
		String mergeOverlapRanges = XlsFormatterControlTableAnalysisTools.getOverlappingRanges(targetCells, xlsfs.mergeRanges, exec, logger);
		if (mergeOverlapRanges != null)
			warningMessageContainer.addMessage("Modification on parts of previously merged range(s) (" + mergeOverlapRanges + ") will have no effect.");
		
		for (CellAddress cell : targetCells) {
			XlsFormatterState.CellState cellState = AddressingTools.safelyGetCellInMap(xlsfs.cells, cell);

			FormattingFlag flag = FormattingFlag.UNMODIFIED;
			if(m_wordWrap.getBooleanValue())
				flag = FormattingFlag.ON;

			if (flag != FormattingFlag.UNMODIFIED)
				cellState.wrapText = flag;

			XlsFormatterState.CellAlignmentHorizontal optionHorizontalAlignment =
					XlsFormatterState.CellAlignmentHorizontal.valueOf(m_horizontalAlignment.getStringValue().toUpperCase());
			if (optionHorizontalAlignment != XlsFormatterState.CellAlignmentHorizontal.UNMODIFIED)
				cellState.cellHorizontalAlignment = optionHorizontalAlignment;

			XlsFormatterState.CellAlignmentVertical optionVertictalAlignment =
					XlsFormatterState.CellAlignmentVertical.valueOf(m_verticalAlignment.getStringValue().toUpperCase());
			if (optionVertictalAlignment != XlsFormatterState.CellAlignmentVertical.UNMODIFIED)
				cellState.cellVerticalAlignment = optionVertictalAlignment;

			if (!m_textRotation.getStringValue().toUpperCase().equals(XlsFormatterState.FormattingFlag.UNMODIFIED.toString())) {
				String value = m_textRotation.getStringValue().replace("°", "");
				try {
					cellState.textTiltDegree = Integer.parseInt(value);
				}
				catch (Exception e) {
					throw new Exception("Coding issue: Text rotation option " + m_textRotation.getStringValue() + " is not parsable.", e);
				}
			}

			String textFormat = m_textFormat.getStringValue();
			if (textFormat != null && !textFormat.equals(""))
				cellState.textFormat = textFormat;

			XlsFormatterState.CellDataType dataType = XlsFormatterUiOptions.getEnumEntryFromString(XlsFormatterState.CellDataType.values(), m_cellStyle.getStringValue());
			if (dataType != XlsFormatterState.CellDataType.UNMODIFIED)
				cellState.cellDataType = dataType;
		}

		XlsFormattingStateValidator.validateState(xlsf, ValidationModes.STYLES, warningMessageContainer, exec, logger);
		if (warningMessageContainer.hasMessage())
			setWarningMessage(warningMessageContainer.getMessage());

		return new PortObject[] { xlsf };
	}

	@Override
	protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {   	

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
		m_horizontalAlignment.saveSettingsTo(settings);
		m_verticalAlignment.saveSettingsTo(settings);
		m_wordWrap.saveSettingsTo(settings);
		m_textRotation.saveSettingsTo(settings);
		m_textFormat.saveSettingsTo(settings);
		m_cellStyle.saveSettingsTo(settings);
		m_textPresets.saveSettingsTo(settings);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {

		m_tag.loadSettingsFrom(settings);
		m_horizontalAlignment.loadSettingsFrom(settings);
		m_verticalAlignment.loadSettingsFrom(settings);
		m_wordWrap.loadSettingsFrom(settings);
		m_textRotation.loadSettingsFrom(settings);
		m_textFormat.loadSettingsFrom(settings);
		m_cellStyle.loadSettingsFrom(settings);
		m_textPresets.loadSettingsFrom(settings);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {

		m_tag.validateSettings(settings);
		m_horizontalAlignment.validateSettings(settings);
		m_verticalAlignment.validateSettings(settings);
		m_wordWrap.validateSettings(settings);
		m_textRotation.validateSettings(settings);
		m_textFormat.validateSettings(settings);
		m_cellStyle.validateSettings(settings);
		m_textPresets.validateSettings(settings);
		
		if (settings.getBoolean("performTagValiation", false)) {
			UiValidation.validateTagString(settings.getString(CFGKEY_TAGSTRING));
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
