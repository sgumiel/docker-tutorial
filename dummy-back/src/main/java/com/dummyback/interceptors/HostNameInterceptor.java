package com.dummyback.interceptors;

import java.lang.invoke.MethodHandles;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class HostNameInterceptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	@Before("execution(public * com.dummyback.controllers.UsersController.*(..))")
	public void doCrossCutStuff() {

		try {
			java.net.InetAddress localMachine = java.net.InetAddress.getLocalHost();
			LOGGER.debug("Request processed by hostname with id: " + localMachine.getHostName());
		} catch (Exception e) {}

	}
}

