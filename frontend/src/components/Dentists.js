import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import './Dentists.css';

const Dentists = ({ token, onLogout }) => {
  const [dentists, setDentists] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    fetchDentists();
  }, []);

  const fetchDentists = async () => {
    try {
      setLoading(true);
      const response = await axios.get('http://localhost:8080/adsweb/api/v1/dentists', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      setDentists(response.data);
      setError('');
    } catch (err) {
      setError('Failed to fetch dentists. Please try again.');
      if (err.response?.status === 401) {
        onLogout();
      }
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!searchTerm.trim()) {
      fetchDentists();
      return;
    }

    try {
      setLoading(true);
      const response = await axios.get(`http://localhost:8080/adsweb/api/v1/dentist/search/${searchTerm}`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      setDentists(response.data);
      setError('');
    } catch (err) {
      setError('Search failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    onLogout();
    navigate('/login');
  };

  return (
    <div className="dentists-container">
      <nav className="navbar">
        <div className="navbar-brand">
          <h2>🦷 Dental Management System</h2>
        </div>
        <div className="navbar-actions">
          <button onClick={() => navigate('/dashboard')} className="nav-button">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
              <path d="M10 20V14H14V20H19V12H22L12 3L2 12H5V20H10Z" fill="currentColor"/>
            </svg>
            Dashboard
          </button>
          <button onClick={handleLogout} className="logout-button">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
              <path d="M17 7L15.59 8.41L18.17 11H8V13H18.17L15.59 15.58L17 17L22 12L17 7ZM4 5H12V3H4C2.9 3 2 3.9 2 5V19C2 20.1 2.9 21 4 21H12V19H4V5Z" fill="currentColor"/>
            </svg>
            Logout
          </button>
        </div>
      </nav>

      <div className="dentists-content">
        <div className="page-header">
          <h1>Dentists Management</h1>
          <p>View and search dentist records</p>
        </div>

        <div className="search-section">
          <form onSubmit={handleSearch} className="search-form">
            <div className="search-input-wrapper">
              <svg className="search-icon" width="20" height="20" viewBox="0 0 24 24" fill="none">
                <path d="M15.5 14H14.71L14.43 13.73C15.41 12.59 16 11.11 16 9.5C16 5.91 13.09 3 9.5 3C5.91 3 3 5.91 3 9.5C3 13.09 5.91 16 9.5 16C11.11 16 12.59 15.41 13.73 14.43L14 14.71V15.5L19 20.49L20.49 19L15.5 14ZM9.5 14C7.01 14 5 11.99 5 9.5C5 7.01 7.01 5 9.5 5C11.99 5 14 7.01 14 9.5C14 11.99 11.99 14 9.5 14Z" fill="#718096"/>
              </svg>
              <input
                type="text"
                placeholder="Search by dentist name..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>
            <button type="submit" className="search-button">Search</button>
            <button type="button" onClick={() => { setSearchTerm(''); fetchDentists(); }} className="reset-button">
              Reset
            </button>
          </form>
        </div>

        {error && <div className="error-message">{error}</div>}

        {loading ? (
          <div className="loading-container">
            <div className="loading-spinner"></div>
            <p>Loading dentists...</p>
          </div>
        ) : (
          <div className="dentists-grid">
            {dentists.length === 0 ? (
              <div className="no-data">
                <svg width="64" height="64" viewBox="0 0 24 24" fill="none">
                  <path d="M12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12C22 6.48 17.52 2 12 2ZM13 17H11V15H13V17ZM13 13H11V7H13V13Z" fill="#cbd5e0"/>
                </svg>
                <h3>No dentists found</h3>
                <p>Try adjusting your search criteria</p>
              </div>
            ) : (
              dentists.map((dentist) => (
                <div key={dentist.id} className="dentist-card">
                  <div className="dentist-avatar">
                    <svg width="48" height="48" viewBox="0 0 24 24" fill="none">
                      <path d="M20 6H18V4C18 2.9 17.1 2 16 2H8C6.9 2 6 2.9 6 4V6H4C2.9 6 2 6.9 2 8V20C2 21.1 2.9 22 4 22H20C21.1 22 22 21.1 22 20V8C22 6.9 21.1 6 20 6ZM8 4H16V6H8V4ZM20 20H4V8H20V20ZM12 19C14.76 19 17 16.76 17 14C17 11.24 14.76 9 12 9C9.24 9 7 11.24 7 14C7 16.76 9.24 19 12 19ZM12 11C13.66 11 15 12.34 15 14C15 15.66 13.66 17 12 17C10.34 17 9 15.66 9 14C9 12.34 10.34 11 12 11Z" fill="url(#gradDentist)"/>
                      <defs>
                        <linearGradient id="gradDentist" x1="2" y1="2" x2="22" y2="22">
                          <stop offset="0%" stopColor="#667eea"/>
                          <stop offset="100%" stopColor="#764ba2"/>
                        </linearGradient>
                      </defs>
                    </svg>
                  </div>
                  <div className="dentist-info">
                    <h3>{dentist.dentistName}</h3>
                    <div className="dentist-details">
                      {dentist.address && (
                        <>
                          <div className="detail-item">
                            <span className="detail-label">Street:</span>
                            <span className="detail-value">{dentist.address.street}</span>
                          </div>
                          <div className="detail-item">
                            <span className="detail-label">City:</span>
                            <span className="detail-value">{dentist.address.city}</span>
                          </div>
                          <div className="detail-item">
                            <span className="detail-label">Zip Code:</span>
                            <span className="detail-value">{dentist.address.zipCode}</span>
                          </div>
                        </>
                      )}
                    </div>
                  </div>
                </div>
              ))
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default Dentists;

