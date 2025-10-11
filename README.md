# Ro-Co 😃🎓

The **Ro-Co** (Rosenheim Community) app is a community-driven web platform that connects students and enables interdisciplinary knowledge exchange. 🤝

We aim to offer students a safe space to ask questions about their studies and private projects, encouraging a maker mentality by leveraging the enormous shared knowledge. As an open-source project, we want to encourage students to contribute and help shape the platform to their needs, creating a central platform that connects students. 🌱✨

---

## Get Started 🚀

1. **Clone the repository:**

   ```bash
   git clone https://github.com/Patrick112233/Ro-Co.git
   cd Ro-Co
   ```

2. **Ensure to distibute TLS certificates:**
   For development and test purpose the system can be initialized as fallowed:
   ```bash
   ./configure.sh
   ```
   This reqires a linux environment or WSL.

2. **Start the application using Docker Compose:**

   ```bash
   docker-compose up --build
   ```
   The first time you run this command, it may take a while to download and build all dependencies. ⏳

3. **Access the application:**

   Open your browser and go to [http://localhost:3000/](http://localhost:3000/) to use the Ro-Co platform. 🌐


## License 📄

This project is licensed under the MIT License. See the `LICENSE` file for details.

## Manuel Configuration

### RestAPI:
All application and security configurations can be made at `restAPI/src/main/resources/application.properties`
Ensure the availability of the fallowing files:
   - `restAPI/src/main/resources/certs/RoCoRootCA.pem`: the Root certificate by the CA
   - `restAPI/src/main/resources/certs/RoCoAPI.pem`: with root certificate signed certificate
### Database:
The database configurations are placed in:
   - `db/mongod.conf`: the Root certificate by the CA
   - `db/mongo-init.js`: Init script for the mongoDB
Further the `docker-compse.yml` contained all the relevant path matching to the configurations in the `db/` folder.

## Contributing 🤗
Contributions are welcome! To contribute:
- Fork the repository
- Create a new branch for your feature or bugfix
- Submit a pull request with a clear description of your changes

For questions or suggestions, feel free to open an issue on GitHub. 💡

---

## Documentation 📚

Full documentation is available at: [https://ro-co.readthedocs.io/en/latest/](https://ro-co.readthedocs.io/en/latest/)

> **Note:**
> This project is under active development. 🛠️

