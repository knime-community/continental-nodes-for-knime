package com.continental.knime.xlsformatter.util;

import java.awt.Color;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.persistence.NodeParametersPersistor;
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
	
	/**
	 * Persistor for enums that are defined in upper case but stored in lower case strings.
	 * 
	 * @param <E> the enum type
	 */
	public static class UpperLowerCaseEnumFieldPersistor<E extends Enum<E>> implements NodeParametersPersistor<E> {

	    private final String m_configKey;

	    private final Class<E> m_enumClass;

	    /**
	     * @param configKey under which the string is to be stored
	     * @param enumClass the class of the to be persisted enum
	     */
	    protected UpperLowerCaseEnumFieldPersistor(final String configKey, final Class<E> enumClass) {
	        m_enumClass = enumClass;
	        m_configKey = configKey;
	    }
	    
	    @Override
	    public E load(final NodeSettingsRO settings) throws InvalidSettingsException {
	        var name = settings.getString(m_configKey);
	        try {
	            return Enum.valueOf(m_enumClass, name.toUpperCase());
	        } catch (IllegalArgumentException ex) {
	            throw new InvalidSettingsException(
	            		createInvalidEnumValueExceptionMessage(m_enumClass, e -> e.toString().toLowerCase(), name), ex);
	        }
	    }

	    @Override
	    public void save(final E obj, final NodeSettingsWO settings) {
	        settings.addString(m_configKey, obj.toString().toLowerCase());
	    }

		@Override
		public String[][] getConfigPaths() {
			return new String[][]{{m_configKey}};
		}
		
	}
	
	/**
	 * Creates an exception message for invalid enum values.
	 * 
	 * @param <E> the enum type
	 * @param enumClass the class of the enum
	 * @param getStringRepresentation function to get the necessary string representation of the enum values
	 * @param name the invalid name
	 * @return the exception message
	 */
	public static <E extends Enum<E>> String createInvalidEnumValueExceptionMessage(final Class<E> enumClass, 
	    final Function<E, String> getStringRepresentation, final String name) {
        var values = Arrays.stream(enumClass.getEnumConstants())
        		.map(getStringRepresentation).collect(Collectors.joining(", "));
        return String.format("Invalid value '%s'. Possible values: %s", name, values);
    }
	
	public static class LegacyColorPersistor implements NodeParametersPersistor<Color> {

		private Color m_defaultColor;
		
		protected LegacyColorPersistor() {
			m_defaultColor = Color.BLACK;
		}
		
		protected LegacyColorPersistor(final Color defaultColor) {
			m_defaultColor = defaultColor;
		}
		
		@Override
		public Color load(final NodeSettingsRO settings) throws InvalidSettingsException {
			final var red = settings.getInt("Red", m_defaultColor.getRed());
			final var green = settings.getInt("Green", m_defaultColor.getGreen());
			final var blue = settings.getInt("Blue", m_defaultColor.getBlue());
			final var alpha = settings.getInt("Alpha", m_defaultColor.getAlpha());
			return new Color(red, green, blue, alpha);
		}

		@Override
		public void save(final Color obj, final NodeSettingsWO settings) {
			settings.addInt("Red", obj.getRed());
			settings.addInt("Green", obj.getGreen());
			settings.addInt("Blue", obj.getBlue());
			settings.addInt("Alpha", obj.getAlpha());
		}

		@Override
		public String[][] getConfigPaths() {
			return new String[][]{{"Red"}, {"Green"}, {"Blue"}, {"Alpha"}};
		}

	}
	
}
