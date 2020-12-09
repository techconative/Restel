package com.pramati.restel.core.managers;


import com.pramati.restel.core.model.BaseConfiguration;
import com.pramati.restel.core.model.RestelExecutionGroup;
import com.pramati.restel.core.model.RestelSuite;
import com.pramati.restel.core.model.RestelTestMethod;
import com.pramati.restel.core.parser.Parser;
import com.pramati.restel.core.parser.ParserEnums;
import com.pramati.restel.core.parser.config.ParserConfig;
import com.pramati.restel.core.parser.dto.BaseConfig;
import com.pramati.restel.core.parser.dto.TestDefinitions;
import com.pramati.restel.core.parser.dto.TestSuiteExecution;
import com.pramati.restel.core.parser.dto.TestSuites;
import com.pramati.restel.exception.RestelException;
import com.pramati.restel.utils.RestelUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class ExcelParseManager {

    private String filepath;

    private List<RestelTestMethod> testMethods;
    private List<RestelSuite> suites;
    private List<RestelExecutionGroup> execGroups;
    private BaseConfiguration baseConfig;

    public ExcelParseManager(String excelfilePath) {
        this.filepath = excelfilePath;
    }

    @PostConstruct
    private void init() {

        log.info("Parsing the file: {}", this.filepath);
        Map<String, Object> excelData;
        try {
            Parser parser = new Parser(ParserConfig.load());
            FileInputStream inputStream = new FileInputStream(new File(this.filepath));
            excelData = parser.parse(inputStream);

        } catch (FileNotFoundException ex) {
            throw new RestelException("File not Found", ex);
        } catch (Exception ex) {
            throw new RestelException("Parser failure", ex);
        }

        List<TestDefinitions> testDefs = (List<TestDefinitions>) excelData.get(ParserEnums.TEST_DEFINITIONS.toString().toLowerCase());
        List<TestSuites> testSuites = (List<TestSuites>) excelData.get(ParserEnums.TEST_SUITES.toString().toLowerCase());
        List<TestSuiteExecution> testSuiteExecutions = (List<TestSuiteExecution>) excelData.get(ParserEnums.TEST_SUITE_EXECUTION.toString().toLowerCase());
        baseConfig = createBaseConfigure((BaseConfig) excelData.get(ParserEnums.BASE_CONFIG.toString().toLowerCase()));

        testMethods = createTestMethod(testDefs, baseConfig);
        suites = createSuites(testSuites);
        execGroups = createExecGroups(testSuiteExecutions);
    }

    public List<RestelTestMethod> getTestMethods() {
        return testMethods;
    }

    public List<RestelSuite> getSuites() {
        return suites;
    }

    public List<RestelExecutionGroup> getExecGroups() {
        return execGroups;
    }

    public BaseConfiguration getBaseConfig() {
        return baseConfig;
    }

    private BaseConfiguration createBaseConfigure(BaseConfig config) {
        return RestelUtils.createBaseConfig(config);
    }

    /**
     * creates List of RestelTestMethod from TestDefinitions
     *
     * @param testDefinitions List of {@link TestDefinitions}
     * @return list of {@link RestelTestMethod}
     */
    private List<RestelTestMethod> createTestMethod(List<TestDefinitions> testDefinitions, BaseConfiguration baseConfig) {
        if (testDefinitions.isEmpty()) {
            throw new RestelException("No test Definition present in the sheet");
        }

        // Create a Map od case name and its Method definition.
        Map<String, RestelTestMethod> testMethodMap = new HashMap<>();
        testDefinitions.forEach(testDefinition ->
                testMethodMap.put(testDefinition.getCaseUniqueName(), RestelUtils.createTestMethod(testDefinition, baseConfig))
        );

        return testDefinitions.stream().map(testDefinition -> {
            RestelTestMethod testMethod = testMethodMap.get(testDefinition.getCaseUniqueName());
            if (!StringUtils.isEmpty(testDefinition.getDependsOn())) {
                List<RestelTestMethod> dependents = Arrays.asList(testDefinition.getDependsOn().split(",")).stream().map(name -> {
                    if (StringUtils.isNotEmpty(name) && Objects.isNull(testMethodMap.get(name))) {
                        throw new RestelException("No Test definition exist with the name: " + name);
                    }
                    return testMethodMap.get(name);
                }).collect(Collectors.toList());
                testMethod.setDependentOn(dependents);
                // added parent testDefinitions
                dependents.forEach(dep -> dep.addParentTest(testMethod.getCaseUniqueName()));
            }
            return testMethod;
        }).collect(Collectors.toList());

    }


    /**
     * creates List of RestelSuites from TestSuites
     *
     * @param testSuites List of {@link TestSuites}
     * @return List of {@link RestelSuite}
     */
    private List<RestelSuite> createSuites(List<TestSuites> testSuites) {
        if (testSuites.isEmpty()) {
            throw new RestelException("No test suites present in the sheet");
        }
        // Create a Map of suite name and its Method definition.
        Map<String, RestelSuite> suitesMap = new HashMap<>();
        testSuites.forEach(test -> suitesMap.put(test.getSuiteUniqueName(), RestelUtils.createSuite(test)));

        return testSuites.stream().map(suite -> {
            RestelSuite restSuite = suitesMap.get(suite.getSuiteUniqueName());
            if (!StringUtils.isEmpty(suite.getDependsOn())) {
                List<RestelSuite> dependents = Arrays.asList(suite.getDependsOn().split(",")).stream().map(name -> {
                    if (StringUtils.isNotEmpty(name) && Objects.isNull(suitesMap.get(name))) {
                        throw new RestelException("No Test suite exist with the name: " + name);
                    }
                    return suitesMap.get(name);
                }).collect(Collectors.toList());
                restSuite.setDependsOn(dependents);
                //add parent suites
                dependents.forEach(dep -> dep.addParentSuite(restSuite.getSuiteName()));
            }
            return restSuite;
        }).collect(Collectors.toList());
    }

    /**
     * creates List of RestelExecutionGroup from TestSuiteExecutions
     *
     * @param testSuiteExecutions List of {@link TestSuiteExecution}
     * @return list of {@link RestelExecutionGroup}
     */
    private List<RestelExecutionGroup> createExecGroups(List<TestSuiteExecution> testSuiteExecutions) {
        if (testSuiteExecutions.isEmpty()) {
            throw new RestelException("No test suite Definition present in the sheet");
        }
        Map<String, RestelExecutionGroup> execMap = new HashMap<>();
        testSuiteExecutions.forEach(test -> execMap.put(test.getTestExecutionUniqueName(), RestelUtils.createExecutionGroup(test)));
        return testSuiteExecutions.stream().map(execution -> {
            RestelExecutionGroup restexec = execMap.get(execution.getTestExecutionUniqueName());
            if (!StringUtils.isEmpty(execution.getDependsOn())) {
                List<RestelExecutionGroup> dependents = Arrays.stream(execution.getDependsOn().split(",")).map(name -> {
                    if (StringUtils.isNotEmpty(name) && Objects.isNull(execMap.get(name))) {
                        throw new RestelException("No Test suite execution exist with the name: " + name);
                    }
                    return execMap.get(name);
                }).collect(Collectors.toList());
                restexec.setDependsOn(dependents);
                //add parent exec
                dependents.forEach(dep -> dep.addParentExecution(restexec.getExecutionGroupName()));
            }
            return restexec;
        }).collect(Collectors.toList());
    }

}
