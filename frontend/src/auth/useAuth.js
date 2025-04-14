import { useContext } from 'react';
import AuthContext from './AuthProvider.jsx';

const useAuth = () => {
    return useContext(AuthContext);
};

export default useAuth;