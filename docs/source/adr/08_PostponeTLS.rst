ADR#8: Postponing and Dismantling TLS Support
=============================================

This ADR documents the decision to temporarily postpone and dismantle TLS (HTTPS) support in the project.

Forces
------
The decision is influenced by:

* Lack of deployment environment (no public domain or infrastructure for certificate management)
* Inability to obtain valid certificates for local or CI environments
* Self-signed certificates are not compatible with our build and test toolchain
* Need to keep the development and test workflow simple and reliable
* Security is still a priority for future deployment

Decision
--------

We will temporarily postpone and dismantle TLS (HTTPS) support in the project until a proper deployment environment is available.

Rationale
---------

* No deployment means no access to trusted certificate authorities
* Self-signed certificates cause issues with browsers, build tools, and automated tests
* Removing TLS simplifies local development and CI/CD pipelines
* Security will be re-evaluated and TLS re-enabled when deployment is possible

Status
------

Temporarily Accepted

Consequences
------------

* All services will run over HTTP during development and testing
* Security for data in transit is reduced in non-production environments
* TLS support will need to be re-implemented and tested before production deployment
* Documentation and configuration should note the temporary nature of this decision
