package net.es.oscars.pss.task;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.cmd.CommandStatus;
import net.es.oscars.dto.pss.st.LifecycleStatus;
import net.es.oscars.pss.svc.CommandQueuer;
import net.es.oscars.pss.svc.CommandRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;



@Slf4j
@Component
public class CommandProcessor {
    private CommandQueuer queuer;
    private CommandRunner runner;

    @Autowired
    public CommandProcessor(CommandQueuer queuer, CommandRunner runner) {

        this.queuer = queuer;
        this.runner = runner;
    }

    @Scheduled(fixedDelay = 1000)
    public void processsCommands() throws InterruptedException {

        queuer.ofLifecycleStatus(LifecycleStatus.WAITING).entrySet().forEach(e -> {
            String commandId = e.getKey();
            CommandStatus status = e.getValue();
            log.info("processing a command with id "+commandId);
            status.setLifecycleStatus(LifecycleStatus.PROCESSING);
            log.info("running command "+commandId);
            queuer.getCommand(commandId).ifPresent(cmd -> runner.run(status, cmd));
            log.info("completed command "+commandId);
            status.setLifecycleStatus(LifecycleStatus.COMPLETED);
            queuer.setCommandStatus(commandId, status);
        });

    }



}
