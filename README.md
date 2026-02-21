# 📧 Automated Email Notification System

A full-stack Microservices application built with **Spring Boot** and **React**, featuring an automated **RabbitMQ** message queue and **SendGrid** integration. The entire infrastructure is containerized and automated using **Docker** and **Jenkins**.

## 🚀 DevOps Features
- **Containerization:** Entire stack (Frontend, Backend, DB, Message Queue) runs in isolated Docker containers.
- **Orchestration:** Managed via `docker-compose` for one-command environment setup.
- **CI/CD Pipeline:** Automated build and deployment using a `Jenkinsfile`.
- **Infrastructure as Code:** Networking and dependencies defined in `docker-compose.yml`.

## 🛠️ Tech Stack
- **Frontend:** React (Vite), Nginx
- **Backend:** Spring Boot 3.x, Spring Data JPA
- **Database:** MySQL 8.0
- **Message Queue:** RabbitMQ
- **Email Service:** SendGrid API

## 🏃 How to Run Locally
Ensure you have **Docker Desktop** installed, then run:

```bash
docker-compose up -d
