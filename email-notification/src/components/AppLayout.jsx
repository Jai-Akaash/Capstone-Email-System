import { Outlet, NavLink } from "react-router-dom";
import {
    BarChartSquare02,
    HomeLine,
    Inbox01,
    Rows01,
    Send01 
} from "@untitledui/icons";
import "../index.css";

const navItems = [
    { label: "Home", path: "/", icon: <HomeLine /> },
    { label: "Dashboard", path: "/dashboard", icon: <BarChartSquare02 /> },
    { label: "Compose", path: "/compose", icon: <Send01 /> }, 
    { label: "All Mails", path: "/mails", icon: <Inbox01 /> },
    { label: "Templates", path: "/templates", icon: <Rows01 /> },
];
export const AppLayout = () => {
    return (
        <div className="app-container">
            <aside className="sidebar">
                <div className="sidebar-header">
                    Email Engine
                </div>
                <nav className="sidebar-nav">
                    {navItems.map((item) => (
                        <NavLink 
                            key={item.label} 
                            to={item.path}
                            className={({ isActive }) => isActive ? "nav-item active" : "nav-item"}
                        >
                            {item.icon}
                            <span>{item.label}</span>
                        </NavLink>
                    ))}
                </nav>
            </aside>
            
            <main className="main-content">
                <Outlet /> {/* Pages render here */}
            </main>
        </div>
    );
};