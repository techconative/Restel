package com.pramati.restel.core.managers;

import com.pramati.restel.core.model.BaseConfiguration;
import com.pramati.restel.core.model.RestelExecutionGroup;
import com.pramati.restel.core.model.RestelSuite;
import com.pramati.restel.core.model.RestelTestMethod;
import com.pramati.restel.core.validators.RestelExcelModelValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * Represents the class that manages the Restel tests
 *
 * @author kannanr
 */
@Component
public class RestelTestManager {

    private List<RestelTestMethod> testDefinitions;
    private List<RestelExecutionGroup> testExecutionDefintions;

    private Map<String, RestelTestMethod> indexedTestDefinitions;
    private Map<String, RestelExecutionGroup> indexedTestExecutions;
    private Map<String, RestelSuite> indexedTestSuites;
    private BaseConfiguration baseConfig;

    @Autowired
    private ExcelParseManager excelParseManager;

    @PostConstruct
    private void init() {
        configure();
    }

    private void configure() {

        baseConfig = excelParseManager.getBaseConfig();

        testDefinitions = new ArrayList<>();
        testExecutionDefintions = new ArrayList<>();
        List<RestelSuite> testSuites = new ArrayList<>();

        indexedTestDefinitions = new HashMap<>();
        indexedTestExecutions = new HashMap<>();
        indexedTestSuites = new HashMap<>();

        for (RestelTestMethod testMethod : excelParseManager.getTestMethods()) {
            indexedTestDefinitions.put(testMethod.getCaseUniqueName(), testMethod);
        }
        for (RestelSuite suite : excelParseManager.getSuites()) {
            indexedTestSuites.put(suite.getSuiteName(), suite);
        }

        for (RestelExecutionGroup execution : excelParseManager.getExecGroups()) {
            indexedTestExecutions.put(execution.getExecutionGroupName(), execution);
        }

        testDefinitions.addAll(indexedTestDefinitions.values());
        testSuites.addAll(indexedTestSuites.values());
        testExecutionDefintions.addAll(indexedTestExecutions.values());
        RestelExcelModelValidator.validateDefinition(testDefinitions);
        RestelExcelModelValidator.validateExecution(testExecutionDefintions, indexedTestDefinitions.keySet());
        RestelExcelModelValidator.validateSuites(testSuites);
    }

    public BaseConfiguration getBaseConfig() {
        return baseConfig;
    }

    /**
     * Gets the method with the given name
     *
     * @param methodName The method name for which the {@link RestelTestMethod} to be
     *                   searched for.
     * @return {@link RestelTestMethod} with the given name
     */
    public RestelTestMethod getTestDefinitions(String methodName) {
        return indexedTestDefinitions.get(methodName);
    }

    /**
     * Get all the test methods available.
     *
     * @return List of test methods
     */
    public List<RestelTestMethod> getTestDefintions() {
        return Collections.unmodifiableList(testDefinitions);
    }

    /**
     * Gets the list of test execution definitions that will be executed.
     *
     * @return List of {@link RestelExecutionGroup} to be executed.
     */
    public List<RestelExecutionGroup> getExecutionDefinitions() {
        return testExecutionDefintions;
    }

    /**
     * Gets the list of test execution definition with the given name.
     *
     * @param executionName The name of the execution.
     * @return {@link RestelExecutionGroup} instance with the given name.
     */
    public RestelExecutionGroup getExecutionDefinition(
            String executionName) {
        return indexedTestExecutions.get(executionName);
    }

    /**
     * Gets the suite with the given name
     *
     * @param suiteName The name of the suite.
     * @return The {@link RestelSuite} instance with the given unique name.
     */
    public RestelSuite getTestSuite(String suiteName) {
        return indexedTestSuites.get(suiteName);
    }

    public Map<String, RestelSuite> getTestSuites() {
        return indexedTestSuites;
    }

}