import axios from "axios";

export default axios.create({
    rejectUnauthorized: false,
    baseURL: "api/v1/",
    headers: {
        "Content-type": "application/json"
    }
})