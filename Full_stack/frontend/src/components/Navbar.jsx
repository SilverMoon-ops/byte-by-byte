import React from 'react';
import { Link, useLocation } from 'react-router-dom';

const Navbar = () => {
  const location = useLocation();

  return (
    <nav className="navbar">
      <Link to="/feed" className="nav-logo">
        ByteSocial
      </Link>
      <div className="nav-links">
        <Link 
          to="/feed" 
          className={`nav-link ${location.pathname === '/feed' ? 'active' : ''}`}
        >
          Feed
        </Link>
        <Link 
          to="/create-post" 
          className={`nav-link ${location.pathname === '/create-post' ? 'active' : ''}`}
        >
          Create Post
        </Link>
      </div>
    </nav>
  );
};

export default Navbar;
