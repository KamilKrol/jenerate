package org.jenerate.internal.ui.dialogs.factory.impl;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.swt.widgets.Shell;
import org.jenerate.internal.domain.data.EqualsHashCodeGenerationData;
import org.jenerate.internal.domain.identifier.CommandIdentifier;
import org.jenerate.internal.domain.identifier.StrategyIdentifier;
import org.jenerate.internal.domain.identifier.impl.MethodsGenerationCommandIdentifier;
import org.jenerate.internal.manage.PreferencesManager;
import org.jenerate.internal.ui.dialogs.factory.DialogFactory;
import org.jenerate.internal.ui.dialogs.factory.DialogFactoryHelper;
import org.jenerate.internal.ui.dialogs.impl.EqualsHashCodeDialog;

/**
 * {@link DialogFactory} implementation for the {@link EqualsHashCodeDialog}
 * 
 * @author maudrain
 */
public class EqualsHashCodeDialogFactory extends
        AbstractDialogFactory<EqualsHashCodeDialog, EqualsHashCodeGenerationData> {

    /**
     * Constructor
     * 
     * @param dialogFactoryHelper the dialog factory helper
     * @param preferencesManager the preference manager
     */
    public EqualsHashCodeDialogFactory(DialogFactoryHelper dialogFactoryHelper, PreferencesManager preferencesManager) {
        super(dialogFactoryHelper, preferencesManager);
    }

    @Override
    public EqualsHashCodeDialog createDialog(Shell parentShell, IType objectClass, Set<IMethod> excludedMethods,
            LinkedHashSet<StrategyIdentifier> possibleStrategies) throws Exception {
        IField[] fields = getObjectClassFields(objectClass);
        boolean disableAppendSuper = getDisableAppendSuper(objectClass);
        LinkedHashMap<String, IJavaElement> insertPositions = dialogFactoryHelper.getInsertPositions(objectClass,
                excludedMethods);
        return new EqualsHashCodeDialog(parentShell, "Generate Equals and HashCode Methods", fields,
                possibleStrategies, disableAppendSuper, preferencesManager, dialogFactoryHelper.getDialogSettings(),
                insertPositions);
    }

    @Override
    public CommandIdentifier getCommandIdentifier() {
        return MethodsGenerationCommandIdentifier.EQUALS_HASH_CODE;
    }

    private boolean getDisableAppendSuper(IType objectClass) throws JavaModelException {
        return isDirectSubclassOfObject(objectClass) || !isEqualsOverriddenInSuperclass(objectClass)
                || !isHashCodeOverriddenInSuperclass(objectClass);
    }

    private boolean isDirectSubclassOfObject(final IType objectClass) throws JavaModelException {
        String superclass = objectClass.getSuperclassName();

        if (superclass == null) {
            return true;
        }
        if (superclass.equals("Object") || superclass.equals("java.lang.Object")) {
            return true;
        }
        return false;
    }

    private boolean isHashCodeOverriddenInSuperclass(final IType objectClass) throws JavaModelException {
        return dialogFactoryHelper.isOverriddenInSuperclass(objectClass, "hashCode", new String[0], "java.lang.Object");
    }

    private boolean isEqualsOverriddenInSuperclass(final IType objectClass) throws JavaModelException {
        return dialogFactoryHelper.isOverriddenInSuperclass(objectClass, "equals", new String[] { "QObject;" },
                "java.lang.Object");
    }
}
