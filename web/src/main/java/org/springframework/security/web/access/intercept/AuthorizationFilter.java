/*
 * Copyright 2002-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.security.web.access.intercept;

import java.io.IOException;
import java.util.function.Supplier;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationEventPublisher;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.event.AuthorizationDeniedEvent;
import org.springframework.security.authorization.event.AuthorizationGrantedEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * An authorization filter that restricts access to the URL using
 * {@link AuthorizationManager}.
 *
 * @author Evgeniy Cheban
 * @since 5.5
 */
public class AuthorizationFilter extends OncePerRequestFilter {

	private final AuthorizationManager<HttpServletRequest> authorizationManager;

	private AuthorizationEventPublisher eventPublisher = AuthorizationFilter::noPublish;

	private boolean shouldFilterAllDispatcherTypes = false;

	/**
	 * Creates an instance.
	 * @param authorizationManager the {@link AuthorizationManager} to use
	 */
	public AuthorizationFilter(AuthorizationManager<HttpServletRequest> authorizationManager) {
		Assert.notNull(authorizationManager, "authorizationManager cannot be null");
		this.authorizationManager = authorizationManager;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		AuthorizationDecision decision = this.authorizationManager.check(this::getAuthentication, request);
		this.eventPublisher.publishAuthorizationEvent(this::getAuthentication, request, decision);
		if (decision != null && !decision.isGranted()) {
			throw new AccessDeniedException("Access Denied");
		}
		filterChain.doFilter(request, response);
	}

	private Authentication getAuthentication() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) {
			throw new AuthenticationCredentialsNotFoundException(
					"An Authentication object was not found in the SecurityContext");
		}
		return authentication;
	}

	@Override
	protected void doFilterNestedErrorDispatch(HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		doFilterInternal(request, response, filterChain);
	}

	@Override
	protected boolean shouldNotFilterAsyncDispatch() {
		return !this.shouldFilterAllDispatcherTypes;
	}

	@Override
	protected boolean shouldNotFilterErrorDispatch() {
		return !this.shouldFilterAllDispatcherTypes;
	}

	/**
	 * Use this {@link AuthorizationEventPublisher} to publish
	 * {@link AuthorizationDeniedEvent}s and {@link AuthorizationGrantedEvent}s.
	 * @param eventPublisher the {@link ApplicationEventPublisher} to use
	 * @since 5.7
	 */
	public void setAuthorizationEventPublisher(AuthorizationEventPublisher eventPublisher) {
		Assert.notNull(eventPublisher, "eventPublisher cannot be null");
		this.eventPublisher = eventPublisher;
	}

	/**
	 * Gets the {@link AuthorizationManager} used by this filter
	 * @return the {@link AuthorizationManager}
	 */
	public AuthorizationManager<HttpServletRequest> getAuthorizationManager() {
		return this.authorizationManager;
	}

	/**
	 * Sets whether to filter all dispatcher types.
	 * @param shouldFilterAllDispatcherTypes should filter all dispatcher types. Default
	 * is {@code false}
	 * @since 5.7
	 */
	public void setShouldFilterAllDispatcherTypes(boolean shouldFilterAllDispatcherTypes) {
		this.shouldFilterAllDispatcherTypes = shouldFilterAllDispatcherTypes;
	}

	private static <T> void noPublish(Supplier<Authentication> authentication, T object,
			AuthorizationDecision decision) {

	}

}
