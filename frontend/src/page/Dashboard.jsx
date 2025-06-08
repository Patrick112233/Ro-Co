import React, {useEffect, useState} from 'react';
import {FontAwesomeIcon} from '@fortawesome/react-fontawesome';
import 'bootstrap/dist/css/bootstrap.min.css';
import Event from './../component/event.jsx';
import Slider from "react-slick";
import "slick-carousel/slick/slick.css";
import "slick-carousel/slick/slick-theme.css";
import CustomNavbar from './../component/navbar.jsx';
import Question from "../component/question.jsx";
import NewBtn from "../component/newBtn.jsx";
import axios from "axios";





const Dashboard = () => {

    const [questionIDs, setQuestionIDs] = useState([1,2,3,4,5,6,7,8,9,10]);
    const [page, setPage] = useState(0);
    const [hasMore, setHasMore] = useState(true);


    // Dynamically load more questions when scrolling to the bottom
    useEffect(() => {
        const fetchQuestions = async () => {
            const lastId = questionIDs.length > 0 ? questionIDs[questionIDs.length - 1] : 0;
            const newIDs = Array.from({ length: 10 }, (_, index) => lastId + index + 1);
            setQuestionIDs((prevQuestions) => Array.from(new Set([...prevQuestions, ...newIDs])));
            /*
            try {
                const response = await axios.get(`/questions?page=${page}`);
                if (response.data.length === 0) {
                    setHasMore(false); // Keine weiteren Fragen verfügbar
                } else {
                    const newIDs = response.data.ids.map(id => ({id}));
                    setQuestionIDs((prevQuestions) => [...prevQuestions, ...newIDs]);
                }
            } catch (error) {
                console.error('Fehler beim Laden der Fragen:', error);
            }*/
        };

        fetchQuestions();
    }, [page]);

    useEffect(() => {
        const handleScroll = () => {
            const scrollTop = window.scrollY;
            const windowHeight = window.innerHeight;
            const documentHeight = document.documentElement.scrollHeight;

            // Prüfen, ob der Benutzer das Ende der Seite erreicht hat
            if (scrollTop + windowHeight >= documentHeight && hasMore) {
                setPage((prevPage) => prevPage + 1); // Nächste Seite laden
            }
        };

        window.addEventListener('scroll', handleScroll);

        return () => {
            window.removeEventListener('scroll', handleScroll);
        };
    }, [hasMore]);


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

    return (
        <>
            <CustomNavbar/>
            <NewBtn/>
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

            {/*Inner main*/}
            <div>
                    {questionIDs.map((question) => (
                            <Question key={question} id={question} />
                    ))}
                    {!hasMore && <p>Keine weiteren Fragen verfügbar.</p>}
            </div>
            {/*/Inner main*/}

        </>
    );
};

export default Dashboard;