package org.jenerate.internal.ui.dialogs.factory;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.jenerate.internal.manage.PreferencesManager;

/**
 * Helper class for holding common methods for all {@link DialogFactory}
 * 
 * @author maudrain
 */
public interface DialogFactoryHelper {

    /**
     * Check if the method specified with methodName and methodParameterTypeSignatures parameters is overridden and
     * concrete in a subclass of the original declared class, and that subclass is a superclass of objectClass.
     * 
     * @param objectClass the class for which the code generation is in effect
     * @param methodName the name of the method to check
     * @param methodParameterTypeSignatures the parameters of the method to check
     * @param originalClassFullyQualifiedName the name of the original class where the method is implemented
     * @return {@code true} if the method is overriden in the superclass, {@code false} otherwise
     * @throws JavaModelException if a problem occurs
     */
    public boolean isOverriddenInSuperclass(IType objectClass, String methodName,
            String[] methodParameterTypeSignatures, String originalClassFullyQualifiedName) throws JavaModelException;

    /**
     * Gets the fields of a class identifier by the {@link IType} objectClass.
     * 
     * @param objectClass the class where to retrieve the fields from
     * @param preferencesManager the preference manager
     * @return all fields to be used by the {@link DialogFactory}
     * @throws JavaModelException if a problem occurs retrieving the class fields
     */
    public IField[] getObjectClassFields(IType objectClass, PreferencesManager preferencesManager)
            throws JavaModelException;

    /**
     * @return the current {@link IDialogSettings}
     */
    public IDialogSettings getDialogSettings();
}
