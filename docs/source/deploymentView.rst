7. Deployment View
=================

Current Deployment Status
------------------------

The deployment of the Ro-Co system is not yet finalized. However, a working development and test environment is provided using Docker Compose, which orchestrates the main system components as containers. This setup allows for easy local deployment and testing of the full stack.

Docker Compose Overview
----------------------

The provided `docker-compose.yml` file defines three main services:

- **frontend** (React app via serve as static web server)
- **backend** (Spring Boot REST API)
- **mongo** (MongoDB database)

Container Interactions
----------------------

- The **frontend** communicates with the **backend** via HTTP (port 8080).
- The **backend** communicates with **mongo** using the standard MongoDB port (27017).
- Environment variables are used to configure database credentials and CORS settings.
- Health checks ensure that MongoDB is ready before the backend starts.

Notes
-----
- The deployment is currently intended for development and testing only.
- For production, further configuration (e.g., secure secrets, HTTPS, scaling, monitoring) will be required.
- The setup can be run locally using `docker-compose up` from the project root.

This deployment view provides a high-level understanding of how the system components are orchestrated using Docker Compose and how they interact via exposed ports and network links.
