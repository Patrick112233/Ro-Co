ADR#2: Database Choice — NoSQL (MongoDB) vs SQL (PostgreSQL)
============================================================

This ADR documents the decision process for choosing the database technology for the project.

Forces
------
The decision is influenced by:

* Performance and speed
* Scalability and distribution
* Security (e.g., SQL injection risk)
* Flexibility of data schema
* Alignment with REST/JSON logic
* Support for complex queries and relational data
* Community support and documentation

Decision
--------

We will use MongoDB (NoSQL) as the primary database for the project.

Rationale
---------

**Pro MongoDB:**

* Fast
* Distributed and scalable
* Secure against SQL injection attacks
* Adaptable (less strict data schema)
* Stores JSON-like objects, closely matching REST logic

**Pro SQL (PostgreSQL):**

* Relational — supports complex queries efficiently (rarely needed in our use case)
* Thoroughly taught in academia
* Less data duplication

**Con SQL:**

* Slower due to relational overhead
* Prone to SQL injection attacks
* More rigid schema

Both MongoDB and PostgreSQL are well established, well documented, and supported by the community.

Status
------

Accepted

Consequences
------------

* We will use Spring Data MongoDB (JPA for Mongo), which is less documented but straightforward when implemented correctly.
* The data model will be designed with fewer relations, favoring document-based storage.
* Some complex relational queries may be less efficient or require denormalization.
