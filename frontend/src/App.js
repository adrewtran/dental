import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import './App.css';
import Login from './components/Login';
import Dashboard from './components/Dashboard';
import Patients from './components/Patients';
import Dentists from './components/Dentists';
import Appointments from './components/Appointments';
import CreateAppointment from './components/CreateAppointment';
import Addresses from './components/Addresses';
import Chatbot from './components/Chatbot';

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [token, setToken] = useState(null);

  useEffect(() => {
    const savedToken = localStorage.getItem('token');
    if (savedToken) {
      setToken(savedToken);
      setIsAuthenticated(true);
    }
  }, []);

  const handleLogin = (newToken) => {
    setToken(newToken);
    setIsAuthenticated(true);
    localStorage.setItem('token', newToken);
  };

  const handleLogout = () => {
    setToken(null);
    setIsAuthenticated(false);
    localStorage.removeItem('token');
  };

  return (
    <Router>
        {isAuthenticated && <Chatbot token={token} />}
      <div className="App">
        <Routes>
          <Route
            path="/login"
            element={
              isAuthenticated ?
                <Navigate to="/dashboard" /> :
                <Login onLogin={handleLogin} />
            }
          />
          <Route
            path="/dashboard"
            element={
              isAuthenticated ?
                <Dashboard token={token} onLogout={handleLogout} /> :
                <Navigate to="/login" />
            }
          />
          <Route
            path="/patients"
            element={
              isAuthenticated ?
                <Patients token={token} onLogout={handleLogout} /> :
                <Navigate to="/login" />
            }
          />
          <Route
            path="/dentists"
            element={
              isAuthenticated ?
                <Dentists token={token} onLogout={handleLogout} /> :
                <Navigate to="/login" />
            }
          />
          <Route
            path="/appointments"
            element={
              isAuthenticated ?
                <Appointments token={token} onLogout={handleLogout} /> :
                <Navigate to="/login" />
            }
          />
          <Route
            path="/create-appointment"
            element={
              isAuthenticated ?
                <CreateAppointment token={token} onLogout={handleLogout} /> :
                <Navigate to="/login" />
            }
          />
          <Route
            path="/addresses"
            element={
              isAuthenticated ?
                <Addresses token={token} onLogout={handleLogout} /> :
                <Navigate to="/login" />
            }
          />
          <Route path="/" element={<Navigate to="/login" />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
