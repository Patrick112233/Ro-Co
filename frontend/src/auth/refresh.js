import createRefresh from 'react-auth-kit/createRefresh';
import axios from '../util/axios.js'

function getRefresh() {
    var b = document.cookie.match("(^|;)\\s*" + "_auth_refresh" + "\\s*=\\s*([^;]+)");
    return b ? b.pop() : "";
}
export { getRefresh };

const refresh = createRefresh({
    interval: 600, //in seconds
    refreshApiCallback: async (param) => {
        try {
            const response = await axios.post("auth/refresh", param, {
                headers: {'Authorization': `Bearer ${param.refreshToken}`}
            })

            return {
                isSuccess: true,
                newAuthToken: response.data?.token,
                newAuthTokenExpireIn: response.data?.tokenExpiresIn,
                newRefreshTokenExpiresIn: response.data?.refreshTokenExpiresIn
            }
        }
        catch(error){
            console.error(error)
            return {
                isSuccess: false
            }
        }
    }
})

export default refresh;
