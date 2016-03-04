package net.es.oscars.ds.conf.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@ConfigurationProperties
@Data
@Component
public class StartupConfigContainer {
    public StartupConfigContainer() {

    }
    @NestedConfigurationProperty
    StartupConfigEntry startupDefaults;

    List<StartupConfigEntry> startupConfigs;


}
