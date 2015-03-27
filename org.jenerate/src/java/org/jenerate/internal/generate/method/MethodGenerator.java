package org.jenerate.internal.generate.method;

import java.util.LinkedHashSet;

import org.eclipse.jdt.core.IType;
import org.eclipse.swt.widgets.Shell;
import org.jenerate.internal.domain.data.MethodGenerationData;
import org.jenerate.internal.strategy.method.Method;
import org.jenerate.internal.strategy.method.skeleton.MethodSkeleton;
import org.jenerate.internal.ui.dialogs.FieldDialog;

public interface MethodGenerator<T extends MethodSkeleton<V>, U extends FieldDialog<V>, V extends MethodGenerationData> {

    void generate(Shell parentShell, IType objectClass, LinkedHashSet<Method<T, V>> methods);

}
