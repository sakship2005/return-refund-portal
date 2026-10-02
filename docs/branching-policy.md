@"
# Branch Naming Rules

- main        : production-ready code only, protected
- develop     : integration branch, all features merge here first
- feature/*   : e.g. feature/create-request, feature/status-workflow
- bugfix/*    : e.g. bugfix/fix-search-filter
- release/*   : e.g. release/v1.0

Rules:
- Never commit directly to main.
- Every feature gets its own branch off develop.
- Branch names are lowercase, hyphen-separated.
"@ | Out-File -Encoding utf8 docs\branching-policy.md