import React, {useEffect, useState} from 'react';
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faChevronDown, faChevronUp, faCircleCheck, faTrash} from "@fortawesome/free-solid-svg-icons";
import {motion} from "motion/react";
import axios from "../util/axios.js";
import handleErrorLogout from "../util/ErrorHandler.jsx";
import {useNavigate} from "react-router-dom";
import useSignOut from "react-auth-kit/hooks/useSignOut";
import useAuthHeader from "react-auth-kit/hooks/useAuthHeader";
import useAuthUser from "react-auth-kit/hooks/useAuthUser";
import Answer from "../component/answer.jsx";
import formatTimespan from "../auth/time.js";
import hash from "object-hash";
//import DOMPurify from 'dompurify'; //@TODO XSS protetction!


const Question = ({ id, title, description, createdAt, answered, author, onDelete }) => {
    const navigate = useNavigate();
    const signOut = useSignOut();
    const authHeader = useAuthHeader();
    const auth = useAuthUser()
    const [answeredLocal, setAnsweredLocal] = useState(answered);
    const [isOpen, setIsOpen] = useState(false);
    const [profileImage, setProfileImage] = useState(null);

    const [answers, setAnswers] = useState([]);
    const [comment, setComment] = useState("");
    let currentPage = 0; // pages up to this are actually loaded
    let intervalId; //variable for answare polling

    const postComment = async () => {
        if (!comment.trim()) {
            console.error("Comentary is empty");
            return;
        }

        try {
            const response = await axios.post(
                `/answer`,
                {
                    authorID: auth.uid,
                    questionID: id,
                    description: comment
                },
                {
                    headers: { 'Content-Type': 'application/json', 'Authorization': authHeader },
                    withCredentials: true,
                }
            );

            if (response.status === 201) {
                response.data.hash = hash(response.data)
                setComment(""); // Kommentarfeld leeren
                setAnswers((prevAnswers) => [...prevAnswers, response.data]);
            } else {
                throw new Error("Error on posting comment");
            }
        } catch (error) {
            console.error("Error on posting comment:", error);
            handleErrorLogout(error, navigate, signOut, authHeader);
        }
    };

    useEffect(() => {
        if (isOpen) {
            pollAnswers();
            intervalId = setInterval(pollAnswers, 10000);
        }else {
            clearInterval(intervalId); // Intervall stoppen
        }
        return () => {
            clearInterval(intervalId); // Clearing polling on component unmount
        };
    }, [isOpen]);


    const pollAnswers = async () => {
        let allAnswers = [];
        let localPage = 0;
        let pageSize = 10;

        while (true) {
            try {
                const response = await axios.get(
                    `/answer/all/${id}?page=${localPage}&size=${pageSize}`,
                    {
                        headers: { 'Content-Type': 'application/json', 'Authorization': authHeader },
                        withCredentials: true,
                    }
                );

                if (response.status === 200 && response.data.length > 0) {
                    /*const newAnswers = response.data.filter(answer =>
                        !answers.some(existingAnswer => existingAnswer.id === answer.id)
                    );*/
                    const newAnswers= response.data.map(answer => ({
                        hash: hash(answer),
                        id: answer.id,
                        description: answer.description,
                        createdAt: answer.createdAt,
                        author: {
                            id: answer.author.id,
                            username: answer.author.username,
                        }
                    }));

                    allAnswers = [...allAnswers, ...newAnswers];
                }

                if (response.data.length >= pageSize) {
                    localPage += 1;
                } else {
                    break;
                }
            } catch (error) {
                console.error('Polling answers failed', error);
                handleErrorLogout(error, navigate, signOut, authHeader);
                break;
            }
        }
        setAnswers(allAnswers)
    };


    useEffect(() => {
        //Load autor avatar
        // fetch User Profile Image
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


    const deleteQuestion = async (questionId) => {
        //check if question is my question
        if (author.id !== auth.uid) {
            console.error('You can only change the status of your own questions.');
            return;
        }
        try {
            const response = await axios.delete(`/question/${questionId}`, {
                headers: { 'Authorization': authHeader },
                withCredentials: true,
            });

            if (response.status === 200) {
                onDelete(questionId);
            } else {
                console.error("Error deleting the answer:", error);
            }
        } catch (error) {
            console.error("Error deleting the answer:", error);
            handleErrorLogout(error, navigate, signOut, authHeader);
        }
    }


    // Funktion zum Umschalten des Status und Aktualisieren der REST-API
    const toggleAnswered = async (questionId) => {
        //check if question is my question
        if (author.id !== auth.uid) {
            console.error('You can only change the status of your own questions.');
            return;
        }
        try {
            const response = await axios.put(
                `/question/${questionId}/status`,
                { answered: !answeredLocal },
                {
                    headers: { 'Content-Type': 'application/json', 'Authorization': authHeader },
                    withCredentials: true,
                }
            );

            if (response.status === 200) {
                setAnsweredLocal(response.data.answered);
            } else {
                throw new Error('Fehler beim Aktualisieren des Status');
            }
        } catch (error) {
            console.error('Fehler beim Umschalten des Status:', error);
            handleErrorLogout(error, navigate, signOut, authHeader);
        }
    };

    const handleDeleteAnswer = (answerId) => {
        pollAnswers()
        //setAnswers((prevAnswers) => prevAnswers.filter((answer) => answer.id !== answerId));
    };

    return (<>
    <div className="px-2 py-1">
        <div className="card mb-2">
            <motion.div className="card-body p-2 p-sm-3 ">
                <div className="d-flex align-items-center">
                    <div className="d-flex align-items-center">
                        <a><img
                            src={profileImage}
                            alt="User Avatar"
                            className="rounded-circle"
                            style={{ width: '40px', height: '40px' }}
                        /></a>
                        <div className="d-flex flex-column align-items-bottom ps-3">
                            <p className="mb-0">
                                <a>{author.username}</a> Asked
                                <span
                                    className="text-secondary font-weight-bold"> {formatTimespan(createdAt)} ago</span>
                            </p>
                            <h4 className="pt-1 mb-1">{title}</h4>
                            { /*<div className="d-flex flex-wrap mb-1">
                                            <span className="badge me-2"
                                                  style={{
                                                      backgroundColor: '#1735CEFF', color: '#fff'
                                                  }}>Informatics</span>
                                <span className="badge me-2"
                                      style={{backgroundColor: '#BF6E06FF', color: '#fff'}}>Database</span>
                                <span className="badge me-2"
                                      style={{backgroundColor: '#095705FF', color: '#fff'}}>Real-Time</span>
                            </div>*/}
                        </div>
                    </div>
                </div>
                <div className="mx-lg-2">
                    <p className="text-secondary mb-2">
                        {description}
                    </p>
                </div>
                <div className="d-flex justify-content-center">
                    <button className="btn btn-link p-0" onClick={() => setIsOpen(!isOpen)}>
                        {isOpen ? (<FontAwesomeIcon icon={faChevronUp}/>) : (<FontAwesomeIcon icon={faChevronDown}/>)}
                    </button>
                </div>
                <div className="position-absolute top-0 end-0 mt-2">
                    { (answered || (author.id === auth.uid)) && (
                            <button
                            className="btn btn-link mr-2"
                            type="button"
                            onClick={() => toggleAnswered(id)}
                            >
                                <FontAwesomeIcon
                                    icon={faCircleCheck}
                                    style={{
                                        fontSize: '2rem',
                                        color: answeredLocal ? 'green' : 'gray',
                                    }}
                                />
                            </button>
                    )}
                    { author.id === auth.uid && (
                        <button
                            className="btn btn-link ml-1"
                            type="button"
                            onClick={() => deleteQuestion(id)}
                        >
                            <FontAwesomeIcon
                                icon={faTrash}
                                style={{
                                    fontSize: '1.3rem',
                                    color: 'gray',
                                }}
                            />
                        </button>
                    )}
                </div>
            </motion.div> {isOpen && (
                <motion.div>
                    <div className="card-footer py-3 border-0">
                        {answers.map((answer) => (
                            <Answer key={answer.hash} id={answer.id} author={answer.author} createdAt={answer.createdAt} description={answer.description} questionID={answer.questionId} onDelete={handleDeleteAnswer}/>
                        ))}

                        {/*Post*/}
                        <div className="d-flex flex-start w-100 pt-3">
                            <div className="form-outline w-100">
                                <textarea
                                    className="form-control border-dark-subtle"
                                    id="textAreaExample"
                                    rows="4"
                                    style={{ background: '#fff' }}
                                    value={comment}
                                    onChange={(e) => setComment(e.target.value)}
                                    onKeyDown={(e) => {
                                        if (e.key === 'Enter') {
                                            if (e.altKey) {
                                                // Alt+Enter erlaubt Zeilenumbruch
                                                return;
                                            }
                                            e.preventDefault(); // Verhindert Zeilenumbruch
                                            postComment(); // Kommentar posten
                                        }
                                    }}
                                ></textarea>
                            </div>
                        </div>
                        <div className="mt-2 pt-1 d-flex justify-content-end">
                            <button
                                type="button"
                                className="me-2 btn btn-primary btn-sm text-white"
                                onClick={postComment}>
                                Post comment
                            </button>
                        </div>
                    </div>
                </motion.div>
                )}
        </div>
    </div>
</>);
}




export default Question;