// src/App.jsx
import { BrowserRouter, Routes, Route } from "react-router-dom";
import { AppLayout } from "./components/AppLayout";
import { Home } from "./pages/Home";
import { Templates } from "./pages/Templates";
import { Compose } from "./pages/Compose";
import { AllMails } from "./pages/AllMails";
import { Dashboard } from "./pages/Dashboard"; // 1. Import your real Dashboard page!

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<AppLayout />}>
          <Route index element={<Home />} />
          <Route path="dashboard" element={<Dashboard />} /> {/* 2. This now uses the real page */}
          <Route path="compose" element={<Compose />} />
          <Route path="mails" element={<AllMails />} />
          <Route path="templates" element={<Templates />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;