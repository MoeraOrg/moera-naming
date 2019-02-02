package org.moera.naming;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Config {

    @Value("${naming.operation-rate.average}")
    private int averageOperationRate;

    @Value("${naming.operation-rate.max}")
    private int maxOperationRate;

    @Value("${naming.mock-network-latency}")
    private boolean mockNetworkLatency;

    public int getAverageOperationRate() {
        return averageOperationRate;
    }

    public void setAverageOperationRate(int averageOperationRate) {
        this.averageOperationRate = averageOperationRate;
    }

    public int getMaxOperationRate() {
        return maxOperationRate;
    }

    public void setMaxOperationRate(int maxOperationRate) {
        this.maxOperationRate = maxOperationRate;
    }

    public boolean isMockNetworkLatency() {
        return mockNetworkLatency;
    }

    public void setMockNetworkLatency(boolean mockNetworkLatency) {
        this.mockNetworkLatency = mockNetworkLatency;
    }

}
