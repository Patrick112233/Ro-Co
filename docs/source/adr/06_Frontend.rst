ADR#6: Frontend Language
==========================================================

This ADR documents the decision process for choosing the language(s) for frontend development.

Forces
------
The decision is influenced by:

* Developer familiarity and learning curve
* Speed of development and prototyping
* Type safety and error prevention
* Community support and ecosystem
* Integration with React and modern tooling
* Maintainability and scalability

Decision
--------

We will use JavaScript (with JSX) for frontend development, without adopting TypeScript at this stage.

Rationale
---------

**Pro JavaScript (with JSX):**

* JavaScript is widely used and understood by most frontend developers
* Fast to develop and prototype, with minimal setup
* Large ecosystem and community support
* JSX is the standard for React development and integrates seamlessly

**Pro TypeScript:**

* Adds static typing, which can prevent many runtime errors
* Improves code maintainability and refactoring in large projects
* Increasingly popular in the React ecosystem
* Better editor support and autocompletion

**Con TypeScript:**

* Additional learning curve and configuration
* Slower initial development for teams unfamiliar with static typing
* May introduce friction for rapid prototyping or onboarding new contributors

Both JavaScript and TypeScript are well supported and have strong communities. TypeScript offers significant advantages for large, long-lived projects, but JavaScript remains the fastest path for rapid development and is accessible to a wider range of contributors.

Status
------

Accepted, Reevaluated in future

Consequences
------------
* The frontend will be developed in JavaScript (with JSX), prioritizing speed and accessibility.
* TypeScript may be adopted in the future as the project grows or as the team’s needs change.
* Some benefits of static typing and type safety will not be realized in the current codebase.
