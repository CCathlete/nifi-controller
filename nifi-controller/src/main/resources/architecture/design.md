Yes! You can use **Spring Boot** to initialize everything instead of manually wiring dependencies in `Main`. This will make the setup **cleaner**, **more scalable**, and **easier to extend** in the future.  

---

## **How to Integrate Spring Boot?**
1. **Mark service & infrastructure classes as Spring components (`@Service`, `@Repository`)**.  
2. **Use `@Autowired` for dependency injection**.  
3. **Use a Spring Boot `@Configuration` class to configure beans**.  
4. **Define `@SpringBootApplication` in `Main` to auto-wire everything**.  

---

## **Updated Code with Spring Boot**
### **1. Application Layer**
#### **1.1. NiFiService Interface**
```java
package com.yourcompany.nifiintegration.application.service;

public interface NiFiService {
    void runFlow(String flowId);
}
```
#### **1.2. NiFiServiceImpl Implementation**
```java
package com.yourcompany.nifiintegration.application.service;

import com.yourcompany.nifiintegration.domain.service.FlowExecutor;
import org.springframework.stereotype.Service;

@Service
public class NiFiServiceImpl implements NiFiService {
    private final FlowExecutor flowExecutor;

    public NiFiServiceImpl(FlowExecutor flowExecutor) {
        this.flowExecutor = flowExecutor;
    }

    @Override
    public void runFlow(String flowId) {
        flowExecutor.executeFlow(flowId);
    }
}
```

---

### **2. Domain Layer**
#### **2.1. FlowExecutor Interface**
```java
package com.yourcompany.nifiintegration.domain.service;

public interface FlowExecutor {
    void executeFlow(String flowId);
}
```
#### **2.2. FlowExecutorImpl Implementation**
```java
package com.yourcompany.nifiintegration.domain.service;

import com.yourcompany.nifiintegration.infrastructure.nifi.NiFiClient;
import org.springframework.stereotype.Service;

@Service
public class FlowExecutorImpl implements FlowExecutor {
    private final NiFiClient nifiClient;

    public FlowExecutorImpl(NiFiClient nifiClient) {
        this.nifiClient = nifiClient;
    }

    @Override
    public void executeFlow(String flowId) {
        try {
            nifiClient.startFlow(flowId);
            System.out.println("NiFi flow " + flowId + " started.");
        } catch (Exception e) {
            throw new RuntimeException("Failed to start NiFi flow: " + flowId, e);
        }
    }
}
```

---

### **3. Infrastructure Layer**
#### **3.1. NiFiClient Interface**
```java
package com.yourcompany.nifiintegration.infrastructure.nifi;

public interface NiFiClient {
    void startFlow(String flowId) throws Exception;
    void stopFlow(String flowId) throws Exception;
    void deleteFlow(String flowId) throws Exception;
}
```
#### **3.2. NiFiClientImpl Implementation**
```java
// Implemented down this doc using NiFi REST api.
```

---

### **4. Presentation Layer**
#### **4.1. NiFiController (REST API)**
```java
package com.yourcompany.nifiintegration.presentation.controller;

import com.yourcompany.nifiintegration.application.service.NiFiService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/nifi")
public class NiFiController {
    private final NiFiService niFiService;

    public NiFiController(NiFiService niFiService) {
        this.niFiService = niFiService;
    }

    @PostMapping("/run/{flowId}")
    public String startFlow(@PathVariable String flowId) {
        niFiService.runFlow(flowId);
        return "Flow " + flowId + " started";
    }
}
```

---

### **5. Configuration (To Auto-Wire NiFi Client)**
```java
package com.yourcompany.nifiintegration.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.yourcompany.nifiintegration.infrastructure.nifi.NiFiClient;
import com.yourcompany.nifiintegration.infrastructure.nifi.NiFiClientImpl;

@Configuration
public class AppConfig {
    @Bean
    public NiFiClient niFiClient() {
        return new NiFiClientImpl("http://localhost:8080/nifi-api");
    }
}
```

---

### **6. Main Class (Spring Boot)**
```java
package com.yourcompany.nifiintegration.presentation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
```

---

## **7. Add Configuration in `application.properties`**
Create a `src/main/resources/application.properties` file:
```
nifi.url=http://localhost:8080/nifi-api
server.port=8081
```
This makes it **easy to configure** without changing the code.

---

## **Final DDD Structure**
```
/src/main/java/com/yourcompany/nifiintegration
│── application/
│   ├── service/
│   │   ├── NiFiService.java
│   │   ├── NiFiServiceImpl.java
│
│── domain/
│   ├── service/
│   │   ├── FlowExecutor.java
│   │   ├── FlowExecutorImpl.java
│
│── infrastructure/
│   ├── nifi/
│   │   ├── NiFiClient.java
│   │   ├── NiFiClientImpl.java
│   ├── config/
│   │   ├── AppConfig.java
│
│── presentation/
│   ├── controller/
│   │   ├── NiFiController.java
│   ├── Main.java
│
/src/main/resources/
│── application.properties
```

---

## **Why This is Better?**
✅ **Spring Boot auto-wires everything** → No need for manual dependency injection.  
✅ **Configuration is in `application.properties`** → Easy to change NiFi URL.  
✅ **REST API (`NiFiController`) works out-of-the-box**.  
✅ **Follows DDD** → Services & infrastructure are **loosely coupled**.  
✅ **Scalable & Maintainable** → You can **extend NiFi features later** without breaking anything.  

---

## **How to Run It?**
1. **Start NiFi** → Ensure NiFi is running on `http://localhost:8080/nifi`.  
2. **Run the Spring Boot App**:
   ```sh
   mvn spring-boot:run
   ```
3. **Call the REST API to start a flow**:
   ```sh
   curl -X POST http://localhost:8081/nifi/run/{your-flow-id}
   ```
4. **Check NiFi UI** → The flow should be running.

---

## **Next Steps**
- Do you need error handling (e.g., retries, exception logging)?
- Do you want scheduled execution (e.g., run flows every X minutes)?
- Would you like unit tests added?

Let me know what you need next! 🚀

<h1>Support for bulk insertion</h1>
Here's a **simplified implementation** of `NiFiClientImpl` using **Spring WebClient** to interact with NiFi's REST API. It includes methods for:  
- **Creating a process group**  
- **Adding a PutDatabaseRecord processor** for bulk inserts  
- **Starting the flow**  

---

### **1️⃣ Update `pom.xml`** (if you haven't added WebClient yet)
```xml
<dependencies>
    <!-- Spring WebClient -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webflux</artifactId>
    </dependency>
</dependencies>
```

---

### **2️⃣ Implement `NiFiClientImpl`**
```java
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
    this.webClient.delete().uri("/process-groups/{processGroupId}", processGroupId).retrieve().toBodilessEntity()
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

```

---

### **3️⃣ How to Use It**
Inside `NiFiServiceImpl.java` (or another service), call:
```java
NiFiClient client = new NiFiClientImpl("http://localhost:8080/nifi-api");

String processGroupId = client.createProcessGroup("root", "BulkInsertFlow");
String processorId = client.addPutDatabaseRecordProcessor(processGroupId, "jdbc:postgresql://db:5432/mydb", "my_table");

client.startProcessor(processorId);
```

---

### **What This Does**
✔️ **Creates a process group** in NiFi  
✔️ **Adds a PutDatabaseRecord processor** for bulk insertion  
✔️ **Starts the processor**  

---

### **Next Steps**
- Do you need to **configure schema and record readers** for `PutDatabaseRecord`?  
- Should we also **connect processors together** (e.g., CSV Reader → PutDatabaseRecord)?