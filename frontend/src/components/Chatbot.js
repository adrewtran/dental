import React, { useState, useEffect, useRef } from 'react';
import './Chatbot.css';

function Chatbot({ token }) {
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState([]);
  const [inputMessage, setInputMessage] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const messagesEndRef = useRef(null);
  const API_BASE_URL = 'http://localhost:8080/adsweb/api/v1';

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  useEffect(() => {
    if (isOpen && messages.length === 0) {
      // Welcome message
      setMessages([
        {
          message: "👋 Hello! I'm your Dental Assistant. I can help you:\n\n• Find patients and dentists\n• View appointments\n• Guide you through making appointments\n\nType 'help' to see all commands!",
          sender: 'bot',
          timestamp: Date.now(),
        },
      ]);
    }
  }, [isOpen]);

  const sendMessage = async () => {
    if (!inputMessage.trim()) return;

    const userMessage = {
      message: inputMessage,
      sender: 'user',
      timestamp: Date.now(),
    };

    setMessages((prev) => [...prev, userMessage]);
    setInputMessage('');
    setIsLoading(true);

    try {
      const response = await fetch(`${API_BASE_URL}/chatbot/message`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ message: inputMessage }),
      });

      if (!response.ok) {
        throw new Error('Failed to get response');
      }

      const data = await response.json();
      
      const botMessage = {
        message: data.message,
        sender: 'bot',
        timestamp: Date.now(),
        type: data.type,
        data: data.data,
        suggestions: data.suggestions,
      };

      setMessages((prev) => [...prev, botMessage]);
    } catch (error) {
      console.error('Error sending message:', error);
      const errorMessage = {
        message: "Sorry, I'm having trouble connecting. Please try again.",
        sender: 'bot',
        timestamp: Date.now(),
      };
      setMessages((prev) => [...prev, errorMessage]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  const handleSuggestionClick = (suggestion) => {
    setInputMessage(suggestion);
  };

  const renderMessageContent = (msg) => {
    if (msg.type === 'patient_list' && msg.data) {
      return (
        <div>
          <p className="chat-message-text">{msg.message}</p>
          <div className="data-list">
            {msg.data.map((patient) => (
              <div key={patient.id} className="data-item">
                <strong>{patient.name}</strong> (ID: {patient.patNo})
                {patient.address && (
                  <div className="data-details">
                    📍 {patient.address.street}, {patient.address.city}, {patient.address.zipCode}
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>
      );
    }

    if (msg.type === 'dentist_list' && msg.data) {
      return (
        <div>
          <p className="chat-message-text">{msg.message}</p>
          <div className="data-list">
            {msg.data.map((dentist) => (
              <div key={dentist.id} className="data-item">
                <strong>{dentist.dentistName}</strong>
                {dentist.address && (
                  <div className="data-details">
                    📍 {dentist.address.street}, {dentist.address.city}, {dentist.address.zipCode}
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>
      );
    }

    if (msg.type === 'appointment_list' && msg.data) {
      return (
        <div>
          <p className="chat-message-text">{msg.message}</p>
          <div className="data-list">
            {msg.data.map((appointment) => (
              <div key={appointment.id} className="data-item">
                <div className="appointment-info">
                  <strong>📅 {new Date(appointment.appointmentDateTime).toLocaleString()}</strong>
                  <div className="data-details">
                    👤 Patient: {appointment.patient?.name || 'N/A'}
                  </div>
                  <div className="data-details">
                    👨‍⚕️ Dentist: {appointment.dentist?.dentistName || 'N/A'}
                  </div>
                  {appointment.surgery && (
                    <div className="data-details">
                      🏥 Surgery: {appointment.surgery.surgeryNo}
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>
      );
    }

    if (msg.type === 'appointment_created' && msg.data) {
      return (
        <div>
          <p className="chat-message-text">{msg.message}</p>
          <div className="data-list">
            <div className="data-item appointment-created">
              <div className="appointment-info">
                <strong>📅 {new Date(msg.data.appointmentDateTime).toLocaleString()}</strong>
                <div className="data-details">
                  👤 Patient: {msg.data.patient?.name || 'N/A'} ({msg.data.patient?.patNo || 'N/A'})
                </div>
                <div className="data-details">
                  👨‍⚕️ Dentist: {msg.data.dentist?.dentistName || 'N/A'}
                </div>
                {msg.data.surgery && (
                  <div className="data-details">
                    🏥 Surgery: {msg.data.surgery.surgeryNo}
                  </div>
                )}
                <div className="data-details success-badge">
                  ✅ Appointment ID: #{msg.data.id}
                </div>
              </div>
            </div>
          </div>
        </div>
      );
    }

    return <p className="chat-message-text">{msg.message}</p>;
  };

  return (
    <div className="chatbot-container">
      {/* Chatbot Toggle Button */}
      <button
        className={`chatbot-toggle ${isOpen ? 'open' : ''}`}
        onClick={() => setIsOpen(!isOpen)}
        title="Chat with AI Assistant"
      >
        {isOpen ? '✕' : '💬'}
      </button>

      {/* Chatbot Window */}
      {isOpen && (
        <div className="chatbot-window">
          <div className="chatbot-header">
            <h3>🤖 Dental Assistant</h3>
            <button className="close-btn" onClick={() => setIsOpen(false)}>
              ✕
            </button>
          </div>

          <div className="chatbot-messages">
            {messages.map((msg, index) => (
              <div key={index} className={`message ${msg.sender}`}>
                <div className="message-content">
                  {renderMessageContent(msg)}
                  {msg.suggestions && msg.suggestions.length > 0 && (
                    <div className="suggestions">
                      {msg.suggestions.map((suggestion, idx) => (
                        <button
                          key={idx}
                          className="suggestion-btn"
                          onClick={() => handleSuggestionClick(suggestion)}
                        >
                          {suggestion}
                        </button>
                      ))}
                    </div>
                  )}
                </div>
              </div>
            ))}
            {isLoading && (
              <div className="message bot">
                <div className="message-content">
                  <div className="typing-indicator">
                    <span></span>
                    <span></span>
                    <span></span>
                  </div>
                </div>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          <div className="chatbot-input">
            <input
              type="text"
              value={inputMessage}
              onChange={(e) => setInputMessage(e.target.value)}
              onKeyPress={handleKeyPress}
              placeholder="Type your message..."
              disabled={isLoading}
            />
            <button onClick={sendMessage} disabled={isLoading || !inputMessage.trim()}>
              ➤
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

export default Chatbot;

