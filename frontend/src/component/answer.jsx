import React, {useEffect, useState} from "react";
import formatTimespan from "../auth/time.js";
import axios from "../util/axios.js";
import handleErrorLogout from "../util/ErrorHandler.jsx";
import {useNavigate} from "react-router-dom";
import useSignOut from "react-auth-kit/hooks/useSignOut";
import useAuthHeader from "react-auth-kit/hooks/useAuthHeader";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faTrash} from "@fortawesome/free-solid-svg-icons";
import useAuthUser from "react-auth-kit/hooks/useAuthUser";

//import DOMPurify from 'dompurify'; //@TODO XSS protetction!

const Answer = ({ id, description, createdAt, author, questionID , onDelete}) => {
    const navigate = useNavigate();
    const signOut = useSignOut();
    const authHeader = useAuthHeader();
    const auth = useAuthUser()


    const [profileImage, setProfileImage] = useState(null);


    useEffect(() => {
        //Load autor avatar
        axios.get(
            '/user/' + author.id + '/icon',
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

    }, [author]);


    const deleteAnswer = async (answerId) => {
        try {
            const response = await axios.delete(`/answer/${answerId}`, {
                headers: { 'Authorization': authHeader },
                withCredentials: true,
            });

            if (response.status === 200) {
                onDelete(answerId);
            } else {
                console.error("Error deleting the answer:", error);
            }
        } catch (error) {
            console.error("Error deleting the answer:", error);
            handleErrorLogout(error, navigate, signOut, authHeader);
        }
    };

    return (
        <>
            <div className="card-body w-100 p-3 mt-2 mb-2 bg-light border border-dark-subtle rounded">
                <div className="d-flex justify-content-between align-items-start">
                    <div className="d-flex flex-start ">
                        <img
                            src={profileImage}
                            alt="User Avatar"
                            className="rounded-circle"
                            style={{ width: '40px', height: '40px' }}
                        />
                        <div className="mx-2">
                            <div className="d-flex align-items-center mb-3">
                                <div>
                                    <h6 className="fw-bold mb-0">{author.username}</h6>
                                    <p className="mb-0">{formatTimespan(createdAt)} ago</p>
                                </div>
                            </div>
                        </div>
                    </div>
                    { auth.uid === author.id &&(
                        <button
                            className="btn btn-link position-relative "
                            type="button"
                            onClick={() => deleteAnswer(id)}
                        >
                            <FontAwesomeIcon icon={faTrash} style={{ fontSize: '1rem', color: 'gray' }} />
                        </button>
                        )
                    }
                </div>

                <p className="mb-1">
                    {description}
                </p>

            </div>

        </>
    );
}

export default Answer;