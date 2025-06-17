6. Runtime View
===============

This section describes the most important runtime scenarios and the interactions between the main components of the Ro-Co system. The focus is on security-relevant flows (login with JWT and refresh token), external API integration (avatar image retrieval), and typical resource operations (CRUD).




Login Sequence (JWT & Refresh Token)
------------------------------------
.. image:: pic/Login_Sequence.drawio.svg
   :alt: Sequence Diagram of API Requests
   :width: 1000px

1. **Browser** loads the **Frontend** application from the **Static Web Server**.
2. **User navigates to the sigin page** and create an accound and send it to the **Backend REST API**.
3. **Backend** validates the registration data and creates a new user in **MongoDB**.
4. **Frontend** request login with creantials to obtain access and refresh token. The **Backend** stores the has of the refresh tocken to be able to invalidate it if neccessary.
5. Now hte **Frontend** can access the protected resources of the **Backend REST API**.
6. If the Token expires, the **Frontend** uses the refresh token to request a new access token from the **Backend**.
7. On logout, the **Frontend** hints the **Backend** to invalidate the refresh token, preventing further access with that token.



External API: Lazy Avatar Image Loading
---------------------------------------

.. image:: pic/UserIconSequence.drawio.svg
   :alt: Sequence Diagram of API Requests
   :width: 1000px

1. **Frontend** requests a user avatar from the **Backend REST API**. The **Backend** checks if the avatar is already cached or stored in the database.
2. If ther is not avatar available, the **Backend** requests the avatar from the **Dice Bear Avatar Service** API using the user's username. The **Backend** stores the avatar image in the database for future use.
3. It the **Dice Bear Avatar Service** API is offline the **Backend** returns a default placeholder image. 
4. The **Backend** returns the avatar image URL to the **Frontend**.

Due to this lazy loading approach, the avatar images are only fetched when needed, reducing initial load time and bandwidth usage.
Futher, the if the **Dice Bear Avatar Service** API is unavailable, the immage get post loaded on the next request, so the user can still use the application without interruption.


General Resource Operations (CRUD)
----------------------------------

.. image:: pic/APIRequest.drawio.svg
   :alt: Sequence Diagram of API Requests
   :width: 1000px

1. **Frontend** sends a request (create, read, update, delete) to the **Backend REST API**.
2. **Backend** validates the request, checks authentication/authorization, and processes the operation.
3. **Backend** interacts with **MongoDB** to persist or retrieve data.
4. **Backend** returns the result (success, data, or error) to the **Frontend**.
5. **Frontend** updates the UI based on the response.

