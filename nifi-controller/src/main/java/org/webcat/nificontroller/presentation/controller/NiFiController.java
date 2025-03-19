package org.webcat.nificontroller.presentation.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.webcat.nificontroller.application.services.interfaces.NiFiService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/nifi")
public class NiFiController {

  private final NiFiService nifiService;

  public NiFiController(NiFiService nifService) {
    this.nifiService = nifService;
  }

  @PostMapping("/run/{flowId}")
  public String startFlow(@PathVariable String flowId) {

    this.nifiService.runFlow(flowId);

    return "Flow " + flowId + " started";
  }

}
