5. Base architecture and tech stack
===================================

.. image:: pic/BuildingBlockView.drawio.svg
   :alt: Building Block View
   :width: 1200px

The Ro-CO app is split into multiple abstraction levels.

User Roles
----------

.. list-table:: User and Admin Roles
   :header-rows: 1
   :widths: 25 75

   * - Role
     - Description
   * - User
     - Can interact with basic web features such as asking questions, answering, and participating in discussions.
   * - Admin
     - Has all user permissions plus access to administrative features such as content moderation and user moderation.

System Context
--------------

.. list-table:: System Context Components
   :header-rows: 1
   :widths: 25 75

   * - Component
     - Purpose
   * - Static Web Server
     - Serves the static frontend files (HTML, CSS, JS) to users.
   * - Frontend (React)
     - Provides the user interface and handles user interactions. Transpiles into HTML and JavaScript.
   * - REST API (Spring Boot)
     - Implements business logic, user authentication, processes requests, and communicates with the database. Exposes a REST API for the frontend.
   * - MongoDB
     - Stores persistent application data.
   * - Dice Bear Avatar Service (external API)
     - Generates user avatars based on usernames.

Frontend Building Blocks
-----------------------

.. list-table:: Frontend Internal Components
   :header-rows: 1
   :widths: 30 70

   * - Internal Component
     - Purpose
   * - main.jsx
     - Entry point for the React application, rendering the main App component and setting up routing.
   * - pages/
     - Contains different page components for the application, such as LoginPage, DashboardPage.
   * - components/
     - Contains reusable UI components like Header, Footer, and QuestionList.
   * - auth/
     - Contains authentication-related components and hooks, such as authentication information and cookie handling.
   * - util/
     - Contains utility functions and constants used throughout the application.
   * - assets/
     - Contains static assets like images, stylesheets, and icons.

.. list-table:: Frontend Libraries/Dependencies
   :header-rows: 1
   :widths: 30 70

   * - Library/Dependency
     - Purpose
   * - react
     - JavaScript library for building user interfaces, particularly single-page applications.
   * - axios
     - Promise-based HTTP client for making requests to the backend API.
   * - auth-kit
     - Library for managing user authentication and session state in React applications.
   * - motion
     - Library for creating animations and transitions in React applications.
   * - object-hash
     - Utility for generating hash values from objects, useful for state management.
   * - fontawesome
     - Icon library for adding scalable vector icons to the application.
   * - Bootstrap
     - CSS framework for responsive design and pre-styled components.

Backend Building Blocks
----------------------

.. list-table:: Backend Internal Components
   :header-rows: 1
   :widths: 30 70

   * - Component
     - Purpose
   * - Controller/
     - Contains REST API endpoints for handling HTTP requests and responses. Also responsible for authentication and authorization.
   * - Service/
     - Contains business logic and interacts with the database through repositories. Also responsible for parsing and validating requests.
   * - Repository/
     - Contains interfaces for database operations, providing methods to interact with MongoDB collections.
   * - Model/
     - Contains data models representing the application's entities, such as User, Question, and Answer.
   * - Data Transition Model (DTO)/
     - Contains data transition models that represent the data structure used in the application, such as UserDTO, QuestionDTO, and AnswerDTO.
   * - Mapper/
     - Contains classes for mapping between data transition models and database entities, ensuring data consistency and integrity.
   * - Security/
     - Contains security-related classes, such as JWT token generation and validation, user authentication, and authorization.

.. list-table:: Backend Libraries/Dependencies
   :header-rows: 1
   :widths: 30 70

   * - Library/Dependency
     - Purpose
   * - Spring Boot
     - Framework for building Java-based web applications, providing features like dependency injection, REST API support, and security. Provides a lot of sub-dependencies, such as database-connect and security.
   * - jsonwebtoken
     - Library for creating and verifying JSON Web Tokens (JWT) for user authentication and authorization.
   * - Mapstruct
     - Library for mapping between different object models, such as data transition models and database entities.
   * - Lombok
     - Library for reducing boilerplate code in Java, providing annotations for generating getters, setters, and constructors automatically.
   * - Jakarta Validation
     - Library for validating data models and ensuring data integrity, providing annotations for field validation.
   * - unirest
     - Lightweight HTTP client for making requests to external APIs, such as the Dice Bear Avatar Service.