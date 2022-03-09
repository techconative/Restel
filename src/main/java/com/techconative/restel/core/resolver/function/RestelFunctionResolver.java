package com.techconative.restel.core.resolver.function;

import com.techconative.restel.exception.RestelException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.techconative.restel.utils.Constants.COMMA;
import static com.techconative.restel.utils.Constants.NS_SEPARATOR_REGEX;

@Slf4j
public class RestelFunctionResolver {

    private static String arrayPattern = "\\[([\\d+,*]+)+\\]";

    private RestelFunctionResolver() {
    }

    public static Object resolveAddOperation(Map<String, Object> payload, String data, List<Object> elements) {
        //TODO Implement the logic
        //resolveAddVariable(payload, data, elements);
        return null;
    }

    private static Map<String, Object> resolveAddVariable(Map<String, Object> context, String variableName, List<Object> elements) {
        Map<String, Object> result = new HashMap<>(context);
        String[] tokens = variableName.split(NS_SEPARATOR_REGEX, 2);
        String var = tokens[0];

        Object object = context.get(var);
        if (tokens.length == 1) {
            //TODO: Implement the logic.
        }

        if (tokens.length > 1) {
            if (object instanceof Map) {
                object = resolveAddVariable((Map) object, tokens[1], elements);
            } else if (object instanceof List) {
                object = resolveAddVariableArray((List) object, tokens[1], elements);
            }
            log.error("The path " + variableName + " is not available in the context.");
        }
        result.put(var, object);
        return result;
    }

    private static List<Object> resolveAddVariableArray(List<Object> context, String variableName, List<Object> elements) {
        List<Object> result = new ArrayList<>(context);
        for (int i = 0; i < context.size(); i++) {
            Object val = null;
            Object element = result.get(i);
            if (element instanceof Map) {
                val = resolveAddVariable((Map) element, variableName, elements);
            } else if (element instanceof List) {
                val = resolveAddVariableArray((List) element, variableName, elements);
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
     * @param context  Context which needs to undergo the operation {@link com.techconative.restel.core.model.functions.FunctionOps}
     * @param data     Variable within the context which needs to undergo the operation {@link com.techconative.restel.core.model.functions.FunctionOps}
     * @param elements Other params
     * @return returns the context Object which has undergone the operation {@link com.techconative.restel.core.model.functions.FunctionOps}
     */
    public static Object resolveRemoveOperation(Object context, String data, List<String> elements) {
        if (CollectionUtils.isEmpty(elements)) {
            if (context instanceof Map) {
                return resolveRemoveVariable((Map<String, Object>) context, data);
            } else if (context instanceof List) {
                return resolveRemoveVariableArray((List) context, data);
            } else {
                throw new RestelException("INVALID_CONTEXT");
            }

        } else return null;
    }

    /**
     * @param context      Map of entity elements whose values needs to undergo a {@link com.techconative.restel.core.model.functions.FunctionOps} (like add / remove).
     * @param variableName name of the variable in the nested context map which needs to undergo a {@link com.techconative.restel.core.model.functions.FunctionOps}.
     * @return returns the context entities which have undergone the {@link com.techconative.restel.core.model.functions.FunctionOps} .
     */
    private static Map<String, Object> resolveRemoveVariable(Map<String, Object> context, String variableName) {
        //TODO : rewrite with proper logic for complex structure
        Map<String, Object> result = new HashMap<>(context);
        String[] tokens = variableName.split(NS_SEPARATOR_REGEX, 2);
        String var = tokens[0];
        Object object;
        if (var.matches(".*" + arrayPattern) && tokens.length == 1) {
            object = resolveRemoveArray(var, context);
            var = var.split(arrayPattern)[0];
        } else if (tokens.length == 1) {
            result.remove(var);
            return result;
        } else {
            object = context.get(var);
        }
        if (tokens.length > 1) {
            if (object instanceof Map) {
                object = resolveRemoveVariable((Map) object, tokens[1]);
            } else if (object instanceof List) {
                object = resolveRemoveVariableArray((List) object, tokens[1]);
            }
            log.error("The path " + variableName + " is not available in the context.");

        }
        result.put(var, object);

        return result;
    }

    /**
     * Iterated through the elements in the context which needs to undergo {@link com.techconative.restel.core.model.functions.FunctionOps} .
     *
     * @param context      Contains a list of elements which needs to be undergo a {@link com.techconative.restel.core.model.functions.FunctionOps}.
     * @param variableName variable name  which needs to be further iterated over.
     * @return return the context where the elements have undergone the {@link com.techconative.restel.core.model.functions.FunctionOps}.
     */
    private static List<Object> resolveRemoveVariableArray(List<Object> context, String variableName) {
        //TODO : rewrite with proper logic for complex structure
        List<Object> result = new ArrayList<>(context);
        for (int i = 0; i < context.size(); i++) {
            Object val = null;
            Object element = result.get(i);
            if (element instanceof Map) {
                val = resolveRemoveVariable((Map) element, variableName);
            } else if (element instanceof List) {
                val = resolveRemoveVariableArray((List) element, variableName);
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
    private static Object resolveRemoveArray(String variable, Map<String, Object> context) {
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