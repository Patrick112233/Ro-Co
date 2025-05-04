import {Navigate, Outlet, useLocation} from "react-router-dom";
import useIsAuthenticated from 'react-auth-kit/hooks/useIsAuthenticated'


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

