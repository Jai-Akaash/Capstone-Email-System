// src/hooks/useTemplates.js
import { useState, useEffect } from "react";
import { message } from "antd";
import { apiClient } from "../api/axiosConfig";

export const useTemplates = () => {
    const [templates, setTemplates] = useState([]);

    const fetchTemplates = async () => {
        try {
            const response = await apiClient.get(`/templates`);
            setTemplates(response.data);
        } catch (error) {
            message.error("Failed to load templates.");
        }
    };

    const saveTemplate = async (editingId, values) => {
        try {
            if (editingId) {
                await apiClient.put(`/templates/${editingId}`, values);
                message.success("Template updated!");
            } else {
                await apiClient.post(`/templates`, values);
                message.success("Template created!");
            }
            fetchTemplates();
            return true;
        } catch (error) {
            message.error("Failed to save template.");
            return false;
        }
    };

    const deleteTemplate = async (id) => {
        try {
            await apiClient.delete(`/templates/${id}`);
            message.success("Template deleted.");
            fetchTemplates();
        } catch (error) {
            message.error("Failed to delete template.");
        }
    };

    useEffect(() => {
        fetchTemplates();
    }, []);

    return { templates, fetchTemplates, saveTemplate, deleteTemplate };
};