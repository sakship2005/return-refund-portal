pipeline {
    agent any

    options {
        skipDefaultCheckout(true)
        timestamps()
    }

    // Remove this block if your Jenkins tool names differ from jdk17 / maven3
    tools {
        jdk 'JDK-21'
        maven 'Maven-3.9.16'
    }

    parameters {
        choice(
            name: 'DEPLOY_ENV',
            choices: ['build-only', 'tomcat-local'],
            description: 'build-only: build and archive the WAR. tomcat-local: also deploy to the local Tomcat service.'
        )
        string(
            name: 'TOMCAT_HOME',
            defaultValue: 'C:\\Program Files\\Apache Software Foundation\\Tomcat 11.0',
            description: 'Tomcat installation directory (used when DEPLOY_ENV is tomcat-local).'
        )
        string(
            name: 'TOMCAT_PORT',
            defaultValue: '8081',
            description: 'HTTP port Tomcat listens on (used by the health check).'
        )
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                bat 'mvn -B clean test'
            }
            post {
                always {
                    // publish the test report; fail if no report exists (means tests never ran)
                    junit allowEmptyResults: false, testResults: 'target/surefire-reports/*.xml'
                    // keep Selenium failure screenshots with the build
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

        stage('Health Check') {
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
