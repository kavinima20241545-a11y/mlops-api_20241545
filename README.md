# MLOps Pipeline Management API

A JAX-RS (Jersey) RESTful API for managing Machine Learning Workspaces, Models, and Evaluation Metrics.

---

## Overview

This API simulates a cloud-native MLOps backend. It exposes three primary resource collections:

| Resource | Base Path |
|---|---|
| Workspaces | `/api/v1/workspaces` |
| Models | `/api/v1/models` |
| Metrics (sub-resource) | `/api/v1/models/{modelId}/metrics` |

**Technology:** JAX-RS 2.x via Jersey 2.39.1, Grizzly2 embedded HTTP server, Jackson JSON.  
**Storage:** In-memory `HashMap` / `ArrayList` — no database.

---

## How to Build and Run

### Prerequisites
- Java 11+
- Maven 3.6+

### Steps

```bash
# 1. Clone the repository
git clone https://github.com/YOUR_USERNAME/mlops-api.git
cd mlops-api

# 2. Build the fat JAR
mvn clean package

# 3. Run the server
java -jar target/mlops-api-1.0-SNAPSHOT.jar
```

The server starts at `http://localhost:8080/api/v1`

---

## Sample curl Commands

### 1. Discovery endpoint
```bash
curl -s http://localhost:8080/api/v1 | python3 -m json.tool
```

### 2. Create a Workspace
```bash
curl -s -X POST http://localhost:8080/api/v1/workspaces \
  -H "Content-Type: application/json" \
  -d '{"teamName":"Robotics Lab","storageQuotaGb":300}' | python3 -m json.tool
```

### 3. List all Models filtered by status
```bash
curl -s "http://localhost:8080/api/v1/models?status=DEPLOYED" | python3 -m json.tool
```

### 4. Register a Model (replace WORKSPACE_ID with a real ID)
```bash
curl -s -X POST http://localhost:8080/api/v1/models \
  -H "Content-Type: application/json" \
  -d '{"framework":"PyTorch","status":"TRAINING","latestAccuracy":0.0,"workspaceId":"WSVISION-01"}' \
  | python3 -m json.tool
```

### 5. Post an Evaluation Metric (replace MODEL_ID)
```bash
curl -s -X POST http://localhost:8080/api/v1/models/MOD-8832/metrics \
  -H "Content-Type: application/json" \
  -d '{"accuracyScore":0.97}' | python3 -m json.tool
```

### 6. Attempt to delete a non-empty Workspace (triggers 409)
```bash
curl -s -X DELETE http://localhost:8080/api/v1/workspaces/WSVISION-01 | python3 -m json.tool
```

### 7. Try to add metric to DEPRECATED model (triggers 403)
```bash
# First deprecate a model via POST, then:
curl -s -X POST http://localhost:8080/api/v1/models/MOD-DEPR/metrics \
  -H "Content-Type: application/json" \
  -d '{"accuracyScore":0.5}' | python3 -m json.tool
```

---

## Report: Answers to Coursework Questions

### Part 1.1 – Role of MessageBodyWriter / JSON Provider (Jackson)

When a JAX-RS resource method returns a Java object, the runtime cannot send an object over HTTP — it must be converted to bytes. This is handled by a `MessageBodyWriter`. Jackson registers itself as an `MessageBodyWriter` for `application/json` via the `JacksonFeature`. When Jersey sees the return type and the `@Produces(MediaType.APPLICATION_JSON)` annotation, it selects Jackson's `JacksonJsonProvider`, which calls `ObjectMapper.writeValue()` to serialise the POJO into a JSON byte stream that is written to the HTTP response body. Without this provider, Jersey would throw a `MessageBodyProviderNotFoundException`.

---

### Part 1.2 – REST Statelessness and Horizontal Scaling

Statelessness means that every HTTP request must contain all information the server needs to process it — the server holds no session state between requests. Authentication tokens, pagination cursors, and filters must be sent on every call rather than stored server-side.

This directly enables horizontal scaling: because no server holds private session data for a client, any node in a load-balanced cluster can handle any request. Adding more servers is as simple as spinning up new instances behind a load balancer — no sticky sessions, no shared memory caches of client state, no data migration. This makes cloud-native APIs vastly easier to scale elastically under variable load.

---

### Part 2.1 – HTTP Cache-Control Headers

