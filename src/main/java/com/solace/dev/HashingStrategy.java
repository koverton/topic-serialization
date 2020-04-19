package com.solace.dev;

import java.util.List;

/**
 * Implements a topic hashing strategy for load-balancing complex topic hierarchies.
 *
 * @param <Event> Event type for translation into hashes
 */
public interface HashingStrategy<Event> {

    /**
     * Construct a hash string using the public attributes of an event.
     * @param event
     * @return hashed string value of the event.
     */
    String makeHash(Event event);

    /**
     * For initialization, it's helpful to be able to iterate through all the buckets.
     *
     * @return list of pre-calculated hash buckets.
     */
    List<String> getBuckets();
}
