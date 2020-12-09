package com.pramati.restel.testng;

import com.pramati.restel.core.managers.*;
import com.pramati.restel.core.model.*;
import com.pramati.restel.core.model.functions.RestelFunction;
import com.pramati.restel.core.resolver.RestelAssertionResolver;
import com.pramati.restel.core.resolver.RestelFunctionResolver;
import com.pramati.restel.exception.InvalidConfigException;
import com.pramati.restel.exception.RestelException;
import com.pramati.restel.utils.Constants;
import com.pramati.restel.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.pramati.restel.utils.Constants.*;

/**
 * Executor takes care of resolving the variables, making API call along with
 * the configured middlewares and does the testing as configured in the
 * {@link RestelTestMethod}.
 */
@Slf4j
public class TestCaseExecutor {
    @Autowired
    private RequestManager requestManager;

    @Autowired
    private RestelTestManager testManager;

    @Autowired
    private ContextManager contextManager;

    @Autowired
    private MatcherFactory matcherFactory;

    private RestelTestMethod testDefinition;

    private RestelExecutionGroup testExecutionDefinition;

    private String executionName;

    private TestContext testContext;

    public TestCaseExecutor(String executionName) {
        this.executionName = executionName;
        testContext = new TestContext(executionName);
    }

    @PostConstruct
    private void init() {

        if (!StringUtils.isEmpty(testManager.getBaseConfig().getBaseUrl())) {
            requestManager = new RequestManager(testManager.getBaseConfig().getBaseUrl());
        } else {
            throw new InvalidConfigException("Invalid Base URL Configured for "
                    + executionName);

        }

        testExecutionDefinition = testManager
                .getExecutionDefinition(executionName);

        if (Objects.isNull(testExecutionDefinition)) {
            throw new InvalidConfigException(
                    "Invalid test execution name "
                            + executionName);
        }

        String testDefinitionName = testExecutionDefinition
                .getTestDefinitionName();
        String suiteName = testExecutionDefinition.getTestSuiteName();

        testDefinition = testManager
                .getTestDefinitions(testDefinitionName);

        if (Objects.isNull(testDefinition)) {
            throw new InvalidConfigException(
                    "Invalid test definition name "
                            + testDefinitionName);
        }

        RestelSuite testSuite = testManager.getTestSuite(suiteName);

        if (testSuite == null) {
            throw new InvalidConfigException(
                    "Invalid test suite name "
                            + suiteName);
        }

        append(testContext, testSuite.getSuiteParams());
        if (MapUtils.isNotEmpty(testExecutionDefinition.getExecutionParams()) && MapUtils.isNotEmpty(testSuite.getSuiteParams())) {
            // validate if same param name exists in both test suite and test suite execution
            testExecutionDefinition.getExecutionParams().keySet().forEach(key -> {
                if (testSuite.getSuiteParams().keySet().contains(key)) {
                    throw new RestelException("Should not have same param name in testSuite and TestSuiteExecution, rename the param name:" + key + " for testExecution:" + testExecutionDefinition.getExecutionGroupName() + " since the same param name is present in the testSuite:" + testSuite.getSuiteName());
                }
            });
        }
        append(testContext, testExecutionDefinition.getExecutionParams());
    }

    public RestelExecutionGroup getExecutionGroup() {
        return testExecutionDefinition;
    }

    /**
     * Appends the second map to the first one if the second one is not null.
     *
     * @param srcMap      The src map to which the other map to be added.
     * @param mapToAppend The map to be appended
     */
    private void append(TestContext srcMap,
                        Map<String, Object> mapToAppend) {
        if (!CollectionUtils.isEmpty(mapToAppend)) {
            srcMap.putAll(mapToAppend);
        }
    }

    /**
     * Make the API call and execute the test corresponding to the given test
     * name.
     *
     * @return true when the test passes. False otherwise
     */
    public boolean executeTest() {
        executeFunctions();
        if (!CollectionUtils.isEmpty(testExecutionDefinition.getAssertions())) {
            executeAssertions();
        }
        RestelDefinitionManager manager = new RestelDefinitionManager(testDefinition, requestManager, matcherFactory, testContext);
        return manager.executeTest(testExecutionDefinition.getExecutionGroupName(), testExecutionDefinition.getTestSuiteName());
    }

