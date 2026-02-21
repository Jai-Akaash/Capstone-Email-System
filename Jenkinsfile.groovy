pipeline {
    agent any

    tools {
        // These names MUST match exactly what you typed in Manage Jenkins -> Tools
        maven 'maven'
        nodejs 'node'
    }

    stages {
        stage('Source Control') {
            steps {
                echo 'Fetching latest code from GitHub...'
            }
        }

        stage('Build Backend') {
            steps {
                dir('capstone') {
                    echo 'Compiling Spring Boot Application...'
                    sh 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Build Frontend') {
            steps {
                dir('email-notification') {
                    echo 'Building React Application...'
                    // Library fix is handled at the container level to avoid permission issues
                    sh 'npm install'
                    sh 'npm run build'
                }
            }
        }

        stage('Containerize & Deploy') {
            steps {
                echo 'Updating Docker Stack...'
                // Using 'docker compose' (newer) vs 'docker-compose' (older)
                // If this fails, try changing it back to 'docker-compose'
                sh 'docker-compose up --build -d'
            }
        }
    }

    post {
        success {
            echo '🚀 Deployment Successful!'
            echo 'UI: http://localhost:5173'
            echo 'API: http://localhost:8080'
        }
        failure {
            echo '❌ Build Failed. Check the Console Output for errors.'
        }
    }
}