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
    
package com.continental.knime.xlsformatter.apply2;

import java.util.List;
import java.util.function.Supplier;

import org.knime.node.parameters.widget.file.FileReaderWidget;
import org.knime.node.parameters.widget.file.FileSelectionWidget;
import org.knime.node.parameters.widget.file.FileSystemOption;
import org.knime.node.parameters.widget.file.FileWriterWidget;
import org.knime.node.parameters.widget.file.SingleFileSelectionMode;
import org.knime.node.parameters.widget.file.WithFileSystem;
import org.knime.node.parameters.experimental.persistence.booleanhelpers.DoNotPersistBoolean;
import org.knime.node.parameters.updates.StateComputationAbortException;
import org.knime.node.parameters.modification.Modification;
import org.knime.node.parameters.modification.Modification.WidgetGroupModifier;
import org.knime.node.parameters.Advanced;
import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.layout.After;
import org.knime.node.parameters.layout.Layout;
import org.knime.node.parameters.layout.Section;
import org.knime.node.parameters.migration.DefaultProvider;
import org.knime.node.parameters.migration.LoadDefaultsForAbsentFields;
import org.knime.node.parameters.migration.Migration;
import org.knime.node.parameters.persistence.Persist;
import org.knime.node.parameters.persistence.Persistor;
import org.knime.node.parameters.legacy.widget.file.LegacyFileWriter;
import org.knime.node.parameters.legacy.widget.file.LegacyFileWriterWithOverwritePolicyOptions;
import org.knime.node.parameters.legacy.widget.file.LegacyFileWriterWithOverwritePolicyOptions.OverwritePolicy;
import org.knime.node.parameters.legacy.widget.file.LegacyFileWriterWithOverwritePolicyOptions.OverwritePolicyChoicesProvider;
import org.knime.node.parameters.updates.Effect;
import org.knime.node.parameters.updates.Effect.EffectType;
import org.knime.node.parameters.updates.EffectPredicate;
import org.knime.node.parameters.updates.EffectPredicateProvider;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.StateProvider;
import org.knime.node.parameters.updates.ValueProvider;
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.updates.util.BooleanReference;