    private void executeAssertions() {
        testExecutionDefinition.getAssertions().forEach(a -> {
            if (a.getActual().matches(".*" + Constants.VARIABLE_PATTERN + ".*")) {
                validateFunctionDataPattern(Utils.removeBraces(a.getActual()));
            }
            if (a.getExpected().matches(".*" + Constants.VARIABLE_PATTERN + ".*")) {
                validateFunctionDataPattern(Utils.removeBraces(a.getExpected()));
            }
            RestelAssertionResolver.resolve(testContext, a);
        });
    }

    /**
     * Execute the {@link RestelFunction} and append the variables to the testContext.
     */
    private void executeFunctions() {
        if (MapUtils.isNotEmpty(testExecutionDefinition.getFunctions())) {
            Map<String, Object> data = testExecutionDefinition.getFunctions().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> evalFunction(e.getValue())));
            data.keySet().forEach(name -> {
                if (testContext.getContextValues().containsKey(name)) {
                    throw new RestelException("The variable name of the function is already taken by test suite/test suite execution params. Please rename this variable: " + name);
                }
            });
            append(testContext, data);
        }
    }

    /**
     * Executes the Restel Functions from the {@link TestContext} data and return the results.
     *
     * @param function {@link RestelFunction}
     * @return Executes the function and return the results.
     */
    private Object evalFunction(RestelFunction function) {
        if (function.getData().matches(".*" + Constants.VARIABLE_PATTERN + ".*")) {
            validateFunctionDataPattern(Utils.removeBraces(function.getData()));
        }
        switch (function.getOperation()) {
            case ADD:
                return new RestelException("TO BE Implemented");
            case REMOVE:
                return execRemoveFunction(function);
            default:
                throw new RestelException("Invalid Function Operation:" + function.getOperation() + " for test suite execution:" + testExecutionDefinition.getExecutionGroupName());

        }
    }

    /**
     * Executes the {@link com.pramati.restel.core.model.functions.FunctionOps#REMOVE} operation of Restel Function from the {@link TestContext} data and return the results.
     *
     * @param function {@link RestelFunction}
     * @return execute the remove operation from {@link com.pramati.restel.core.model.functions.FunctionOps} and return the results.
     */
    private Object execRemoveFunction(RestelFunction function) {
        if (function.getData().matches(".*" + Constants.VARIABLE_PATTERN + ".*")) {
            function.setData(Utils.removeBraces(function.getData()));
            Map<String, Object> payload;
            if (function.getData().matches(REQUEST_PATTERN)) {
                payload = getPayload(function.getData(), REQUEST_PATTERN);
                function.setData(StringUtils.removeStartIgnoreCase(function.getData(), function.getData().split(REQUEST)[0]));
                return ((Map<String, Object>) RestelFunctionResolver.resolveRemoveOperation(payload, function.getData(), function.getElement())).get(REQUEST);

            } else if (function.getData().matches(RESPONSE_PATTERN)) {
                payload = getPayload(function.getData(), RESPONSE_PATTERN);
                function.setData(StringUtils.removeStartIgnoreCase(function.getData(), function.getData().split(RESPONSE)[0]));
                return ((Map<String, Object>) RestelFunctionResolver.resolveRemoveOperation(payload, function.getData(), function.getElement())).get(RESPONSE);

            } else {
                throw new RestelException("Error in variable pattern: " + function.getData() + " for the test suite execution:" + testExecutionDefinition.getExecutionGroupName());
            }

        } else {
            return RestelFunctionResolver.resolveRemoveOperation(GlobalContext.getInstance().getAll(), function.getData(), function.getElement());

        }
    }

    /**
     * Gets the request or response payload of the Test Suite execution based on the variable .
     * Eg:: for variable:- get_user_exec.get_user.response.userGroup and regex of with response parser will return the response payload of 'get_user' test_definition.
     *
     * @param variable pattern of the variable which tells about the test suite or test suite execution and test definition .
     *                 Generally should of of format - Eg: get_user_exec.get_user.response.userGroup or get_user_exec.get_user.request.userGroup.
     * @param regex    Regex pattern for parsing the request or response.
     * @return Parse the request or response and returns its payload.
     */
    private Map<String, Object> getPayload(String variable, String regex) {
        ContextManager manager = new ContextManager();
        Matcher m = Pattern.compile(regex).matcher(variable);
        if (m.find()) {
            Object data = manager.resolveVariableInNS(GlobalContext.getInstance().getAll(), m.group(1));
            if (data instanceof Map) {
                return (Map<String, Object>) data;
            }
        }
        throw new RestelException("Invalid Pattern: " + variable + " for test suite execution:" + testExecutionDefinition.getExecutionGroupName());
    }

    /**
     * Validates if follows the pattern of format :- Eg: get_user_exec.get_user.response.userGroup or get_user_exec.get_user.request.userGroup.
     * else throws an Exception.
     *
     * @param data is a pattern for validates if its
     *             of the format:- Eg: get_user_exec.get_user.response.userGroup or get_user_exec.get_user.request.userGroup .
     */
    private void validateFunctionDataPattern(String data) {
        String[] variables = data.split(Constants.NS_SEPARATOR_REGEX, 2);

        //Check if the execution name exists.
        if (!hasExecutionName(testExecutionDefinition.getDependsOn(), variables[0])) {
            throw new RestelException("the variable pattern: " + data + " does not have the execution name: "
                    + variables[0] + " for Test Suite Execution: " + testExecutionDefinition.getExecutionGroupName());
        }
        //Check if the test definition exists
        String[] tokens = variables[1].split(Constants.NS_SEPARATOR_REGEX, 2);
        if (!hasDefinitionName(testManager.getTestDefinitions(testManager.getExecutionDefinition(variables[0]).getTestDefinitionName()), tokens[0])) {
            throw new RestelException("the variable pattern: " + data + " does not have the test definition name: "
                    + tokens[0] + " for Test Suite Execution: " + testExecutionDefinition.getExecutionGroupName());
        }

        //check the corresponding token has request or response
        if (!StringUtils.startsWithIgnoreCase(tokens[1], Constants.RESPONSE) && !StringUtils.startsWithIgnoreCase(tokens[1], Constants.REQUEST)) {
            throw new RestelException("the variable pattern: " + data + " should have request or response after the test definition name : "
                    + tokens[0] + " for Test Suite Execution: " + testExecutionDefinition.getExecutionGroupName());

        }
    }

    /**
     * @param testDefinitions {@link RestelTestMethod}
     * @param definitionName  name of the test definition which needs to be checked if its belongs to the given test definition or its child test definition
     * @return Check whether the definitionName is equals to given tesDefinitions or its child testDefinitions,
     */
    private boolean hasDefinitionName(RestelTestMethod testDefinitions, String definitionName) {
        if (StringUtils.equals(testDefinitions.getCaseUniqueName(), definitionName)) {
            return true;
        } else {
            for (RestelTestMethod testMethod : testDefinitions.getDependentOn()) {
                if (hasDefinitionName(testMethod, definitionName)) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * @param executionGroups List of {@link RestelExecutionGroup}
     * @param executionName   name of the test suite execution.
     * @return validates if the executionName is present in the executionGroups or its child executionGroups (depends on elements)
     */
    private boolean hasExecutionName(List<RestelExecutionGroup> executionGroups, String executionName) {
        if (CollectionUtils.isEmpty(executionGroups)) {
            return false;
        } else {
            //Check if the variable exists in the dependent list.
            boolean hasValue = executionGroups.stream().filter(restelExecutionGroup -> StringUtils.equals(restelExecutionGroup.getExecutionGroupName(), executionName)).count() > 0;
            if (!hasValue) {
                for (RestelExecutionGroup executionGroup : executionGroups) {
                    // Check inside the child executions.
                    if (hasExecutionName(executionGroup.getDependsOn(), executionName)) {
                        return true;
                    }
                }
            } else {
                return true;
            }
        }
        return false;
    }

}