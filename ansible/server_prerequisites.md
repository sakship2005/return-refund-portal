@"
# Server Prerequisites Specification
## Return and Refund Management Portal (RRP)

### 1. Operating System
- Target: Ubuntu 22.04 LTS / Debian 12 (Linux target node)

### 2. System Packages Required
- `docker.io` / `docker-ce`: Container runtime
- `containerd`: Container supervisor
- `curl`, `wget`: Health checking and diagnostic tools
- `openjdk-21-jre-headless`: Java runtime for standalone fallback execution
- `git`: Version control operations
- `ufw`: Host firewall management

### 3. Dedicated System Users & Groups
- User: `portalapp` (Service account, non-login shell `/bin/false`)
- Group: `docker` (Enables running containers without root privileges)

### 4. Folder Hierarchy & Permissions
- `/opt/rrp`: Application installation root (Owner: `portalapp`, `0755`)
- `/opt/rrp/logs`: Persistent application runtime logs (Owner: `portalapp`, `0775`)
- `/opt/rrp/config`: External configuration directory (Owner: `portalapp`, `0750`)

### 5. Network Ports
- Port `22`: SSH remote administration
- Port `8081`: Internal Spring Boot application port
- Port `8082`: Publicly mapped Docker container port
- Port `8080`: Jenkins CI/CD controller port

### 6. System Services
- `docker.service`: Enabled and active on boot
- `ufw.service`: Firewall enabled with ports 22, 8081, 8082 permitted
"@ | Set-Content -Path ansible/server_prerequisites.md -Encoding UTF8