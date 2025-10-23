import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import './CreateAppointment.css';

const CreateAppointment = ({ token, onLogout }) => {
  const [patients, setPatients] = useState([]);
  const [dentists, setDentists] = useState([]);
  const [surgeries, setSurgeries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [formData, setFormData] = useState({
    patientId: '',
    dentistId: '',
    surgeryId: '',
    appointmentDate: '',
    appointmentTime: ''
  });

  const navigate = useNavigate();

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      setLoading(true);
      const headers = { 'Authorization': `Bearer ${token}` };

      const [patientsRes, dentistsRes, surgeriesRes] = await Promise.all([
        axios.get('http://localhost:8080/adsweb/api/v1/patients', { headers }),
        axios.get('http://localhost:8080/adsweb/api/v1/dentists', { headers }),
        axios.get('http://localhost:8080/adsweb/api/v1/surgeries', { headers })
      ]);

      setPatients(patientsRes.data);
      setDentists(dentistsRes.data);
      setSurgeries(surgeriesRes.data);
      setError('');
    } catch (err) {
      setError('Failed to load data. Please try again.');
      if (err.response?.status === 401) {
        onLogout();
      }
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    setError('');
    setSuccess('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    // Validation
    if (!formData.patientId || !formData.dentistId || !formData.surgeryId ||
        !formData.appointmentDate || !formData.appointmentTime) {
      setError('Please fill in all fields');
      return;
    }

    try {
      setSubmitting(true);
      setError('');

      // Combine date and time into LocalDateTime format
      const appointmentDateTime = `${formData.appointmentDate}T${formData.appointmentTime}:00`;

      const appointmentData = {
        appointmentDateTime: appointmentDateTime,
        patient: { id: parseInt(formData.patientId) },
        dentist: { id: parseInt(formData.dentistId) },
        surgery: { id: parseInt(formData.surgeryId) }
      };

      await axios.post('http://localhost:8080/adsweb/api/v1/appointments',
        appointmentData,
        {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        }
      );

      setSuccess('Appointment created successfully!');

      // Reset form
      setFormData({
        patientId: '',
        dentistId: '',
        surgeryId: '',
        appointmentDate: '',
        appointmentTime: ''
      });

      // Redirect to appointments page after 2 seconds
      setTimeout(() => {
        navigate('/appointments');
      }, 2000);

    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create appointment. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleLogout = () => {
    onLogout();
    navigate('/login');
  };

  return (
    <div className="create-appointment-container">
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
          <button onClick={() => navigate('/appointments')} className="nav-button">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
              <path d="M19 3H18V1H16V3H8V1H6V3H5C3.89 3 3 3.9 3 5V19C3 20.1 3.89 21 5 21H19C20.1 21 21 20.1 21 19V5C21 3.9 20.1 3 19 3ZM19 19H5V8H19V19Z" fill="currentColor"/>
            </svg>
            Appointments
          </button>
          <button onClick={handleLogout} className="logout-button">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
              <path d="M17 7L15.59 8.41L18.17 11H8V13H18.17L15.59 15.58L17 17L22 12L17 7ZM4 5H12V3H4C2.9 3 2 3.9 2 5V19C2 20.1 2.9 21 4 21H12V19H4V5Z" fill="currentColor"/>
            </svg>
            Logout
          </button>
        </div>
      </nav>

      <div className="create-appointment-content">
        <div className="page-header">
          <h1>Create New Appointment</h1>
          <p>Schedule an appointment for a patient with a dentist</p>
        </div>

        {loading ? (
          <div className="loading-container">
            <div className="loading-spinner"></div>
            <p>Loading form data...</p>
          </div>
        ) : (
          <div className="form-container">
            <form onSubmit={handleSubmit} className="appointment-form">
              {error && <div className="error-message">{error}</div>}
              {success && <div className="success-message">{success}</div>}

              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="patientId">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                      <path d="M12 12C14.21 12 16 10.21 16 8C16 5.79 14.21 4 12 4C9.79 4 8 5.79 8 8C8 10.21 9.79 12 12 12ZM12 14C9.33 14 4 15.34 4 18V20H20V18C20 15.34 14.67 14 12 14Z" fill="#667eea"/>
                    </svg>
                    Select Patient *
                  </label>
                  <select
                    id="patientId"
                    name="patientId"
                    value={formData.patientId}
                    onChange={handleInputChange}
                    required
                  >
                    <option value="">-- Choose a patient --</option>
                    {patients.map(patient => (
                      <option key={patient.id} value={patient.id}>
                        {patient.name} (#{patient.patNo})
                      </option>
                    ))}
                  </select>
                </div>

                <div className="form-group">
                  <label htmlFor="dentistId">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                      <path d="M20 6H18V4C18 2.9 17.1 2 16 2H8C6.9 2 6 2.9 6 4V6H4C2.9 6 2 6.9 2 8V20C2 21.1 2.9 22 4 22H20C21.1 22 22 21.1 22 20V8C22 6.9 21.1 6 20 6ZM8 4H16V6H8V4ZM20 20H4V8H20V20Z" fill="#4facfe"/>
                    </svg>
                    Select Dentist *
                  </label>
                  <select
                    id="dentistId"
                    name="dentistId"
                    value={formData.dentistId}
                    onChange={handleInputChange}
                    required
                  >
                    <option value="">-- Choose a dentist --</option>
                    {dentists.map(dentist => (
                      <option key={dentist.id} value={dentist.id}>
                        {dentist.dentistName}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="surgeryId">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                      <path d="M12 2C8.13 2 5 5.13 5 9C5 14.25 12 22 12 22C12 22 19 14.25 19 9C19 5.13 15.87 2 12 2ZM12 11.5C10.62 11.5 9.5 10.38 9.5 9C9.5 7.62 10.62 6.5 12 6.5C13.38 6.5 14.5 7.62 14.5 9C14.5 10.38 13.38 11.5 12 11.5Z" fill="#f5576c"/>
                    </svg>
                    Select Surgery *
                  </label>
                  <select
                    id="surgeryId"
                    name="surgeryId"
                    value={formData.surgeryId}
                    onChange={handleInputChange}
                    required
                  >
                    <option value="">-- Choose a surgery --</option>
                    {surgeries.map(surgery => (
                      <option key={surgery.id} value={surgery.id}>
                        {surgery.surgeryNo} - {surgery.address?.city || 'N/A'}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="appointmentDate">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                      <path d="M19 3H18V1H16V3H8V1H6V3H5C3.89 3 3 3.9 3 5V19C3 20.1 3.89 21 5 21H19C20.1 21 21 20.1 21 19V5C21 3.9 20.1 3 19 3ZM19 19H5V8H19V19Z" fill="#43e97b"/>
                    </svg>
                    Appointment Date *
                  </label>
                  <input
                    type="date"
                    id="appointmentDate"
                    name="appointmentDate"
                    value={formData.appointmentDate}
                    onChange={handleInputChange}
                    min={new Date().toISOString().split('T')[0]}
                    required
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="appointmentTime">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                      <path d="M12 2C6.5 2 2 6.5 2 12C2 17.5 6.5 22 12 22C17.5 22 22 17.5 22 12C22 6.5 17.5 2 12 2ZM12 20C7.59 20 4 16.41 4 12C4 7.59 7.59 4 12 4C16.41 4 20 7.59 20 12C20 16.41 16.41 20 12 20ZM12.5 7H11V13L16.2 16.2L17 14.9L12.5 12.2V7Z" fill="#43e97b"/>
                    </svg>
                    Appointment Time *
                  </label>
                  <input
                    type="time"
                    id="appointmentTime"
                    name="appointmentTime"
                    value={formData.appointmentTime}
                    onChange={handleInputChange}
                    required
                  />
                </div>
              </div>

              <div className="form-actions">
                <button
                  type="button"
                  onClick={() => navigate('/appointments')}
                  className="cancel-button"
                  disabled={submitting}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="submit-button"
                  disabled={submitting}
                >
                  {submitting ? (
                    <>
                      <span className="loading-spinner"></span>
                      Creating...
                    </>
                  ) : (
                    <>
                      <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                        <path d="M9 16.2L4.8 12L3.4 13.4L9 19L21 7L19.6 5.6L9 16.2Z" fill="currentColor"/>
                      </svg>
                      Create Appointment
                    </>
                  )}
                </button>
              </div>
            </form>

            <div className="form-info">
              <h3>Quick Tips</h3>
              <ul>
                <li>Select a patient from the dropdown list</li>
                <li>Choose an available dentist</li>
                <li>Select the surgery location</li>
                <li>Pick a future date and time for the appointment</li>
                <li>All fields are required to create an appointment</li>
              </ul>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default CreateAppointment;