Adding `Cache-Control: public, max-age=60` to `GET /api/v1/workspaces` instructs both the client and any intermediate CDN/proxy to cache the workspace list for 60 seconds. Benefits:
- **Client side:** the browser/Postman/HTTP client avoids sending a repeat request within the 60-second window, cutting latency to near-zero for repeated reads.
- **Server side:** the server receives far fewer requests during peak load. Since workspace lists change infrequently, stale-for-60-seconds data is an acceptable trade-off and dramatically reduces CPU and I/O cycles.

---

### Part 2.2 – HEAD vs GET for Existence Checks

A client should use the **HEAD** method. HEAD is identical to GET but the server omits the response body. This means the client can inspect the HTTP status code (200 = exists, 404 = missing) and all response headers without downloading the full JSON payload. This is ideal when the client only needs a boolean "does this workspace exist?" answer and wants to conserve bandwidth — particularly important when workspaces carry large `modelIds` lists.

---

### Part 3.1 – Server-Generated IDs (Security & Data Integrity)

**Security:** If clients could supply their own IDs, a malicious actor could overwrite an existing resource by guessing or replaying a known ID (e.g., re-POSTing `"id":"MOD-8832"` to hijack a deployed model's record). Server-generated UUIDs are cryptographically random and unpredictable.

**Data Integrity:** Server generation guarantees ID uniqueness within the system. Client-supplied IDs risk collisions (two clients submitting the same ID simultaneously), which could silently overwrite data. The server is the single source of truth for identity, preventing orphaned references or inconsistent foreign-key relationships.

---

### Part 3.2 – URL Encoding for Special Characters

If a user queries `?framework=Scikit Learn & Tools`, the space and `&` are reserved or illegal in a URL query string. The client must **percent-encode** the value: `?framework=Scikit%20Learn%20%26%20Tools`. This is necessary because:
- Spaces are not valid in URLs and are ambiguous across HTTP implementations.
- `&` is the delimiter between query parameters — an unencoded `&` would split the value into two separate parameters, corrupting the query.
- Percent-encoding (RFC 3986) maps each reserved byte to `%XX` (its hex value), giving the server a single unambiguous string to decode.

---

### Part 4.1 – Class-Level vs Method-Level @Produces

Placing `@Produces(MediaType.APPLICATION_JSON)` at the **class level** sets a default for every method in that class, avoiding duplication. If a single method needs a different media type (e.g., returning a CSV download), it can declare its own `@Produces("text/csv")` annotation, which **overrides** the class-level value for that method only. JAX-RS always prefers the most specific annotation — method-level beats class-level, which beats the application default.

---

### Part 5.2 – Why Validation Failures Are 4xx, Not 5xx

HTTP status classes encode *who is at fault*:
- **4xx (Client Error):** The client sent a request that the server understood but cannot fulfil due to a problem with the client's data. A non-existent `workspaceId` is the client's mistake — they referenced a resource that does not exist. A 422 communicates "your payload was syntactically valid JSON but semantically incorrect."
- **5xx (Server Error):** The server itself failed through no fault of the client's request (e.g., database crash, uncaught `NullPointerException`).

Returning a 5xx for a validation error would mislead the client into thinking the server is broken and that retrying might help. A 4xx correctly signals "fix your request."

---

### Part 5.4 – Exception Mapper Resolution Priority

When both a specific mapper (e.g., `ExceptionMapper<LinkedWorkspaceNotFoundException>`) and the global `ExceptionMapper<Throwable>` are registered, the JAX-RS runtime selects the **most specific** mapper whose type is assignable from the thrown exception. It walks the exception's class hierarchy and picks the mapper registered for the most-derived matching type. `LinkedWorkspaceNotFoundException` is more specific than `Throwable`, so its mapper wins. The global `Throwable` mapper only fires when no more specific mapper exists — acting as the true safety net of last resort.

---

### Part 5.5 – Crucial HTTP Metadata from Filter Contexts

From **ContainerRequestContext**:
1. `getMethod()` — the HTTP verb (GET, POST, DELETE). Essential for understanding *what* operation triggered an error.
2. `getUriInfo().getRequestUri()` — the full request URI including query parameters. Pinpoints *which* resource path was accessed.

From **ContainerResponseContext**:
1. `getStatus()` — the HTTP status code returned. Immediately reveals whether the request succeeded or failed.
2. `getMediaType()` — the `Content-Type` of the response body. Useful for detecting serialisation issues (e.g., unexpected `text/html` instead of `application/json`).
