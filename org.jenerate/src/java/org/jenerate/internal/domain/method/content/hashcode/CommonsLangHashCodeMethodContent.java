package org.jenerate.internal.domain.method.content.hashcode;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.jenerate.internal.domain.MethodContentStrategyIdentifier;
import org.jenerate.internal.domain.data.EqualsHashCodeGenerationData;
import org.jenerate.internal.domain.method.content.AbstractMethodContent;
import org.jenerate.internal.domain.method.content.MethodContentLibraries;
import org.jenerate.internal.domain.method.skeleton.impl.HashCodeMethodSkeleton;
import org.jenerate.internal.domain.preference.impl.JeneratePreference;
import org.jenerate.internal.lang.MethodGenerations;
import org.jenerate.internal.lang.generators.GeneratorsCommonMethodsDelegate;
import org.jenerate.internal.manage.PreferencesManager;

public class CommonsLangHashCodeMethodContent extends AbstractMethodContent<HashCodeMethodSkeleton, EqualsHashCodeGenerationData> {

    public CommonsLangHashCodeMethodContent(MethodContentStrategyIdentifier methodContentStrategyIdentifier,
            PreferencesManager preferencesManager, GeneratorsCommonMethodsDelegate generatorsCommonMethodsDelegate) {
        super(methodContentStrategyIdentifier, preferencesManager, generatorsCommonMethodsDelegate);
    }

    @Override
    public String getMethodContent(IType objectClass, EqualsHashCodeGenerationData data) throws JavaModelException {
        boolean cacheHashCode = ((Boolean) preferencesManager
                .getCurrentPreferenceValue(JeneratePreference.CACHE_HASHCODE)).booleanValue();
        boolean isCacheable = cacheHashCode
                && generatorsCommonMethodsDelegate.areAllFinalFields(data.getCheckedFields());
        String cachingField = "";
        if (isCacheable) {
            cachingField = (String) preferencesManager
                    .getCurrentPreferenceValue(JeneratePreference.HASHCODE_CACHING_FIELD);
        }

        IJavaElement currentPosition = data.getElementPosition();
        IField field = objectClass.getField(cachingField);
        if (field.exists()) {
            field.delete(true, null);
        }
        if (isCacheable) {
            String fieldSrc = "private transient int " + cachingField + ";\n\n";
            objectClass.createField(fieldSrc, currentPosition, true, null);
        }

        return MethodGenerations.generateHashCodeMethodContent(data, cachingField);
    }

    @Override
    public Set<String> getLibrariesToImport(EqualsHashCodeGenerationData data) {
        boolean useCommonsLang3 = false;
        if (MethodContentStrategyIdentifier.USE_COMMONS_LANG3.equals(methodContentStrategyIdentifier)) {
            useCommonsLang3 = true;
        }
        return Collections.singleton(MethodContentLibraries.getHashCodeBuilderLibrary(useCommonsLang3));
    }

    @Override
    public Class<HashCodeMethodSkeleton> getRelatedMethodSkeletonClass() {
        return HashCodeMethodSkeleton.class;
    }
}
