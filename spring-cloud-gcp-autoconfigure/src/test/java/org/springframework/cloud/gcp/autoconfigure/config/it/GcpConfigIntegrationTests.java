/*
 *  Copyright 2018 original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.gcp.autoconfigure.config.it;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.gcp.autoconfigure.config.GcpConfigBootstrapConfiguration;
import org.springframework.cloud.gcp.autoconfigure.core.GcpContextAutoConfiguration;

import static org.assertj.core.api.Assumptions.assumeThat;

/**
 * @author João André Martins
 */
public class GcpConfigIntegrationTests {

	private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(GcpContextAutoConfiguration.class,
					GcpConfigBootstrapConfiguration.class))
			.withPropertyValues("spring.cloud.gcp.config.enabled=true",
					"spring.cloud.gcp.config.name=myapp",
					"spring.cloud.gcp.config.profile=prod");

	@BeforeClass
	public static void enableTests() {
		assumeThat(System.getProperty("it.config")).isEqualTo("true");
	}

	@Test
	public void testConfiguration() {
		this.contextRunner.run(context -> {

		});
	}
}
