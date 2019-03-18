package org.moera.naming.registry;

import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.moera.naming.Config;
import org.moera.naming.data.Operation;
import org.moera.naming.data.OperationRepository;
import org.moera.naming.rpc.OperationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OperationRunner {

    private static Logger log = LoggerFactory.getLogger(OperationRunner.class);

    @Inject
    private Config config;

    @Inject
    private Registry registry;

    @Inject
    private OperationRepository operationRepository;

    private int capacity;

    @PostConstruct
    public void init() {
        capacity = config.getMaxOperationRate();
    }

    @Scheduled(fixedDelayString = "PT1M") // every minute
    public void runOperationQueue() {
        capacity += config.getAverageOperationRate();
        if (capacity > config.getMaxOperationRate()) {
            capacity = config.getMaxOperationRate();
        }
        log.debug("Fetching up to {} operations", capacity);

        List<Operation> operations = operationRepository.findAllByStatusOrderByAdded(OperationStatus.ADDED,
                PageRequest.of(0, capacity));
        capacity -= operations.size();
        operations.forEach(registry::executeOperation);
    }

}
