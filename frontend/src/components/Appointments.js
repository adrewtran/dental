import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import './Appointments.css';

const Appointments = ({ token, onLogout }) => {
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    fetchAppointments();
  }, []);

  const fetchAppointments = async () => {
    try {
      setLoading(true);
      const response = await axios.get('http://localhost:8080/adsweb/api/v1/appointments', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      setAppointments(response.data);
      setError('');
    } catch (err) {
      setError('Failed to fetch appointments. Please try again.');
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

  const formatDateTime = (dateTimeString) => {
    const date = new Date(dateTimeString);
    return date.toLocaleString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  return (
    <div className="appointments-container">
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

      <div className="appointments-content">
        <div className="page-header">
          <div className="header-content">
            <div>
              <h1>Appointments Management</h1>
              <p>View all appointments sorted by date and time</p>
            </div>
            <button onClick={() => navigate('/create-appointment')} className="create-button">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                <path d="M19 13H13V19H11V13H5V11H11V5H13V11H19V13Z" fill="currentColor"/>
              </svg>
              Create Appointment
            </button>
          </div>
        </div>

        {error && <div className="error-message">{error}</div>}

        {loading ? (
          <div className="loading-container">
            <div className="loading-spinner"></div>
            <p>Loading appointments...</p>
          </div>
        ) : (
          <div className="appointments-table-container">
            {appointments.length === 0 ? (
              <div className="no-data">
                <svg width="64" height="64" viewBox="0 0 24 24" fill="none">
                  <path d="M19 3H18V1H16V3H8V1H6V3H5C3.89 3 3 3.9 3 5V19C3 20.1 3.89 21 5 21H19C20.1 21 21 20.1 21 19V5C21 3.9 20.1 3 19 3ZM19 19H5V8H19V19Z" fill="#cbd5e0"/>
                </svg>
                <h3>No appointments found</h3>
                <p>There are no appointments scheduled</p>
              </div>
            ) : (
              <div className="table-wrapper">
                <table className="appointments-table">
                  <thead>
                    <tr>
                      <th>Date & Time</th>
                      <th>Patient</th>
                      <th>Dentist</th>
                      <th>Surgery</th>
                      <th>Location</th>
                    </tr>
                  </thead>
                  <tbody>
                    {appointments.map((appointment) => (
                      <tr key={appointment.id}>
                        <td>
                          <div className="datetime-cell">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                              <path d="M19 3H18V1H16V3H8V1H6V3H5C3.89 3 3 3.9 3 5V19C3 20.1 3.89 21 5 21H19C20.1 21 21 20.1 21 19V5C21 3.9 20.1 3 19 3ZM19 19H5V8H19V19Z" fill="#667eea"/>
                            </svg>
                            <span className="datetime-value">{formatDateTime(appointment.appointmentDateTime)}</span>
                          </div>
                        </td>
                        <td>
                          {appointment.patient ? (
                            <div className="info-cell">
                              <span className="name">{appointment.patient.name}</span>
                              <span className="id-badge">#{appointment.patient.patNo}</span>
                            </div>
                          ) : (
                            <span className="no-data-text">N/A</span>
                          )}
                        </td>
                        <td>
                          {appointment.dentist ? (
                            <span className="dentist-name">{appointment.dentist.dentistName}</span>
                          ) : (
                            <span className="no-data-text">N/A</span>
                          )}
                        </td>
                        <td>
                          {appointment.surgery ? (
                            <span className="surgery-badge">{appointment.surgery.surgeryNo}</span>
                          ) : (
                            <span className="no-data-text">N/A</span>
                          )}
                        </td>
                        <td>
                          {appointment.surgery?.address ? (
                            <div className="location-cell">
                              <span>{appointment.surgery.address.city}</span>
                              <small>{appointment.surgery.address.street}</small>
                            </div>
                          ) : (
                            <span className="no-data-text">N/A</span>
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
                <path d="M19 3H18V1H16V3H8V1H6V3H5C3.89 3 3 3.9 3 5V19C3 20.1 3.89 21 5 21H19C20.1 21 21 20.1 21 19V5C21 3.9 20.1 3 19 3ZM19 19H5V8H19V19Z" fill="white"/>
              </svg>
            </div>
            <div className="summary-content">
              <div className="summary-number">{appointments.length}</div>
              <div className="summary-label">Total Appointments</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Appointments;

