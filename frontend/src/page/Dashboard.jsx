import React, {useEffect, useState} from 'react';
import {FontAwesomeIcon} from '@fortawesome/react-fontawesome';
import 'bootstrap/dist/css/bootstrap.min.css';
import Event from './../component/event.jsx';
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
import useAuthUser from "react-auth-kit/hooks/useAuthUser";


const Dashboard = () => {
    let prevPage = -1;
    const navigate = useNavigate();
    const signOut = useSignOut();
    const authHeader = useAuthHeader();
    const [questions, setQuestions] = useState([]);

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
            setQuestions([...allLoadedQuestions.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))]); // Neue Referenz erstellen
        } catch (error) {
            console.error('Fehler beim Laden der Fragen:', error);
        }
    };


    useEffect(() => {
        // Initialer Aufruf von fetchQuestions
        fetchQuestions();
        // Auto polling 10s
        const intervalId = setInterval(() => {
            fetchQuestions();
        }, 10000);

        // Bereinigung des Intervalls beim Verlassen der Komponente
        return () => {
            clearInterval(intervalId);
        };
    }, []);



    const handleNewQuestion = async (newQuestion) => {
        fetchQuestions();
    };

    const handleDeleteQuestion = (questionId) => {
        fetchQuestions();
    };

    /*
    const events = [
        {id: 1},
        {id: 1}
    ];


    const getSlidesToShow = (num) => {
        return Math.min(num, events.length)
    };


    const sliderSettings = {
        dots: false,
        infinite: true,
        speed: 500,
        slidesToShow: getSlidesToShow(5),
        slidesToScroll: 1,
        autoplay: true,
        autoplaySpeed: 5000,
        cssEase: "linear",
        centerMode: true,
        arrows: true,
        responsive: [
            {
                breakpoint: 1850, // or less
                settings: {
                    slidesToShow: getSlidesToShow(4),
                    slidesToScroll: 1,
                },
            },
            {
                breakpoint: 1500, // or less
                settings: {
                    slidesToShow: getSlidesToShow(3),
                    slidesToScroll: 1,
                },
            },
            {
                breakpoint: 1150, // or less
                settings: {
                    slidesToShow: getSlidesToShow(2),
                    slidesToScroll: 1,
                },
            },
            {
                breakpoint: 800,  // or less
                settings: {
                    slidesToShow: 1,
                    slidesToScroll: 1,
                    centerMode: false,
                    arrows: false,
                },
            },

        ]
    }
*/

    return (
        <>
            <CustomNavbar/>

            <AskQuestion refreshHook={handleNewQuestion} />
            {/*Inner main*
            <div className="m-auto justify-content-center" style={{width: '90vw'}}>
                <div className="mt-5">
                    <Slider {...sliderSettings}>
                        {
                            events.map((event, index) => (
                                <Event key={index} eventID={event.id}/>
                            ))
                        }
                    </Slider>
                </div>
            </div>
            <hr className="mx-5 my-3"/>
            */}

            {/*Inner main*/}
            <ReactPullToRefresh onRefresh={fetchQuestions}>
            {questions.map((question) => (
                            <Question key={question.hash} id={question.id} title={question.title} description={question.description} createdAt={question.createdAt} answered={question.answered} author={question.author} onDelete={handleDeleteQuestion} />
                    ))}
                    {questions.length === 0 &&  <h2 className="align-content-center text-center">No questions available</h2>}
            </ReactPullToRefresh>
            {/*/Inner main*/}

        </>
    );
};

export default Dashboard;