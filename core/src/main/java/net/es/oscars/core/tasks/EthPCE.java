package net.es.oscars.core.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


@Slf4j
@Component
public class EthPCE {
    @Autowired
    private RestTemplate restTemplate;


    @Scheduled(fixedDelay = 10000)
    public void findPath() {

    }

}
