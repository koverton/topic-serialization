package com.solace.dev;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Level {
    enum LvlType { STATIC, FIELD }
    static Level stat(String val) {
        return new Level(LvlType.STATIC, val);
    }
    static Level fld(String val) {
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
}
