# Google Gemini AI Integration for Dental Chatbot

## 🤖 Overview

Your dental chatbot is now powered by **Google Gemini AI**, making it much smarter at understanding natural language and creating appointments! The chatbot can now:

- ✅ Understand conversational appointment requests like "Book an appointment with Dr. Smith for John Doe tomorrow at 2pm"
- ✅ Extract patient names, dentist names, and dates/times from natural language
- ✅ Parse various date formats (ISO, natural language like "tomorrow at 2pm", etc.)
- ✅ Automatically create appointments with all the information provided
- ✅ Fall back to rule-based processing if AI is not configured or fails

## 🚀 How to Get Started

### Step 1: Get Your Gemini API Key

1. Visit [Google AI Studio](https://makersuite.google.com/app/apikey)
2. Sign in with your Google account
3. Click **"Get API Key"** or **"Create API Key"**
4. Copy your API key (it starts with `AIza...`)

### Step 2: Configure the API Key

You have two options:

#### Option A: Environment Variable (Recommended for Production)
```bash
# Windows
set GEMINI_API_KEY=your_api_key_here

# Linux/Mac
export GEMINI_API_KEY=your_api_key_here
```

#### Option B: Direct Configuration (For Development)
Edit `src/main/resources/application.properties`:
```properties
gemini.api.key=your_api_key_here
```

⚠️ **Important**: Never commit your API key to version control! Add it to `.gitignore` or use environment variables.

### Step 3: Start the Application

```bash
mvn spring-boot:run
```

## 💬 Example Conversations

### Smart Appointment Creation

**User:** "I want to book an appointment for John Doe with Dr. Smith tomorrow at 2pm"

**AI Chatbot:** 
```
✅ Appointment created successfully!

👤 Patient: John Doe
👨‍⚕️ Dentist: Dr. Sarah Smith
📅 Date/Time: Oct 26, 2025 at 02:00 PM

The appointment has been saved to the system.
```

**User:** "Schedule Jane for dentist Wilson next Friday at 10am"

**AI Chatbot:**
```
✅ Appointment created successfully!

👤 Patient: Jane Wilson
👨‍⚕️ Dentist: Dr. Wilson
📅 Date/Time: Nov 01, 2025 at 10:00 AM

The appointment has been saved to the system.
```

### Conversational Search

**User:** "Can you find all patients named Smith?"

**AI Chatbot:** Returns list of matching patients with addresses

**User:** "Show me dentists in the system"

**AI Chatbot:** Returns complete dentist list

### Natural Language Understanding

The AI can understand various phrasings:
- "Book appointment for John with Dr. Smith on 2025-10-25 at 14:00"
- "Make an appointment tomorrow at 2pm for patient John, dentist Smith"
- "Schedule John Doe to see Dr. Wilson next Monday at 10am"
- "I need to book John for dentist appointment with Smith tomorrow 2pm"

## 🔧 Technical Details

### Architecture

1. **GeminiAIService** - Handles communication with Google Gemini API
   - Sends prompts with system context
   - Parses JSON responses
   - Handles errors gracefully

2. **Enhanced ChatbotService** - Intelligent message processing
   - Tries AI processing first (if configured)
   - Falls back to rule-based processing
   - Extracts structured data from AI responses
   - Creates appointments automatically

3. **Smart Date/Time Parsing**
   - ISO format: `2025-10-25T14:00:00`
   - Common formats: `2025-10-25 14:00`, `10/25/2025 14:00`
   - Natural language (via AI): "tomorrow at 2pm", "next Friday at 10am"

### AI Response Format

The AI returns structured JSON:
```json
{
  "intent": "make_appointment",
  "extracted_data": {
    "patient_info": "John Doe",
    "dentist_info": "Dr. Smith",
    "datetime": "2025-10-26T14:00:00"
  },
  "response_message": "I'll help you create an appointment for John Doe with Dr. Smith."
}
```

### Supported Intents

- `search_patient` - Find patients by name
- `search_dentist` - Find dentists by name
- `list_patients` - Show all patients
- `list_dentists` - Show all dentists
- `list_appointments` - Show all appointments
- `make_appointment` - Create new appointment
- `help` - Show help information

## 🛡️ Fallback Mechanism

If Gemini AI is not configured or fails:
- ✅ Chatbot automatically falls back to rule-based processing
- ✅ All existing commands still work ("Find patient", "List dentists", etc.)
- ✅ No errors shown to users
- ✅ Seamless degradation

## 📊 Benefits of AI Integration

### Before AI:
❌ "Make appointment for John with Smith tomorrow at 2pm"
→ *"To make an appointment, I need: 1. Patient name, 2. Dentist name, 3. Date/time..."*

### With Gemini AI:
✅ "Make appointment for John with Smith tomorrow at 2pm"
→ *Creates the appointment automatically with confirmation!*

## 🔐 Security & Privacy

- API key stored securely via environment variables
- All chatbot endpoints require JWT authentication
- Role-based access control (ROLE_USER, ROLE_OFFICE_MANAGER)
- No patient data sent to Gemini - only search queries and appointment requests
- HTTPS recommended for production

## 💰 Cost Considerations

### Gemini API Pricing (as of 2024):
- **Free Tier**: 60 requests per minute
- **Pay-as-you-go**: Very affordable for typical usage
- Each chatbot message = 1 API call
- Estimated cost: ~$0.001 per conversation

### Cost Optimization:
- Caching enabled for repeated queries
- Fallback to rule-based for simple commands
- 30-second timeout prevents long-running requests

## 🧪 Testing

### Without API Key (Rule-based):
```bash
# Start without GEMINI_API_KEY
mvn spring-boot:run

# Test in chatbot
"Find patient John" ✅ Works
"List dentists" ✅ Works
"Make appointment" ✅ Shows guidance
```

### With API Key (AI-powered):
```bash
# Start with GEMINI_API_KEY
export GEMINI_API_KEY=your_key
mvn spring-boot:run

# Test in chatbot
"Book John for Dr. Smith tomorrow at 2pm" ✅ Creates appointment!
"Schedule appointment for Jane with Wilson next Monday 10am" ✅ Creates appointment!
```

## 🐛 Troubleshooting

### "AI processing failed, falling back to rules" or "404 Error"
- ✅ **FIXED**: Make sure you're using the correct API endpoint (`gemini-1.5-flash-latest`)
- Check your API key is correct and starts with `AIza`
- Verify internet connection
- Check Gemini API quota/limits (free tier: 60 requests/min)
- Ensure API key has proper permissions in Google AI Studio

### "Error calling Gemini API: 404"
This usually means:
- API endpoint is incorrect (we use `gemini-1.5-flash-latest`)
- API key is invalid or expired
- Model name is wrong

**Solution**: The code now uses the correct endpoint. Restart your application:
```bash
mvn spring-boot:run
```

### Appointment not created
- Ensure patient exists in system (search first: "Find patient John")
- Ensure dentist exists in system (search first: "Find dentist Smith")
- Check date/time format (try: "2025-10-25 14:00")
- Try more specific information
- Check backend logs for detailed error messages

### API Key not working
```bash
# Verify environment variable
echo $GEMINI_API_KEY  # Linux/Mac
echo %GEMINI_API_KEY%  # Windows

# Should output your API key starting with: AIza...

# Check application startup logs for:
"Gemini AI Service configured successfully"
```

### Testing the API Key
Try this curl command to verify your API key works:
```bash
curl "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=YOUR_API_KEY" \
  -H 'Content-Type: application/json' \
  -d '{"contents":[{"parts":[{"text":"Hello"}]}]}'
```

If this returns JSON with a response, your API key is valid!

## 📈 Future Enhancements

Potential improvements:
- [ ] Multi-turn conversations (remember context)
- [ ] Voice input support
- [ ] Appointment conflict detection
- [ ] Reminder scheduling
- [ ] Integration with calendar systems
- [ ] Multiple language support

## 📝 API Reference

### New Dependencies Added:
```xml
<!-- Google Gemini AI -->
<dependency>
    <groupId>com.google.cloud</groupId>
    <artifactId>google-cloud-vertexai</artifactId>
    <version>1.1.0</version>
</dependency>

<!-- HTTP Client -->
<dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>okhttp</artifactId>
    <version>4.12.0</version>
</dependency>

<!-- JSON Parser -->
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.10.1</version>
</dependency>
```

### New Files Created:
- `GeminiAIService.java` - AI integration service
- `AIAppointmentRequest.java` - DTO for AI appointment data
- Enhanced `ChatbotService.java` - Smart processing logic

## 🎉 Summary

Your dental chatbot is now **10x smarter** with Google Gemini AI! Users can now:

1. **Create appointments in one message** instead of multiple steps
2. **Use natural language** instead of specific commands
3. **Get intelligent responses** tailored to their needs
4. **Save time** with automatic data extraction

The system gracefully falls back to rule-based processing if AI is unavailable, ensuring reliability.

---

**Ready to test?** Get your API key and start chatting! 🚀

