import React from 'react'
import { Routes, Route } from 'react-router-dom';
import SignupForm from "./page/SignupForm.jsx";
import Dashboard from './page/Dashboard.jsx';
import ProtectedRoutes from './auth/ProtectedRoutes.jsx';


const ROLES = {
    'User': "USER",
    'Admin': "ADMIN"
}

const App = () => {
    return (
            <Routes>
                <Route path="/login" element={<SignupForm />} />
                <Route element={<ProtectedRoutes/>}>
                    <Route path="/" element={<Dashboard />} />
                </Route>
                <Route path="*" element={<SignupForm />} />
            </Routes>
    )
}
export default App
