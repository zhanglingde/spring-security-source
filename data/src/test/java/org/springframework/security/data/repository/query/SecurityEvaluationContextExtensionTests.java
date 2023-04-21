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

package org.springframework.security.data.repository.query;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.security.access.expression.DenyAllPermissionEvaluator;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.hierarchicalroles.NullRoleHierarchy;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

public class SecurityEvaluationContextExtensionTests {

	SecurityEvaluationContextExtension securityExtension;

	@BeforeEach
	public void setup() {
		this.securityExtension = new SecurityEvaluationContextExtension();
	}

	@AfterEach
	public void cleanup() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void getRootObjectSecurityContextHolderAuthenticationNull() {
		assertThatIllegalArgumentException().isThrownBy(() -> getRoot().getAuthentication());
	}

	@Test
	public void getRootObjectSecurityContextHolderAuthentication() {
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("user", "password", "ROLE_USER");
		SecurityContextHolder.getContext().setAuthentication(authentication);
		assertThat(getRoot().getAuthentication()).isSameAs(authentication);
	}

	@Test
	public void getRootObjectExplicitAuthenticationOverridesSecurityContextHolder() {
		TestingAuthenticationToken explicit = new TestingAuthenticationToken("explicit", "password", "ROLE_EXPLICIT");
		this.securityExtension = new SecurityEvaluationContextExtension(explicit);
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("user", "password", "ROLE_USER");
		SecurityContextHolder.getContext().setAuthentication(authentication);
		assertThat(getRoot().getAuthentication()).isSameAs(explicit);
	}

	@Test
	public void getRootObjectExplicitAuthentication() {
		TestingAuthenticationToken explicit = new TestingAuthenticationToken("explicit", "password", "ROLE_EXPLICIT");
		this.securityExtension = new SecurityEvaluationContextExtension(explicit);
		assertThat(getRoot().getAuthentication()).isSameAs(explicit);
	}

	@Test
	public void getRootObjectWhenAdditionalFieldsNotSetThenVerifyDefaults() {
		TestingAuthenticationToken explicit = new TestingAuthenticationToken("explicit", "password", "ROLE_EXPLICIT");
		this.securityExtension = new SecurityEvaluationContextExtension(explicit);
		SecurityExpressionRoot root = getRoot();
		assertThat(root).extracting("trustResolver").isInstanceOf(AuthenticationTrustResolverImpl.class);
		assertThat(root).extracting("roleHierarchy").isInstanceOf(NullRoleHierarchy.class);
		assertThat(root).extracting("permissionEvaluator").isInstanceOf(DenyAllPermissionEvaluator.class);
		assertThat(root).extracting("defaultRolePrefix").isEqualTo("ROLE_");
	}

	private SecurityExpressionRoot getRoot() {
		return this.securityExtension.getRootObject();
	}

}
