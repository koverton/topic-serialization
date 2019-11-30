package com.solace.dev;

import com.solacesystems.jcsmp.Topic;

import java.util.Map;

/**
 * Implements a full topic strategy for Event-Driven pub-sub architectures.
 *
 * @param <Event> Event type for translation into topics and subscriptions
 */
public interface TopicStrategy<Event> {
    /**
     * For Publishers: Create a full topic for publication based upon the contents of a message
     * and the underlying topic architecture rules.
     * @param evt Fully-specified event for publication.
     * @return Topic instance matching the topic architecture based upon contents of the evt parameter.
     */
    Topic makeTopic(Event evt);

    /**
     * For Subscribers: Create a full topic-subscription filter for subscription based upon the contents
     * of a request filter and the the underlying topic architecture rules.
     *
     * @param keyBasedFilters subscription filter in key-value pairs; e.g. itemName:'router'
     * @return Subscription filter instance matching the topic architecture based upon contents of the keyBasedFilters parameter.
     */
    Topic makeSubscription(Map<String,String> keyBasedFilters);
}
