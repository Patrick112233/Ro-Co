import React from "react";
import {Navigate, Outlet, useLocation} from "react-router-dom";
import useIsAuthenticated from 'react-auth-kit/hooks/useIsAuthenticated'


/**
 * Implements a protekted rout element to ensure authentification is valid when a rout is changed.
 * @returns {JSX.Element}
 */
const ProtectedRoutes = () => {
    const isAuthenticated = useIsAuthenticated()
    const location = useLocation();

    if (isAuthenticated) {
        return <Outlet state={{ from: location }} replace/>
    } else {
        return <Navigate to="/login" state={{ from: location }} replace />
    }

}

export default ProtectedRoutes

