package restel.core.manager;

import com.techconative.restel.core.managers.ExcelParseManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@RunWith(MockitoJUnitRunner.class)
public class ExcelParseManagerTest {

    @InjectMocks
    private ExcelParseManager excelParseManager;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    private void invoke(String filepath) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        FieldSetter.setField(excelParseManager, excelParseManager.getClass().getDeclaredField("filepath"), filepath);
        Method postConstruct = ExcelParseManager.class.getDeclaredMethod("init"); // methodName,parameters
        postConstruct.setAccessible(true);
        postConstruct.invoke(excelParseManager);
    }

    @Test
    public void testGetTestMethodsTest() throws NoSuchMethodException, NoSuchFieldException, IllegalAccessException, InvocationTargetException {
        invoke("src/test/resources/Sample_Suite_definition.xlsx");
        Assert.assertFalse(excelParseManager.getTestMethods().isEmpty());
    }

    @Test
    public void testGetExecGroupsTest() throws NoSuchMethodException, NoSuchFieldException, IllegalAccessException, InvocationTargetException {
        invoke("src/test/resources/Sample_Suite_definition.xlsx");
        Assert.assertFalse(excelParseManager.getExecGroups().isEmpty());
    }

    @Test
    public void testGetSuitesTest() throws NoSuchMethodException, NoSuchFieldException, IllegalAccessException, InvocationTargetException {
        invoke("src/test/resources/Sample_Suite_definition.xlsx");
        Assert.assertFalse(excelParseManager.getSuites().isEmpty());
    }

    @Test
    public void testGetBaseConfigTest() throws NoSuchMethodException, NoSuchFieldException, IllegalAccessException, InvocationTargetException {
        invoke("src/test/resources/Sample_Suite_definition.xlsx");
        Assert.assertNotNull(excelParseManager.getBaseConfig());
    }

    @Test(expected = Exception.class)
    public void testgetTestMethodsWithEmpty() throws NoSuchMethodException, NoSuchFieldException, IllegalAccessException, InvocationTargetException {
        invoke("src/test/resources/Sample_Suite_definition_empty.xlsx");
        excelParseManager.getTestMethods();
    }

    @Test(expected = Exception.class)
    public void testGetSuitesWithEmpty() throws NoSuchMethodException, NoSuchFieldException, IllegalAccessException, InvocationTargetException {
        invoke("src/test/resources/Sample_Suite_definition_empty_suites.xlsx");
        excelParseManager.getSuites();
    }

    @Test(expected = Exception.class)
    public void testGetExecutionWithEmpty() throws NoSuchMethodException, NoSuchFieldException, IllegalAccessException, InvocationTargetException {
        invoke("src/test/resources/Sample_Suite_definition_empty_exec.xlsx");
        excelParseManager.getExecGroups();
    }

    @Test(expected = Exception.class)
    public void testGetExecutionNoFile() throws NoSuchMethodException, NoSuchFieldException, IllegalAccessException, InvocationTargetException {
        invoke("");
        excelParseManager.getExecGroups();
    }

    @Test(expected = Exception.class)
    public void testGetExecutionInvalidFile() throws NoSuchMethodException, NoSuchFieldException, IllegalAccessException, InvocationTargetException {
        invoke("src/test/resources/Sample_Suite_definition_invalid.xlsx");
        excelParseManager.getExecGroups();
    }
}
