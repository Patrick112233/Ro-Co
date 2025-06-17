ADR#3: Frontend Build Tool — Vite vs npm Scripts
===============================================

This ADR documents the decision process for choosing the frontend build tool for the project.

Forces
------
The decision is influenced by:

* Development speed and feedback loop
* Modern JavaScript/TypeScript support
* Hot Module Replacement (HMR) and live reload
* Build performance and optimization
* Community support and documentation
* Integration with React
* Simplicity and configuration overhead

Decision
--------

We will use Vite as the primary frontend build tool for the project.

Rationale
---------

**Pro Vite:**

* Extremely fast development server with instant HMR
* Modern build system
* Out-of-the-box support for React and other frameworks
* Simple configuration and easy to extend
* Actively maintained with a large and growing community
* Produces highly optimized production builds

**Pro npm Scripts:**

* Simple and native to Node.js projects
* No extra dependencies required
* Simplicity for small projects

**Con npm Scripts:**

* Slower development feedback loop (no native HMR)
* Manual configuration needed for advanced features (bundling, transpiling, etc.)
* Not optimized for modern frontend frameworks

Both Vite and npm scripts are well supported and documented, but Vite offers a superior developer experience for modern web apps.

Status
------

Accepted

Consequences
------------

* The frontend will use Vite for development and production builds.
* Some legacy or custom scripts may still use npm scripts as needed, but Vite is the default.
