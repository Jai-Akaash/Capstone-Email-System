// src/pages/Home.jsx
import { Button } from "antd";
import { useNavigate } from "react-router-dom";
import "../index.css";

export const Home = () => {
    const navigate = useNavigate();

    return (
        <div className="hero-container">
            <h1 className="hero-title">High-Performance Email Delivery</h1>
            <p style={{ fontSize: '1.25rem', color: '#6b7280', maxWidth: '600px', marginBottom: '32px' }}>
                Your centralized engine for event-driven notifications. Manage templates, track delivery status in real-time, and monitor system health.
            </p>
            <div style={{ display: 'flex', gap: '16px' }}>
                <Button type="primary" size="large" onClick={() => navigate('/dashboard')}>
                    View Dashboard
                </Button>
                <Button size="large" onClick={() => navigate('/templates')}>
                    Manage Templates
                </Button>
            </div>
        </div>
    );
};