package com.solace.dev;

import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.Topic;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static com.solace.dev.ReflectionHelper.applyGetter;
import static com.solace.dev.ReflectionHelper.getters;
import static com.solace.dev.TopicDefinitionParser.parse;

public class ReflectionBasedHashingTopicStrategy implements TopicStrategy<Object> {
    final private String             topicDefinition;
    final private Map<String,Method> getterMap;
    final private List<Level>        topicLevels;
    final private HashingStrategy<Object> hashingStrategy;

    public ReflectionBasedHashingTopicStrategy(Class clazz, String topicStrategy, ReflectionBasedHashingStrategy hashingStrategy) {
        this.getterMap   = getters(clazz);
        this.topicDefinition = topicStrategy;
        this.topicLevels = parse(topicStrategy);
        this.hashingStrategy = hashingStrategy;
    }

    @Override
    public Topic makeTopic(Object instance) {
        StringBuilder sb = new StringBuilder();
        for(Level l : topicLevels) {
            if (l.type.equals(Level.LvlType.FIELD)) {
                // Insert hash for {hash} field
                if (l.value.equals("hash")) {
                    String hash = hashingStrategy.makeHash(instance);
                    if (hash==null || hash.length()==0)
                        hash = "_";
                    sb.append(hash);
                }
                else {
                    // lookup and append a field value
                    Method method = getterMap.get(l.value);
                    sb.append( applyGetter(method, instance) );
                }
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

    @Override
    public String getTopicDefinition() {
        return topicDefinition;
    }
}
