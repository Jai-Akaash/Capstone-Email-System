// src/hooks/useDashboard.js
import { useState, useEffect } from "react";
import { message } from "antd";
import { apiClient } from "../api/axiosConfig";

export const useDashboard = () => {
    const [stats, setStats] = useState(null);
    const [recentLogs, setRecentLogs] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchDashboardData = async () => {
            try {
                const [statsRes, recentRes] = await Promise.all([
                    apiClient.get(`/dashboard/stats`),
                    apiClient.get(`/dashboard/recent`)
                ]);
                setStats(statsRes.data);
                setRecentLogs(recentRes.data);
            } catch (error) {
                console.error("Dashboard Error:", error);
                message.error("Failed to load dashboard analytics.");
            } finally {
                setLoading(false);
            }
        };

        fetchDashboardData();
    }, []);

    return { stats, recentLogs, loading };
};