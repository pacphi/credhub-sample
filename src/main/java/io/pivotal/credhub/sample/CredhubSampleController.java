package io.pivotal.credhub.sample;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import  io.pivotal.cfenv.core.CfEnv;
import io.pivotal.cfenv.core.CfService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class CredhubSampleController {

	private CfEnv cfEnv;
	
	public CredhubSampleController(CfEnv cfEnv) {
		this.cfEnv = cfEnv;
	}

	@GetMapping("/")
	public Map<String, Object> getAllProperties() {
		CfService credhub = cfEnv.findServiceByTag("credhub");
		Map<String, Object> result = new HashMap<>();
		if (credhub !=null ) {
			log.trace("Found service named {}", credhub.getName());
			if (credhub.getCredentials() != null) {
				result.put("clouds", credhub.getCredentials().getMap().get("clouds"));
				result.put("languages", credhub.getCredentials().getMap().get("languages"));
			}
		}
		return result;
	}
}