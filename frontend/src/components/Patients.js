import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import './Patients.css';

const Patients = ({ token, onLogout }) => {
  const [patients, setPatients] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    fetchPatients();
  }, []);

  const fetchPatients = async () => {
    try {
      setLoading(true);
      const response = await axios.get('http://localhost:8080/adsweb/api/v1/patients', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      setPatients(response.data);
      setError('');
    } catch (err) {
      setError('Failed to fetch patients. Please try again.');
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
      fetchPatients();
      return;
    }

    try {
      setLoading(true);
      const response = await axios.get(`http://localhost:8080/adsweb/api/v1/patient/search/${searchTerm}`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      setPatients(response.data);
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
    <div className="patients-container">
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

      <div className="patients-content">
        <div className="page-header">
          <h1>Patients Management</h1>
          <p>View and search patient records</p>
        </div>

        <div className="search-section">
          <form onSubmit={handleSearch} className="search-form">
            <div className="search-input-wrapper">
              <svg className="search-icon" width="20" height="20" viewBox="0 0 24 24" fill="none">
                <path d="M15.5 14H14.71L14.43 13.73C15.41 12.59 16 11.11 16 9.5C16 5.91 13.09 3 9.5 3C5.91 3 3 5.91 3 9.5C3 13.09 5.91 16 9.5 16C11.11 16 12.59 15.41 13.73 14.43L14 14.71V15.5L19 20.49L20.49 19L15.5 14ZM9.5 14C7.01 14 5 11.99 5 9.5C5 7.01 7.01 5 9.5 5C11.99 5 14 7.01 14 9.5C14 11.99 11.99 14 9.5 14Z" fill="#718096"/>
              </svg>
              <input
                type="text"
                placeholder="Search by name, patient number..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>
            <button type="submit" className="search-button">Search</button>
            <button type="button" onClick={() => { setSearchTerm(''); fetchPatients(); }} className="reset-button">
              Reset
            </button>
          </form>
        </div>

        {error && <div className="error-message">{error}</div>}

        {loading ? (
          <div className="loading-container">
            <div className="loading-spinner"></div>
            <p>Loading patients...</p>
          </div>
        ) : (
          <div className="patients-grid">
            {patients.length === 0 ? (
              <div className="no-data">
                <svg width="64" height="64" viewBox="0 0 24 24" fill="none">
                  <path d="M12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12C22 6.48 17.52 2 12 2ZM13 17H11V15H13V17ZM13 13H11V7H13V13Z" fill="#cbd5e0"/>
                </svg>
                <h3>No patients found</h3>
                <p>Try adjusting your search criteria</p>
              </div>
            ) : (
              patients.map((patient) => (
                <div key={patient.id} className="patient-card">
                  <div className="patient-avatar">
                    <svg width="48" height="48" viewBox="0 0 24 24" fill="none">
                      <path d="M12 12C14.21 12 16 10.21 16 8C16 5.79 14.21 4 12 4C9.79 4 8 5.79 8 8C8 10.21 9.79 12 12 12ZM12 14C9.33 14 4 15.34 4 18V20H20V18C20 15.34 14.67 14 12 14Z" fill="url(#gradPatient)"/>
                      <defs>
                        <linearGradient id="gradPatient" x1="4" y1="4" x2="20" y2="20">
                          <stop offset="0%" stopColor="#667eea"/>
                          <stop offset="100%" stopColor="#764ba2"/>
                        </linearGradient>
                      </defs>
                    </svg>
                  </div>
                  <div className="patient-info">
                    <h3>{patient.name}</h3>
                    <div className="patient-details">
                      <div className="detail-item">
                        <span className="detail-label">Patient No:</span>
                        <span className="detail-value">{patient.patNo}</span>
                      </div>
                      {patient.address && (
                        <>
                          <div className="detail-item">
                            <span className="detail-label">Street:</span>
                            <span className="detail-value">{patient.address.street}</span>
                          </div>
                          <div className="detail-item">
                            <span className="detail-label">City:</span>
                            <span className="detail-value">{patient.address.city}</span>
                          </div>
                          <div className="detail-item">
                            <span className="detail-label">Zip Code:</span>
                            <span className="detail-value">{patient.address.zipCode}</span>
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

export default Patients;

