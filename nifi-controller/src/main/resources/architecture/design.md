You're absolutely right! We need:  
1. **A `Main` class in the presentation layer** to **bootstrap everything**.  
2. **Interfaces for services and infrastructure** to **follow DDD best practices**.  
3. **Move the REST controller to the presentation layer** to keep the structure clean.  

---

## **Updated DDD Structure**
```
/src/main/java/com/yourcompany/nifiintegration
│── application/                     → Application Layer (Use Cases)
│   ├── service/
│   │   ├── NiFiService.java          → Interface
│   │   ├── NiFiServiceImpl.java      → Implementation
│
│── domain/                          → Domain Layer (Core Business Logic)
│   ├── model/
│   │   ├── NiFiFlow.java             → Aggregate
│   │
│   ├── service/
│   │   ├── FlowExecutor.java         → Interface
│   │   ├── FlowExecutorImpl.java     → Implementation
│
│── infrastructure/                   → Infrastructure Layer (NiFi API)
│   ├── nifi/
│   │   ├── NiFiClient.java           → Interface
│   │   ├── NiFiClientImpl.java       → Implementation
│
│── presentation/                     → Presentation Layer (REST & Main)
│   ├── controller/
│   │   ├── NiFiController.java       → REST API
│   │
│   ├── Main.java                      → Entry point
```
---

## **1. Interfaces for Services and Infrastructure**
### **1.1. NiFiClient Interface**
```java
package com.yourcompany.nifiintegration.infrastructure.nifi;

public interface NiFiClient {
    void startFlow(String flowId) throws Exception;
    void stopFlow(String flowId) throws Exception;
    void deleteFlow(String flowId) throws Exception;
}
```
#### **1.2. Implementation (NiFiClientImpl)**
```java
package com.yourcompany.nifiintegration.infrastructure.nifi;

import org.apache.nifi.client.NiFiClient;
import org.apache.nifi.client.NiFiClientConfig;
import org.apache.nifi.client.NiFiClientFactory;
import org.apache.nifi.client.controller.ControllerClient;

public class NiFiClientImpl implements NiFiClient {
    private final NiFiClient nifiClient;
    private final ControllerClient controllerClient;

    public NiFiClientImpl(String nifiUrl) {
        this.nifiClient = NiFiClientFactory.createNiFiClient(
                NiFiClientConfig.builder().baseUrl(nifiUrl).build());
        this.controllerClient = nifiClient.getControllerClient();
    }

    @Override
    public void startFlow(String flowId) throws Exception {
        controllerClient.scheduleProcessGroup(flowId, "RUNNING");
    }

    @Override
    public void stopFlow(String flowId) throws Exception {
        controllerClient.scheduleProcessGroup(flowId, "STOPPED");
    }

    @Override
    public void deleteFlow(String flowId) throws Exception {
        controllerClient.deleteProcessGroup(flowId);
    }
}
```
---

## **2. Domain Layer**
### **2.1. FlowExecutor Interface**
```java
package com.yourcompany.nifiintegration.domain.service;

public interface FlowExecutor {
    void executeFlow(String flowId);
}
```
### **2.2. FlowExecutor Implementation**
```java
package com.yourcompany.nifiintegration.domain.service;

import com.yourcompany.nifiintegration.infrastructure.nifi.NiFiClient;

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

## **3. Application Layer**
### **3.1. NiFiService Interface**
```java
package com.yourcompany.nifiintegration.application.service;

public interface NiFiService {
    void runFlow(String flowId);
}
```
### **3.2. NiFiService Implementation**
```java
package com.yourcompany.nifiintegration.application.service;

import com.yourcompany.nifiintegration.domain.service.FlowExecutor;

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

## **4. Presentation Layer**
### **4.1. NiFiController (REST API)**
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
### **4.2. Main Class (Bootstrapping Everything)**
```java
package com.yourcompany.nifiintegration.presentation;

import com.yourcompany.nifiintegration.application.service.NiFiService;
import com.yourcompany.nifiintegration.application.service.NiFiServiceImpl;
import com.yourcompany.nifiintegration.domain.service.FlowExecutor;
import com.yourcompany.nifiintegration.domain.service.FlowExecutorImpl;
import com.yourcompany.nifiintegration.infrastructure.nifi.NiFiClient;
import com.yourcompany.nifiintegration.infrastructure.nifi.NiFiClientImpl;

public class Main {
    public static void main(String[] args) {
        String nifiUrl = "http://localhost:8080/nifi-api";
        NiFiClient niFiClient = new NiFiClientImpl(nifiUrl);
        FlowExecutor flowExecutor = new FlowExecutorImpl(niFiClient);
        NiFiService niFiService = new NiFiServiceImpl(flowExecutor);

        // Start a flow with a hardcoded ID (Replace with actual ID)
        String flowId = "your-flow-id";
        niFiService.runFlow(flowId);
    }
}
```
---

## **Final Thoughts**
✅ **Follows Clean DDD Architecture** (Separated layers).  
✅ **Uses Interfaces & Implementations** (Loose coupling, future extensibility).  
✅ **Main Class Bootstraps Everything** (No Spring needed unless you want REST).  
✅ **Supports REST API via `NiFiController`** (Optional but useful).  

**Next Steps**:  
1. Replace `"your-flow-id"` with an actual **NiFi flow ID**.  
2. **Run NiFi** and ensure it's reachable.  
3. **Test execution via the main class** or **REST API**.  

Would you like **error handling** or **logging improvements** added? 🚀