import createRefresh from 'react-auth-kit/createRefresh';
import axios from '@/util/axios.js'

/**
 * Get the refresh token from the current stored cookies.
 * Warning: This function depends on the cousen name for the auth store
 * @returns {string|string}
 */
function getRefresh() {
    var b = document.cookie.match("(^|;)\\s*" + "_auth_refresh" + "\\s*=\\s*([^;]+)");
    return b ? b.pop() : "";
}
export { getRefresh };

/**
 * Queries a new JWT access token via the refresh token.
 * @param {object} param
 * @returns {Promise<object>}
 */
export async function refreshApiCallback(param) {
    try {
        const response = await axios.post("auth/refresh", param, {
            headers: { 'Authorization': `Bearer ${param.refreshToken}` }
        });

        return {
            isSuccess: true,
            newAuthToken: response.data?.token,
            newAuthTokenExpireIn: response.data?.tokenExpiresIn,
            newRefreshTokenExpiresIn: response.data?.refreshTokenExpiresIn
        };
    } catch (error) {
        console.error(error);
        return {
            isSuccess: false
        };
    }
}

const refresh = createRefresh({
    interval: 600, // in seconds
    refreshApiCallback
});

export default refresh;
