package com.solace.dev;

import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.Topic;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static com.solace.dev.ReflectionHelper.*;
import static com.solace.dev.TopicDefinitionParser.parse;

public class ReflectionBasedTopicStrategy implements TopicStrategy<Object> {
    final private Map<String,Method> getterMap;
    final private List<Level>        topicLevels;

    public ReflectionBasedTopicStrategy(Class clazz, String topicStrategy) {
        this.getterMap   = getters(clazz);
        this.topicLevels = parse(topicStrategy);
    }

    @Override
    public Topic makeTopic(Object instance) {
        StringBuilder sb = new StringBuilder();
        for(Level l : topicLevels) {
            if (l.type.equals(Level.LvlType.FIELD)) {
                // lookup and append a field value
                Method method = getterMap.get(l.value);
                sb.append( applyGetter(method, instance) );
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
            if ( l.type.equals(Level.LvlType.FIELD) ) {
                // lookup and append a field value
                sb.append( valueOrSplat(keyBasedFilters, l.value) );
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