/**
 * Node parameters for XLS Formatter (apply).
 * 
 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
@SuppressWarnings("restriction")
@LoadDefaultsForAbsentFields
final class XlsFormatterApplyNodeParameters implements NodeParameters {

	@Section(title = "Source")
	interface SourceSection {
	}

	@Section(title = "Destination")
	@After(SourceSection.class)
	interface DestinationSection {
	}
	
	@After(DestinationSection.class)
	@Advanced
	interface AdvancedSection {
	}

	@Layout(SourceSection.class)
	@Widget(title = "Source file", description = """
        Select a file system which stores the .xlsx file you want to read and specify the path to the file.
        The file must be a valid Excel (.xlsx) file that will be formatted according to the formatting instructions
        from the connected XLS Formatter port.
        """)
	@Modification(LegacyFileWriterModifier.class)
    @FileReaderWidget(fileExtensions = {"xlsx"})
    @Persist(configKey = "InputFile")
    LegacyFileWriter m_sourceFile = new LegacyFileWriter();

    static final class LegacyFileWriterModifier implements LegacyFileWriter.Modifier {
        
    	@Override
        public void modify(final Modification.WidgetGroupModifier group) {
            final var fileSelection = findFileSelection(group);
            fileSelection //
                .modifyAnnotation(Widget.class) //
                .withProperty("title", "File") //
                .withProperty("description", "Select a .xlsx file to apply the formatting to.") //
                .modify();
            fileSelection //
                .addAnnotation(FileSelectionWidget.class) //
                .withProperty("value", SingleFileSelectionMode.FILE) //
                .modify();
            fileSelection //
            	.addAnnotation(WithFileSystem.class) //
            	.withProperty("value", new FileSystemOption[]{FileSystemOption.LOCAL, FileSystemOption.SPACE,
            			FileSystemOption.EMBEDDED, FileSystemOption.CONNECTED})
            	.modify();
        }
    	
    }
	
    @Layout(DestinationSection.class)
    @Widget(title = "Destination file", description = """
        Select a file system in which you want to write the formatted .xlsx file and specify the output path.
        The formatted Excel file will be written to this location.
        """)
    @Modification(OutputFileModification.class)
    @FileWriterWidget(fileExtension = "xlsx")
    @Persist(configKey = "OutputFile")
    @ValueReference(DestinationFileRef.class)
    LegacyFileWriterWithOverwritePolicyOptions m_destinationFile = new LegacyFileWriterWithOverwritePolicyOptions();

    static final class DestinationFileRef implements ParameterReference<LegacyFileWriterWithOverwritePolicyOptions> {
    }
    
    static class OutputFileModification implements LegacyFileWriterWithOverwritePolicyOptions.Modifier {

        @Override
        public void modify(final WidgetGroupModifier group) {
            findFileSelection(group) //
                .modifyAnnotation(Widget.class) //
                .withProperty("title", "File") //
                .withProperty("description", "The file to write the formatted .xlsx to.") //
                .modify();
            findFileSelection(group) //
                .addAnnotation(FileSelectionWidget.class) //
                .withValue(SingleFileSelectionMode.FILE) //
                .modify();
            findFileSelection(group) //
                .addAnnotation(WithFileSystem.class) //
                .withProperty("value", new FileSystemOption[]{FileSystemOption.LOCAL, FileSystemOption.SPACE,
                    FileSystemOption.EMBEDDED, FileSystemOption.CUSTOM_URL, FileSystemOption.CONNECTED})
                .modify();
            restrictOverwritePolicyOptions(group, XlsFormatterApplyOverwritePolicyChoicesProvider.class);
        }
        
        static final class XlsFormatterApplyOverwritePolicyChoicesProvider extends OverwritePolicyChoicesProvider {

			@Override
			protected List<OverwritePolicy> getChoices() {
				return List.of(OverwritePolicy.overwrite, OverwritePolicy.fail);
			}
			
		}

    }
    
    @Layout(AdvancedSection.class)
    @Widget(title = "Preserve source file's cell number formats", description = """
        When selected, the original number formats from the source file (e.g. date cells written by KNIME)
        will be preserved in the output file. When unchecked, default formatting may be applied.
        """)
    @Persist(configKey = "PreserveSourceNumberFormats")
    @Migration(LoadFalseForOldNodes.class)
    boolean m_preserveSourceNumberFormats = true;
    
    static class LoadFalseForOldNodes implements DefaultProvider<Boolean> {
    	
        @Override
        public Boolean getDefault() {
            return false;
        }
        
    }

    @Layout(AdvancedSection.class)
    @Widget(title = "Open output file after execution", description = """
        Once the node execution has been finished, the output .xlsx file will be opened automatically.
        Note: Only files on your local file system can be opened.
        """)
    @Persist(configKey = "OpenOutputFile")
    @Effect(predicate = IsOpenOutputFileEnabled.class, type = EffectType.ENABLE)
    boolean m_openOutputFile;
    
    @Persistor(DoNotPersistBoolean.class)
    @ValueProvider(IsOpenOutputFileEnabledProvider.class)
    @ValueReference(IsOpenOutputFileEnabled.class)
    boolean m_isOpenOutputFileEnabled;
    
    static final class IsOpenOutputFileEnabled implements BooleanReference {
    }
    
    static final class IsOpenOutputFileEnabledProvider implements StateProvider<Boolean> {

    	private Supplier<LegacyFileWriterWithOverwritePolicyOptions> m_destinationFileSupplier;
    	
		@Override
		public void init(StateProviderInitializer initializer) {
			initializer.computeBeforeOpenDialog();
			m_destinationFileSupplier = initializer.computeFromValueSupplier(DestinationFileRef.class);
		}

		@Override
		public Boolean computeState(NodeParametersInput parametersInput) throws StateComputationAbortException {
			final var destinationFile = m_destinationFileSupplier.get();
			if (destinationFile == null) {
				return false;
			}
			return !XlsFormatterApplyNodeModel.isHeadlessOrRemote() && XlsFormatterApplyNodeModel.categoryIsSupported(
					destinationFile.getFileSelection().getFSLocation().getFSCategory());
		}
    	
    }
    
}
