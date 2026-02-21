// src/hooks/useMails.js
import { useState, useEffect } from "react";
import { message } from "antd";
import { apiClient } from "../api/axiosConfig";

export const useMails = (searchTerm, statusFilter) => {
    const [mails, setMails] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchMails = async () => {
            setLoading(true);
            try {
                let url = `/email/history`;
                const params = [];
                
                if (searchTerm) params.push(`recipient=${encodeURIComponent(searchTerm)}`);
                if (statusFilter !== "ALL") params.push(`status=${statusFilter}`);
                
                if (params.length > 0) url += `?${params.join("&")}`;

                const response = await apiClient.get(url);
                const sortedMails = response.data.sort((a, b) => b.id - a.id);
                setMails(sortedMails);
            } catch (error) {
                console.error(error);
                message.error("Failed to load email history.");
            } finally {
                setLoading(false);
            }
        };

        const delayDebounceFn = setTimeout(() => {
            fetchMails();
        }, 500);

        return () => clearTimeout(delayDebounceFn);
    }, [searchTerm, statusFilter]);

    return { mails, loading };
};