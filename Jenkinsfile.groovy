pipeline {
    agent any
    tools {
        maven 'maven'   // This MUST match the name you gave it in "Tools"
        nodejs 'node'    // This MUST match the name you gave it in "Tools"
    }
    stages {
        stage('Source Control') {
            steps {
                echo 'Fetching code from GitHub...'
                // This happens automatically when you link GitHub to Jenkins
            }
        }

        stage('Build Backend') {
            steps {
                dir('capstone') {
                    echo 'Compiling Java Application...'
                    sh 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Build Frontend') {
            steps {
                dir('email-notification') {
                    echo 'Building React Application...'
                    sh 'npm install'
                    sh 'npm run build'
                }
            }
        }

        stage('Containerize & Deploy') {
            steps {
                echo 'Updating Docker Containers...'
                sh 'docker-compose up --build -d'
            }
        }
    }
    
    post {
        success {
            echo 'Deployment Successful! App is live at http://localhost:5173'
        }
    }
}