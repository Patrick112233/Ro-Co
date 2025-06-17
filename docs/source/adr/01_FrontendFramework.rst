ADR#1: Frontend Framework
=========================

It's about deciding the used frontend technology stack.
The decision should consider:

* Development speed (it should be easy and fast to develop frontend)
* Documentation and community support
* Implementation with RestAPI
* Security and JWT authentication
* Adaptability

Decision
--------

Decision Overview: We will use React (JavaScript), Bootstrap, and HTML to write a web frontend.

Rationale
---------

* React has a large community and is used frequently on various big web platforms.
* React has good documentation (`react api <https://react.dev/reference/react>`_).
* Development speed is okay since Bootstrap comes with predefined templates and themes.
* Security: the provided technology stack is client-only; however, React comes with a bunch of APIs to support JWT handling.
* React and JavaScript by default support REST calls.

Rejected alternatives
~~~~~~~~~~~~~~~~~~~~

**Spring Boot Vaadin:** A server-side frontend implementation of Spring Boot.

Cons:
  * Overly complicated API
  * Bad documentation
  * Small community
  * No split between backend and frontend

Pros:
  * Server-side validation
  * Security (less knowledge required for attacker)

Status
------

Accepted

Consequences
------------

* Client-side rendering and server for the serving the frontend required.
* Validation at both front and backend is required.