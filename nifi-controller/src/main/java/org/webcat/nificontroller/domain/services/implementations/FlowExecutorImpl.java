package org.webcat.nificontroller.domain.services.implementations;

import org.springframework.stereotype.Service;
import org.webcat.nificontroller.domain.services.interfaces.FlowExecutor;
import org.webcat.nificontroller.infrastructure.nifi.interfaces.NiFiClient;

@Service
public class FlowExecutorImpl implements FlowExecutor {
  private final NiFiClient nifiClient;

  public FlowExecutorImpl(NiFiClient nifiClient) {
    this.nifiClient = nifiClient;
  }

  @Override
  public void executeFlow(String flowId) {
    try {
      this.nifiClient.startFlow(flowId);
      System.out.printf("\nNiFi flow %s started.\n", flowId);
    } catch (Exception e) {
      throw new RuntimeException("Failed to start NiFi flow: " + flowId, e);
    }
  }

}
