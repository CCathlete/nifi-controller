package org.webcat.nificontroller.application.services.implementations;

import org.springframework.stereotype.Service;
import org.webcat.nificontroller.application.services.interfaces.NiFiService;
import org.webcat.nificontroller.domain.services.interfaces.FlowExecutor;

/**
 * An application service that runs the domain service for executing a flow.
 */
@Service
public class NiFiServiceImpl implements NiFiService {

  private final FlowExecutor flowExecutor;

  public NiFiServiceImpl(
      FlowExecutor flowExecutor) {

    this.flowExecutor = flowExecutor;

  }

  @Override
  public void runFlow(String flowId) {

    flowExecutor.executeFlow(flowId);

  }

}
