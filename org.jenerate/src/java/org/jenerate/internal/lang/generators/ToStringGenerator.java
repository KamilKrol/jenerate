// $Id$
package org.jenerate.internal.lang.generators;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.jenerate.internal.data.ToStringDialogData;
import org.jenerate.internal.lang.MethodGenerations;
import org.jenerate.internal.ui.dialogs.ToStringDialog;
import org.jenerate.internal.ui.dialogs.provider.DialogProvider;
import org.jenerate.internal.ui.preferences.JeneratePreference;
import org.jenerate.internal.ui.preferences.PreferencesManager;
import org.jenerate.internal.util.JavaUiCodeAppender;
import org.jenerate.internal.util.JeneratePluginCodeFormatter;

/**
 * XXX test caching field empty for toString
 * 
 * @author jiayun, maudrain
 */
public final class ToStringGenerator implements ILangGenerator {

    private final JavaUiCodeAppender javaUiCodeAppender;
    private final PreferencesManager preferencesManager;
    private final DialogProvider<ToStringDialog, ToStringDialogData> dialogProvider;
    private final JeneratePluginCodeFormatter jeneratePluginCodeFormatter;
    private final GeneratorsCommonMethodsDelegate generatorsCommonMethodsDelegate;

    public ToStringGenerator(JavaUiCodeAppender javaUiCodeAppender, PreferencesManager preferencesManager,
            DialogProvider<ToStringDialog, ToStringDialogData> dialogProvider,
            JeneratePluginCodeFormatter jeneratePluginCodeFormatter,
            GeneratorsCommonMethodsDelegate generatorsCommonMethodsDelegate) {
        this.javaUiCodeAppender = javaUiCodeAppender;
        this.preferencesManager = preferencesManager;
        this.dialogProvider = dialogProvider;
        this.jeneratePluginCodeFormatter = jeneratePluginCodeFormatter;
        this.generatorsCommonMethodsDelegate = generatorsCommonMethodsDelegate;
    }

    @Override
    public void generate(Shell parentShell, IType objectClass) {
        Set<IMethod> excludedMethods = getExcludedMethods(objectClass);
        try {
            IField[] fields = generatorsCommonMethodsDelegate.getObjectClassFields(objectClass, preferencesManager);

            boolean disableAppendSuper = getDisableAppendSuper(objectClass);

            ToStringDialog dialog = dialogProvider.getDialog(parentShell, objectClass, excludedMethods, fields,
                    disableAppendSuper, preferencesManager);
            int returnCode = dialog.open();
            if (returnCode == Window.OK) {

                for (IMethod excludedMethod : excludedMethods) {
                    if (excludedMethod.exists()) {
                        excludedMethod.delete(true, null);
                    }
                }

                generateCode(parentShell, objectClass, dialog.getData());
            }

        } catch (CoreException e) {
            MessageDialog.openError(parentShell, "Method Generation Failed", e.getMessage());
        }

    }

    private boolean getDisableAppendSuper(IType objectClass) throws JavaModelException {
        return !isToStringConcreteInSuperclass(objectClass);
    }

    private Set<IMethod> getExcludedMethods(IType objectClass) {
        Set<IMethod> excludedMethods = new HashSet<>();

        IMethod existingMethod = objectClass.getMethod("toString", new String[0]);

        if (existingMethod.exists()) {
            excludedMethods.add(existingMethod);
        }
        return excludedMethods;
    }

    private void generateCode(final Shell parentShell, final IType objectClass, ToStringDialogData data)
            throws PartInitException, JavaModelException {

        boolean cacheToString = ((Boolean) preferencesManager
                .getCurrentPreferenceValue(JeneratePreference.CACHE_TOSTRING)).booleanValue();
        boolean isCacheable = cacheToString
                && generatorsCommonMethodsDelegate.areAllFinalFields(data.getCheckedFields());
        String cachingField = "";
        if (isCacheable) {
            cachingField = (String) preferencesManager
                    .getCurrentPreferenceValue(JeneratePreference.TOSTRING_CACHING_FIELD);
        }

        boolean addOverridePreference = ((Boolean) preferencesManager
                .getCurrentPreferenceValue(JeneratePreference.ADD_OVERRIDE_ANNOTATION)).booleanValue();
        boolean addOverride = addOverridePreference
                && generatorsCommonMethodsDelegate.isSourceLevelGreaterThanOrEqualTo5(objectClass);

        boolean useCommonLang3 = ((Boolean) preferencesManager
                .getCurrentPreferenceValue(JeneratePreference.USE_COMMONS_LANG3)).booleanValue();
        String styleConstant = getStyleConstantAndAddImport(data.getToStringStyle(), objectClass, useCommonLang3);
        String toStringMethodContent = MethodGenerations.generateToStringMethodContent(data, styleConstant,
                cachingField);
        String source = MethodGenerations.createToStringMethod(data, addOverride, toStringMethodContent);

        String formattedContent = format(parentShell, objectClass, source);

        objectClass.getCompilationUnit().createImport(
                CommonsLangLibraryUtils.getToStringBuilderLibrary(useCommonLang3), null, null);
        IMethod created = objectClass.createMethod(formattedContent, data.getElementPosition(), true, null);

        IField field = objectClass.getField(cachingField);
        if (field.exists()) {
            field.delete(true, null);
        }
        if (isCacheable) {
            String fieldSrc = "private transient String " + cachingField + ";\n\n";
            String formattedFieldSrc = format(parentShell, objectClass, fieldSrc);
            objectClass.createField(formattedFieldSrc, created, true, null);
        }

        javaUiCodeAppender.revealInEditor(objectClass, created);
    }

    /**
     * XXX test me Package private for testing
     */
    String getStyleConstantAndAddImport(final String style, final IType objectClass, boolean useCommonLang3)
            throws JavaModelException {

        String styleConstant = null;
        if (!style.equals(CommonsLangLibraryUtils.getToStringStyleLibraryDefaultStyle(useCommonLang3))
                && !style.equals("")) {

            int lastDot = style.lastIndexOf('.');
            if (lastDot != -1 && lastDot != (style.length() - 1)) {

                String styleClass = style.substring(0, lastDot);
                if (styleClass.length() == 0) {
                    return null;
                }

                int lastDot2 = styleClass.lastIndexOf('.');
                if (lastDot2 != (styleClass.length() - 1)) {

                    styleConstant = style.substring(lastDot2 + 1, style.length());
                    if (lastDot2 != -1) {
                        objectClass.getCompilationUnit().createImport(styleClass, null, null);
                    }
                }
            }
        }
        return styleConstant;
    }

    private String format(final Shell parentShell, final IType objectClass, String source) throws JavaModelException {
        try {
            return jeneratePluginCodeFormatter.formatCode(objectClass, source);
        } catch (BadLocationException e) {
            MessageDialog.openError(parentShell, "Error", e.getMessage());
            return "";
        }
    }

    public boolean isToStringConcreteInSuperclass(final IType objectClass) throws JavaModelException {
        return generatorsCommonMethodsDelegate.isOverriddenInSuperclass(objectClass, "toString", new String[0], null);
    }

}
