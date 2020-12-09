package com.pramati.restel.core.resolver;

import com.pramati.restel.exception.RestelException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.pramati.restel.utils.Constants.COMMA;
import static com.pramati.restel.utils.Constants.NS_SEPARATOR_REGEX;

@Slf4j
public class RestelFunctionResolver {

    private static String arrayPattern = "\\[([\\d+,*]+)+\\]";

    private RestelFunctionResolver() {
    }

    /**
     * @param context  Context which needs to undergo the operation {@link com.pramati.restel.core.model.functions.FunctionOps}
     * @param data     Variable within the context which needs to undergo the operation {@link com.pramati.restel.core.model.functions.FunctionOps}
     * @param elements Other params
     * @return returns the context Object which has undergone the operation {@link com.pramati.restel.core.model.functions.FunctionOps}
     */
    public static Object resolveRemoveOperation(Object context, String data, List<String> elements) {
        if (CollectionUtils.isEmpty(elements)) {
            if (context instanceof Map) {
                return resolveVariable((Map<String, Object>) context, data);
            } else if (context instanceof List) {
                return resolveVariableArray((List) context, data);
            } else {
                throw new RestelException("Invalid context ");
            }

        } else return null;
    }

    /**
     * @param context      Map of entity elements whose values needs to undergo a {@link com.pramati.restel.core.model.functions.FunctionOps} (like add / remove).
     * @param variableName name of the variable in the nested context map which needs to undergo a {@link com.pramati.restel.core.model.functions.FunctionOps}.
     * @return returns the context entities which have undergone the {@link com.pramati.restel.core.model.functions.FunctionOps} .
     */
    private static Map<String, Object> resolveVariable(Map<String, Object> context, String variableName) {
        //TODO : rewrite with proper logic for complex structure
        Map<String, Object> result = new HashMap<>();
        String[] tokens = variableName.split(NS_SEPARATOR_REGEX, 2);
        String var = tokens[0];
        Object object;
        if (var.matches(".*" + arrayPattern)) {
            object = resolveArray(var, context);
            var = var.split(arrayPattern)[0];
        } else {
            object = context.get(var);
        }
        if (tokens.length > 1) {
            if (object instanceof Map) {
                object = resolveVariable((Map) object, tokens[1]);
            } else if (object instanceof List) {
                object = resolveVariableArray((List) object, tokens[1]);
            }
            log.error("The path " + variableName + " is not available in the context.");

        }
        result.put(var, object);
        return result;
    }


    /**
     * Iterated through the elements in the context which needs to undergo {@link com.pramati.restel.core.model.functions.FunctionOps} .
     *
     * @param context      Contains a list of elements which needs to be undergo a {@link com.pramati.restel.core.model.functions.FunctionOps}.
     * @param variableName variable name  which needs to be further iterated over.
     * @return return the context where the elements have undergone the {@link com.pramati.restel.core.model.functions.FunctionOps}.
     */
    private static List<Object> resolveVariableArray(List<Object> context, String variableName) {
        //TODO : rewrite with proper logic for complex structure
        List<Object> result = new ArrayList<>(context);
        for (int i = 0; i < context.size(); i++) {
            Object val = null;
            Object element = result.get(i);
            if (element instanceof Map) {
                val = resolveVariable((Map) element, variableName);
            } else if (element instanceof List) {
                val = resolveVariableArray((List) element, variableName);
            }
            // replace the elements
            if (!Objects.isNull(val)) {
                result.set(i, val);
            } else {
                result.set(i, element);
            }
        }
        return result;
    }

    /**
     * resolve the variable by removing the indexed value of the array.
     * Eg: for context:- {userGroup:[{name: Adam},{name:Sam},{name:Tom}]} , the variable userGroup[1,2] will return the object:- {name:Adam}
     *
     * @param variable The variable name to be looked at. Usually the variables will be of format eg: userGroup[0]
     * @param context  Context in which the resolution to be done.
     * @return The value represented the variable
     */
    private static Object resolveArray(String variable, Map<String, Object> context) {
        Matcher m = Pattern.compile(arrayPattern).matcher(variable);
        String[] arrayToken = variable.split(arrayPattern);
        Object object = context.get(arrayToken[0]);
        if (m.find() && object instanceof List) {
            List<Object> obj = new ArrayList<>((List<Object>) object);
            for (int numb : Arrays.stream(m.group(1).split(COMMA)).map(Integer::valueOf).collect(Collectors.toList())) {
                obj.remove(((List<Object>) object).get(numb));
            }
            return obj;
        }
        return object;
    }

    private static List<List<String>> fetchArrayIndexes(String variable) {
        Matcher m = Pattern.compile(arrayPattern).matcher(variable);
        List<List<String>> indexes = new ArrayList<>();
        while (m.find()) {
            indexes.add(Arrays.stream(m.group(1).split(COMMA)).collect(Collectors.toList()));
        }
        return indexes;
    }
}