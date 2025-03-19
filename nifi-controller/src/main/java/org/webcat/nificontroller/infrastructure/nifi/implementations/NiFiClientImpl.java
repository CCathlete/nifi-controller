package org.webcat.nificontroller.infrastructure.nifi.implementations;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;
import org.webcat.nificontroller.infrastructure.nifi.interfaces.NiFiClient;

@Repository
public class NiFiClientImpl implements NiFiClient {
  private final WebClient webClient;
  // private final String nifiBaseUrl;

  public NiFiClientImpl(String nifiBaseUrl) {
    // this.nifiBaseUrl = nifiBaseUrl;
    this.webClient = WebClient.builder().baseUrl(nifiBaseUrl).build();
  }

  /**
   * Creating a new process group and returning the id of the new process group.
   */
  @Override
  public String createProcessGroup(String parentId, String groupName) {
    Map<String, Object> requestBody = Map.of(
        "revision", Map.of("version", 0),
        "component", Map.of("name", groupName, "position", Map.of("x", 0.0, "y", 0.0)));

    // We return the response body (as string) of the POST request.
    return this.webClient.post().uri("/process-groups/{parentId}/process-groups", parentId)
        .contentType(MediaType.APPLICATION_JSON).bodyValue(requestBody).retrieve().bodyToMono(Map.class)
        .map(response -> {

          if (response.get("id") instanceof String id) {
            return (String) id;
          }

          throw new RuntimeException("ID is not a string: " + response.get("id"));

        }).block();
  }

  @Override
  public void deleteProcessGroup(String processGroupId) {
    this.webClient.get()
        .uri("/process-groups/{processGroupId}", processGroupId)
        .retrieve()
        .bodyToMono(Map.class) // Returns a raw Map<?, ?>
        .map(response -> { // An arrow function that checks the types of the response.
          if (!(response.get("revision") instanceof Map revision)) {
            throw new RuntimeException("Unexpected response structure: " + response);
          }
          if (!(revision.get("version") instanceof Integer version)) {
            throw new RuntimeException("Version is not an Integer: " + revision);
          }
          return version;
        })
        .flatMap(version -> this.webClient.delete()
            .uri("/process-groups/{processGroupId}?version={version}", processGroupId, version)
            .retrieve()
            .toBodilessEntity())
        .block();
  }

  /**
   * Adding a PutDatabaseRecord processor for bulk inserts.
   */
  @Override
  public String addPutDatabaseRecordProcessor(String processGroupId, String dbUrl, String tableName) {
    Map<String, Object> requestBody = Map.of(
        "revision", Map.of("version", 0),
        "component", Map.of(
            "type", "org.apache.nifi.processors.standard.PutDatabaseRecord",
            "position", Map.of("x", 100.0, "y", 100.0),
            "configurations", Map.of(
                "database.url", dbUrl,
                "table.name", tableName,
                "statement.type", "INSERT")));

    return webClient.post().uri("/process-groups/{processGroupId}/processors", processGroupId)
        .contentType(MediaType.APPLICATION_JSON).bodyValue(requestBody).retrieve().bodyToMono(Map.class)
        .map(response -> {
          if (response.get("id") instanceof String id) {
            return (String) id;
          }

          throw new RuntimeException("ID is not a string: " + response.get("id"));

        }).block();
  }

  @Override
  public void startProcessor(String processorId) {
    Map<String, Object> requestBody = Map.of(
        "revision", Map.of("version", 0),
        "component", Map.of("state", "RUNNING"));

    this.webClient.put()
        .uri("/processors/{processorId}/run-status", processorId)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(requestBody)
        .retrieve()
        .toBodilessEntity()
        .block();
  }

  @Override
  public void stopProcessor(String processorId) {
    Map<String, Object> requestBody = Map.of(
        "revision", Map.of("version", 0),
        "component", Map.of("state", "STOPPED"));

    this.webClient.put()
        .uri("/processors/{processorId}/run-status", processorId)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(requestBody)
        .retrieve()
        .toBodilessEntity()
        .block();
  }

  @Override
  public void deleteProcessor(String processorId) {
    this.webClient.delete()
        .uri("/processors/{processorId}", processorId)
        .retrieve()
        .toBodilessEntity()
        .block();
  }

  @Override
  public void startFlow(String flowId) throws Exception {
    // A flow could have multiple processors. Here, we assume a simple case:
    this.startProcessor(flowId);
  }

  @Override
  public void stopFlow(String flowId) throws Exception {
    // Same as above.
    this.stopProcessor(flowId);
  }

  @Override
  public void deleteFlow(String flowId) throws Exception {
    // For now, deleting a flow means deleting its processor group.
    this.deleteProcessGroup(flowId);
  }

}
