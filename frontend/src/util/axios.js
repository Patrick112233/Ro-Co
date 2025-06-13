import axios from "axios";
const baseURL = process.env.BACKEND_URL + "/api/v1/";

//const baseURL = "/api/v1/";

export default axios.create({
    rejectUnauthorized: false,
    withCredentials: true,
    baseURL: baseURL,
    headers: {
        "Content-type": "application/json"
    }
})