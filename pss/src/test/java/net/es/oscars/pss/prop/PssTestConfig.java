package net.es.oscars.pss.prop;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "pss_test")
@Data
@Component
@NoArgsConstructor
public class PssTestConfig {


    @NonNull
    private String caseDirectory;
}


