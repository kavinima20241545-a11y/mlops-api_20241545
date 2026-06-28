# MLOps Pipeline Management API

This is my coursework project for 5COSC022W Client-Server Architectures. I built a RESTful API using JAX-RS (Jersey) to manage Machine Learning Workspaces, Models and Evaluation Metrics for an AI research lab scenario.

---

## What This API Does

The API lets you create and manage ML workspaces and the models inside them. You can also track evaluation metrics for each model over time. Everything is stored in memory using HashMaps and ArrayLists — no database is used.

The three main things you can interact with are:

| Resource | Path |
|---|---|
| Workspaces | `/api/v1/workspaces` |
| Models | `/api/v1/models` |
| Metrics | `/api/v1/models/{modelId}/metrics` |

**Tech used:** Jersey 2.39.1, Grizzly2 HTTP server, Jackson for JSON, JDK 21, Maven

---

## How to Build and Run

### What you need installed
- Java 11 or higher (I used JDK 21)
- Maven 3.6+

### Steps to run it

**Step 1** — Clone the repo
```bash
git clone https://github.com/YOUR_USERNAME/mlops-api.git
cd mlops-api
```

**Step 2** — Build the JAR file
```bash
mvn clean package
```
You should see BUILD SUCCESS at the end.

**Step 3** — Start the server
```bash
java -jar target/mlops-api-1.0-SNAPSHOT.jar
```

Once it starts you will see a message saying the server is running. The API is then available at:
```
http://localhost:8080/api/v1
```
Leave the terminal open while testing.

---

## Sample curl Commands

### 1. Discovery endpoint — check the API is running
```bash
curl -s http://localhost:8080/api/v1 | python3 -m json.tool
```

### 2. Create a new Workspace
```bash
curl -s -X POST http://localhost:8080/api/v1/workspaces \
  -H "Content-Type: application/json" \
  -d '{"teamName":"Robotics Lab","storageQuotaGb":300}' | python3 -m json.tool
```

### 3. List Models filtered by status
```bash
curl -s "http://localhost:8080/api/v1/models?status=DEPLOYED" | python3 -m json.tool
```

### 4. Register a new Model
```bash
curl -s -X POST http://localhost:8080/api/v1/models \
  -H "Content-Type: application/json" \
  -d '{"framework":"PyTorch","status":"TRAINING","latestAccuracy":0.0,"workspaceId":"WSVISION-01"}' \
  | python3 -m json.tool
```

### 5. Post an Evaluation Metric for a model
```bash
curl -s -X POST http://localhost:8080/api/v1/models/MOD-8832/metrics \
  -H "Content-Type: application/json" \
  -d '{"accuracyScore":0.97}' | python3 -m json.tool
```

### 6. Try deleting a Workspace that still has models (returns 409)
```bash
curl -s -X DELETE http://localhost:8080/api/v1/workspaces/WSVISION-01 | python3 -m json.tool
```

### 7. Try adding a metric to a DEPRECATED model (returns 403)
```bash
curl -s -X POST http://localhost:8080/api/v1/models/MOD-DEPR/metrics \
  -H "Content-Type: application/json" \
  -d '{"accuracyScore":0.5}' | python3 -m json.tool
```

---

## Answers to Report Questions

### Part 1.1 – What does Jackson (MessageBodyWriter) do?

When a resource method returns a Java object, JAX-RS cannot send a Java object directly over HTTP. It needs to convert it to text first. This is the job of a `MessageBodyWriter`. In my project I registered `JacksonFeature` which tells Jersey to use Jackson as the JSON provider. When Jersey sees `@Produces(MediaType.APPLICATION_JSON)` on a method, it picks Jackson's `JacksonJsonProvider` to do the conversion. Jackson then calls its `ObjectMapper` internally to turn the Java object into a JSON string that goes into the HTTP response body. Without this, Jersey would throw an error saying no writer was found for the type.

---

### Part 1.2 – What is statelessness and why does it help with scaling?

Statelessness means the server does not remember anything about a client between requests. Every single request must include all the information needed to process it — things like who is making the request and what they want. The server does not store any session data.

