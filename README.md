# Tracking Number Generator API

## Technologies
- Java 17
- Spring Boot 3.x
- MongoDB
- RabbitMQ
- Zipkin
- Docker & Kubernetes

Tracking Generator

A Spring Boot microservice application that generates unique tracking numbers using country codes, supports persistence with MongoDB, asynchronous logging with RabbitMQ, and distributed tracing via Zipkin.

---

## 🚀 Features

- Unique tracking number generation using regex pattern
- MongoDB persistence of tracking requests
- REST API with Spring Boot
- Zipkin integration for distributed tracing
- Docker + Docker Compose support
- Kubernetes-ready configuration
- Exception handling and logs for observability

---

## 🔧 Tech Stack

- Java 17
- Spring Boot 3.x
- MongoDB
- RabbitMQ
- Zipkin (via Spring Cloud Sleuth / Micrometer Tracing)
- Docker / Docker Compose
- Kubernetes-ready YAML
- JUnit + Mockito (optional)
- Lombok & MapStruct

## 📬 API Endpoint

### ➤ Generate Tracking Number

**Request**
```http
GET /api/next-tracking-number
Query Parameters

originCountryId: IN

destinationCountryId: US

weight: 2.5

createdAt: 2025-06-07T09:00:00Z

customerId: 123e4567-e89b-12d3-a456-426614174000

customerName: Srikanth

customerSlug: jeldi-enterprises

Example

GET http://localhost:8080/api/next-tracking-number?originCountryId=IN&destinationCountryId=US&weight=2.5&createdAt=2025-06-07T09:00:00Z&customerId=123e4567-e89b-12d3-a456-426614174000&customerName=Srikanth&customerSlug=jeldi-enterprises

Response

json

{
  "trackingNumber": "INUSABC123XYZ",
  "generatedAt": "2025-06-07T09:00:01Z"
}
** Running with Docker Compose**
bash:

docker-compose up --build

## Zipkin
Visit http://localhost:9411 to view traces.
