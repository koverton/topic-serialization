package com.solace.dev;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Topic hierarchy definition parser. The rules for constructing a topic definition are:
 * 1) Topic levels are delimited via a single '/' character; topics cannot begin nor end with a delimiter
 *    EX:    one/two
 *
 * 2) Literal strings may be included anywhere in the definition
 *    EX:    Case/Sensitive/Literal/Level
 *
 * 3) Fieldnames may be included anywhere in the definition surrounded by curly braces '{}'
 *    EX:    Literal1/{fieldName}/Literal2
 *
 * 4) A {fieldName} will be substituted with the associated getter on the event instance.
 *    EX:    {OrderID}  will substitute the return value from instance.getOrderID()
 *
 * Here are some example topic definitions:
 *
 *     OMS/{instance}/orders/{orderID}/{orderState}
 *
 *     Stores/{state}/{storeNumber}/Order/{orderId}/{action}
 *
 *     Location/{latitude}/{longitude}/{vid}
ß */
class TopicDefinitionParser {
    final static private Pattern fieldPattern = Pattern.compile("^(.*?)\\{(.+?)\\}(.*)$");

    /**
     * Parses an input topic definition return an ordered list of topic levels.
     *
     * @param expression topic definition as a string expression
     * @return Ordered list of topic levels as static field attributes
     */
    static List<Level> parse(String expression) {
        List<Level> matches = new ArrayList<>();
        while (expression != null && expression.length() > 0 ) {
            Matcher matcher = fieldPattern.matcher(expression);
            if (matcher.find()) {
                String preamble = matcher.group(1);
                if (!isNullOrEmpty(preamble)) matches.add(Level.stat(preamble));
                String field = matcher.group(2);
                if (!isNullOrEmpty(field))    matches.add(Level.fld(field.toLowerCase()));
                expression = matcher.group(matcher.groupCount());
            }
            else {
                matches.add(Level.stat(expression));
                expression = null;
            }
        }
        return matches;
    }

    private static boolean isNullOrEmpty(String s) {
        return (s == null) || (s.length()==0);
    }
}
