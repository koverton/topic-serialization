package com.solace.dev;

import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.Topic;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class ReflectionBasedTopicStrategy implements TopicStrategy<Object> {
    final private Map<String,Method> getterMap;
    final private List<Level> topicLevels;

    public ReflectionBasedTopicStrategy(Class clazz, String topicStrategy) {
        this.getterMap = ReflectionHelper.getters(clazz);
        this.topicLevels = TopicDefinitionParser.parse(topicStrategy);
    }

    @Override
    public Topic makeTopic(Object instance) {
        StringBuilder sb = new StringBuilder();
        for(Level l : topicLevels) {
            if (l.type.equals(Level.LvlType.FIELD)) {
                // lookup a field value
                Method method = getterMap.get(l.value);
                Object value = null;
                if (method != null) {
                    try {
                        value = method.invoke(instance);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (value != null)
                    sb.append(value.toString());
                else
                    sb.append('_');
            }
            else {
                // append static value
                sb.append(l.value);
            }
        }
        return JCSMPFactory.onlyInstance().createTopic(sb.toString());
    }

    @Override
    public Topic makeSubscription(Map<String,String> keyBasedFilters) {
        StringBuilder sb = new StringBuilder();
        for(Level l : topicLevels) {
            if (l.type.equals(Level.LvlType.FIELD)) {
                // lookup a field value
                sb.append(valueOrSplat(keyBasedFilters, l.value));
            }
            else {
                // append static value
                sb.append(l.value);
            }
        }
        return JCSMPFactory.onlyInstance().createTopic(sb.toString());
    }
    private String valueOrSplat(Map map, Object field) {
        if (map.containsKey(field)) return map.get(field).toString();
        return "*";
    }

}
