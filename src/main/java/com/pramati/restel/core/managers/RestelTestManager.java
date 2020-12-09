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
     * check cyclic dependency
     *
     * @param restelSuites
     */
    private void validateSuite(List<RestelSuite> restelSuites) {
        restelSuites.forEach(testMethod -> isCyclic(testMethod, testMethod.getDependsOn()));
    }

    private void isCyclic(RestelSuite method, List<RestelSuite> childMethods) {
        if (!CollectionUtils.isEmpty(childMethods)) {
            childMethods.forEach(m -> {
                        if (m.getSuiteName().equals(method.getSuiteName())) {
                            throw new RestelException("Cyclic dependency exist for TestSuite: " + method.getSuiteName());
                        } else {
                            if (!CollectionUtils.isEmpty(m.getDependsOn())) {
                                isCyclic(method, m.getDependsOn());
                            }
                        }
                    }
            );
        }
    }

    /**
     * check cyclic Dependency
     *
     * @param restelExecutionGroups
     */
    private void validateExecution(List<RestelExecutionGroup> restelExecutionGroups) {
        restelExecutionGroups.forEach(testMethod -> isCyclic(testMethod, testMethod.getDependsOn()));
    }

    private void isCyclic(RestelExecutionGroup method, List<RestelExecutionGroup> childMethods) {
        if (!CollectionUtils.isEmpty(childMethods)) {
            childMethods.forEach(m -> {
                        if (m.getExecutionGroupName().equals(method.getExecutionGroupName())) {
                            throw new RestelException("Cyclic dependency exist for TestSuiteExecution: " + method.getExecutionGroupName());
                        } else {
                            if (!CollectionUtils.isEmpty(m.getDependsOn())) {
                                isCyclic(method, m.getDependsOn());
                            }
                        }
                    }
            );
        }
    }

    /**
     * check if dependency is cyclic
     *
     * @param testMethods
     */
    private void validateDefinition(List<RestelTestMethod> testMethods) {
        testMethods.forEach(testMethod -> isCyclic(testMethod, testMethod.getDependentOn()));
    }

    private void isCyclic(RestelTestMethod method, List<RestelTestMethod> childMethods) {
        if (!CollectionUtils.isEmpty(childMethods)) {
            childMethods.forEach(m -> {
                        if (m.getCaseUniqueName().equals(method.getCaseUniqueName())) {
                            throw new RestelException("Cyclic dependency exist for TestDefinition: " + method.getCaseUniqueName());
                        } else {
                            if (!CollectionUtils.isEmpty(m.getDependentOn())) {
                                isCyclic(method, m.getDependentOn());
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