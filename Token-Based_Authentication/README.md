# Token-Based Authentication

A learning project implementing token-based authentication using Node.js, Express, MongoDB, and JSON Web Tokens (JWT).

## Features

- User Registration
- JWT-based authentication using cookies
- Protected routes

## Technologies Used

- **Node.js**: JavaScript runtime
- **Express.js**: Web framework for Node.js
- **MongoDB**: NoSQL database
- **Mongoose**: ODM for MongoDB
- **JSON Web Tokens (JWT)**: For secure authentication
- **Cookie-Parser**: For handling browser cookies
- **Dotenv**: For environment variable management

## Prerequisites

- [Node.js](https://nodejs.org/) installed
- [MongoDB](https://www.mongodb.com/) installed and running

## Installation

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd Token-Based_Authentication
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Create a `.env` file in the root directory and add the following:
   ```env
   MONGO_URI=mongodb://localhost:27017/token-auth
   JWT_SECRET=your_super_secret_key
   ```

## Running the Application

Start the server:
```bash
node server.js
```
The server will start on `http://localhost:3000`.

## API Endpoints

### Authentication
- `POST /api/auth/register`: Register a new user and receive a JWT cookie.

### Posts (Protected)
- `POST /api/posts/create`: Create a post. Requires a valid JWT in the `token` cookie.

## Project Structure

```text
├── src/
│   ├── controllers/   # Route logic
│   ├── db/            # Database connection logic
│   ├── models/        # Mongoose schemas
│   ├── routes/        # Express routes
│   └── app.js         # Express app configuration
├── server.js          # Entry point
├── .env               # Environment variables (gitignored)
├── .gitignore         # Files to ignore in Git
└── package.json       # Dependencies and scripts
```

## License

This project is licensed under the ISC License.
