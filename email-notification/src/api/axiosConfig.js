// src/api/axiosConfig.js
import axios from "axios";

// 1. Pull the URL from the .env file if it exists, otherwise fall back to localhost
const backendUrl = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

export const apiClient = axios.create({
    // 2. Use the dynamic variable and append '/api' to it
    baseURL: `${backendUrl}/api`,
    headers: {
        "Content-Type": "application/json"
    }
});