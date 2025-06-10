import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faArrowRightFromBracket} from "@fortawesome/free-solid-svg-icons";
import React, {useEffect, useState} from "react";
import axios from "@/util/axios.js";
import {useNavigate} from "react-router-dom";
import useSignOut from 'react-auth-kit/hooks/useSignOut'
import useAuthHeader from 'react-auth-kit/hooks/useAuthHeader';
import useAuthUser from "react-auth-kit/hooks/useAuthUser";
import handleErrorLogout from "@/util/ErrorHandler.jsx";

/**
 * Makes the Nav-Bar on top  of the page.
 * @returns {Element}
 * @constructor
 */
const CustomNavbar = () => {
    const navigate = useNavigate();
    const signOut = useSignOut();
    const authHeader = useAuthHeader();
    const auth = useAuthUser()

    const [userName, setUserName] = useState('');
    const [profileImage, setProfileImage] = useState(null);

    /**
     * Initialy fetches username and profile image
     */
    useEffect(() => {
        //get general user data
        axios.get(
            '/user/' + auth.uid,
            {
                headers: {'Content-Type': 'application/json',  'Authorization': authHeader},
                withCredentials: true
            }
            ).then((response) => {
                if (response.status !== 200) {
                    throw new Error('Cant fetch user data');
                }
                setUserName(response.data?.username);
            }).catch( (error) => {
                handleErrorLogout(error, navigate, signOut, authHeader);
            });

        //get profile image
        axios.get(
            '/user/' + auth.uid + '/icon',
            {
                    headers: {'Content-Type': 'application/json',  'Authorization': authHeader},
                    withCredentials: true
                }
            ).then((response) => {
                if (response.status !== 200) {
                    throw new Error('Cant fetch user data');
                }
                const blob = new Blob([response.data], { type: 'image/svg+xml' });
                const localUrl = URL.createObjectURL(blob);
                setProfileImage(localUrl);
            }).catch( (error) => {
                handleErrorLogout(error, navigate, signOut, authHeader);
            });

    }, [authHeader, auth.uid ]);

    /**
     * logs user out (deletes cookies + rout to login)
     */
    const handleLogoutClick = () => {
        handleErrorLogout('',navigate, signOut, authHeader);
    };


    return (
        <>
            <nav className="navbar navbar-light bg-primary justify-content-between w-100">
                <div className="d-flex justify-content-start btn-group ms-2">
                        <img
                            src={profileImage}
                            alt="User Avatar"
                            className="rounded-circle"
                            style={{ width: '40px', height: '40px' }}
                        />
                    <a className="navbar-brand text-white fw-bold ms-2">{userName}</a>
                </div>
                    <div className="d-flex justify-content-end btn-group">
                        <button className="btn me-2" type="button" onClick={handleLogoutClick}>
                            <FontAwesomeIcon icon={faArrowRightFromBracket} className="text-white fs-4" />
                        </button>
                    </div>
            </nav>
        </>
    );
};

export default CustomNavbar;