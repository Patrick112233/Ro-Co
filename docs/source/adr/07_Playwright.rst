ADR#7: E2E Testing Framework
==================================================================

This ADR documents the decision process for choosing the end-to-end (E2E) testing framework for the project.

Forces
------
The decision is influenced by:

* Speed and efficiency of test development
* Developer experience and tooling
* Platform compatibility
* Community support and documentation
* Feature set and reliability

Decision
--------

We will use Playwright as the primary E2E testing framework for the project.

Rationale
---------

**Pro Playwright:**

* Faster test development and execution
* Superior developer experience, including test generation and locator picking tools
* Modern API and active development
* Good documentation and community support

**Pro Cypress:**

* Popular and widely used in the industry
* Good documentation and ecosystem
* Visual test runner is helpful for debugging

**Con Cypress:**

* Limited or problematic support for ARM64 systems (currently used by our team)
* Slower test execution and development feedback loop compared to Playwright

Both Playwright and Cypress are capable E2E testing frameworks, but Playwright offers a better fit for our team's needs and hardware.

Status
------

Accepted

Consequences
------------

* The project will use Playwright for all E2E tests.
* Developers benefit from faster test writing, better tooling.
