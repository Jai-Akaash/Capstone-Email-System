// src/hooks/useCompose.js
import { useState } from "react";
import { message } from "antd";
import { apiClient } from "../api/axiosConfig";

export const useCompose = () => {
    const [loading, setLoading] = useState(false);

    const sendEmail = async (values) => {
        setLoading(true);
        try {
            await apiClient.post(`/email/send`, {
                to: values.to,
                subject: values.subject,
                body: values.body
            });
            message.success("Email successfully queued for sending!");
            return true;
        } catch (error) {
            console.error(error);
            message.error("Failed to send email. Check your backend connection.");
            return false;
        } finally {
            setLoading(false);
        }
    };

    return { sendEmail, loading };
};