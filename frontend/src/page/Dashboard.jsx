import React from 'react';
import {FontAwesomeIcon} from '@fortawesome/react-fontawesome';
import {faChevronDown} from '@fortawesome/free-solid-svg-icons';
import 'bootstrap/dist/css/bootstrap.min.css';
import Event from './../component/event.jsx';

import Slider from "react-slick";
import "slick-carousel/slick/slick.css";
import "slick-carousel/slick/slick-theme.css";
import CustomNavbar from './../component/navbar.jsx';
import AddBtn from "../component/addBtn.jsx";
import Question from "../component/question.jsx";


const Dashboard = () => {
    const events = [
        {id: 1},
        {id: 1},
        {id: 1},
        {id: 1},
        {id: 1},
        {id: 1},
        {id: 1},
        {id: 1},
        {id: 1},
        {id: 1},
        {id: 1},
        {id: 1},
        {id: 1},
        {id: 2}
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
            <AddBtn/>
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
                <Question/>
                <Question/>

                <div className="d-flex justify-content-center">
                    <ul className="pagination">
                            <li className="page-item">
                                <a className="page-link" href="#">«</a>
                            </li>
                            <li className="page-item active">
                                <a className="page-link" href="#">1</a>
                            </li>
                            <li className="page-item">
                                <a className="page-link" href="#">2</a>
                            </li>
                            <li className="page-item">
                                <a className="page-link" href="#">3</a>
                            </li>
                            <li className="page-item">
                                <a className="page-link" href="#">4</a>
                            </li>
                            <li className="page-item">
                                <a className="page-link" href="#">5</a>
                            </li>
                            <li className="page-item">
                                <a className="page-link" href="#">»</a>
                            </li>
                        </ul>
                </div>

            </div>
            {/*/Inner main*/}

        </>
    );
};

export default Dashboard;