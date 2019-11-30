package com.solace.dev;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Level {
    enum LvlType { STATIC, FIELD }
    private static Level stat(String val) {
        return new Level(LvlType.STATIC, val);
    }
    private static Level fld(String val) {
        return new Level(LvlType.FIELD, val);
    }
    private Level(LvlType type, String val) {
        this.type = type; this.value = val;
    }
    LvlType type;
    String  value;

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if ( !(obj instanceof Level) ) return false;
        Level rhs = (Level) obj;
        return ( rhs.type.equals(this.type) && rhs.value.equals(this.value) );
    }


    /**
     * Topic hierarchy definition parser
     */

    final static private Pattern fieldPattern = Pattern.compile("^(.*?)\\{(.+?)\\}(.*)$");
    static List<Level> parseTopicStrategy(String expression) {
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
