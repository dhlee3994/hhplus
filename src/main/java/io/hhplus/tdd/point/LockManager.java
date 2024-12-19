package io.hhplus.tdd.point;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Component;

@Component
public class LockManager {

	private final Map<Long, Lock> lockMap = new ConcurrentHashMap<>();

	public Lock getLock(final long id) {
		return lockMap.computeIfAbsent(id, k -> new ReentrantLock(true));
	}
}
