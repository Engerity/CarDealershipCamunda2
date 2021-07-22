package dealership.car.config;

import org.camunda.bpm.spring.boot.starter.rest.CamundaJerseyResourceConfig;
import org.springframework.stereotype.Component;

import javax.ws.rs.ApplicationPath;

@Component
@ApplicationPath("/engine-rest")
public class RestApiConfig extends CamundaJerseyResourceConfig {

}