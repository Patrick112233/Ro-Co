import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom';

import 'bootstrap/dist/css/bootstrap.min.css';
import App from './App.jsx'
import { AuthProvider } from './auth/AuthProvider.jsx';

createRoot(document.getElementById('root')).render(
  <StrictMode>
      <BrowserRouter>
      <AuthProvider>
              <App />
        </AuthProvider>
      </BrowserRouter>
  </StrictMode>,
)
