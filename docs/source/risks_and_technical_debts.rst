11. Risks and Technical Debts
========================

This section outlines the current technical and business risks, as well as known technical debts in the Ro-Co project. Each risk is described along with its status and, where applicable, planned mitigation steps.

Frontend Risks
--------------

.. list-table:: Frontend Risks
   :header-rows: 1
   :widths: 30 50 20

   * - Risk
     - Description
     - Status
   * - Dependency on security of Auth-kit library
     - The frontend relies on the security of the Auth-kit library for authentication. If vulnerabilities exist in the library, they may impact the application. No alternative is currently available.
     - Accepted
   * - Dependency on XSS protection of Spring Boot server
     - The frontend depends on the backend to sanitize and protect against XSS in data output. Improvements are in progress.
     - WIP
   * - Potential heavy load from large answer sets
     - If a question receives a very large number of answers, the frontend loads all answers at once (no pagination), which may impact performance. This design avoids out-of-sync issues but could cause slowdowns.
     - Accepted

Backend Risks
-------------

.. list-table:: Backend Risks
   :header-rows: 1
   :widths: 30 50 20

   * - Risk
     - Description
     - Status
   * - Sending potential personal data (username) to external service
     - Usernames are sent to the external avatar service.
     - Accepted
   * - Lack of XSS protection for data output
     - Data returned by the backend is not fully sanitized for XSS. Improvements are in progress.
     - WIP
   * - Lack of encryption (SSL/TLS postponed)
     - Communication is not encrypted as SSL/TLS is not yet enabled in the current deployment.
     - WIP
    * Lack of zero-trust policy
     - The backend does not yet implement zero-trust security (especcialy encryption and autentification of database) principles, which may expose it to unauthorized access.
     - Open

Business Risks
--------------

.. list-table:: Business Risks
   :header-rows: 1
   :widths: 30 50 20

   * - Risk
     - Description
     - Status
   * - Legal compliance (missing consent)
     - The platform is not yet fully compliant with all legal requirements, such as user consent for data processing.
     - WIP
   * - Lack of deployment
     - The platform is not yet deployed for production use, limiting user access and feedback.
     - Open
   * - Lack of marketing
     - No active marketing efforts are in place to attract users or contributors.
     - Open
   * - Lack of development community
     - The project currently lacks a broad development community, which may slow progress and reduce sustainability.
     - Open

This list is regularly reviewed and updated as risks are addressed or new issues are identified.
