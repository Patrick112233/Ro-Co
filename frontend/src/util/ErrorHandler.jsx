import axios from '../util/axios.js'
import {getRefresh} from "../auth/refresh.js";

/**
 * Handles errors by deleting cookies and force rout to login, also hints API to purge refreshtokens.
 * @param errorMsg {string} erromessage printed to console
 * @param navigate {NavigateFunction} Navigation object for routing
 * @param signOut {function} signout reference fuction from react-auth-kit
 * @param authHeader {function} auth theader for Api requests
 */
const handleErrorLogout = (errorMsg, navigate, signOut, authHeader) => {
    if(typeof errorMsg === 'string' && errorMsg) {
        console.error(errorMsg);
    }

    let token = getRefresh();
        if (token) {
            axios.post(
                '/auth/logout',
                {
                    refreshToken: token
                },
                {
                    headers: {'Content-Type': 'application/json', 'Authorization': authHeader},
                    withCredentials: true,
                    timeout: 3000 // 3 Sekunden
                }
            ).catch((error) => {
                console.error('Fehler beim Logout:', error);
            });
        }
        signOut();
        navigate('/login', {replace: true});
};

export default handleErrorLogout;