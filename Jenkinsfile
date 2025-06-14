pipeline {
    agent any

    tools {
        maven 'Maven-3.9.6'
        jdk 'jdk-17'
    }

    environment {
        DOCKER_IMAGE = 'srikanthjeldi/tracking-generator:latest'   // customize for Docker Hub
        PATH = "${tool 'jdk-17'}/bin:${env.PATH}"
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/srikanth-jeldi/tracking-generator.git'
            }
        }

        stage('Build & Test') {
            steps {
                bat 'mvn clean install'
            }
        }

        stage('Archive JAR') {
            steps {
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    echo "Building Docker image..."
                    bat 'docker build -t %DOCKER_IMAGE% .'
                }
            }
        }

        stage('Push Docker Image') {
            when {
                expression { return env.DOCKER_USERNAME != null }
            }
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker-hub-creds', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                    script {
                        echo "Logging into Docker Hub..."
                        bat "docker login -u %DOCKER_USERNAME% -p %DOCKER_PASSWORD%"
                        echo "Pushing image..."
                        bat "docker push %DOCKER_IMAGE%"
                    }
                }
            }
        }
    }

    post {
        always {
            echo 'Pipeline complete'
        }
    }
}
