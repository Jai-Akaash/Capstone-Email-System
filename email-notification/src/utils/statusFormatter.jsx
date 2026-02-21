// src/utils/statusFormatter.jsx
import { Chip } from "@mui/material";
import { Tag } from "antd";

export const getStatusChip = (status) => {
    switch (status?.toUpperCase()) {
        case "DELIVERED": return <Chip label="Delivered" color="success" size="small" />;
        case "PROVIDER_SUCCESS": return <Chip label="Sent to Provider" color="primary" size="small" />;
        case "PROCESSING":
        case "IN_QUEUE":
        case "DEFERRED": return <Chip label="In Queue" color="warning" size="small" />;
        case "PROVIDER_FAILED":
        case "BOUNCED":
        case "DROPPED": return <Chip label="Failed" color="error" size="small" />;
        default: return <Chip label={status} size="small" />;
    }
};

export const getStatusTag = (status) => {
    switch (status?.toUpperCase()) {
        case "DELIVERED": return <Tag color="success">Delivered</Tag>;
        case "PROVIDER_SUCCESS": return <Tag color="processing">Provider Success</Tag>;
        case "IN_QUEUE":
        case "DEFERRED": return <Tag color="warning">Delayed / Queued</Tag>;
        case "BOUNCED":
        case "DROPPED": return <Tag color="error">Failed</Tag>;
        default: return <Tag>{status}</Tag>;
    }
};