# Task 7: Jenkins Continuous Integration

## Objective

Build and test the Return and Refund Management Portal on every change and retain the packaged WAR as a Jenkins build artifact.

## Prerequisites

- Jenkins LTS installed on the CI machine.
- Java 17 configured in Jenkins as `jdk17`.
- Maven 3.9 or newer configured in Jenkins as `maven3`.
- Git available to the Jenkins agent.
- The Jenkins agent can access the GitHub repository.

## Jenkins Job

Create a Jenkins **Freestyle project** named `return-refund-portal-ci`.

### Source Code Management

- Select **Git**.
- Repository URL: the GitHub URL for this repository.
- Branch specifier: `*/develop`.
- Configure GitHub credentials only when the repository is private.

### Build Environment

- Select the JDK installation `jdk17`.
- Select the Maven installation `maven3`.

### Build Trigger

Use one of these trigger configurations:

- **GitHub hook trigger for GITScm polling** for webhook-driven builds.
- **Poll SCM** with `H/5 * * * *` when a webhook is not available.

For a GitHub webhook, set the repository payload URL to:

`https://<jenkins-host>/github-webhook/`

Use a reachable HTTPS Jenkins endpoint and configure the GitHub push event.

### Build Step

Select **Invoke top-level Maven targets** and configure:

- Maven version: `maven3`
- Goals: `clean verify`

The `verify` lifecycle compiles the application and runs all unit/integration tests before packaging the WAR.

### Post-build Actions

1. **Archive the artifacts**
   - Files to archive: `target/*.war`
   - Enable **Fingerprint artifacts**.
2. **Publish JUnit test result report**
   - Test report XMLs: `target/surefire-reports/*.xml`
   - Allow empty results only while the project has no tests; disable this option once tests exist.

A failed Maven build must fail the Jenkins job and must not archive a release artifact as a successful build.

## Local CI Reproduction

Run the same build locally from the repository root:

```text
mvn clean verify
```

Expected outputs:

- `target/rrp.war`
- `target/surefire-reports/*.xml` when tests are present

## Task 7 Evidence Checklist

Record the following in the project report after Jenkins is running:

- Jenkins version and Java version.
- Job name and source branch.
- Trigger configuration or GitHub webhook delivery result.
- Console output showing `BUILD SUCCESS`.
- Build number and archived `rrp.war` artifact.
- Published JUnit report summary.
- A second build started by a commit or SCM poll.

## Current Repository Baseline

The project uses Java 17, Maven, Spring Boot, and WAR packaging. The Maven build has been verified locally with `mvn test`; run `mvn clean verify` for the exact Jenkins job command.
