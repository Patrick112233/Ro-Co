import axios from "axios";

export default axios.create({
    rejectUnauthorized: false,
    baseURL: "https://localhost:443",
    headers: {
        "Content-type": "application/json"
    }
})