# Task 8: Jenkins Pipeline as Code

## Pipeline job

Create a Jenkins **Pipeline** job named `return-refund-portal-pipeline`.

Configure **Pipeline definition** as **Pipeline script from SCM**:

- SCM: Git
- Repository: `https://github.com/sakship2005/return-refund-portal.git`
- Branch: `*/develop`
- Script path: `Jenkinsfile`

The pipeline checks out the selected branch, runs `mvn clean verify`, archives `target/rrp.war`, and publishes Surefire results. Empty test results are allowed until the Selenium/unit test tasks add tests.

## Parameters

- `DEPLOY_ENV=build-only`: build and archive the WAR without deploying.
- `DEPLOY_ENV=tomcat-local`: copy the WAR to the local Tomcat 11 installation and run a health check.
- `TOMCAT_HOME`: defaults to `C:\\Program Files\\Apache Software Foundation\\Tomcat 11.0`.

The deploy stage stops `Tomcat11`, replaces `webapps\\rrp.war`, starts the service, and checks:

`http://localhost:8080/rrp/health`

## Evidence checklist

- Pipeline job configuration showing `Jenkinsfile` from `develop`.
- Successful console output for `DEPLOY_ENV=build-only`.
- Archived `rrp.war` artifact.
- Successful console output for `DEPLOY_ENV=tomcat-local`.
- Application response from the Tomcat health URL.

The Jenkins service account must have permission to stop/start `Tomcat11` and write to the Tomcat `webapps` directory before using `tomcat-local`.
