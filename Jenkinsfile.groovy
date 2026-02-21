pipeline {
    agent any

    tools {
        // These names MUST match exactly what you typed in Manage Jenkins -> Tools
        maven 'maven'
        nodejs 'node'
    }

    environment {
        // Injects the SendGrid key securely from the Jenkins Vault
        SENDGRID_API_KEY = credentials('SENDGRID_API_KEY')
    }

    stages {
        stage('Source Control') {
            steps {
                echo 'Fetching latest code from GitHub...'
            }
        }

        stage('Build Backend & Run Tests') {
            steps {
                dir('capstone') {
                    echo 'Compiling and Testing Java Application...'
                    // We changed this from 'package -DskipTests' to 'verify'
                    // This forces Maven to run your tests so JaCoCo can measure coverage!
                    sh 'mvn clean verify'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                dir('capstone') {
                    withSonarQubeEnv('sonarqube') {
                        // We added the exclusions flag to ignore configs, DTOs, and the main app file
                        sh '''
                        mvn sonar:sonar \
                          -Dsonar.projectKey=capstone-backend \
                          -Dsonar.projectName="Capstone Backend" \
                          -Dsonar.coverage.exclusions="**/config/**,**/dto/**,**/*Application.*"
                        '''
                    }
                }
            }
        }

        stage('Quality Gate') {
            steps {
                // This forces Jenkins to pause and wait for SonarQube's Pass/Fail webhook
                timeout(time: 10, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
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
                // Using 'docker-compose' as configured on your host
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