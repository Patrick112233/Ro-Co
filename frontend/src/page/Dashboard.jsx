import React from 'react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faUser, faBell, faPlus, faArrowRight } from '@fortawesome/free-solid-svg-icons';
import 'bootstrap/dist/css/bootstrap.min.css';
import Event from './../component/event.jsx';

import Slider from "react-slick";
import "slick-carousel/slick/slick.css";
import "slick-carousel/slick/slick-theme.css";


const Dashboard = () => {
  const events = [
      {id:1},
      {id:1},
      {id:1},
      {id:1},
      {id:1},
      {id:1},
      {id:1},
      {id:1},
      {id:1},
      {id:1},
      {id:1},
      {id:1},
      {id:1},
      {id:2}
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
          <nav className="navbar navbar-light bg-primary justify-content-between w-100">
              <div className="d-flex justify-content-start btn-group ms-2">
                  <button className="rounded-circle btn btn-light" type="button">
                      <FontAwesomeIcon icon={faUser} className="text-dark"/>
                  </button>
                  <a className="navbar-brand text-white fw-bold ms-2">User Name </a>
              </div>

              <div className="d-flex justify-content-end btn-group">
                  <button className="btn me-2" type="button">
                      <FontAwesomeIcon icon={faBell} className="text-dark fs-4"/>
                  </button>
              </div>
          </nav>

          <div className="dropstart position-absolute bottom-0 end-0" style={{marginRight: '3VW', marginBottom: '3VW'}}>
              <button type="button" className="rounded-circle btn btn-primary" id="dropdownMenuButton"
                      data-bs-toggle="dropdown" aria-expanded="false">
                  <FontAwesomeIcon icon={faPlus} className="text-white p-2 fs-4"/>
              </button>
              <ul className="dropdown-menu" aria-labelledby="dropdownMenuButton">
                  <li><a className="dropdown-item" href="#">Ask Question</a></li>
                  <li><a className="dropdown-item" href="#">Create Event</a></li>
              </ul>
          </div>

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


              </>
              );
          };

          export default Dashboard;