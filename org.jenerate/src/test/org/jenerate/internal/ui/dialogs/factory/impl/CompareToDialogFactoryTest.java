package org.jenerate.internal.ui.dialogs.factory.impl;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.JavaModelException;
import org.jenerate.internal.domain.identifier.impl.MethodsGenerationCommandIdentifier;
import org.jenerate.internal.ui.dialogs.impl.CompareToDialog;
import org.jenerate.internal.util.JavaInterfaceCodeAppender;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@link CompareToDialogFactory}
 * 
 * @author maudrain
 */
@RunWith(MockitoJUnitRunner.class)
public class CompareToDialogFactoryTest extends AbstractDialogFactoryTest {

    @Mock
    private JavaInterfaceCodeAppender javaInterfaceCodeAppender;

    private CompareToDialogFactory compareToDialogFactory;

    @Override
    public void callbackAfterSetUp() throws Exception {
        mockDisableAppendSuper(false);
        compareToDialogFactory = new CompareToDialogFactory(dialogFactoryHelper, preferencesManager,
                javaInterfaceCodeAppender);
    }

    @Test
    public void testGetCommandIdentifier() {
        assertEquals(MethodsGenerationCommandIdentifier.COMPARE_TO, compareToDialogFactory.getCommandIdentifier());
    }

    @Test
    public void testCreateDialogWithEmptyFieldsAndNoDisableAppendSuper() throws Exception {
        validateDialogCreation(createFields(), false);
    }

    @Test
    public void testCreateDialogWithFieldsAndNoDisableAppendSuper() throws Exception {
        validateDialogCreation(createFields(field1, field2), false);
    }

    @Test
    public void testCreateDialogWithFieldsAndDisableAppendSuper() throws Exception {
        mockDisableAppendSuper(true);
        validateDialogCreation(createFields(field1, field2), true);
    }

    private void validateDialogCreation(IField[] iFields, boolean disableAppendSuper) throws JavaModelException,
            Exception {
        when(dialogFactoryHelper.getObjectClassFields(objectClass, preferencesManager)).thenReturn(iFields);
        CompareToDialog compareToDialog = compareToDialogFactory.createDialog(parentShell, objectClass,
                excludedMethods, possibleStrategyIdentifiers);
        assertArrayEquals(iFields, compareToDialog.getFields());
        assertEquals(disableAppendSuper, !compareToDialog.getAppendSuper());
    }

    private void mockDisableAppendSuper(boolean isDisableAppendSuper) throws JavaModelException {
        when(dialogFactoryHelper.isOverriddenInSuperclass(objectClass, "compareTo", new String[] { "QObject;" }, null))
                .thenReturn(!isDisableAppendSuper);
        when(javaInterfaceCodeAppender.isImplementedInSupertype(objectClass, "Comparable")).thenReturn(
                !isDisableAppendSuper);
    }
}
