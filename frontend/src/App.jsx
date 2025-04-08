import React from 'react'
import { Routes, Route } from 'react-router-dom';
import LoginSignupForm from "./components/LoginSignupForm.jsx";
import Dashboard from './components/Dashboard.jsx';


const App = () => {
    return (
        <>
            <Routes>
                <Route path="/" element={<LoginSignupForm />} />
                <Route path="/dashboard" element={<Dashboard />} />
            </Routes>
        </>
    )
}
export default App
