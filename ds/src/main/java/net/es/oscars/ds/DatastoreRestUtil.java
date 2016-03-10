package net.es.oscars.ds;

import net.es.oscars.rest.RestProperties;
import net.es.oscars.rest.RestTemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@ComponentScan(basePackageClasses=RestProperties.class)
public class DatastoreRestUtil {

    @Autowired
    private RestProperties restProperties;

    @Bean
    public RestTemplate rest() throws Exception {
        return new RestTemplateBuilder().build(restProperties);
    }
}
