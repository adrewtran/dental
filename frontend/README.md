# Dental Management System - React Frontend

A professional, elegant, and modern React frontend for the Dental Management System.

## Features

- **Beautiful Login Page** - Elegant gradient background with smooth animations
- **Dashboard** - Clean overview with navigation cards and statistics
- **Patients Management** - View, search, and manage patient records
- **Addresses Management** - Display patient addresses in a professional table view
- **Responsive Design** - Works perfectly on desktop, tablet, and mobile devices
- **JWT Authentication** - Secure token-based authentication

## Prerequisites

- Node.js (version 14 or higher)
- npm (comes with Node.js)
- Running Spring Boot backend on http://localhost:8080

## Installation

1. Navigate to the frontend directory:
```bash
cd E:\projects\cs489\dental\frontend
```

2. Install dependencies:
```bash
npm install
```

## Running the Application

Start the development server:
```bash
npm start
```

The application will open automatically in your browser at http://localhost:3000

## Default Login Credentials

- **Username:** admin
- **Password:** password

## Available Pages

### Login (`/login`)
- Professional login form with validation
- Error handling for invalid credentials
- Auto-redirect to dashboard after successful login

### Dashboard (`/dashboard`)
- Welcome section
- Navigation cards to Patients and Addresses
- Statistics display
- Logout functionality

### Patients (`/patients`)
- View all patients sorted by last name
- Search patients by name or patient number
- Display patient details with address information
- Professional card-based layout

### Addresses (`/addresses`)
- View all addresses sorted by city
- Professional table layout
- Display associated patient information
- Summary statistics

## Building for Production

Create an optimized production build:
```bash
npm run build
```

The build files will be in the `build` folder.

## Project Structure

```
frontend/
├── public/
│   └── index.html          # HTML template
├── src/
│   ├── components/         # React components
│   │   ├── Login.js        # Login page component
│   │   ├── Login.css       # Login page styles
│   │   ├── Dashboard.js    # Dashboard component
│   │   ├── Dashboard.css   # Dashboard styles
│   │   ├── Patients.js     # Patients page component
│   │   ├── Patients.css    # Patients page styles
│   │   ├── Addresses.js    # Addresses page component
│   │   └── Addresses.css   # Addresses page styles
│   ├── App.js              # Main app component with routing
│   ├── App.css             # Global app styles
│   ├── index.js            # React entry point
│   └── index.css           # Global styles
└── package.json            # Project dependencies

```

## Technologies Used

- **React** - UI library
- **React Router** - Client-side routing
- **Axios** - HTTP client for API calls
- **CSS3** - Modern styling with gradients, animations, and flexbox/grid

## Color Scheme

- Primary Gradient: `#667eea` to `#764ba2`
- Secondary Gradient: `#f093fb` to `#f5576c`
- Background: Purple gradient
- Text: Dark gray (`#2d3748`)
- Accent: Light gray (`#718096`)

## API Endpoints Used

- `POST /auth/login` - User authentication
- `GET /adsweb/api/v1/patients` - Get all patients
- `GET /adsweb/api/v1/patients/{id}` - Get patient by ID
- `GET /adsweb/api/v1/patient/search/{searchString}` - Search patients
- `GET /adsweb/api/v1/addresses` - Get all addresses

## Notes

- Make sure the Spring Boot backend is running before starting the frontend
- The backend should be accessible at http://localhost:8080
- JWT token is stored in localStorage for authentication
- CORS should be enabled on the backend for http://localhost:3000

## Troubleshooting

### Backend Connection Issues
If you get connection errors, ensure:
1. Spring Boot backend is running on port 8080
2. CORS is properly configured in the backend
3. The API endpoints match those defined in the components

### Login Issues
If login fails:
1. Check that the user exists in the database
2. Verify the JWT secret is properly configured in the backend
3. Check browser console for detailed error messages

## Future Enhancements

- Add patient creation/editing forms
- Implement address management (CRUD operations)
- Add appointment scheduling
- Include data visualization/charts
- Add pagination for large datasets
- Implement advanced search filters

