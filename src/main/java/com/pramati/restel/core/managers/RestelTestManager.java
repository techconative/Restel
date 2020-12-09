package com.pramati.restel.core.managers;

import com.pramati.restel.core.model.BaseConfiguration;
import com.pramati.restel.core.model.RestelExecutionGroup;
import com.pramati.restel.core.model.RestelSuite;
import com.pramati.restel.core.model.RestelTestMethod;
import com.pramati.restel.exception.RestelException;
import org.apache.commons.collections4.CollectionUtils;
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
        validateDefinition(testDefinitions);
        validateExecution(testExecutionDefintions);
        validateSuite(testSuites);
    }

    /**
     * check if dependency is cyclic. Note: all the restelSuites with dependencies should be Direct Acyclic Graphs.
     *
     * @param restelSuites
     */
    private void validateSuite(List<RestelSuite> restelSuites) {
        restelSuites.forEach(testMethod -> isCyclic(testMethod, testMethod.getDependsOn()));
    }

    /**
     * check if restelSuite has cyclic dependency .
     *
     * @param restelSuite {@link RestelSuite}
     * @param childSuites list of child {@link RestelSuite} for restelSuite.
     */
    private void isCyclic(RestelSuite restelSuite, List<RestelSuite> childSuites) {
        if (!CollectionUtils.isEmpty(childSuites)) {
            childSuites.forEach(m -> {
                        if (m.getSuiteName().equals(restelSuite.getSuiteName())) {
                            throw new RestelException("Cyclic dependency exist for TestSuite: " + restelSuite.getSuiteName());
                        } else {
                            if (!CollectionUtils.isEmpty(m.getDependsOn())) {
                                isCyclic(restelSuite, m.getDependsOn());
                            }
                        }
                    }
            );
        }
    }

    /**
     * check if dependency is cyclic. Note: all the restelExecutionGroups with dependencies should be Direct Acyclic Graphs.
     *
     * @param restelExecutionGroups
     */
    private void validateExecution(List<RestelExecutionGroup> restelExecutionGroups) {
        restelExecutionGroups.forEach(testMethod -> isCyclic(testMethod, testMethod.getDependsOn()));
    }

    /**
     * check if executionGroup has cyclic dependency .
     *
     * @param executionGroup {@link RestelExecutionGroup}
     * @param childGroups    list of child {@link RestelExecutionGroup} for executionGroup.
     */
    private void isCyclic(RestelExecutionGroup executionGroup, List<RestelExecutionGroup> childGroups) {
        if (!CollectionUtils.isEmpty(childGroups)) {
            childGroups.forEach(m -> {
                        if (m.getExecutionGroupName().equals(executionGroup.getExecutionGroupName())) {
                            throw new RestelException("Cyclic dependency exist for TestSuiteExecution: " + executionGroup.getExecutionGroupName());
                        } else {
                            if (!CollectionUtils.isEmpty(m.getDependsOn())) {
                                isCyclic(executionGroup, m.getDependsOn());
                            }
                        }
                    }
            );
        }
    }

    /**
     * check if dependency is cyclic. Note: all the testMethods with dependencies should be Direct Acyclic Graphs.
     *
     * @param testMethods
     */
    private void validateDefinition(List<RestelTestMethod> testMethods) {
        testMethods.forEach(testMethod -> isCyclic(testMethod, testMethod.getDependentOn()));
    }

    /**
     * checks if there is any cyclic dependencies for testMethod.
     *
     * @param testMethod   {@link RestelTestMethod}
     * @param childMethods list of child {@link RestelTestMethod} for testMethod.
     */
    private void isCyclic(RestelTestMethod testMethod, List<RestelTestMethod> childMethods) {
        if (!CollectionUtils.isEmpty(childMethods)) {
            childMethods.forEach(m -> {
                        if (m.getCaseUniqueName().equals(testMethod.getCaseUniqueName())) {
                            throw new RestelException("Cyclic dependency exist for TestDefinition: " + testMethod.getCaseUniqueName());
                        } else {
                            if (!CollectionUtils.isEmpty(m.getDependentOn())) {
                                isCyclic(testMethod, m.getDependentOn());
                            }
                        }
                    }
            );
        }
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