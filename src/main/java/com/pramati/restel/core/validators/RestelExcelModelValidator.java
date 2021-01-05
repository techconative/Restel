package com.pramati.restel.core.validators;

import com.pramati.restel.core.model.RestelExecutionGroup;
import com.pramati.restel.core.model.RestelSuite;
import com.pramati.restel.core.model.RestelTestMethod;
import com.pramati.restel.exception.RestelException;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RestelExcelModelValidator {
    private RestelExcelModelValidator() {
    }

    /**
     * check if dependency is cyclic. Note: all the testMethods with dependencies should be Direct Acyclic Graphs.
     *
     * @param testMethods
     */
    public static void validateDefinition(List<RestelTestMethod> testMethods) {
        List<String> names = testMethods.stream().map(RestelTestMethod::getCaseUniqueName).collect(Collectors.toList());
        // check dependOn
        testMethods.forEach(test -> validateDefinition(test, names));
        // check cyclic dependency
        testMethods.forEach(testMethod -> isCyclic(testMethod, testMethod.getDependentOn()));
    }

    /**
     * Check if the dependsOn is valid.
     *
     * @param testMethods         {@link RestelTestMethod}
     * @param testDefinitionNames list of all test definition names.
     */
    public static void validateDefinition(RestelTestMethod testMethods, List<String> testDefinitionNames) {
        if (CollectionUtils.isNotEmpty(testMethods.getDependentOn())) {
            testMethods.getDependentOn().forEach(depTest -> {
                if (!testDefinitionNames.contains(depTest.getCaseUniqueName())) {
                    throw new RestelException("TEST_INVALID_ERROR", depTest.getCaseUniqueName(), testMethods.getCaseUniqueName());
                }
            });
        }
    }

    /**
     * checks if there is any cyclic dependencies for testMethod.
     *
     * @param testMethod   {@link RestelTestMethod}
     * @param childMethods list of child {@link RestelTestMethod} for testMethod.
     */
    private static void isCyclic(RestelTestMethod testMethod, List<RestelTestMethod> childMethods) {
        if (!CollectionUtils.isEmpty(childMethods)) {
            childMethods.forEach(m -> {
                        if (m.getCaseUniqueName().equals(testMethod.getCaseUniqueName())) {
                            throw new RestelException("DEF_DEPENDENCY_ERROR", testMethod.getCaseUniqueName());
                        } else {
                            if (!CollectionUtils.isEmpty(m.getDependentOn())) {
                                isCyclic(testMethod, m.getDependentOn());
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
    public static void validateExecution(List<RestelExecutionGroup> restelExecutionGroups, Set<String> testDefinitions) {
        List<String> names = restelExecutionGroups.stream().map(RestelExecutionGroup::getExecutionGroupName).collect(Collectors.toList());
        // Check if dependent test suite execution exist
        restelExecutionGroups.forEach(exec -> validateExecution(exec, names, testDefinitions));

        //check dependency
        restelExecutionGroups.forEach(testMethod -> isCyclic(testMethod, testMethod.getDependsOn()));
    }

    /**
     * @param restelExecutionGroup      {@link RestelExecutionGroup}
     * @param restelExecutionGroupNames list of testSuiteExecutionNames
     * @param testDefinitions           list of test definition names.
     */
    public static void validateExecution(RestelExecutionGroup restelExecutionGroup, List<String> restelExecutionGroupNames, Set<String> testDefinitions) {
        // Check test definition name is valid
        if (!testDefinitions.contains(restelExecutionGroup.getTestDefinitionName())) {
            throw new RestelException("EXEC_TEST_INVALID_ERROR", restelExecutionGroup.getTestDefinitionName(), restelExecutionGroup.getExecutionGroupName());
        }

        //check if depends on is valid
        if (CollectionUtils.isNotEmpty(restelExecutionGroup.getDependsOn())) {
            String suiteName = restelExecutionGroup.getTestSuiteName();
            restelExecutionGroup.getDependsOn().forEach(dependExec -> {
                if (!restelExecutionGroupNames.contains(dependExec.getExecutionGroupName())) {
                    throw new RestelException("EXEC_INVALID_ERROR", dependExec.getExecutionGroupName(), restelExecutionGroup.getExecutionGroupName());
                }
                if (!suiteName.equals(dependExec.getTestSuiteName())) {
                    throw new RestelException("EXEC_DEPEND_INVALID_ERROR", dependExec.getExecutionGroupName(), suiteName, restelExecutionGroup.getExecutionGroupName());
                }
            });
        }

    }

    /**
     * check if executionGroup has cyclic dependency .
     *
     * @param executionGroup {@link RestelExecutionGroup}
     * @param childGroups    list of child {@link RestelExecutionGroup} for executionGroup.
     */
    private static void isCyclic(RestelExecutionGroup executionGroup, List<RestelExecutionGroup> childGroups) {
        if (!CollectionUtils.isEmpty(childGroups)) {
            childGroups.forEach(m -> {
                        if (m.getExecutionGroupName().equals(executionGroup.getExecutionGroupName())) {
                            throw new RestelException("EXEC_DEPENDENCY_ERROR", executionGroup.getExecutionGroupName());
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
     * check if dependency is cyclic. Note: all the restelSuites with dependencies should be Direct Acyclic Graphs.
     *
     * @param restelSuites
     */
    public static void validateSuites(List<RestelSuite> restelSuites) {
        List<String> names = restelSuites.stream().map(RestelSuite::getSuiteName).collect(Collectors.toList());
        // Check if dependent test suite exist
        restelSuites.forEach(suite -> suiteNames(suite, names));
        // Test is cyclic
        restelSuites.forEach(testMethod -> isCyclic(testMethod, testMethod.getDependsOn()));

    }

    /**
     * Check if the suite depends_on field is valid or else throws an Exception.
     *
     * @param suite      {@link RestelSuite}
     * @param suiteNames list of all the suiteNames.
     */
    public static void suiteNames(RestelSuite suite, List<String> suiteNames) {
        if (CollectionUtils.isNotEmpty(suite.getDependsOn())) {
            suite.getDependsOn().forEach(su -> {
                if (!suiteNames.contains(su.getSuiteName())) {
                    throw new RestelException("SUITE_INVALID_ERROR", su.getSuiteParams(), suite.getSuiteName());
                }
            });
        }
    }

    /**
     * check if restelSuite has cyclic dependency .
     *
     * @param restelSuite {@link RestelSuite}
     * @param childSuites list of child {@link RestelSuite} for restelSuite.
     */
    private static void isCyclic(RestelSuite restelSuite, List<RestelSuite> childSuites) {
        if (!CollectionUtils.isEmpty(childSuites)) {
            childSuites.forEach(m -> {
                        if (m.getSuiteName().equals(restelSuite.getSuiteName())) {
                            throw new RestelException("SUITE_DEPENDENCY_ERROR", restelSuite.getSuiteName());
                        } else {
                            if (!CollectionUtils.isEmpty(m.getDependsOn())) {
                                isCyclic(restelSuite, m.getDependsOn());
                            }
                        }
                    }
            );
        }
    }

}
