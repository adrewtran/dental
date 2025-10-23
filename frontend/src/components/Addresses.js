import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import './Addresses.css';

const Addresses = ({ token, onLogout }) => {
  const [addresses, setAddresses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    fetchAddresses();
  }, []);

  const fetchAddresses = async () => {
    try {
      setLoading(true);
      const response = await axios.get('http://localhost:8080/adsweb/api/v1/addresses', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      setAddresses(response.data);
      setError('');
    } catch (err) {
      setError('Failed to fetch addresses. Please try again.');
      if (err.response?.status === 401) {
        onLogout();
      }
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    onLogout();
    navigate('/login');
  };

  return (
    <div className="addresses-container">
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

      <div className="addresses-content">
        <div className="page-header">
          <h1>Addresses Management</h1>
          <p>View all patient addresses sorted by city</p>
        </div>

        {error && <div className="error-message">{error}</div>}

        {loading ? (
          <div className="loading-container">
            <div className="loading-spinner"></div>
            <p>Loading addresses...</p>
          </div>
        ) : (
          <div className="addresses-table-container">
            {addresses.length === 0 ? (
              <div className="no-data">
                <svg width="64" height="64" viewBox="0 0 24 24" fill="none">
                  <path d="M12 2C8.13 2 5 5.13 5 9C5 14.25 12 22 12 22C12 22 19 14.25 19 9C19 5.13 15.87 2 12 2ZM12 11.5C10.62 11.5 9.5 10.38 9.5 9C9.5 7.62 10.62 6.5 12 6.5C13.38 6.5 14.5 7.62 14.5 9C14.5 10.38 13.38 11.5 12 11.5Z" fill="#cbd5e0"/>
                </svg>
                <h3>No addresses found</h3>
                <p>There are no addresses in the system</p>
              </div>
            ) : (
              <div className="table-wrapper">
                <table className="addresses-table">
                  <thead>
                    <tr>
                      <th>Street</th>
                      <th>City</th>
                      <th>Zip Code</th>
                      <th>Patient Name</th>
                      <th>Patient No</th>
                    </tr>
                  </thead>
                  <tbody>
                    {addresses.map((address) => (
                      <tr key={address.id}>
                        <td>
                          <div className="cell-content">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                              <path d="M12 2C8.13 2 5 5.13 5 9C5 14.25 12 22 12 22C12 22 19 14.25 19 9C19 5.13 15.87 2 12 2ZM12 11.5C10.62 11.5 9.5 10.38 9.5 9C9.5 7.62 10.62 6.5 12 6.5C13.38 6.5 14.5 7.62 14.5 9C14.5 10.38 13.38 11.5 12 11.5Z" fill="#667eea"/>
                            </svg>
                            {address.street}
                          </div>
                        </td>
                        <td>
                          <span className="city-badge">{address.city}</span>
                        </td>
                        <td>{address.zipCode}</td>
                        <td>
                          {address.patient ? (
                            <div className="patient-info-cell">
                              <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                                <path d="M12 12C14.21 12 16 10.21 16 8C16 5.79 14.21 4 12 4C9.79 4 8 5.79 8 8C8 10.21 9.79 12 12 12ZM12 14C9.33 14 4 15.34 4 18V20H20V18C20 15.34 14.67 14 12 14Z" fill="#764ba2"/>
                              </svg>
                              {address.patient.name}
                            </div>
                          ) : (
                            <span className="no-patient">N/A</span>
                          )}
                        </td>
                        <td>
                          {address.patient ? (
                            <span className="patient-no">{address.patient.patNo}</span>
                          ) : (
                            <span className="no-patient">N/A</span>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}

        <div className="summary-section">
          <div className="summary-card">
            <div className="summary-icon">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none">
                <path d="M12 2C8.13 2 5 5.13 5 9C5 14.25 12 22 12 22C12 22 19 14.25 19 9C19 5.13 15.87 2 12 2ZM12 11.5C10.62 11.5 9.5 10.38 9.5 9C9.5 7.62 10.62 6.5 12 6.5C13.38 6.5 14.5 7.62 14.5 9C14.5 10.38 13.38 11.5 12 11.5Z" fill="white"/>
              </svg>
            </div>
            <div className="summary-content">
              <div className="summary-number">{addresses.length}</div>
              <div className="summary-label">Total Addresses</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Addresses;

