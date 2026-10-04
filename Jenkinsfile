pipeline {
    agent any

    options {
        skipDefaultCheckout(true)
        timestamps()
    }

    parameters {
        choice(
            name: 'DEPLOY_ENV',
            choices: ['build-only', 'tomcat-local'],
            description: 'Build only, or deploy the WAR to the local Tomcat service.'
        )
        string(
            name: 'TOMCAT_HOME',
            defaultValue: 'C:\\Program Files\\Apache Software Foundation\\Tomcat 11.0',
            description: 'Tomcat installation directory used when DEPLOY_ENV is tomcat-local.'
        )
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build and Test') {
            steps {
                bat 'mvn clean verify'
            }
        }

        stage('Archive WAR') {
            steps {
                archiveArtifacts artifacts: 'target/rrp.war', fingerprint: true
                junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
            }
        }

        stage('Deploy to Tomcat') {
            when {
                expression { params.DEPLOY_ENV == 'tomcat-local' }
            }
            steps {
                bat '''
                    powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference = 'Stop'; $tomcatHome = '${TOMCAT_HOME}'; Stop-Service -Name 'Tomcat11' -Force -ErrorAction SilentlyContinue; Remove-Item -Path (Join-Path $tomcatHome 'webapps\\rrp.war') -Force -ErrorAction SilentlyContinue; Remove-Item -Path (Join-Path $tomcatHome 'webapps\\rrp') -Recurse -Force -ErrorAction SilentlyContinue; Copy-Item -Path 'target\\rrp.war' -Destination (Join-Path $tomcatHome 'webapps\\rrp.war'); Start-Service -Name 'Tomcat11'"
                '''
            }
        }

        stage('Health Check') {
            when {
                expression { params.DEPLOY_ENV == 'tomcat-local' }
            }
            steps {
                bat '''
                    powershell -NoProfile -Command "$response = Invoke-WebRequest -Uri 'http://localhost:8080/rrp/health' -UseBasicParsing; if ($response.StatusCode -ne 200) { exit 1 }"
                '''
            }
        }
    }

    post {
        success {
            echo "Pipeline completed for ${params.DEPLOY_ENV}."
        }
        failure {
            echo 'Pipeline failed; deployment and release evidence must not be treated as successful.'
        }
    }
}
