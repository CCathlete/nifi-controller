package org.webcat.nificontroller.infrastructure.nifi.interfaces;

public interface NiFiClient {
  // Process Group Management.
  String createProcessGroup(String parentId, String groupName) throws Exception;

  void deleteProcessGroup(String processGroupId) throws Exception;

  // Processor Management.
  String addPutDatabaseRecordProcessor(String processGroupId, String dbUrl, String tableName) throws Exception;

  void startProcessor(String processorId) throws Exception;

  void stopProcessor(String processorId) throws Exception;

  void deleteProcessor(String processorId) throws Exception;

  // Flow Management.
  void startFlow(String flowId) throws Exception;

  void stopFlow(String flowId) throws Exception;

  void deleteFlow(String flowId) throws Exception;
}
