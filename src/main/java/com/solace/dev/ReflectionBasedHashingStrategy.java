package com.solace.dev;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ReflectionBasedHashingStrategy implements HashingStrategy<Object> {

    public ReflectionBasedHashingStrategy(int maxHashCount, String idFieldGetterName) {
        this.maxHashCount = maxHashCount;
        int places = (int) Math.ceil( Math.log10((double)maxHashCount) );
        this.partitionFormat = "%0" + places + "d";
        for(long l = 0; l < maxHashCount; ++l) {
            buckets.add( makeHashInternal(l) );
        }
        this.idFieldGetterName = idFieldGetterName;
    }

    //
    // HASHING
    //


    @Override
    public List<String> getBuckets() {
        return buckets;
    }

    @Override
    public String makeHash(Object l) {
        Class<?> theClass = l.getClass();
        try {
            Method method = theClass.getMethod(idFieldGetterName);
            Object instID = method.invoke(l);
            if (instID instanceof Long) {
                Long id = (Long) instID;
                return makeHashInternal(id);
            }
            else if (instID instanceof Integer) {
                Integer id = (Integer) instID;
                return makeHashInternal(id);
            }
        }
        catch(Exception e) {}
        return ""; // TODO: now what?
    }

    private String makeHashInternal(long l) {
        String partition = String.format(partitionFormat, (l % maxHashCount));
        return String.format("%s", reverse(partition));
    }

    /**
     * Reversing the integer-based ID provides better load-balancing behavior
     * because it avoids bucketing consecutive values together.
     *
     * @param input String value to be reversed.
     * @return String reversal of the input string.
     */
    private static String reverse(String input)
    {
        char[] cs = input.toCharArray();
        int n = cs.length;
        int mid = n / 2;
        for (int i = 0; i < mid; ++i) {
            char c = cs[i];
            cs[i] = cs[n-1-i];
            cs[n-1-i] = c;
        }
        return new String(cs);
    }

    final int maxHashCount;
    final String idFieldGetterName;
    final String partitionFormat;
    final private List<String> buckets = new ArrayList<>();
}
