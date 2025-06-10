import React from "react";
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import {BrowserRouter, Routes, Route} from "react-router-dom";
import AuthProvider from 'react-auth-kit';
import createStore from 'react-auth-kit/createStore';
import SignupForm from "@/page/SignupForm.jsx";
import Dashboard from "@/page/Dashboard.jsx";
import 'bootstrap/dist/js/bootstrap.bundle.min.js';
import 'bootstrap/dist/css/bootstrap.min.css';
import '@/custom-bootstrap.scss';
import ProtectedRoutes from "@/auth/ProtectedRoutes.jsx";
import refresh from '@/auth/refresh.js';


const store = createStore({
    authName: '_auth',
    authType: 'cookie',
    cookieDomain: window.location.hostname,
    cookieSecure: true,
    refresh: refresh
});

createRoot(document.getElementById('root')).render(
  <StrictMode>
      <AuthProvider store={store}>
          <BrowserRouter>
              <Routes>
                  <Route element={<ProtectedRoutes/>}>
                      <Route path="/" element={<Dashboard />} />
                  </Route>
                  <Route path="/login" element={<SignupForm />} />
                  <Route path="*" element={<SignupForm />} />
              </Routes>
          </BrowserRouter>
      </AuthProvider>
  </StrictMode>,
)
