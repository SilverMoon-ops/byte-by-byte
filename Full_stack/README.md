# ByteSocial - Full Stack Image Sharing App

ByteSocial is a modern, responsive full-stack application that allows users to share images with captions. It features a polished "glassmorphism" UI and a robust backend with secure image storage.

## ✨ Features

### Frontend
- **Responsive Design:** Fully optimized for Mobile, Tablet, and Desktop using CSS Grid and Flexbox.
- **Modern UI:** A unique look featuring glassmorphism effects, smooth transitions, and a clean color palette.
- **Dynamic Feed:** Real-time fetching of posts with "Newest First" sorting.
- **Intuitive Creation:** Simple image upload form with file name feedback and loading states.
- **Navigation:** Sticky backdrop-blur navbar for seamless navigation.

### Backend
- **Image Storage:** Integrated with ImageKit for cloud-based image management.
- **Data Persistence:** MongoDB with Mongoose for structured data storage.
- **Validation:** Server-side validation for uploads and captions.
- **Error Handling:** Centralized error-handling middleware for graceful failure reports.
- **Timestamps:** Automatic tracking of post creation times.

## 🛠 Tech Stack

**Frontend:**
- React 19
- Vite
- React Router DOM
- Axios
- Vanilla CSS (Custom Variable System)

**Backend:**
- Node.js
- Express
- MongoDB / Mongoose
- Multer (File Handling)
- ImageKit SDK

---

## 🚀 Getting Started

### Prerequisites
- Node.js (v18+)
- MongoDB (Local or Atlas)
- ImageKit Account

### 1. Backend Setup
1. Navigate to the backend folder:
   ```bash
   cd backend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Create a `.env` file and add your credentials:
   ```env
   IMAGEKIT_PRIVATE_KEY=your_private_key
   # Add any other variables required by your setup
   ```
4. Start the server:
   ```bash
   node server.js
   ```

### 2. Frontend Setup
1. Navigate to the frontend folder:
   ```bash
   cd frontend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start the development server:
   ```bash
   npm run dev
   ```

---

## 📂 Project Structure

```text
Full_stack/
├── backend/
│   ├── src/
│   │   ├── db/          # Database connection
│   │   ├── model/       # Mongoose schemas
│   │   ├── services/    # External integrations (ImageKit)
│   │   └── app.js       # Express routes & logic
│   └── server.js        # Entry point
├── frontend/
│   ├── src/
│   │   ├── components/  # Reusable UI components
│   │   ├── pages/       # Feed and CreatePost views
│   │   ├── App.jsx      # Routing
│   │   └── App.css      # Core styling
└── README.md
```

## 🎨 UI Standards
The project uses a custom CSS variable system defined in `frontend/src/index.css`. This ensures consistent colors, spacing, and border-radii across the entire application.
