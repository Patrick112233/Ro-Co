import React from "react";


import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faPlus } from '@fortawesome/free-solid-svg-icons';


const getEvent = (eventID) => {
    //TODO: fetch event data from server
    return {
        id: eventID,
        organizerId: "12314dasf23",
        extern: false,
        title: "Event Title",
        description: "This is a wider card with supporting text below as a natural lead-in to additional content. This content is a little bit longer.",
        date: "16.04.2015",
        startTime: "16:00",
        endTime: "18:00",
        location: "Az 205",
        participants: 0,
        maxParticipants: 16
    };
};


const Event = ({ eventID }) => {

    const event = getEvent(eventID);

    return(
        <div className="card m-auto" style={{height: '300px', width:'300px'}}>
            <div className="card-body">
                <h5 className="card-title">{event.title}</h5>
                <div className="card-text">
                    <p style={{overflow: 'clip', textOverflow: 'ellipsis' }}>
                        {event.description}
                    </p>
                </div>
            </div>
            <div className="card-footer" style={{height: '100px'}}>
                <div className="d-flex justify-content-between align-items-top">
                    <div>
                        <div className="d-flex align-items-top">
                            <p className="fw-bold m-0">Am: </p> <p className="ps-1 m-0">{event.date}</p>
                        </div>
                        <div className="d-flex align-items-top">
                            <p className="fw-bold m-0 ps-0">von </p> <p className="ps-1  m-0">{event.startTime}</p> <p className="fw-bold ps-1  m-0">bis</p> <p className="ps-1  m-0">{event.endTime} </p>
                        </div>
                        <div className="d-flex ">
                            <p className="fw-bold m-0">Ort: </p> <p className="ps-1 m-0">{event.location}</p>
                        </div>
                    </div>
                    <div className="me-2">
                        <button className="btn btn-primary rounded-circle text-white">
                            <FontAwesomeIcon icon={faPlus} />
                        </button>
                        <p className="ps-1 pt-1 m-0">{event.participants}/{event.maxParticipants} </p>

                    </div>
                </div>
            </div>
        </div>
    );


    /*
    return(
        <div className="card mb-3" style={{width: '30VW !important', minWidth: '300px', maxWidth: '400px'}}>
            <div className="card-body" style={{maxHeight: '30VH', overflow: 'auto'}}>
                <h5 className="card-title">{event.title}</h5>
                <div className="card-text">
                    <p style={{overflow: 'clip', textOverflow: 'ellipsis' }}>
                        {event.description}
                    </p>
                </div>
            </div>
            <div className="card-footer" style={{maxHeight: '200px'}}>
                <div className="d-flex justify-content-between align-items-top">
                    <div>
                        <div className="d-flex align-items-top">
                            <p className="fw-bold m-0">Am: </p> <p className="ps-1 m-0">{event.date}</p> <p className="fw-bold  m-0 ps-1">von </p> <p className="ps-1  m-0">{event.startTime}</p> <p className="fw-bold ps-1  m-0">bis</p> <p className="ps-1  m-0">{event.endTime} </p>
                        </div>
                        <div className="d-flex ">
                            <p className="fw-bold m-0">Ort: </p> <p className="ps-1 m-0">{event.location}</p>
                            <p className=" ps-3 fw-bold m-0">Teilnehmer:</p> <p className="ps-1 m-0">{event.participants}/{event.maxParticipants} </p>
                        </div>
                    </div>
                    <div className="me-2">
                        <button className="btn btn-primary rounded-circle text-white">
                            <FontAwesomeIcon icon={faPlus} />
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
*/
}

export default Event;
