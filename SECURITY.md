# Security Policy

## Supported Versions

The following versions of **PyMApp PLAT.mod - Java SE** currently receive security updates:

| Version | Supported          |
| ------- | ------------------ |
| 1.6.x   | Yes                |
| < 1.6   | No                 |

## Reporting a Vulnerability

We take security issues seriously. If you discover a vulnerability, please do **not** open a public issue.

Report it through one of the following channels:

* **GitHub private security advisory** (preferred): open a private report in the Security tab of the repository at https://github.com/feroldev/PyMApp_plat_jse/security.
* **Direct contact:** contact the maintainer (Fernando R. Olmedo {ferol.dev}) directly.

Please include the following information:

* Affected version(s).
* Steps to reproduce (as detailed as possible).
* Potential impact (what an attacker could gain or break).
* Proposed mitigation if available.

### What happens next

1. The report is acknowledged as quickly as possible.
2. The issue is validated and reproduced.
3. A fix is prepared and released for the supported versions.
4. The vulnerability is disclosed responsibly after the fix is available.

## Security Notes

* Report security issues privately; do not disclose them publicly before a fix is available.
* The module performs file I/O on configuration and log resources; ensure the directories used for these resources have appropriate access permissions.
* The configuration resources are plain text, XML or JSON files; do not store secrets in them (use the application's own secret management when available).

