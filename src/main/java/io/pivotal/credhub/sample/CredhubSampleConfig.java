package io.pivotal.credhub.sample;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.pivotal.cfenv.core.CfEnv;

@Configuration
public class CredhubSampleConfig {

	@Bean
	public CfEnv cfEnv() {
		return new CfEnv();
	}
}
