import {Navigate, Outlet, useLocation} from "react-router-dom";
import { jwtDecode } from "jwt-decode";
import useAuth from "./useAuth.js";

const ProtectedRoutes = () => {
    const {auth} = useAuth();
    const location = useLocation();
    let allowAccess = false;
    if (!auth?.accessToken) {
        allowAccess = false;
    } else {
        const decodedToken = jwtDecode(auth.accessToken);
        const currentDate = new Date();
        allowAccess = decodedToken.exp * 1000 > currentDate.getTime();
    }

    if (allowAccess) {
        return <Outlet state={{ from: location }} replace/>
    } else {
        return <Navigate to="/login" state={{ from: location }} replace />
    }

}

export default ProtectedRoutes