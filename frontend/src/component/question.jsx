import React, {useEffect, useState} from 'react';
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faChevronDown, faChevronUp, faCircleCheck, faTrash} from "@fortawesome/free-solid-svg-icons";
import {motion} from "motion/react";
import axios from "@/util/axios.js";
import handleErrorLogout from "@/util/ErrorHandler.jsx";
import {useNavigate} from "react-router-dom";
import useSignOut from "react-auth-kit/hooks/useSignOut";
import useAuthHeader from "react-auth-kit/hooks/useAuthHeader";
import useAuthUser from "react-auth-kit/hooks/useAuthUser";
import Answer from "@/component/answer.jsx";
import formatTimespan from "@/util/time.js";
import hash from "object-hash";

/**
 * The Question object represents a User question
 * @param id question ID
 * @param title subject of question
 * @param description detailed question
 * @param createdAt {String} ISO 8601 timestamp UTC
 * @param answered {boolean} marks if question was answered
 * @param author {object} autor object containing id and username
 * @param onDelete hook to triggere when component is deleted
 * @returns {Element}
 */
const Question = ({ id, title, description, createdAt, answered, author, onDelete }) => {
    const navigate = useNavigate();
    const signOut = useSignOut();
    const authHeader = useAuthHeader();
    const auth = useAuthUser()

    const [isAnswered, setIsAnswered] = useState(answered);
    const [isOpen, setIsOpen] = useState(false);
    const [profileImage, setProfileImage] = useState(null);
    const [answers, setAnswers] = useState([]);
    const [comment, setComment] = useState("");
    let answerFetchIntervalId;

    /**
     * Fetches answers initially and then polls them in a 10s interwall when the answer section is extracted.
     */
    useEffect(() => {
        if (isOpen) {
            //initial fetch + Polling
            fetchAnswers();
            answerFetchIntervalId = setInterval(fetchAnswers, 10000); //10s
        }else {
            //stop polling if subsection was closed
            clearInterval(answerFetchIntervalId);
        }
        return () => {
            //clear polling if not rendered
            clearInterval(answerFetchIntervalId);
        };
    }, [isOpen]);

    /**
     * Post loads the user avatar as soon as the componen get rendered.
     * Warnig: does not check for avatar changes as this is not intendet.
     */
    useEffect(() => {
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
            //extract image as a server resource via url.
            const blob = new Blob([response.data], { type: 'image/svg+xml' });
            const localUrl = URL.createObjectURL(blob);
            setProfileImage(localUrl);
        }).catch( (error) => {
            handleErrorLogout(error, navigate, signOut, authHeader);
        });

    }, [author]);

    /**
     * Handels hook when an answere is delted.
     * @param answerId
     */
    const handleDeleteAnswer = (answerId) => {
        fetchAnswers()
    };

    /**
     * Takes the answer from the input field, and post it to the Rest API and also add it to local UI.
     * @returns {Promise<void>}
     */
    const postAnswer = async () => {
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
                //clear field
                setComment("");
                // add to local gui
                response.data.hash = hash(response.data)
                setAnswers((prevAnswers) => [...prevAnswers, response.data]);
            } else {
                throw new Error("Error on posting comment");
            }
        } catch (error) {
            handleErrorLogout(error, navigate, signOut, authHeader);
        }
    };

    /**
     * Fetch all answers of the question.
     * @returns {Promise<void>}
     */
    const fetchAnswers = async () => {
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
                    //using hash as key to ensure that object get rerendered
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
                    //collect data
                    allAnswers = [...allAnswers, ...newAnswers];
                }

                //Check for next page
                if (response.data.length >= pageSize) {
                    localPage += 1;
                } else {
                    break;
                }
            } catch (error) {
                handleErrorLogout(error, navigate, signOut, authHeader);
                break;
            }
        }
        setAnswers(allAnswers)
    };


    /**
     * Deletes a question from the Rest API, and calls onDelete to refresh UI.
     * @param questionId {String}
     * @returns {Promise<void>}
     */
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
                //call hook from Dashboard to refresh UI
                onDelete(questionId);
            } else {
                throw new Error("Wrong response code");
            }
        } catch (error) {
            console.error("Error deleting the answer:", error);
            handleErrorLogout(error, navigate, signOut, authHeader);
        }
    }


    // Funktion zum Umschalten des Status und Aktualisieren der REST-API
    /**
     * Signalm Rest API that a question is marked as answered. Ounly allow own questions to be marked.
     * @param questionId
     * @returns {Promise<void>}
     */
    const toggleAnswered = async (questionId) => {
        //check if it is own question
        if (author.id !== auth.uid) {
            console.error('You can only change the status of your own questions.');
            return;
        }
        try {
            const response = await axios.put(
                `/question/${questionId}/status`,
                { answered: !isAnswered },
                {
                    headers: { 'Content-Type': 'application/json', 'Authorization': authHeader },
                    withCredentials: true,
                }
            );

            if (response.status === 200) {
                setIsAnswered(response.data.answered);
            } else {
                throw new Error('Fehler beim Aktualisieren des Status');
            }
        } catch (error) {
            handleErrorLogout(error, navigate, signOut, authHeader);
        }
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
                        </div>
                    </div>
                </div>
                <div className="mx-lg-2">
                    <p className="text-secondary mb-2">
                        {description}
                    </p>
                </div>
                <div className="d-flex justify-content-center">
                    <button className="btn btn-link p-0" onClick={() => setIsOpen(!isOpen)} data-testid="OpenAnswerButton">
                        {isOpen ? (<FontAwesomeIcon icon={faChevronUp}/>) : (<FontAwesomeIcon icon={faChevronDown}/>)}
                    </button>
                </div>
                <div className="position-absolute top-0 end-0 mt-2">
                    { (answered || (author.id === auth.uid)) && (
                            <button
                            className="btn btn-link mr-2"
                            type="button"
                            data-testid="ToggleAnsweredButton"
                            onClick={() => toggleAnswered(id)}
                            >
                                <FontAwesomeIcon
                                    icon={faCircleCheck}
                                    style={{
                                        fontSize: '2rem',
                                        color: isAnswered ? 'green' : 'gray',
                                    }}
                                />
                            </button>
                    )}
                    { author.id === auth.uid && (
                        <button
                            className="btn btn-link ml-1"
                            type="button"
                            data-testid="DeleteQuestionButton"
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
                                    data-testid="comment"
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
                                            postAnswer(); // Kommentar posten
                                        }
                                    }}
                                ></textarea>
                            </div>
                        </div>
                        <div className="mt-2 pt-1 d-flex justify-content-end">
                            <button
                                type="button"
                                className="me-2 btn btn-primary btn-sm text-white"
                                onClick={postAnswer}>
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