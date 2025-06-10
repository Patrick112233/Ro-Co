import React, {useEffect, useState} from 'react';
import 'bootstrap/dist/css/bootstrap.min.css';
import Slider from "react-slick";
import "slick-carousel/slick/slick.css";
import "slick-carousel/slick/slick-theme.css";
import CustomNavbar from './../component/navbar.jsx';
import Question from "../component/question.jsx";
import AskQuestion from "../component/askQuestion.jsx";
import axios from "../util/axios.js";
import handleErrorLogout from "../util/ErrorHandler.jsx";
import {useNavigate} from "react-router-dom";
import useSignOut from "react-auth-kit/hooks/useSignOut";
import useAuthHeader from "react-auth-kit/hooks/useAuthHeader";
import ReactPullToRefresh from 'react-pull-to-refresh';
import hash from 'object-hash';


const Dashboard = () => {
    const navigate = useNavigate();
    const signOut = useSignOut();
    const authHeader = useAuthHeader();
    const [questions, setQuestions] = useState([]);

    /**
     * Polls questions in a 10s intervall
     */
    useEffect(() => {
        fetchQuestions();
        const intervalId = setInterval(() => {
            fetchQuestions();
        }, 10000);
        return () => {
            clearInterval(intervalId);
        };
    }, []);

    /**
     * Refresh when a new question was created.
     * @param newQuestion new Question Json Object
     * @returns {Promise<void>}
     */
    const handleNewQuestion = async (newQuestion) => {
        fetchQuestions();
    };

    /**
     * Refresh when a question was deleted
     * @param questionId
     */
    const handleDeleteQuestion = (questionId) => {
        fetchQuestions();
    };


    /**
     * Fetches to most recent 100 Questions from the Rest API.
     * @returns {Promise<void>}
     */
    const fetchQuestions = async () => {
        let size = 10;
        let pages = 10;

        try {
            let allLoadedQuestions = [];
            for (let i = 0; i <= pages; i++) {
                const response = await axios.get(
                    `/question/all?page=${i}&size=${size}`,
                    {
                        headers: {'Content-Type': 'application/json',  'Authorization': authHeader},
                        withCredentials: true
                    }
                );

                if(response.status !== 200) {
                    handleErrorLogout('',navigate, signOut, authHeader);
                }
                const newQuestions = response.data.map(question => ({
                    hash: hash(question),
                    id: question.id,
                    title: question.title,
                    description: question.description,
                    createdAt: question.createdAt,
                    answered: question.answered,
                    author: {
                        id: question.author.id,
                        username: question.author.username,
                    }
                }));
                allLoadedQuestions = [...allLoadedQuestions, ...newQuestions];
            }
            //set Questions array and sort it (newer first)
            setQuestions([...allLoadedQuestions.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))]); // Neue Referenz erstellen
        } catch (error) {
            console.error('Fehler beim Laden der Fragen:', error);
        }
    };


    return (
        <>
            <CustomNavbar/>
            <AskQuestion refreshHook={handleNewQuestion} />
            <ReactPullToRefresh onRefresh={fetchQuestions}>
            {questions.map((question) => (
                            <Question key={question.hash} id={question.id} title={question.title} description={question.description} createdAt={question.createdAt} answered={question.answered} author={question.author} onDelete={handleDeleteQuestion} />
                    ))}
                    {questions.length === 0 &&  <h2 className="align-content-center text-center">No questions available</h2>}
            </ReactPullToRefresh>
        </>
    );
};

export default Dashboard;