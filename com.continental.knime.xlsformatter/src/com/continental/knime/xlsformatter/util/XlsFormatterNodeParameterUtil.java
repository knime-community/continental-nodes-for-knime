package com.continental.knime.xlsformatter.util;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Modification;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Modification.WidgetGroupModifier;
import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.persistence.NodeParametersPersistor;
import org.knime.node.parameters.persistence.Persistor;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.StateProvider;
import org.knime.node.parameters.widget.text.TextInputWidget;
import org.knime.node.parameters.widget.text.TextInputWidgetValidation;
import org.knime.node.parameters.widget.text.TextInputWidgetValidation.PatternValidation;

import com.continental.knime.xlsformatter.commons.XlsFormatterTagTools;

/**
 * Utility class for XLS Formatter Node Parameters.
 * 
 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
 */
@SuppressWarnings("restriction")
public final class XlsFormatterNodeParameterUtil {

	private XlsFormatterNodeParameterUtil() {
		// utility class
	}
	
	/**
     * Feature flag for web UI dialogs for the continental nodes in local AP.
     */
    private static final boolean SYSPROP_WEBUI_DIALOG_AP =
        "js".equals(System.getProperty("com.continental.knime.ui.mode"));

    /**
     * If we are headless and a dialog is required (i.e. remote workflow editing), we enforce webUI dialogs.
     */
    private static final boolean SYSPROP_HEADLESS = Boolean.getBoolean("java.awt.headless");

    /**
     * Feature flag for web UI dialogs for the continental nodes.
     */
    public static final boolean HAS_WEBUI_DIALOG = SYSPROP_HEADLESS || SYSPROP_WEBUI_DIALOG_AP;
	
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
	
	/**
	 * Node parameters for legacy color representation using hex color with alpha.
	 * 
	 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
	 */
	public static final class LegacyColorNodeParameters implements NodeParameters {

		public abstract static class LegacyColorModification implements Modification.Modifier {

			private String m_title;
			
			private String m_description;
			
			/**
	         * Constructor.
	         *
	         * @param title Title of the widget.
	         * @param description Description of the widget.
	         */
	        protected LegacyColorModification(final String title, final String description) {
	            m_title = title;
	            m_description = description;
	        }
	        
	        /**
	         * Constructor.
	         *
	         * @param title Title of the widget.
	         */
	        protected LegacyColorModification(final String title) {
	        	this(title, null);
	        }

	        @Override
	        public void modify(final WidgetGroupModifier group) {
	            if (m_title != null) {
	                group.find(HexColorWithAlphaModRef.class).modifyAnnotation(Widget.class)
	                    .withProperty("title", m_title)
	                    .modify();
	            }
	            if (m_description != null) {
	                group.find(HexColorWithAlphaModRef.class).modifyAnnotation(Widget.class)
	                    .withProperty("description", m_description)
	                    .modify();
	            }
	        }
	        
	    }
		
		public LegacyColorNodeParameters() {
		}

		public LegacyColorNodeParameters(final String hexColorWithAlpha) {
			m_hexColorWithAlpha = hexColorWithAlpha;
		}

		@Widget(title = "Color", description = "Choose a color in hex format with optional alpha channel.")
		@TextInputWidget(patternValidation = HexColorWithAlphaPatternValidation.class)
		@Persistor(LegacyHexColorWithAlphaPersistor.class)
		@Modification.WidgetReference(HexColorWithAlphaModRef.class)
		String m_hexColorWithAlpha = "#000000FF"; // Default black with full alpha
		
	    interface HexColorWithAlphaModRef extends ParameterReference<String>, Modification.Reference {
	    }

		public static class HexColorWithAlphaPatternValidation extends TextInputWidgetValidation.PatternValidation {

			@Override
			public String getErrorMessage() {
				return "Invalid color format. Please provide a hex color in the format #RRGGBB or #RRGGBBAA.";
			}

			@Override
			protected String getPattern() {
				// Pattern to match hex color with optional alpha channel
				return "^#(?:[0-9a-fA-F]{6}|[0-9a-fA-F]{8})$";
			}

		}

		static final class LegacyHexColorWithAlphaPersistor implements NodeParametersPersistor<String> {

			@Override
			public String load(final NodeSettingsRO settings) throws InvalidSettingsException {
				final var red = settings.getInt("Red", -1);
				final var green = settings.getInt("Green", -1);
				final var blue = settings.getInt("Blue", -1);
				final var alpha = settings.getInt("Alpha", 255);
				if (alpha == 255) {
					return String.format("#%02X%02X%02X", red, green, blue);
				}
				return String.format("#%02X%02X%02X%02X", red, green, blue, alpha);
			}

			@Override
			public void save(final String obj, final NodeSettingsWO settings) {
				int rgba = (int) Long.parseLong(obj.substring(1), 16);
				if (obj.length() == 9) {
					settings.addInt("Red", (rgba >> 24) & 0xFF);
					settings.addInt("Green", (rgba >> 16) & 0xFF);
					settings.addInt("Blue", (rgba >> 8) & 0xFF);
					settings.addInt("Alpha", rgba & 0xFF);
				} else {
					settings.addInt("Red", (rgba >> 16) & 0xFF);
					settings.addInt("Green", (rgba >> 8) & 0xFF);
					settings.addInt("Blue", rgba & 0xFF);
					settings.addInt("Alpha", 255);
				}
			}

			@Override
			public String[][] getConfigPaths() {
				return new String[][]{{"Red"}, {"Green"}, {"Blue"}, {"Alpha"}};
			}

		}

	}
	
}
