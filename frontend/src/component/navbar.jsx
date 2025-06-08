import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faArrowRightFromBracket, faUser} from "@fortawesome/free-solid-svg-icons";
import React, {useEffect, useState} from "react";
import axios from "../util/axios.js";
import {useNavigate} from "react-router-dom";
import useSignOut from 'react-auth-kit/hooks/useSignOut'
import useAuthHeader from 'react-auth-kit/hooks/useAuthHeader';
import useAuthUser from "react-auth-kit/hooks/useAuthUser";
import handleErrorLogout from "../util/ErrorHandler.jsx";

const CustomNavbar = () => {
    const navigate = useNavigate();
    const signOut = useSignOut();
    const authHeader = useAuthHeader();
    const auth = useAuthUser()
    const [user, setUser] = useState('');
    const [profileImage, setProfileImage] = useState(null);

    useEffect(() => {
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
                setUser(response.data?.username);
            }).catch( (error) => {
                handleErrorLogout(error, navigate, signOut, authHeader);
            });

            // fetch User Profile Image
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



    const handleLogoutClick = () => {
        handleErrorLogout('',navigate, signOut, authHeader);
    };

    /*const mobileBtnCount = 3; // Anzahl der Buttons

    const buttonStyle = {
        width: `calc((100% / ${mobileBtnCount}) - 10px)`, // 10px = 5px Padding auf beiden Seiten
        padding: '5px',
        boxSizing: 'border-box',
    };*/

    return (
        <>
            {/* Desktop Navbar */}
            <nav className="navbar navbar-light bg-primary justify-content-between w-100">
                <div className="d-flex justify-content-start btn-group ms-2">
                        <img
                            src={profileImage}
                            alt="User Avatar"
                            className="rounded-circle"
                            style={{ width: '40px', height: '40px' }}
                        />
                        {/*'/src/assets/default_avatar.svg'/>*/}
                    <a className="navbar-brand text-white fw-bold ms-2">{user}</a>
                </div>
                    <div className="d-flex justify-content-end btn-group">
                        <button className="btn me-2" type="button" onClick={handleLogoutClick}>
                            <FontAwesomeIcon icon={faArrowRightFromBracket} className="text-white fs-4" />
                        </button>
                    </div>

            </nav>

            {/* Mobile Navbar Cuurently not supported!
            <nav className="navbar navbar-light bg-primary justify-content-around w-100 fixed-bottom d-md-none">
                <button className="btn" type="button" style={buttonStyle}>
                    <FontAwesomeIcon icon={faUser} className="text-white fs-4"/>
                </button>
                <button className="btn" type="button" style={buttonStyle}>
                    <FontAwesomeIcon icon={faComment} className="text-white fs-4" />
                </button>
                <button className="btn" type="button" style={buttonStyle}>
                    <FontAwesomeIcon icon={faCalendar} className="text-white fs-4"/>
                </button>
            </nav>
            */}
        </>
    );
};

export default CustomNavbar;