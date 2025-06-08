# Tracking Number Generator API

## Technologies
- Java 17
- Spring Boot 3.x
- MongoDB
- RabbitMQ
- Zipkin
- Docker & Kubernetes

## How to Run

### Docker Compose
```bash
docker-compose up --build
```

### API Endpoint
```http
GET /api/next-tracking-number?origin_country_id=MY&destination_country_id=ID&weight=1.234&created_at=2023-11-20T19:29:32+08:00&customer_id=uuid&customer_name=RedBox%20Logistics&customer_slug=redbox-logistics
```

## Deployment
Deployable to any container orchestration platform (Kubernetes, Heroku, etc.)

## Zipkin
Visit http://localhost:9411 to view traces.