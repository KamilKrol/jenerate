package org.jenerate.internal.domain.method.impl;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.jenerate.internal.data.EqualsHashCodeDialogData;
import org.jenerate.internal.lang.MethodGenerations;
import org.jenerate.internal.lang.generators.GeneratorsCommonMethodsDelegate;
import org.jenerate.internal.ui.preferences.PreferencesManager;

public class HashCodeMethod extends AbstractMethod<EqualsHashCodeDialogData> {

    public HashCodeMethod(PreferencesManager preferencesManager,
            GeneratorsCommonMethodsDelegate generatorsCommonMethodsDelegate) {
        super(preferencesManager, generatorsCommonMethodsDelegate);
    }

    @Override
    public String getMethod(IType objectClass, EqualsHashCodeDialogData data, String methodContent)
            throws JavaModelException {
        boolean addOverride = addOverride(objectClass);
        return MethodGenerations.createHashCodeMethod(data, addOverride, methodContent);
    }

}