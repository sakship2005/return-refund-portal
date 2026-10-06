@"
# Automated Return and Refund Management Portal (RRP)
## Final Technical Documentation & DevOps Architecture Report

---

### 1. Project Overview & Architecture
- **Application:** Spring Boot 3.2.5 (Java 21), Spring Data JPA, Embedded H2 Database.
- **UI:** Thymeleaf, Bootstrap 5 responsive layout.
- **Packaging:** Standalone executable WAR (\`rrp.war\`).
- **Target Environments:**
  - Apache Tomcat 11 on port 8081
  - Docker container on port 8082:8081

---

### 2. End-to-End DevOps Lifecycle Pipeline

\`\`\`
[Developer Commit]
       │
       ▼
[GitHub Repository (Feature Branch)]
       │
       ▼ (Webhook / SCM Polling)
[Jenkins CI/CD Pipeline]
       ├── 1. Checkout SCM
       ├── 2. Build & Automated Tests (mvn clean test)
       │         └── Selenium WebDriver Quality Gate (6 Journeys)
       │         └── Failure Screenshot Capture & JUnit Reporting
       ├── 3. Package Artifact (mvn package -DskipTests)
       ├── 4. Docker Build & Tag (return-refund-portal:\${BUILD_NUMBER})
       ├── 5. Deploy Fresh Container (docker run -p 8082:8081)
       └── 6. Automated Health Check (HTTP 200 Verification)
       │
       ▼
[Ansible Configuration Management & Provisioning]
       ├── Idempotent Host Provisioning (packages, users, directories, firewall)
       ├── Automated Application Deployment (ansible/deploy_app.yml)
       └── Disaster Recovery & Rollback Playbook (ansible/rollback.yml)
\`\`\`

---

### 3. Troubleshooting Guide & Lessons Learned

| Issue Encountered | Root Cause | Resolution Implemented |
|---|---|---|
| **Port Collision on 8080** | Jenkins runs on 8080 by default. | Mapped host port to \`8082\` (\`-p 8082:8081\`). |
| **Selenium Headless Execution** | Background CI runs without display. | Enabled \`--headless=new\`, \`--no-sandbox\`, \`--disable-dev-shm-usage\`. |
| **Jenkins Service Docker Access** | Windows service account lacked Docker pipe access. | Enabled TCP daemon (\`tcp://localhost:2375\`) and injected explicit user \`PATH\`. |
| **XML Parse Error in POM** | Accidental blank line before \`<?xml\`. | Cleaned leading whitespace to ensure declaration at line 1, column 1. |
| **Accidental Script in Dockerfile** | PowerShell wrapper saved in Dockerfile. | Replaced with clean 5-line Dockerfile instructions. |

---

### 4. Limitations
1. **In-Memory Storage:** Uses H2 in-memory DB by default; persistent storage requires an external PostgreSQL/MySQL instance.
2. **Local Registry:** Docker images are tagged locally on the host engine rather than pushed to a public/private Docker Hub registry.
3. **Single Node Deployment:** Currently targets standalone server configurations rather than a distributed Kubernetes cluster.

---

### 5. Future Enhancement Plan
1. **Kubernetes Orchestration:** Add Helm charts and Kubernetes manifests for multi-replica auto-scaling and zero-downtime rolling updates.
2. **Security Gates:** Integrate SonarQube for static code analysis and Trivy for container vulnerability scanning in the Jenkins pipeline.
3. **Cloud Migration:** Deploy target nodes on AWS EC2 or GCP using Terraform infrastructure-as-code.
"@ | Set-Content -Path FINAL_REPORT.md -Encoding UTF8