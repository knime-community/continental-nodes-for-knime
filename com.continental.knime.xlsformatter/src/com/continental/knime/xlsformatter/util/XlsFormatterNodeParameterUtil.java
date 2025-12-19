package com.continental.knime.xlsformatter.util;

import java.util.function.Supplier;

import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.StateProvider;
import org.knime.node.parameters.widget.text.TextInputWidgetValidation;
import org.knime.node.parameters.widget.text.TextInputWidgetValidation.PatternValidation;

import com.continental.knime.xlsformatter.commons.XlsFormatterTagTools;

/**
 * Utility class for XLS Formatter Node Parameters.
 * 
 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
 */
public final class XlsFormatterNodeParameterUtil {

	private XlsFormatterNodeParameterUtil() {
		// utility class
	}
	
	/**
	 * State provider for tag validation in XLS Formatter nodes.
	 * 
	 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
	 */
	public static class XlsTagValidationProvider implements StateProvider<TextInputWidgetValidation.PatternValidation> {

		private Class<? extends ParameterReference<String>> m_tagParameter;
		
		protected XlsTagValidationProvider(final Class<? extends ParameterReference<String>> tagParameter) {
			m_tagParameter = tagParameter;
		}
		
    	Supplier<String> m_tagSupplier;
    	
		@Override
		public void init(StateProviderInitializer initializer) {
			initializer.computeAfterOpenDialog();
			m_tagSupplier = initializer.computeFromValueSupplier(m_tagParameter);
		}

		@Override
		public PatternValidation computeState(NodeParametersInput parametersInput) {
			final var tag = m_tagSupplier.get();
		    final String errorMessage;
			final String pattern;
			
			if (tag == null || tag.trim().isEmpty()) {
			    errorMessage = "Tag cannot be empty. Please set a freely chosen tag that matches an entry in the "
				    + "provided control table.";
		        pattern = "(?!)";
		    } else {
			    errorMessage = "Tag contains invalid characters (" + XlsFormatterTagTools.INVALID_TAG_CHARACTERS + ").";
				pattern = "^[^%s]*$".formatted(XlsFormatterTagTools.INVALID_TAG_CHARACTERS);
			}
			
		    return new PatternValidation() {
		    	
				@Override
				public String getErrorMessage() {
					return errorMessage;
				}

				@Override
				public String getPattern() {
					return pattern;
				}
				
			};
		}

	}
	
}