This makes horizontal scaling much easier. If the server holds no client state, you can add as many extra server instances as you want behind a load balancer and any of them can handle any request. You do not need to worry about sending a user to the same server every time. This is very useful for cloud APIs because you can scale up or down depending on how busy things are without any complicated data sharing between servers.

---

### Part 2.1 – How do Cache-Control headers help performance?

I added `Cache-Control: public, max-age=60` to the GET workspaces endpoint. This tells the client it can reuse the response for 60 seconds without making a new request to the server. On the client side this means faster responses because no network call is needed for repeated requests within that window. On the server side it means fewer requests to handle during busy periods. Workspace data does not change very frequently so a 60 second cache is a reasonable trade-off.

---

### Part 2.2 – Which HTTP method should a client use just to check if a workspace exists?

The client should use **HEAD**. HEAD works exactly like GET but the server does not send a response body back. The client just gets the status code — 200 if it exists or 404 if it does not. This saves bandwidth because the client does not have to download the full workspace JSON just to check if it is there. This is especially useful when the workspace object is large.

---

### Part 3.1 – Why should the server generate the model ID instead of the client?

There are two main reasons. First, security — if clients could choose their own IDs, a malicious user could deliberately send an ID that already exists in the system and overwrite that record. Server-generated UUIDs are random and unpredictable so this cannot happen. Second, data integrity — if two clients send a POST at the same time and both pick the same ID, one will silently overwrite the other. The server generating the ID centrally prevents duplicates and keeps the data consistent.

---

### Part 3.2 – How must a client encode special characters in query parameters?

If a user tries to search for `?framework=Scikit Learn & Tools`, the space and the `&` character will break the URL. Spaces are not allowed in URLs and `&` is used to separate query parameters, so the value would be split in the wrong place. The client needs to percent-encode these characters. The space becomes `%20` and `&` becomes `%26`, giving `?framework=Scikit%20Learn%20%26%20Tools`. This is defined in RFC 3986 and ensures the server receives the value as one complete string.

---

### Part 4.1 – What is the difference between class-level and method-level @Produces?

If I put `@Produces(MediaType.APPLICATION_JSON)` at the top of the class, it applies to every method in that class automatically. This avoids having to repeat it on every single method. If one specific method needs to return something different, for example a CSV file, I can put `@Produces("text/csv")` on just that method and it will override the class-level annotation for that method only. JAX-RS always uses the most specific annotation it can find, so method-level beats class-level.

---

### Part 5.2 – Why is a missing workspaceId a 4xx error and not a 5xx error?

The difference between 4xx and 5xx is about whose fault the problem is. A 4xx error means the client did something wrong — they sent a request the server understood but could not process because the client's data was bad. In this case the client sent a `workspaceId` that does not exist, which is a mistake in their request. A 422 status tells them the JSON was valid but the content was not acceptable. A 5xx error means the server itself has a problem, like a crash or a bug. Returning a 5xx here would be misleading because the server is fine — it is the client's request that is wrong.

---

### Part 5.4 – How does JAX-RS decide which ExceptionMapper to use?

JAX-RS looks at the exception that was thrown and tries to find the most specific mapper registered for that type. For example if `LinkedWorkspaceNotFoundException` is thrown, JAX-RS checks all registered mappers and finds one specifically for that exception type. That one is used. The global `ExceptionMapper<Throwable>` only gets used if no more specific mapper matches the thrown exception. This means the specific mappers always take priority and the global one acts as a last resort safety net to catch anything unexpected.

---

### Part 5.5 – What useful information can you get from the filter contexts?

From `ContainerRequestContext` I can get:
1. `getMethod()` — tells me the HTTP method like GET or POST, which helps identify what kind of request caused a problem
2. `getUriInfo().getRequestUri()` — gives the full URL including query parameters, which tells me exactly which endpoint was called

From `ContainerResponseContext` I can get:
1. `getStatus()` — the HTTP status code that was sent back, so I can immediately see if something went wrong
2. `getMediaType()` — the content type of the response, useful for spotting if the wrong format was returned
