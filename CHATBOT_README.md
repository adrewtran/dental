# Dental Chatbot AI Assistant

## Overview
An intelligent chatbot assistant integrated into the Dental Management System to help users quickly search for patients, dentists, view appointments, and get guidance on creating appointments.

## Features

### 🔍 Smart Search Capabilities
- **Patient Search**: Find patients by name with natural language
  - Example: "Find patient John", "Search patient Smith"
- **Dentist Search**: Locate dentists by name
  - Example: "Find dentist Dr. Wilson", "Show dentist Smith"

### 📋 Information Retrieval
- **List All Patients**: View complete patient database
  - Command: "List all patients", "Show all patients"
- **List All Dentists**: Display all registered dentists
  - Command: "List all dentists", "Show dentists"
- **View Appointments**: See all scheduled appointments
  - Command: "Show appointments", "List appointments"

### 📅 Appointment Assistance
- Get guidance on creating appointments
- Command: "Make appointment", "Book appointment", "Schedule appointment"

### 💡 Interactive Features
- **Quick Suggestions**: After each response, the bot provides relevant action buttons
- **Natural Language Processing**: Understands casual language queries
- **Real-time Results**: Displays patient/dentist information with addresses
- **Help System**: Type "help" or "?" for command list

## Backend Implementation

### API Endpoints

#### POST /adsweb/api/v1/chatbot/message
Send a message to the chatbot and get an intelligent response.

**Request:**
```json
{
  "message": "Find patient John"
}
```

**Response:**
```json
{
  "message": "Found 2 patient(s) matching 'John':",
  "type": "patient_list",
  "data": [
    {
      "id": 1,
      "patNo": "P001",
      "name": "John Doe",
      "address": {
        "id": 1,
        "street": "123 Main St",
        "city": "Springfield",
        "zipCode": "12345"
      }
    }
  ],
  "suggestions": [
    "Make appointment",
    "Find dentist",
    "Search another patient"
  ]
}
```

#### GET /adsweb/api/v1/chatbot/help
Get help information about chatbot capabilities.

### Service Architecture

**ChatbotService** (`ChatbotService.java`)
- Natural language processing for user queries
- Pattern matching for intents (search, list, help)
- Integration with PatientService, DentistService, and AppointmentService
- Contextual response generation

**Key Methods:**
- `processMessage(String message)`: Main entry point for processing user queries
- `searchPatient(String message)`: Search patients by name
- `searchDentist(String message)`: Search dentists by name
- `listAllPatients()`: Retrieve all patients
- `listAllDentists()`: Retrieve all dentists
- `listAllAppointments()`: Get all appointments
- `handleAppointmentRequest()`: Provide appointment creation guidance

## Frontend Implementation

### Component: `Chatbot.js`

**Features:**
- Floating chat button (bottom-right corner)
- Expandable chat window
- Message history
- Typing indicator
- Quick action suggestions
- Responsive design

**Props:**
- `token`: JWT authentication token

**State Management:**
- `isOpen`: Controls chat window visibility
- `messages`: Chat history array
- `inputMessage`: Current user input
- `isLoading`: Loading state during API calls

### Styling: `Chatbot.css`

- Modern gradient design
- Smooth animations
- Responsive layout (mobile-friendly)
- Custom scrollbars
- Hover effects
- Message bubbles (user vs bot)

## Usage Examples

### Example 1: Find a Patient
**User:** "Find patient John Doe"

**Bot Response:**
```
Found 1 patient(s) matching 'John Doe':

John Doe (ID: P001)
📍 123 Main Street, Springfield, 12345

[Make appointment] [Find dentist] [Search another patient]
```

### Example 2: List All Dentists
**User:** "Show all dentists"

**Bot Response:**
```
Here are all dentists in the system (5 total):

Dr. Sarah Smith
📍 456 Oak Ave, Springfield, 12345

Dr. Michael Johnson
📍 789 Elm St, Springfield, 12345

[Search dentist] [Make appointment] [Find patient]
```

### Example 3: View Appointments
**User:** "Show appointments"

**Bot Response:**
```
Here are all appointments (3 total):

📅 2025-10-25 10:00 AM
👤 Patient: John Doe
👨‍⚕️ Dentist: Dr. Sarah Smith
🏥 Surgery: S001

[Make appointment] [Find patient] [Find dentist]
```

## Integration Guide

### Backend Integration
The chatbot is already integrated into the Spring Boot application:

1. **Controller**: `ChatbotController.java` - Handles HTTP requests
2. **Service**: `ChatbotService.java` - Business logic and NLP
3. **DTOs**: 
   - `ChatRequestDto.java` - Request payload
   - `ChatResponseDto.java` - Response payload
   - `ChatMessageDto.java` - Message structure

### Frontend Integration
The chatbot is integrated into `App.js`:

```javascript
import Chatbot from './components/Chatbot';

// In App component
{isAuthenticated && <Chatbot token={token} />}
```

## Security

- **Authentication Required**: All chatbot endpoints require valid JWT token
- **Role-Based Access**: Users with `ROLE_USER` or `ROLE_OFFICE_MANAGER` can access chatbot
- **CORS Enabled**: Configured for cross-origin requests

## Future Enhancements

Potential improvements for the chatbot:
- [ ] Natural date/time parsing for appointment scheduling
- [ ] Multi-step conversation flows
- [ ] Voice input support
- [ ] Export search results
- [ ] Advanced filtering options
- [ ] Integration with external AI services (OpenAI, etc.)
- [ ] Appointment conflict detection
- [ ] Reminder notifications

## Testing

### Manual Testing
1. Start the backend: `mvn spring-boot:run`
2. Start the frontend: `cd frontend && npm start`
3. Login to the application
4. Click the chatbot icon (bottom-right)
5. Try various commands:
   - "Find patient John"
   - "List all dentists"
   - "Show appointments"
   - "Help"

### API Testing with cURL
```bash
# Get chatbot help
curl -X GET http://localhost:8080/adsweb/api/v1/chatbot/help \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Send message
curl -X POST http://localhost:8080/adsweb/api/v1/chatbot/message \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"message": "Find patient John"}'
```

## Troubleshooting

### Chatbot not appearing
- Verify you're logged in (token exists)
- Check browser console for errors
- Ensure backend is running on port 8080

### No response from chatbot
- Verify JWT token is valid
- Check network tab for failed requests
- Ensure database has data (patients, dentists)

### Search returns no results
- Verify data exists in database
- Check search string spelling
- Try using "List all patients" to see available data

## Technologies Used

**Backend:**
- Spring Boot 3.5.6
- Spring Security with JWT
- JPA/Hibernate
- Lombok

**Frontend:**
- React 18
- CSS3 with animations
- Fetch API for HTTP requests
- React Hooks (useState, useEffect, useRef)

## License
Part of the Dental Management System - MIU CS489 Project

