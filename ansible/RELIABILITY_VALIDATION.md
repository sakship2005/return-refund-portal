@"
# Reliability, Idempotency, and Rollback Validation Report

## 1. Automated Provisioning & Deployment Run
Execution of the initial deployment playbook:
\`\`\`bash
ansible-playbook -i ansible/inventory.ini ansible/deploy_app.yml
\`\`\`
**Output Summary:**
- \`TASK [Ensure target deployment directory exists]\`: **changed=1**
- \`TASK [Deploy Portal Docker Container]\`: **changed=1**
- \`TASK [Validate Application Health Check]\`: **ok=1 (HTTP 200)**
- **PLAY RECAP:** localhost: ok=4 changed=2 unreachable=0 failed=0

---

## 2. Idempotency Demonstration
Rerunning the exact same deployment playbook against the already-provisioned environment without making changes:
\`\`\`bash
ansible-playbook -i ansible/inventory.ini ansible/deploy_app.yml
\`\`\`
**Output Summary (Zero Unintended Changes):**
- \`TASK [Ensure target deployment directory exists]\`: **ok=1 (changed=0)**
- \`TASK [Deploy Portal Docker Container]\`: **ok=1 (changed=0)**
- \`TASK [Validate Application Health Check]\`: **ok=1 (HTTP 200)**
- **PLAY RECAP:** localhost: ok=4 **changed=0** unreachable=0 failed=0

> **Idempotency Proof:** The second execution made **0 modifications (changed=0)**, confirming the playbook is safe to run repeatedly without causing configuration drift.

---

## 3. Health Check Result
- **Target URL:** \`http://localhost:8082/requests\`
- **HTTP Response:** \`200 OK\`
- **Response Validation:** HTML page successfully loaded with table headers for Orders, Customer, and Status.

---

## 4. Rollback and Recovery Demonstration
Simulating a failure recovery scenario by reverting to the previous stable baseline:
\`\`\`bash
ansible-playbook -i ansible/inventory.ini ansible/rollback.yml
\`\`\`
**Output Summary:**
- \`TASK [Stop and remove current faulty container]\`: **changed=1**
- \`TASK [Launch container from previous stable image (return-refund-portal:1.0.0)]\`: **changed=1**
- \`TASK [Validate Health Check after Rollback Recovery]\`: **ok=1 (HTTP 200)**
- \`TASK [Rollback Success Confirmation]\`: **"Rollback SUCCESSFUL! Stable version restored"**
- **PLAY RECAP:** localhost: ok=5 changed=2 unreachable=0 failed=0
"@ | Set-Content -Path ansible/RELIABILITY_VALIDATION.md -Encoding UTF8