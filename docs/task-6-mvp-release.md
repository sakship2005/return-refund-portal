# Task 6: MVP Completion and Git Collaboration

## Release baseline

- Integration branch: `develop`
- Integrated feature: `feature/status-workflow`
- Validation: `mvn clean verify`
- Artifact: `target/rrp.war`
- Release tag: `v1.1.0`

## MVP backlog status

| Capability | Status | Evidence |
| --- | --- | --- |
| Create return request | Done | `/requests/new` and `POST /requests` |
| View and search requests | Done | `/requests` and the `q` query parameter |
| Update request details | Done | `/requests/{id}/edit` |
| Role-driven status workflow | Done | Advance/reject actions with actor capture |
| Status history | Done | History persisted and shown on request detail |
| Summary dashboard | Done | `/dashboard` |

## Collaboration evidence

The status-workflow feature was merged into `develop` with a non-fast-forward merge commit. The merged baseline was rebuilt with Maven before tagging. The next backlog item is Jenkins continuous integration: create the job, trigger it from `develop`, publish JUnit results, and archive the WAR.