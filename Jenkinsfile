pipeline {
    agent any

    options {
        skipDefaultCheckout(true)
        timestamps()
    }

    tools {
        jdk 'JDK-21'
        maven 'Maven-3.9.16'
    }

    environment {
        PATH = "C:\\Program Files\\Docker\\Docker\\resources\\bin;${env.PATH}"
        DOCKER_HOST = 'tcp://localhost:2375'
    }

    parameters {
        choice(
            name: 'DEPLOY_ENV',
            choices: ['docker-local', 'build-only', 'tomcat-local'],
            description: 'docker-local: build versioned Docker image and deploy container. tomcat-local: deploy to Tomcat. build-only: build WAR only.'
        )
        string(
            name: 'DOCKER_CONTAINER_PORT',
            defaultValue: '8082',
            description: 'Host port to expose the Docker container on.'
        )
        string(
            name: 'TOMCAT_HOME',
            defaultValue: 'C:\\Program Files\\Apache Software Foundation\\Tomcat 11.0',
            description: 'Tomcat directory (used when DEPLOY_ENV is tomcat-local).'
        )
        string(
            name: 'TOMCAT_PORT',
            defaultValue: '8081',
            description: 'Tomcat port (used when DEPLOY_ENV is tomcat-local).'
        )
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                bat 'mvn -B clean test'
            }
            post {
                always {
                    junit allowEmptyResults: false, testResults: 'target/surefire-reports/*.xml'
                    archiveArtifacts artifacts: 'target/screenshots/*.png', allowEmptyArchive: true
                }
            }
        }

        stage('Package') {
            steps {
                bat 'mvn -B package -DskipTests'
                archiveArtifacts artifacts: 'target/rrp.war', fingerprint: true
            }
        }

        stage('Docker Build & Tag') {
            when {
                expression { params.DEPLOY_ENV == 'docker-local' }
            }
            steps {
                powershell """
                    \$env:Path = "C:\\Program Files\\Docker\\Docker\\resources\\bin;" + \$env:Path
                    \$env:DOCKER_HOST = "tcp://localhost:2375"

                    Write-Host "Building versioned image: return-refund-portal:${BUILD_NUMBER}"
                    docker build -t return-refund-portal:${BUILD_NUMBER} .
                    docker tag return-refund-portal:${BUILD_NUMBER} return-refund-portal:latest
                    Write-Host "Docker images built and tagged successfully."
                """
            }
        }

        stage('Deploy Docker Container') {
            when {
                expression { params.DEPLOY_ENV == 'docker-local' }
            }
            steps {
                powershell """
                    \$env:Path = "C:\\Program Files\\Docker\\Docker\\resources\\bin;" + \$env:Path
                    \$env:DOCKER_HOST = "tcp://localhost:2375"

                    Write-Host "Stopping and removing existing rrp-container if present..."
                    try { docker stop rrp-container } catch {}
                    try { docker rm rrp-container } catch {}

                    Write-Host "Deploying new container rrp-container from image return-refund-portal:${BUILD_NUMBER} on port ${params.DOCKER_CONTAINER_PORT}:8081..."
                    docker run -d -p ${params.DOCKER_CONTAINER_PORT}:8081 --name rrp-container return-refund-portal:${BUILD_NUMBER}
                """
            }
        }

        stage('Docker Health Check') {
            when {
                expression { params.DEPLOY_ENV == 'docker-local' }
            }
            steps {
                powershell """
                    \$url = "http://localhost:${params.DOCKER_CONTAINER_PORT}/requests"
                    Write-Host "Waiting for container to be healthy at \$url..."
                    for (\$i = 1; \$i -le 15; \$i++) {
                        try {
                            \$r = Invoke-WebRequest -Uri \$url -UseBasicParsing -TimeoutSec 5
                            if (\$r.StatusCode -eq 200) {
                                Write-Host "Container healthy! Received HTTP 200 from \$url."
                                exit 0
                            }
                        } catch {
                            Write-Host "Attempt \$i of 15: Waiting for container service..."
                        }
                        Start-Sleep -Seconds 4
                    }
                    throw "Health check failed: \$url did not respond with 200 OK."
                """
            }
        }

        stage('Deploy to Tomcat') {
            when {
                expression { params.DEPLOY_ENV == 'tomcat-local' }
            }
            steps {
                powershell '''
                    $ErrorActionPreference = 'Stop'
                    $service = 'Tomcat11'
                    $webapps = Join-Path $env:TOMCAT_HOME 'webapps'

                    Write-Host "Deploying to $webapps"
                    Stop-Service -Name $service -Force -ErrorAction SilentlyContinue
                    (Get-Service -Name $service).WaitForStatus('Stopped', '00:00:30')

                    Remove-Item -Path (Join-Path $webapps 'rrp.war') -Force -ErrorAction SilentlyContinue
                    Remove-Item -Path (Join-Path $webapps 'rrp') -Recurse -Force -ErrorAction SilentlyContinue
                    Copy-Item -Path 'target\\rrp.war' -Destination (Join-Path $webapps 'rrp.war') -Force

                    Start-Service -Name $service
                    Write-Host 'Tomcat service started.'
                '''
            }
        }

        stage('Tomcat Health Check') {
            when {
                expression { params.DEPLOY_ENV == 'tomcat-local' }
            }
            steps {
                powershell '''
                    $url = "http://localhost:$($env:TOMCAT_PORT)/rrp/health"
                    for ($i = 1; $i -le 12; $i++) {
                        try {
                            $r = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 5
                            if ($r.StatusCode -eq 200) {
                                Write-Host "Healthy: $url returned 200"
                                exit 0
                            }
                        } catch {
                            Write-Host "Attempt $i of 12: not ready yet"
                        }
                        Start-Sleep -Seconds 5
                    }
                    throw "Health check failed: $url"
                '''
            }
        }
    }

    post {
        success {
            echo "Pipeline succeeded (DEPLOY_ENV=${params.DEPLOY_ENV})."
        }
        failure {
            echo 'Pipeline failed; do not treat this deployment as successful.'
        }
    }
}