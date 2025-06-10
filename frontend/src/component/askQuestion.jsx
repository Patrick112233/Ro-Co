import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faPencil} from "@fortawesome/free-solid-svg-icons";
import React, {useState} from "react";
import Popup from "@/component/popup.jsx";
import {useNavigate} from "react-router-dom";
import useSignOut from "react-auth-kit/hooks/useSignOut";
import useAuthHeader from "react-auth-kit/hooks/useAuthHeader";
import useAuthUser from "react-auth-kit/hooks/useAuthUser";
import axios from "@/util/axios.js";


/**
 *
 * @param refreshHook
 * @returns {Element}
 * @constructor
 */
const AskQuestion = (refreshHook) => {
    const navigate = useNavigate();
    const authHeader = useAuthHeader();
    const auth = useAuthUser()
    const [askPopUp, setAskPopUp] = useState(false)
    const [title, setTitle] = useState("")
    const [description, setDescription] = useState("")


    const onPost = async () => {
        const authorId = auth.uid;

        const payload = {
            title: title,
            description: description,
            authorId: authorId,
        };
        try {
            const response = await axios.post(
                "/question",
                payload,
                {
                    headers: {'Content-Type': 'application/json',  'Authorization': authHeader},
                    withCredentials: true

                });

            if (response.status === 201) {
                const newQuestions = {
                    id: response.data.id,
                    title: response.data.title,
                    description: response.data.description,
                    createdAt: response.data.createdAt,
                    answered: response.data.answered,
                    author: {
                        id: response.data.author.id,
                        username: response.data.author.username,
                    }
                };
                setAskPopUp(false);
                refreshHook.refreshHook(newQuestions);
            } else {
                console.error("Fehler beim Posten der Frage:", response.status);
            }
        } catch (error) {
            console.error("Fehler beim Posten der Frage:", error);
        }
    };

    return (
        <>
            <div className="dropstart position-fixed bottom-0 end-0" style={{zIndex: 100, marginRight: '5VW', marginBottom: '70px'}}>
                <button type="button" className="rounded-circle btn btn-primary" onClick={() => setAskPopUp(true)}>
                    <FontAwesomeIcon icon={faPencil} className="text-white p-2 fs-4"/>
                </button>
            </div>
            <Popup trigger={askPopUp} setTrigger={setAskPopUp}>
                <h2>Ask your Question</h2>
                <div className="form-outline w-100">
                    <h5 className="mt-2 mb-2">Subject:</h5>
                    <input
                        className="form-control border-dark-subtle"
                        id="subject"
                        style={{ background: '#fff' }}
                        value={title}
                        onChange={(e) => setTitle(e.target.value)}

                    />
                    <h5 className="mt-2 mb-2">Question:</h5>
                    <textarea
                        className="form-control border-dark-subtle flex-grow-1"
                        id="question"
                        style={{ background: '#fff', resize: 'none' }}
                        value={description}
                        onChange={(e) => setDescription(e.target.value)}
                    />
                </div>
                <div className="mt-3 d-flex justify-content-end">
                    <button className="btn btn-primary text-white" onClick={onPost} style={{ width: '70px', marginRight: '5px'}}>
                        Post
                    </button>
                    <button className="btn btn-secondary" onClick={() => setAskPopUp(false)} style={{ width: '70px', marginLeft: '5px' }}>
                        Close
                    </button>

                </div>
            </Popup>
        </>
    );
}

export default AskQuestion;