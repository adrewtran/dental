import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import './Dashboard.css';

const Dashboard = ({ onLogout }) => {
  const navigate = useNavigate();

  const handleLogout = () => {
    onLogout();
    navigate('/login');
  };

  const cards = [
    {
      title: 'Patients',
      description: 'Manage patient records and information',
      icon: (
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none">
          <path d="M12 12C14.21 12 16 10.21 16 8C16 5.79 14.21 4 12 4C9.79 4 8 5.79 8 8C8 10.21 9.79 12 12 12ZM12 14C9.33 14 4 15.34 4 18V20H20V18C20 15.34 14.67 14 12 14Z" fill="url(#grad1)"/>
          <defs>
            <linearGradient id="grad1" x1="4" y1="4" x2="20" y2="20">
              <stop offset="0%" stopColor="#667eea"/>
              <stop offset="100%" stopColor="#764ba2"/>
            </linearGradient>
          </defs>
        </svg>
      ),
      link: '/patients',
      color: '#667eea'
    },
    {
      title: 'Dentists',
      description: 'View and manage dentist records',
      icon: (
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none">
          <path d="M20 6H18V4C18 2.9 17.1 2 16 2H8C6.9 2 6 2.9 6 4V6H4C2.9 6 2 6.9 2 8V20C2 21.1 2.9 22 4 22H20C21.1 22 22 21.1 22 20V8C22 6.9 21.1 6 20 6ZM8 4H16V6H8V4ZM20 20H4V8H20V20ZM12 19C14.76 19 17 16.76 17 14C17 11.24 14.76 9 12 9C9.24 9 7 11.24 7 14C7 16.76 9.24 19 12 19ZM12 11C13.66 11 15 12.34 15 14C15 15.66 13.66 17 12 17C10.34 17 9 15.66 9 14C9 12.34 10.34 11 12 11Z" fill="url(#grad3)"/>
          <defs>
            <linearGradient id="grad3" x1="2" y1="2" x2="22" y2="22">
              <stop offset="0%" stopColor="#4facfe"/>
              <stop offset="100%" stopColor="#00f2fe"/>
            </linearGradient>
          </defs>
        </svg>
      ),
      link: '/dentists',
      color: '#4facfe'
    },
    {
      title: 'Appointments',
      description: 'Schedule and view appointments',
      icon: (
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none">
          <path d="M19 3H18V1H16V3H8V1H6V3H5C3.89 3 3 3.9 3 5V19C3 20.1 3.89 21 5 21H19C20.1 21 21 20.1 21 19V5C21 3.9 20.1 3 19 3ZM19 19H5V8H19V19Z" fill="url(#grad4)"/>
          <defs>
            <linearGradient id="grad4" x1="3" y1="3" x2="21" y2="21">
              <stop offset="0%" stopColor="#43e97b"/>
              <stop offset="100%" stopColor="#38f9d7"/>
            </linearGradient>
          </defs>
        </svg>
      ),
      link: '/appointments',
      color: '#43e97b'
    },
    {
      title: 'Addresses',
      description: 'View and manage patient addresses',
      icon: (
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none">
          <path d="M12 2C8.13 2 5 5.13 5 9C5 14.25 12 22 12 22C12 22 19 14.25 19 9C19 5.13 15.87 2 12 2ZM12 11.5C10.62 11.5 9.5 10.38 9.5 9C9.5 7.62 10.62 6.5 12 6.5C13.38 6.5 14.5 7.62 14.5 9C14.5 10.38 13.38 11.5 12 11.5Z" fill="url(#grad2)"/>
          <defs>
            <linearGradient id="grad2" x1="5" y1="2" x2="19" y2="22">
              <stop offset="0%" stopColor="#f093fb"/>
              <stop offset="100%" stopColor="#f5576c"/>
            </linearGradient>
          </defs>
        </svg>
      ),
      link: '/addresses',
      color: '#f5576c'
    }
  ];

  return (
    <div className="dashboard-container">
      <nav className="navbar">
        <div className="navbar-brand">
          <h2>🦷 Dental Management System</h2>
        </div>
        <button onClick={handleLogout} className="logout-button">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
            <path d="M17 7L15.59 8.41L18.17 11H8V13H18.17L15.59 15.58L17 17L22 12L17 7ZM4 5H12V3H4C2.9 3 2 3.9 2 5V19C2 20.1 2.9 21 4 21H12V19H4V5Z" fill="currentColor"/>
          </svg>
          Logout
        </button>
      </nav>

      <div className="dashboard-content">
        <div className="welcome-section">
          <h1>Welcome to Dental Management</h1>
          <p>Select a section below to get started</p>
        </div>

        <div className="cards-grid">
          {cards.map((card, index) => (
            <Link to={card.link} key={index} className="card" style={{'--card-color': card.color}}>
              <div className="card-icon">
                {card.icon}
              </div>
              <div className="card-content">
                <h3>{card.title}</h3>
                <p>{card.description}</p>
              </div>
              <div className="card-arrow">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                  <path d="M12 4L10.59 5.41L16.17 11H4V13H16.17L10.59 18.59L12 20L20 12L12 4Z" fill="currentColor"/>
                </svg>
              </div>
            </Link>
          ))}
        </div>

        <div className="stats-section">
          <div className="stat-card">
            <div className="stat-number">500+</div>
            <div className="stat-label">Total Patients</div>
          </div>
          <div className="stat-card">
            <div className="stat-number">95%</div>
            <div className="stat-label">Satisfaction Rate</div>
          </div>
          <div className="stat-card">
            <div className="stat-number">24/7</div>
            <div className="stat-label">Support Available</div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;

