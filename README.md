# Product Service

A small Spring Boot REST service for learning Java microservices and an AWS DevOps deployment path.

## Run locally

Requires Java 17+ and Maven 3.6.3+.

```powershell
mvn spring-boot:run
```

Try the endpoints from another PowerShell window:

```powershell
Invoke-RestMethod http://localhost:8080/api/products
Invoke-RestMethod http://localhost:8080/api/products/1
```

## Build a container image

```powershell
mvn clean package
docker build -t product-service:local .
docker run --rm -p 8080:8080 product-service:local
```

Next learning steps: add an order service, run both locally, push images to Amazon ECR, deploy with Amazon ECS on Fargate, then automate the build and deployment with GitHub Actions.
