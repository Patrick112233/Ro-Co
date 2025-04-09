import React from 'react'
import { Routes, Route } from 'react-router-dom';
import SignupForm from "./page/SignupForm.jsx";
import Dashboard from './page/Dashboard.jsx';


const App = () => {
    return (
        <>
            <Routes>
                <Route path="/dashboard" element={<Dashboard />} />
                <Route path="/signup" element={<SignupForm />} />
                <Route path="/" element={<SignupForm />} />
                <Route path="*" element={<SignupForm />} />
            </Routes>
        </>
    )
}
export default App
