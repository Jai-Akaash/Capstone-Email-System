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
🔑 Phase 1: External API Setup
1. SendGrid Configuration
Log in to SendGrid.

Navigate to Settings -> Sender Authentication and verify the email address you will use to send emails (e.g., your-email@gmail.com).

Navigate to Settings -> API Keys and click Create API Key.

Choose Full Access, name it "Capstone-API", and click Create.

⚠️ Save this API Key immediately. You will need it for Jenkins and your backend environment variables.

2. Ngrok Setup (For Webhooks)
SendGrid needs a public URL to communicate with your local machine when an email is delivered.

Open a terminal and start an HTTP tunnel pointing to your Spring Boot backend port (8080):

Bash
ngrok http 8080
Copy the Forwarding URL (e.g., https://1a2b-3c4d.ngrok-free.dev). Keep this terminal running!

3. SendGrid Event Webhook
In SendGrid, go to Settings -> Mail Settings -> Event Webhook.

Under HTTP Post URL, paste your Ngrok URL and append the backend endpoint:
https://1a2b-3c4d.ngrok-free.dev/api/webhook/sendgrid

Under Events to be POSTed, check Delivered, Bounced, Dropped, and Deferred.

Set the Status to Enabled and click Save.

🏗️ Phase 2: DevOps Infrastructure (Jenkins & SonarQube)
1. Spin up the DevOps Containers
Navigate to the directory containing your DevOps docker-compose.yml (the one with Jenkins and SonarQube) and run:

Bash
docker-compose up -d
2. ⚠️ The Docker Socket Fix (Crucial for Mac/Linux)
Jenkins runs inside a container but needs permission to build other containers on your host machine. You must unlock the Docker socket:

Bash
docker exec -u root jenkins chmod 666 /var/run/docker.sock
(Note: You must run this command every time you completely restart the Jenkins container).

📊 Phase 3: SonarQube Configuration
Open your browser and go to http://localhost:9000.

Log in with the default credentials: Username: admin, Password: admin (You will be prompted to change this).

Create the Project:

Click Create Project -> Manually.

Name it: capstone-backend.

Set the Branch name to main (or master).

Generate the Sonar Token:

Click on your Profile icon (Top Right) -> My Account -> Security.

Under "Generate Tokens", enter a name (e.g., "jenkins-token"), select "User Token", and click Generate.

⚠️ Save this token! You will put this into Jenkins.

(Optional Demo Fix) Relax the Quality Gate:

Go to Quality Gates (top menu).

Create a new Quality Gate named "Demo Gate".

Remove strict conditions (like 80% coverage) so your pipeline doesn't block deployments during presentations.

Set "Demo Gate" as the default.

⚙️ Phase 4: Jenkins Configuration
Access Jenkins at http://localhost:9090. (If it asks for an initial admin password, run docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword).

1. Install Required Plugins
Navigate to Manage Jenkins -> Plugins -> Available Plugins and install:

SonarQube Scanner

NodeJS Plugin

Docker Pipeline

2. Configure Global Tools
Navigate to Manage Jenkins -> Global Tool Configuration:

JDK: Add JDK 17 (Name it jdk17, check "Install automatically").

NodeJS: Add NodeJS (Name it node, choose version ~20.x, check "Install automatically").

SonarQube Scanner: Add Scanner (Name it sonar-scanner, check "Install automatically").

3. Add Global Credentials
Navigate to Manage Jenkins -> Credentials -> System -> Global credentials (unrestricted) -> Add Credentials:

SonarQube Token:

Kind: Secret text

Secret: Paste your SonarQube Token (from Phase 3)

ID: sonar-token

SendGrid API Key:

Kind: Secret text

Secret: Paste your SendGrid API Key (from Phase 1)

ID: sendgrid-api-key

GitHub Credentials:

Kind: Username with password

Username: Your GitHub username

Password: Your GitHub Personal Access Token (PAT)

ID: github-credentials

4. Link SonarQube to Jenkins
Navigate to Manage Jenkins -> System:

Scroll down to SonarQube servers.

Click "Add SonarQube".

Name: sonarqube

Server URL: http://sonarqube:9000

Server authentication token: Select the sonar-token credential you just created.

Click Save.

🚀 Phase 5: Pipeline Execution & Deployment
1. Configure Application Environment
In your application code repository (where your Spring Boot and React code lives), ensure your application's docker-compose.yml pulls the SendGrid key securely. The backend service should look like this:

YAML
    environment:
      - SENDGRID_API_KEY=${SENDGRID_API_KEY}
2. Create the Jenkins Pipeline
In Jenkins, click New Item.

Name it Capstone-Pipeline and select Pipeline, then click OK.

Scroll down to the Pipeline section.

Definition: Pipeline script from SCM

SCM: Git

Repository URL: Your GitHub repo link.

Credentials: Select github-credentials.

Branch Specifier: */main (or your active branch).

Script Path: Jenkinsfile

Click Save and then Build Now.

🖥️ Phase 6: Accessing the Application
Once the Jenkins pipeline successfully completes the Containerize & Deploy stage, your application is live!

Frontend UI (React): http://localhost:5173

Backend API (Spring Boot): http://localhost:8080

RabbitMQ Management Console: http://localhost:15672 (Login: guest / guest)

SonarQube Dashboard: http://localhost:9000

🎉 Testing the System
Open the React UI and submit a new email request.

Check your terminal running docker logs -f capstone-backend to watch the Worker pick up the task.

Check the RabbitMQ console to see the message queue spike and clear.

Verify the email arrives in your inbox.

Watch the Ngrok terminal to see the 200 OK webhook response from SendGrid updating your database to DELIVERED!
