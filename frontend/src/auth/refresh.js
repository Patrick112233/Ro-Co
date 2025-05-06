import createRefresh from 'react-auth-kit/createRefresh';
import axios from '../api/axios.js'


const refresh = createRefresh({
    interval: 10, //in seconds
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
