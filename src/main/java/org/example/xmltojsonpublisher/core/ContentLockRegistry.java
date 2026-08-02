package org.example.xmltojsonpublisher.core;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class ContentLockRegistry {
    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    public ReentrantLock getLock(String contentId) {
        return locks.computeIfAbsent(contentId, id -> new ReentrantLock());
    }
}
